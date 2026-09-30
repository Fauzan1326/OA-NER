import time
from typing import Optional

class ArthroscanException(Exception):
    def __init__(
        self,
        error_code: str,
        message: str,
        module: str = "CORE",
        severity: str = "ERROR",
        recoverable: bool = False,
        technical_details: str = "",
        session_id: Optional[str] = None
    ):
        super().__init__(message)
        self.error_code = error_code
        self.timestamp = int(time.time() * 1000)
        self.module = module
        self.severity = severity
        self.recoverable = recoverable
        self.message = message
        self.technical_details = technical_details
        self.session_id = session_id

class ContractException(ArthroscanException):
    pass

class InvalidPacketException(ContractException):
    def __init__(self, message: str, technical_details: str = "", session_id: Optional[str] = None):
        super().__init__("ERR_INVALID_PACKET", message, module="CONTRACT", severity="ERROR", recoverable=False, technical_details=technical_details, session_id=session_id)

class MissingFieldException(ContractException):
    def __init__(self, field_name: str, session_id: Optional[str] = None):
        super().__init__("ERR_MISSING_FIELD", f"Required field missing: {field_name}", module="CONTRACT", severity="ERROR", technical_details=field_name, session_id=session_id)

class SchemaVersionMismatchException(ContractException):
    def __init__(self, expected: str, actual: str, session_id: Optional[str] = None):
        super().__init__("ERR_SCHEMA_MISMATCH", f"Schema version mismatch: expected {expected}, got {actual}", module="CONTRACT", severity="CRITICAL", technical_details=f"expected={expected}, actual={actual}", session_id=session_id)

class HardwareException(ArthroscanException):
    pass

class SensorConnectionException(HardwareException):
    def __init__(self, sensor_id: str, details: str = ""):
        super().__init__("ERR_SENSOR_CONNECTION", f"Failed to connect to sensor: {sensor_id}", module="HARDWARE", severity="HIGH", recoverable=True, technical_details=details)

class SensorTimeoutException(HardwareException):
    def __init__(self, sensor_id: str, timeout_ms: int):
        super().__init__("ERR_SENSOR_TIMEOUT", f"Sensor {sensor_id} timed out after {timeout_ms}ms", module="HARDWARE", severity="HIGH", recoverable=True, technical_details=f"timeout={timeout_ms}")

class SensorCalibrationException(HardwareException):
    def __init__(self, sensor_id: str, details: str = ""):
        super().__init__("ERR_SENSOR_CALIBRATION", f"Calibration error for sensor {sensor_id}: {details}", module="HARDWARE", severity="MEDIUM", recoverable=True, technical_details=details)

class SensorProtocolException(HardwareException):
    def __init__(self, sensor_id: str, details: str = ""):
        super().__init__("ERR_SENSOR_PROTOCOL", f"Protocol framing error from {sensor_id}: {details}", module="HARDWARE", severity="HIGH", recoverable=True, technical_details=details)

class ProcessingException(ArthroscanException):
    pass

class ModelException(ArthroscanException):
    pass

class ModelNotFoundException(ModelException):
    def __init__(self, model_id: str):
        super().__init__("ERR_MODEL_NOT_FOUND", f"Model artifact not found: {model_id}", module="AI", severity="CRITICAL")

class ModelIntegrityException(ModelException):
    def __init__(self, model_id: str, expected_sha: str, actual_sha: str):
        super().__init__("ERR_MODEL_INTEGRITY", f"SHA-256 hash mismatch for model {model_id}", module="AI", severity="CRITICAL", technical_details=f"expected={expected_sha}, actual={actual_sha}")

class ModelCompatibilityException(ModelException):
    def __init__(self, model_id: str, expected_schema: str, actual_schema: str):
        super().__init__("ERR_MODEL_COMPATIBILITY", f"Model {model_id} expects feature schema {expected_schema}, got {actual_schema}", module="AI", severity="CRITICAL", technical_details=f"expected={expected_schema}, actual={actual_schema}")

class StorageException(ArthroscanException):
    pass

class ConfigurationException(ArthroscanException):
    pass
