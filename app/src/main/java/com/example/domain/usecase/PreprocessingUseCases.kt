package com.example.domain.usecase

import com.example.core.contract.SensorPacket
import com.example.preprocessing.PreprocessedFrame
import com.example.preprocessing.PreprocessingPipeline

class PreprocessPacketUseCase(
    private val pipeline: PreprocessingPipeline = PreprocessingPipeline()
) {
    fun execute(packet: SensorPacket): PreprocessedFrame {
        return pipeline.processPacket(packet)
    }

    fun reset() {
        pipeline.reset()
    }
}
