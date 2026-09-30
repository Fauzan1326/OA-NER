package com.example.domain.usecase

import com.example.core.contract.Modality
import com.example.core.contract.SensorPacket
import com.example.features.FeatureStore
import com.example.features.ImuFeatureExtractor
import com.example.features.ModalityFeatureVector
import com.example.features.NormalizationParam
import com.example.features.RfFeatureExtractor
import com.example.features.SemgFeatureExtractor
import com.example.features.VagFeatureExtractor
import com.example.preprocessing.PreprocessedFrame

class FeatureExtractionPipeline(
    val featureStore: FeatureStore = FeatureStore(rejectQualityFailures = true),
    private val imuExtractor: ImuFeatureExtractor = ImuFeatureExtractor(),
    private val vagExtractor: VagFeatureExtractor = VagFeatureExtractor(),
    private val semgExtractor: SemgFeatureExtractor = SemgFeatureExtractor(),
    private val rfExtractor: RfFeatureExtractor = RfFeatureExtractor()
) {
    fun extract(frame: PreprocessedFrame, packet: SensorPacket): ModalityFeatureVector? {
        val qa = frame.quality

        val vector = when (packet.modality) {
            Modality.IMU -> {
                val window: List<List<Double>> = listOf(frame.cleanedValues)
                imuExtractor.extract(
                    window = window,
                    samplingRateHz = packet.sampling.rateHz,
                    qa = qa,
                    sourceSensorId = packet.sensorId,
                    subjectId = packet.subjectId,
                    sessionId = packet.sessionId,
                    startTimeMs = packet.timestamp.deviceTimeMs,
                    endTimeMs = packet.timestamp.deviceTimeMs + 10L
                )
            }
            Modality.VAG -> {
                val window: List<Double> = frame.cleanedValues
                vagExtractor.extract(
                    window = window,
                    samplingRateHz = packet.sampling.rateHz,
                    qa = qa,
                    sourceSensorId = packet.sensorId,
                    subjectId = packet.subjectId,
                    sessionId = packet.sessionId,
                    startTimeMs = packet.timestamp.deviceTimeMs,
                    endTimeMs = packet.timestamp.deviceTimeMs + 1L
                )
            }
            Modality.SEMG -> {
                val window: List<List<Double>> = listOf(frame.cleanedValues)
                semgExtractor.extract(
                    window = window,
                    samplingRateHz = packet.sampling.rateHz,
                    qa = qa,
                    sourceSensorId = packet.sensorId,
                    subjectId = packet.subjectId,
                    sessionId = packet.sessionId,
                    startTimeMs = packet.timestamp.deviceTimeMs,
                    endTimeMs = packet.timestamp.deviceTimeMs + 1L
                )
            }
            Modality.RF -> {
                val window: List<List<Double>> = listOf(frame.cleanedValues)
                rfExtractor.extract(
                    window = window,
                    samplingRateHz = packet.sampling.rateHz,
                    qa = qa,
                    sourceSensorId = packet.sensorId,
                    subjectId = packet.subjectId,
                    sessionId = packet.sessionId,
                    startTimeMs = packet.timestamp.deviceTimeMs,
                    endTimeMs = packet.timestamp.deviceTimeMs + 20L
                )
            }
            else -> null
        }

        if (vector != null) {
            featureStore.insert(vector)
        }
        return vector
    }

    fun reset() {
        featureStore.clear()
    }
}

class ExtractFeaturesUseCase(
    val pipeline: FeatureExtractionPipeline = FeatureExtractionPipeline()
) {
    fun execute(frame: PreprocessedFrame, packet: SensorPacket): ModalityFeatureVector? {
        return pipeline.extract(frame, packet)
    }

    fun reset() {
        pipeline.reset()
    }
}

class QueryFeatureStoreUseCase(
    private val featureStore: FeatureStore
) {
    fun getLatest(modality: Modality): ModalityFeatureVector? = featureStore.getLatest(modality)

    fun getCount(modality: Modality? = null): Int = featureStore.count(modality)

    fun getNormalizationStats(modality: Modality): Map<String, NormalizationParam> =
        featureStore.computeNormalizationStats(modality)

    fun query(modality: Modality? = null, limit: Int = 100): List<ModalityFeatureVector> {
        val list = featureStore.query(modality = modality)
        return if (list.size > limit) list.takeLast(limit) else list
    }
}
