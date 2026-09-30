from abc import ABC, abstractmethod
from typing import Dict, Any, Optional
from arthroscan.schemas.contract import SensorPacket, DeviceStatus

class SensorAdapter(ABC):
    @abstractmethod
    def connect(self) -> bool:
        pass

    @abstractmethod
    def disconnect(self) -> bool:
        pass

    @abstractmethod
    def start_stream(self) -> bool:
        pass

    @abstractmethod
    def stop_stream(self) -> bool:
        pass

    @abstractmethod
    def read_sample(self) -> Optional[SensorPacket]:
        pass

    @abstractmethod
    def get_status(self) -> DeviceStatus:
        pass

    @abstractmethod
    def calibrate(self) -> Dict[str, Any]:
        pass

    @abstractmethod
    def get_metadata(self) -> Dict[str, Any]:
        pass
