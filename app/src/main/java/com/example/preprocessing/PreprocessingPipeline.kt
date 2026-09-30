package com.example.preprocessing

import com.example.core.contract.Modality
import com.example.core.contract.SensorPacket
import com.example.core.contract.SignalQualityStatus

data class PreprocessedFrame(
    val modality: Modality,
    val cleanedValues: List<Double>,
    val derivedMetrics: Map<String, Double>,
    val quality: QualityAssessment
)

class PreprocessingPipeline {
    val imuPrep = ImuPreprocessor()
    val vagPrep = VagPreprocessor()
    val semgPrep = SemgPreprocessor()
    val rfPrep = RfPreprocessor()
    val qualityGate = SignalQualityGate()

    private val historyImu = ArrayList<List<Double>>()
    private val historyVag = ArrayList<Double>()
    private val historySemg = ArrayList<List<Double>>()
    private val historyRf = ArrayList<List<Double>>()
    private val windowMaxLen = 50

    fun processPacket(packet: SensorPacket): PreprocessedFrame {
        val mod = packet.modality
        val vals = packet.values

        return when (mod) {
            Modality.IMU -> {
                val out = imuPrep.processSample(vals)
                historyImu.add(out.cleanedValues)
                if (historyImu.size > windowMaxLen) historyImu.removeAt(0)
                val qa = qualityGate.assessImu(historyImu)
                PreprocessedFrame(
                    modality = mod,
                    cleanedValues = out.cleanedValues,
                    derivedMetrics = mapOf(
                        "acc_magnitude" to out.accMagnitude,
                        "gyro_magnitude" to out.gyroMagnitude
                    ),
                    quality = qa
                )
            }
            Modality.VAG -> {
                val out = vagPrep.processSample(vals)
                historyVag.add(out.cleanedValues[0])
                if (historyVag.size > windowMaxLen) historyVag.removeAt(0)
                val qa = qualityGate.assessVag(historyVag)
                PreprocessedFrame(
                    modality = mod,
                    cleanedValues = out.cleanedValues,
                    derivedMetrics = mapOf(
                        "acoustic_envelope" to out.acousticEnvelope,
                        "acoustic_power" to out.acousticPower
                    ),
                    quality = qa
                )
            }
            Modality.SEMG -> {
                val out = semgPrep.processSample(vals)
                historySemg.add(out.cleanedValues)
                if (historySemg.size > windowMaxLen) historySemg.removeAt(0)
                val qa = qualityGate.assessSemg(historySemg)
                PreprocessedFrame(
                    modality = mod,
                    cleanedValues = out.cleanedValues,
                    derivedMetrics = mapOf(
                        "mean_rms_envelope" to out.meanRms
                    ),
                    quality = qa
                )
            }
            Modality.RF -> {
                val out = rfPrep.processSample(vals)
                historyRf.add(out.cleanedValues)
                if (historyRf.size > windowMaxLen) historyRf.removeAt(0)
                val qa = qualityGate.assessRf(historyRf)
                PreprocessedFrame(
                    modality = mod,
                    cleanedValues = out.cleanedValues,
                    derivedMetrics = mapOf(
                        "freq_shift_mhz" to out.freqShiftMhz
                    ),
                    quality = qa
                )
            }
            else -> {
                val qa = QualityAssessment(mod, 1.0, SignalQualityStatus.PASS, listOf(ArtifactType.NONE))
                PreprocessedFrame(modality = mod, cleanedValues = vals, derivedMetrics = emptyMap(), quality = qa)
            }
        }
    }

    fun reset() {
        imuPrep.reset()
        vagPrep.reset()
        semgPrep.reset()
        rfPrep.reset()
        historyImu.clear()
        historyVag.clear()
        historySemg.clear()
        historyRf.clear()
    }
}
