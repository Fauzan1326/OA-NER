package com.example

import com.example.core.contract.Modality
import com.example.core.contract.SignalQualityStatus
import com.example.features.FEATURE_SCHEMA_VERSION
import com.example.features.FeatureDomain
import com.example.features.FeatureMath
import com.example.features.FeatureStore
import com.example.features.ImuFeatureExtractor
import com.example.features.RfFeatureExtractor
import com.example.features.SemgFeatureExtractor
import com.example.features.VagFeatureExtractor
import com.example.preprocessing.ArtifactType
import com.example.preprocessing.QualityAssessment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class Module4FeaturesTest {

    @Test
    fun testMathPrimitives() {
        val vals = listOf(1.0, -1.0, 1.0, -1.0)
        assertEquals(0.0, FeatureMath.mean(vals), 0.001)
        assertEquals(1.0, FeatureMath.rootMeanSquare(vals), 0.001)
        assertEquals(1.0, FeatureMath.zeroCrossingRate(vals), 0.001)

        val fs = 100.0
        val n = 64
        val sine = (0 until n).map { t -> sin(2.0 * PI * 10.0 * t / fs) }
        val (freqs, mags) = FeatureMath.computeDftMagnitude(sine, fs)
        val (centroid, _, domFreq) = FeatureMath.spectralCentroidAndSpread(freqs, mags)
        assertEquals(10.0, domFreq, 2.0)
        assertTrue(centroid > 0.0)
    }

    @Test
    fun testImuFeatureExtractor() {
        val extractor = ImuFeatureExtractor()
        val window = List(20) { listOf(0.0, 0.0, 1.0, 0.0, 0.0, 0.0) }
        val qa = QualityAssessment(
            modality = Modality.IMU,
            sqiScore = 0.95,
            status = SignalQualityStatus.PASS,
            detectedArtifacts = emptyList()
        )

        val vec = extractor.extract(
            window = window,
            samplingRateHz = 100.0,
            qa = qa,
            startTimeMs = 0L,
            endTimeMs = 200L
        )

        assertEquals(Modality.IMU, vec.provenance.modality)
        assertEquals(SignalQualityStatus.PASS, vec.provenance.qualityStatus)
        assertEquals(FEATURE_SCHEMA_VERSION, vec.provenance.featureSchemaVersion)
        assertEquals(1.0, vec.getFeature("acc_mean_mag") ?: 0.0, 0.001)
        assertEquals(1.0, vec.getFeature("acc_rms_mag") ?: 0.0, 0.001)
        assertEquals(0.0, vec.getFeature("acc_std_mag") ?: 0.0, 0.001)
    }

    @Test
    fun testVagFeatureExtractor() {
        val extractor = VagFeatureExtractor()
        val window = (0 until 50).map { 0.1 * sin(it * 0.5) }
        val qa = QualityAssessment(
            modality = Modality.VAG,
            sqiScore = 0.90,
            status = SignalQualityStatus.PASS,
            detectedArtifacts = emptyList()
        )

        val vec = extractor.extract(
            window = window,
            samplingRateHz = 2000.0,
            qa = qa,
            startTimeMs = 1000L,
            endTimeMs = 1025L
        )

        assertEquals(Modality.VAG, vec.provenance.modality)
        assertTrue(vec.features.containsKey("vag_rms"))
        assertTrue(vec.features.containsKey("vag_spectral_centroid_hz"))
        assertTrue((vec.getFeature("vag_rms") ?: 0.0) > 0.0)
    }

    @Test
    fun testSemgFeatureExtractor() {
        val extractor = SemgFeatureExtractor()
        val window = List(40) { listOf(0.2, 0.1) }
        val qa = QualityAssessment(
            modality = Modality.SEMG,
            sqiScore = 0.88,
            status = SignalQualityStatus.PASS,
            detectedArtifacts = emptyList()
        )

        val vec = extractor.extract(
            window = window,
            samplingRateHz = 1000.0,
            qa = qa,
            startTimeMs = 500L,
            endTimeMs = 540L
        )

        assertEquals(Modality.SEMG, vec.provenance.modality)
        assertEquals(0.2, vec.getFeature("semg_rms_vm") ?: 0.0, 0.01)
        // 0.2 / (0.2 + 0.1) = 0.6667
        assertEquals(0.667, vec.getFeature("semg_co_contraction_ratio") ?: 0.0, 0.02)
    }

    @Test
    fun testRfFeatureExtractor() {
        val extractor = RfFeatureExtractor(baselineResFreqMhz = 2450.0)
        val window = List(10) { listOf(-18.5, 0.2, 2448.0) }
        val qa = QualityAssessment(
            modality = Modality.RF,
            sqiScore = 0.92,
            status = SignalQualityStatus.PASS,
            detectedArtifacts = emptyList()
        )

        val vec = extractor.extract(
            window = window,
            samplingRateHz = 50.0,
            qa = qa,
            startTimeMs = 0L,
            endTimeMs = 200L
        )

        assertEquals(Modality.RF, vec.provenance.modality)
        assertEquals(FeatureDomain.EXPERIMENTAL_RF, vec.features["rf_resonance_shift_mhz"]?.domain)
        assertEquals(-2.0, vec.getFeature("rf_resonance_shift_mhz") ?: 0.0, 0.01)
        assertEquals(-18.5, vec.getFeature("rf_min_reflection_db") ?: 0.0, 0.01)
    }

    @Test
    fun testFeatureStoreValidationAndQuery() {
        val store = FeatureStore(rejectQualityFailures = true)
        val imuExtractor = ImuFeatureExtractor()
        val window = List(10) { listOf(0.0, 0.0, 1.0, 0.0, 0.0, 0.0) }

        val qaPass = QualityAssessment(Modality.IMU, 0.95, SignalQualityStatus.PASS, emptyList())
        val qaFail = QualityAssessment(Modality.IMU, 0.30, SignalQualityStatus.FAIL, listOf(ArtifactType.CLIPPING_SATURATION))

        val vecPass = imuExtractor.extract(window, 100.0, qaPass, startTimeMs = 0L, endTimeMs = 100L)
        val vecFail = imuExtractor.extract(window, 100.0, qaFail, startTimeMs = 100L, endTimeMs = 200L)

        assertTrue(store.insert(vecPass))
        assertFalse(store.insert(vecFail))

        assertEquals(1, store.count())
        assertEquals(1, store.count(Modality.IMU))
        assertEquals(0, store.count(Modality.VAG))

        val queryRes = store.query(modality = Modality.IMU, startMs = 0L, endMs = 100L)
        assertEquals(1, queryRes.size)

        val stats = store.computeNormalizationStats(Modality.IMU)
        assertTrue(stats.containsKey("acc_mean_mag"))
        assertEquals(1.0, stats["acc_mean_mag"]?.mean ?: 0.0, 0.01)
    }
}
