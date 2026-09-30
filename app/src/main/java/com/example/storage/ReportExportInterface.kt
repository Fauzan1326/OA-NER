package com.example.storage

import com.example.core.contract.ScreeningResult
import java.io.File

/**
 * Report Export Interface
 * PDF is primary field format; FHIR serves as an extension point for future integration.
 */
interface ReportExportEngine {
    suspend fun generateFieldSummaryPdf(result: ScreeningResult): File
}

/**
 * FHIR Export Extension Point Interface
 * NOTE: Architectural extension point for future clinical systems integration.
 * Not claimed as a completed feature in Module 1.
 */
interface FhirExportExtensionPoint {
    val fhirVersion: String get() = "R4"
    fun isIntegrationConfigured(): Boolean = false
    suspend fun buildObservationBundleJson(result: ScreeningResult): String?
}
