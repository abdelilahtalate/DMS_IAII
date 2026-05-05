# backend/ai/BaseInstantDetector.py
from ai.BaseTemporalDetector import BaseTemporalDetector


class BaseInstantDetector(BaseTemporalDetector):
    """
    Base class for instant detectors.
    Processes frames immediately without temporal window.
    """
    
    def __init__(self, buffer_size: int = 1, stride: int = 1):
        # Pour les détecteurs instantanés, on utilise buffer_size=1
        super().__init__(buffer_size=buffer_size, stride=stride)