package com.example.dms_app.feature_insights.domain

import com.example.dms_app.feature_insights.data.InsightsRepository

class GetInsightsUseCase(private val repository: InsightsRepository) {
    suspend operator fun invoke(userId: Int): Result<InsightOverviewDomain> {
        return try {
            val response = repository.getInsights(userId)
            val domainData = InsightOverviewDomain(
                stats = InsightStats(
                    averageScore = response.stats.averageScore,
                    scoreChange = response.stats.scoreChange,
                    bestDay = response.stats.bestDay,
                    bestScore = response.stats.bestScore,
                    totalTrips = response.stats.totalTrips,
                    eventCount = response.stats.eventCount,
                    totalDistance = response.stats.totalDistance,
                    totalDuration = response.stats.totalDuration
                ),
                dayScores = response.dayScores.map { DayScore(it.day, it.score) },
                heatmapData = response.heatmapData ?: emptyList()
            )
            Result.success(domainData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class InsightOverviewDomain(
    val stats: InsightStats,
    val dayScores: List<DayScore>,
    val heatmapData: List<List<Int>>
)
