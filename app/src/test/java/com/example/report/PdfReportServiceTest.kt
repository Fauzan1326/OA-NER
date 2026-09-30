package com.example.report

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.admin.OperationalAuditEvent
import com.example.auth.UserRole
import com.example.portal.AshaScreeningSession
import com.example.portal.AshaSessionManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.FileInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PdfReportServiceTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testScreeningPdfGenerationCreatesValidPdfFile() {
        val manager = com.example.portal.AshaSessionManager()
        val session = manager.getAllSessions().first()

        val result = PdfReportService.generateScreeningReportPdf(context, session)
        if (result.isFailure) {
            println("=== TEST SCREENING PDF EXCEPTION ===")
            result.exceptionOrNull()?.printStackTrace()
        }
        assertTrue("PDF generation must succeed: ${result.exceptionOrNull()?.message}", result.isSuccess)

        val file = result.getOrThrow()
        assertTrue("Generated file must exist", file.exists())
        assertTrue("File size must be greater than 100 bytes", file.length() > 100L)
        assertTrue("File name must end with .pdf", file.name.endsWith(".pdf"))

        // Verify PDF Magic Bytes (%PDF-)
        val bytes = ByteArray(5)
        FileInputStream(file).use { it.read(bytes) }
        val header = String(bytes)
        assertEquals("%PDF-", header)
    }

    @Test
    fun testAuditPdfGenerationCreatesValidPdfFile() {
        val auditLogs = listOf(
            OperationalAuditEvent(
                actorUsername = "superadmin.assam",
                actorRole = UserRole.SUPER_ADMIN,
                action = "CALIBRATION_RATIFIED",
                target = "CONFIG_ROOT",
                result = "SUCCESS",
                details = "IRB protocol AMCH-NER-ETH-2026-081B ratified"
            ),
            OperationalAuditEvent(
                actorUsername = "asha.anita",
                actorRole = UserRole.ASHA_WORKER,
                action = "SCREENING_COMPLETED",
                target = "SES-TEST-001",
                result = "SUCCESS",
                details = "Participant PART-NER-001 screened with 4 modalities"
            )
        )

        val result = PdfReportService.generateAuditReportPdf(
            context = context,
            totalRecords = auditLogs.size,
            latestHash = "a4f89b12c3d4e5f67890abcdef1234567890abcdef",
            genesisRoot = "0000000000000000",
            auditLogs = auditLogs
        )

        assertTrue("Audit PDF generation must succeed", result.isSuccess)
        val file = result.getOrThrow()
        assertTrue("Generated audit file must exist", file.exists())
        assertTrue("File size must be greater than 100 bytes", file.length() > 100L)
        assertTrue("File name must end with .pdf", file.name.endsWith(".pdf"))

        val bytes = ByteArray(5)
        FileInputStream(file).use { it.read(bytes) }
        val header = String(bytes)
        assertEquals("%PDF-", header)
    }
}
