package filtering

import model.LottoResult
import scoring.LottoScoringSystem
import scoring.LottoScoringSystem.PatternScore

class ScoreBasedFilter(
    private val scoringSystem: LottoScoringSystem
) {

    data class FilterConfig(
        val minScore: Double = 80.0,
        val maxResults: Int = 1000,
        val includeGrades: Set<String> = setOf("S+", "S", "A+", "A"),
        val sortBy: SortOption = SortOption.SCORE_DESC,
        val requirePatterns: Set<String> = emptySet(),
        val excludePatterns: Set<String> = emptySet()
    )

    enum class SortOption {
        SCORE_DESC, SCORE_ASC, ROUND_DESC, ROUND_ASC
    }

    data class FilterResult(
        val filteredScores: List<PatternScore>,
        val totalEvaluated: Int,
        val totalPassed: Int,
        val filteringRate: Double,
        val timeElapsedMs: Long
    )

    fun filterByScore(
        combinations: List<LottoResult>,
        historicalData: List<LottoResult>,
        config: FilterConfig = FilterConfig()
    ): FilterResult {
        val startTime = System.currentTimeMillis()

        val scores = combinations.asSequence()
            .map { scoringSystem.calculateScore(it, historicalData) }
            .filter { score ->
                score.score >= config.minScore &&
                config.includeGrades.contains(score.grade) &&
                checkRequiredPatterns(score, config.requirePatterns) &&
                checkExcludedPatterns(score, config.excludePatterns)
            }
            .toList()

        val sorted = when (config.sortBy) {
            SortOption.SCORE_DESC -> scores.sortedByDescending { it.score }
            SortOption.SCORE_ASC -> scores.sortedBy { it.score }
            SortOption.ROUND_DESC -> scores.sortedByDescending { it.round }
            SortOption.ROUND_ASC -> scores.sortedBy { it.round }
        }

        val limited = sorted.take(config.maxResults)
        val timeElapsed = System.currentTimeMillis() - startTime

        return FilterResult(
            filteredScores = limited,
            totalEvaluated = combinations.size,
            totalPassed = scores.size,
            filteringRate = (scores.size.toDouble() / combinations.size) * 100,
            timeElapsedMs = timeElapsed
        )
    }

    fun filterByScoreRange(
        combinations: List<LottoResult>,
        historicalData: List<LottoResult>,
        minScore: Double,
        maxScore: Double,
        maxResults: Int = 1000
    ): FilterResult {
        val startTime = System.currentTimeMillis()

        val scores = combinations.asSequence()
            .map { scoringSystem.calculateScore(it, historicalData) }
            .filter { it.score in minScore..maxScore }
            .sortedByDescending { it.score }
            .take(maxResults)
            .toList()

        val timeElapsed = System.currentTimeMillis() - startTime

        return FilterResult(
            filteredScores = scores,
            totalEvaluated = combinations.size,
            totalPassed = scores.size,
            filteringRate = (scores.size.toDouble() / combinations.size) * 100,
            timeElapsedMs = timeElapsed
        )
    }

    fun filterWithMultipleThresholds(
        combinations: List<LottoResult>,
        historicalData: List<LottoResult>,
        thresholds: List<Double>
    ): Map<Double, FilterResult> {
        val results = mutableMapOf<Double, FilterResult>()

        thresholds.forEach { threshold ->
            val config = FilterConfig(minScore = threshold)
            results[threshold] = filterByScore(combinations, historicalData, config)
        }

        return results
    }

    fun getOptimalThreshold(
        combinations: List<LottoResult>,
        historicalData: List<LottoResult>,
        targetResultCount: Int
    ): Double {
        val thresholds = (60..95 step 5).map { it.toDouble() }
        val results = filterWithMultipleThresholds(combinations, historicalData, thresholds)

        return results.entries
            .minByOrNull { Math.abs(it.value.totalPassed - targetResultCount) }
            ?.key ?: 80.0
    }

    fun filterByPatternCount(
        combinations: List<LottoResult>,
        historicalData: List<LottoResult>,
        minSatisfiedPatterns: Int,
        maxViolatedPatterns: Int
    ): FilterResult {
        val startTime = System.currentTimeMillis()

        val scores = combinations.asSequence()
            .map { scoringSystem.calculateScore(it, historicalData) }
            .filter { score ->
                score.satisfiedPatterns.size >= minSatisfiedPatterns &&
                score.violatedPatterns.size <= maxViolatedPatterns
            }
            .sortedByDescending { it.score }
            .toList()

        val timeElapsed = System.currentTimeMillis() - startTime

        return FilterResult(
            filteredScores = scores,
            totalEvaluated = combinations.size,
            totalPassed = scores.size,
            filteringRate = (scores.size.toDouble() / combinations.size) * 100,
            timeElapsedMs = timeElapsed
        )
    }

    private fun checkRequiredPatterns(score: PatternScore, requiredPatterns: Set<String>): Boolean {
        if (requiredPatterns.isEmpty()) return true
        val satisfiedNames = score.satisfiedPatterns.map { it.name }.toSet()
        return requiredPatterns.all { it in satisfiedNames }
    }

    private fun checkExcludedPatterns(score: PatternScore, excludedPatterns: Set<String>): Boolean {
        if (excludedPatterns.isEmpty()) return true
        val violatedNames = score.violatedPatterns.map { it.name }.toSet()
        return excludedPatterns.none { it in violatedNames }
    }

    fun printFilterResult(result: FilterResult, detailed: Boolean = false) {
        println("\n============= 필터링 결과 =============")
        println("평가된 조합 수: ${result.totalEvaluated}")
        println("통과한 조합 수: ${result.totalPassed}")
        println("필터링 비율: ${String.format("%.2f", result.filteringRate)}%")
        println("소요 시간: ${result.timeElapsedMs}ms")
        println("결과 개수: ${result.filteredScores.size}")

        if (detailed && result.filteredScores.isNotEmpty()) {
            scoringSystem.printScoreReport(result.filteredScores)
        }
    }

    fun getTopScoreCombinations(
        combinations: List<LottoResult>,
        historicalData: List<LottoResult>,
        topN: Int = 100
    ): List<PatternScore> {
        return combinations
            .asSequence()
            .map { scoringSystem.calculateScore(it, historicalData) }
            .sortedByDescending { it.score }
            .take(topN)
            .toList()
    }

    fun analyzeFilterEffectiveness(
        testData: List<LottoResult>,
        historicalData: List<LottoResult>,
        actualWinningNumbers: List<LottoResult>,
        thresholds: List<Double> = listOf(70.0, 75.0, 80.0, 85.0, 90.0)
    ): Map<Double, EffectivenessReport> {
        val reports = mutableMapOf<Double, EffectivenessReport>()

        thresholds.forEach { threshold ->
            val config = FilterConfig(minScore = threshold)
            val result = filterByScore(testData, historicalData, config)

            val winningScores = actualWinningNumbers
                .map { scoringSystem.calculateScore(it, historicalData) }

            val capturedWinners = winningScores.count { it.score >= threshold }
            val captureRate = (capturedWinners.toDouble() / actualWinningNumbers.size) * 100

            reports[threshold] = EffectivenessReport(
                threshold = threshold,
                totalFiltered = result.totalPassed,
                filteringRate = result.filteringRate,
                winnersCapture = capturedWinners,
                captureRate = captureRate,
                avgWinnerScore = winningScores.map { it.score }.average()
            )
        }

        return reports
    }

    data class EffectivenessReport(
        val threshold: Double,
        val totalFiltered: Int,
        val filteringRate: Double,
        val winnersCapture: Int,
        val captureRate: Double,
        val avgWinnerScore: Double
    )

    fun printEffectivenessReport(reports: Map<Double, EffectivenessReport>) {
        println("\n============= 필터 효과성 분석 =============")
        println("임계값 | 통과수 | 필터율 | 당첨포함 | 포함률 | 당첨평균")
        println("------|--------|--------|---------|-------|--------")

        reports.toSortedMap().forEach { (_, report) ->
            println(String.format(
                "%5.1f | %6d | %6.2f%% | %7d | %5.1f%% | %6.2f",
                report.threshold,
                report.totalFiltered,
                report.filteringRate,
                report.winnersCapture,
                report.captureRate,
                report.avgWinnerScore
            ))
        }
    }
}