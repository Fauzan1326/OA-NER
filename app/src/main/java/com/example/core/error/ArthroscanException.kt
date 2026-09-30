package com.example.core.error

/**
 * Standardized Hierarchical Domain Exceptions
 * SIH 2026 Problem SIH26004 - Team GOD'S PLAN
 */
sealed class ArthroscanException(
    val errorCode: String,
    override val message: String,
    val module: String = "CORE",
    val severity: String = "ERROR",
    val recoverable: Boolean = false,
    val technicalDetails: String = "",
    val sessionId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) : Exception(message) {

    // 1. Contract Exceptions
    sealed class ContractException(
        code: String,
        msg: String,
        details: String = "",
        sessId: String? = null
    ) : ArthroscanException(code, msg, module = "CONTRACT", severity = "ERROR", technicalDetails = details, sessionId = sessId) {

        class InvalidPacketException(details: String, sessId: String? = null) :
            ContractException("ERR_INVALID_PACKET", "Invalid SensorPacket: $details", details, sessId)

        class MissingFieldException(fieldName: String, sessId: String? = null) :
            ContractException("ERR_MISSING_FIELD", "Required field missing: $fieldName", fieldName, sessId)

        class SchemaVersionMismatchException(expected: String, actual: String, sessId: String? = null) :
            ContractException("ERR_SCHEMA_MISMATCH", "Schema version mismatch: expected $expected, got $actual", "expected=$expected, actual=$actual", sessId)
    }

    // 2. Hardware Exceptions
    sealed class HardwareException(
        code: String,
        msg: String,
        severity: String = "HIGH",
        details: String = "",
        sessId: String? = null
    ) : ArthroscanException(code, msg, module = "HARDWARE", severity = severity, recoverable = true, technicalDetails = details, sessionId = sessId) {

        class SensorConnectionException(val sensorId: String, details: String = "") :
            HardwareException("ERR_SENSOR_CONNECTION", "Failed to connect to sensor [$sensorId]: $details", "HIGH", details)

        class SensorTimeoutException(val sensorId: String, val timeoutMs: Long) :
            HardwareException("ERR_SENSOR_TIMEOUT", "Sensor [$sensorId] timed out after $timeoutMs ms", "HIGH", "timeout=$timeoutMs")

        class SensorCalibrationException(val sensorId: String, details: String = "") :
            HardwareException("ERR_SENSOR_CALIBRATION", "Calibration error on [$sensorId]: $details", "MEDIUM", details)

        class SensorProtocolException(val sensorId: String, details: String = "") :
            HardwareException("ERR_SENSOR_PROTOCOL", "Protocol error on [$sensorId]: $details", "HIGH", details)
    }

    // 3. Processing Exceptions
    class ProcessingException(code: String, msg: String, details: String = "") :
        ArthroscanException(code, msg, module = "PROCESSING", technicalDetails = details)

    // 4. Model & Inference Exceptions
    sealed class ModelException(
        code: String,
        msg: String,
        details: String = ""
    ) : ArthroscanException(code, msg, module = "AI", severity = "CRITICAL", technicalDetails = details) {

        class ModelNotFoundException(val modelId: String) :
            ModelException("ERR_MODEL_NOT_FOUND", "Model artifact not found: $modelId", modelId)

        class ModelIntegrityException(val modelId: String, expectedSha: String, actualSha: String) :
            ModelException("ERR_MODEL_INTEGRITY", "Hash mismatch for model [$modelId]", "expected=$expectedSha, actual=$actualSha")

        class ModelCompatibilityException(val modelId: String, expectedSchema: String, actualSchema: String) :
            ModelException("ERR_MODEL_COMPATIBILITY", "Model [$modelId] incompatible with feature schema: expected $expectedSchema, got $actualSchema", "expected=$expectedSchema, actual=$actualSchema")
    }

    // 5. Storage Exceptions
    class StorageException(msg: String, details: String = "") :
        ArthroscanException("ERR_STORAGE", msg, module = "STORAGE", technicalDetails = details)

    // 6. Configuration Exceptions
    class ConfigurationException(msg: String, details: String = "") :
        ArthroscanException("ERR_CONFIGURATION", msg, module = "CONFIGURATION", technicalDetails = details)
}
