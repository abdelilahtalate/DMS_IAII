from collections import deque
from typing import Optional, Any


class BaseTemporalDetector:
    """
    Base class for ALL temporal models.

    Handles:
    - sliding window buffer
    - stride-based computation
    - safe frame ingestion
    """

    def __init__(self, buffer_size: int = 30, stride: int = 5):

        # =========================
        # 🧠 CONFIG
        # =========================
        self.buffer_size = buffer_size
        self.stride = stride

        # =========================
        # 📦 MEMORY (sliding window)
        # =========================
        self.buffer = deque(maxlen=buffer_size)

        # =========================
        # ⏱ CONTROL COUNTER
        # =========================
        self.step = 0

        # =========================
        # 💾 LAST OUTPUT
        # =========================
        self.last_output = None

    # =========================================================
    # 🚀 MAIN ENTRY POINT
    # =========================================================
    def push_frame(self, frame_context) -> Optional[Any]:
        """
        Called every frame.

        Returns:
            - None → not enough data or not stride step
            - value → computed model output
        """

        # 1. store frame
        self.buffer.append(frame_context)

        # 2. update step counter
        self.step += 1

        # 3. always maintain sliding window (deque auto handles it)

        # 4. stride check (skip computation most frames)
        if self.step % self.stride != 0:
            return None

        # 5. not enough data yet
        if len(self.buffer) < self.buffer_size:
            return None

        # 6. compute model output
        result = self.compute()

        # 7. store last output
        if result is not None:
            self.last_output = result

        return result

    # =========================================================
    # 🧠 TO BE IMPLEMENTED BY CHILD CLASSES
    # =========================================================
    def compute(self):
        """
        Override in child classes.
        Uses self.buffer (last N frames).
        """
        raise NotImplementedError("compute() must be implemented in subclass")

    # =========================================================
    # 📊 UTILITY
    # =========================================================
    def get_last(self):
        return self.last_output