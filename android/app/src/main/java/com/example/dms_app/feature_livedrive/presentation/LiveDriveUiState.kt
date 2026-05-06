
data class FatigueData(
    val is_fatigued: Boolean,
    val score: Float
)

data class DistractionData(
    val is_distracted: Boolean,
    val score: Float
)

data class AiResult(
    val fatigue: FatigueData,
    val distraction: DistractionData
)

data class AiResultWrapper(
    val scores: AiResult
)

data class LiveDriveUiState(
    val isMonitoring: Boolean = false,
    val lastResult: AiResult? = null,
    val error: String? = null
)