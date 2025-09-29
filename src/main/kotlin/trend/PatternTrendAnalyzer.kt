package trend

import analyzer.LottoResultCustomAnalyzer
import analyzer.PatternReliabilityAnalyzer
import model.LottoResult
import kotlin.math.abs

class PatternTrendAnalyzer(
    private val analyzer: LottoResultCustomAnalyzer,
    private val reliabilityAnalyzer: PatternReliabilityAnalyzer
) {

    data class PatternTrend(
        val patternName: String,
        val overallSatisfactionRate: Double,
        val recentSatisfactionRate: Double,
        val trendValue: Double,
        val trendDirection: TrendDirection,
        val volatility: Double,
        val recentViolations: List<Int>,
        val recommendation: String
    )

    enum class TrendDirection {
        STRONGLY_UP, UP, STABLE, DOWN, STRONGLY_DOWN
    }

    data class WindowAnalysis(
        val windowSize: Int,
        val startRound: Int,
        val endRound: Int,
        val satisfactionRate: Double,
        val violationRate: Double
    )

    fun analyzePatternTrends(
        data: List<LottoResult>,
        recentCount: Int = 50,
        windowSize: Int = 10
    ): Map<String, PatternTrend> {
        val sortedData = data.sortedByDescending { it.round }
        val recentData = sortedData.take(recentCount)

        val overallReliability = reliabilityAnalyzer.analyzePatternReliability(data)
        val recentReliability = reliabilityAnalyzer.analyzePatternReliability(recentData)

        val trends = mutableMapOf<String, PatternTrend>()

        val patternNames = listOf(
            "총합구간(100-175)",
            "AC값(7이상)",
            "홀짝비율(6:0제외)",
            "고저비율(6:0제외)",
            "동일끝수(0-3개)",
            "끝수총합(14-38)",
            "연속번호(0,2연번)",
            "소수(0-3개)",
            "합성수(0-3개)",
            "완전제곱수(0-2개)",
            "3,5배수규칙",
            "쌍수(0-2개)",
            "시작끝번호규칙",
            "동일구간(3개미만)",
            "모서리패턴(1-3개)",
            "삼각패턴(전체선택X)",
            "개구리패턴(전체선택X)"
        )

        patternNames.forEach { patternName ->
            val overall = overallReliability[patternName]
            val recent = recentReliability[patternName]

            if (overall != null && recent != null) {
                val volatility = calculateVolatility(sortedData, patternName, windowSize)
                val recentViolations = findRecentViolations(recentData, patternName)
                val trendValue = recent.satisfactionRate - overall.satisfactionRate
                val trendDirection = determineTrendDirection(trendValue)
                val recommendation = generateRecommendation(
                    patternName,
                    trendDirection,
                    recent.satisfactionRate,
                    volatility
                )

                trends[patternName] = PatternTrend(
                    patternName = patternName,
                    overallSatisfactionRate = overall.satisfactionRate,
                    recentSatisfactionRate = recent.satisfactionRate,
                    trendValue = trendValue,
                    trendDirection = trendDirection,
                    volatility = volatility,
                    recentViolations = recentViolations,
                    recommendation = recommendation
                )
            }
        }

        return trends
    }

    fun analyzeMovingWindow(
        data: List<LottoResult>,
        patternName: String,
        windowSizes: List<Int> = listOf(10, 20, 30, 50)
    ): List<WindowAnalysis> {
        val sortedData = data.sortedByDescending { it.round }
        val analyses = mutableListOf<WindowAnalysis>()

        windowSizes.forEach { windowSize ->
            if (sortedData.size >= windowSize) {
                val windowData = sortedData.take(windowSize)
                val satisfactionCount = windowData.count { lotto ->
                    checkPattern(lotto, patternName)
                }
                val satisfactionRate = (satisfactionCount.toDouble() / windowSize) * 100
                val violationRate = 100 - satisfactionRate

                analyses.add(
                    WindowAnalysis(
                        windowSize = windowSize,
                        startRound = windowData.last().round,
                        endRound = windowData.first().round,
                        satisfactionRate = satisfactionRate,
                        violationRate = violationRate
                    )
                )
            }
        }

        return analyses
    }

    fun findPatternCycles(
        data: List<LottoResult>,
        patternName: String,
        minCycleLength: Int = 5,
        maxCycleLength: Int = 50
    ): List<PatternCycle> {
        val violations = data.sortedByDescending { it.round }
            .filter { !checkPattern(it, patternName) }
            .map { it.round }

        val cycles = mutableListOf<PatternCycle>()

        for (i in 0 until violations.size - 1) {
            val gap = violations[i] - violations[i + 1]
            if (gap in minCycleLength..maxCycleLength) {
                cycles.add(
                    PatternCycle(
                        patternName = patternName,
                        startRound = violations[i + 1],
                        endRound = violations[i],
                        cycleLength = gap,
                        violationRounds = listOf(violations[i + 1], violations[i])
                    )
                )
            }
        }

        return cycles
    }

    data class PatternCycle(
        val patternName: String,
        val startRound: Int,
        val endRound: Int,
        val cycleLength: Int,
        val violationRounds: List<Int>
    )

    fun analyzePatternCorrelations(
        data: List<LottoResult>,
        threshold: Double = 0.7
    ): List<PatternCorrelation> {
        val patternNames = listOf(
            "총합구간(100-175)",
            "AC값(7이상)",
            "홀짝비율(6:0제외)",
            "고저비율(6:0제외)",
            "동일끝수(0-3개)",
            "끝수총합(14-38)",
            "연속번호(0,2연번)",
            "소수(0-3개)",
            "합성수(0-3개)",
            "완전제곱수(0-2개)",
            "3,5배수규칙",
            "쌍수(0-2개)",
            "시작끝번호규칙",
            "동일구간(3개미만)",
            "모서리패턴(1-3개)",
            "삼각패턴(전체선택X)",
            "개구리패턴(전체선택X)"
        )

        val correlations = mutableListOf<PatternCorrelation>()

        for (i in patternNames.indices) {
            for (j in i + 1 until patternNames.size) {
                val pattern1 = patternNames[i]
                val pattern2 = patternNames[j]

                var bothSatisfied = 0
                var pattern1Only = 0
                var pattern2Only = 0
                var neitherSatisfied = 0

                data.forEach { lotto ->
                    val check1 = checkPattern(lotto, pattern1)
                    val check2 = checkPattern(lotto, pattern2)

                    when {
                        check1 && check2 -> bothSatisfied++
                        check1 && !check2 -> pattern1Only++
                        !check1 && check2 -> pattern2Only++
                        else -> neitherSatisfied++
                    }
                }

                val correlation = calculateCorrelation(
                    bothSatisfied,
                    pattern1Only,
                    pattern2Only,
                    neitherSatisfied
                )

                if (abs(correlation) >= threshold) {
                    correlations.add(
                        PatternCorrelation(
                            pattern1 = pattern1,
                            pattern2 = pattern2,
                            correlationValue = correlation,
                            correlationType = if (correlation > 0) "positive" else "negative"
                        )
                    )
                }
            }
        }

        return correlations.sortedByDescending { abs(it.correlationValue) }
    }

    data class PatternCorrelation(
        val pattern1: String,
        val pattern2: String,
        val correlationValue: Double,
        val correlationType: String
    )

    private fun calculateCorrelation(
        bothSatisfied: Int,
        pattern1Only: Int,
        pattern2Only: Int,
        neitherSatisfied: Int
    ): Double {
        val total = bothSatisfied + pattern1Only + pattern2Only + neitherSatisfied
        if (total == 0) return 0.0

        val p1 = (bothSatisfied + pattern1Only).toDouble() / total
        val p2 = (bothSatisfied + pattern2Only).toDouble() / total
        val p12 = bothSatisfied.toDouble() / total

        val denominator = Math.sqrt(p1 * (1 - p1) * p2 * (1 - p2))
        return if (denominator == 0.0) 0.0 else (p12 - p1 * p2) / denominator
    }

    private fun calculateVolatility(
        data: List<LottoResult>,
        patternName: String,
        windowSize: Int
    ): Double {
        if (data.size < windowSize * 2) return 0.0

        val windows = mutableListOf<Double>()
        for (i in 0..data.size - windowSize) {
            val windowData = data.subList(i, i + windowSize)
            val satisfactionCount = windowData.count { checkPattern(it, patternName) }
            windows.add((satisfactionCount.toDouble() / windowSize) * 100)
        }

        if (windows.size < 2) return 0.0

        val mean = windows.average()
        val variance = windows.map { (it - mean) * (it - mean) }.average()
        return Math.sqrt(variance)
    }

    private fun findRecentViolations(
        recentData: List<LottoResult>,
        patternName: String
    ): List<Int> {
        return recentData
            .filter { !checkPattern(it, patternName) }
            .map { it.round }
            .take(10)
    }

    private fun checkPattern(lotto: LottoResult, patternName: String): Boolean {
        return when (patternName) {
            "총합구간(100-175)" -> analyzer.run { listOf(lotto).filterTotalSection().isNotEmpty() }
            "AC값(7이상)" -> analyzer.run { listOf(lotto).filterAcCalc().isNotEmpty() }
            "홀짝비율(6:0제외)" -> analyzer.run { listOf(lotto).filterOddOrEvenBias().isNotEmpty() }
            "고저비율(6:0제외)" -> analyzer.run { listOf(lotto).filterRatioOfHighAndLow().isNotEmpty() }
            "동일끝수(0-3개)" -> analyzer.run { listOf(lotto).filterFinalNumber().isNotEmpty() }
            "끝수총합(14-38)" -> analyzer.run { listOf(lotto).filterTotalFinalNumber().isNotEmpty() }
            "연속번호(0,2연번)" -> analyzer.run { listOf(lotto).filterDiscontinuousOrTwo().isNotEmpty() }
            "소수(0-3개)" -> analyzer.run { listOf(lotto).filterDecimalCount().isNotEmpty() }
            "합성수(0-3개)" -> analyzer.run { listOf(lotto).filterCompositeNumber().isNotEmpty() }
            "완전제곱수(0-2개)" -> analyzer.run { listOf(lotto).filterPerfectSquare().isNotEmpty() }
            "3,5배수규칙" -> analyzer.run { listOf(lotto).filterMultiple().isNotEmpty() }
            "쌍수(0-2개)" -> analyzer.run { listOf(lotto).filterDual().isNotEmpty() }
            "시작끝번호규칙" -> analyzer.run { listOf(lotto).filterRange().isNotEmpty() }
            "동일구간(3개미만)" -> analyzer.run { listOf(lotto).filterFiveSection().isNotEmpty() }
            "모서리패턴(1-3개)" -> analyzer.run { listOf(lotto).filterNotCornerPattern().isNotEmpty() }
            "삼각패턴(전체선택X)" -> analyzer.run { listOf(lotto).filterTriangle().isNotEmpty() }
            "개구리패턴(전체선택X)" -> analyzer.run { listOf(lotto).filterFrogPattern().isNotEmpty() }
            else -> false
        }
    }

    private fun determineTrendDirection(trendValue: Double): TrendDirection {
        return when {
            trendValue > 5.0 -> TrendDirection.STRONGLY_UP
            trendValue > 2.0 -> TrendDirection.UP
            trendValue > -2.0 -> TrendDirection.STABLE
            trendValue > -5.0 -> TrendDirection.DOWN
            else -> TrendDirection.STRONGLY_DOWN
        }
    }

    private fun generateRecommendation(
        patternName: String,
        direction: TrendDirection,
        recentRate: Double,
        volatility: Double
    ): String {
        val stabilityStatus = if (volatility < 5.0) "안정적" else if (volatility < 10.0) "보통" else "변동성 높음"

        return when (direction) {
            TrendDirection.STRONGLY_UP -> {
                "최근 상승세가 강함 (${String.format("%.1f", recentRate)}%). $stabilityStatus. 가중치 상향 조정 고려"
            }
            TrendDirection.UP -> {
                "상승 추세 (${String.format("%.1f", recentRate)}%). $stabilityStatus. 현재 가중치 유지 권장"
            }
            TrendDirection.STABLE -> {
                "안정적 유지 (${String.format("%.1f", recentRate)}%). $stabilityStatus. 기본 가중치 적용"
            }
            TrendDirection.DOWN -> {
                "하락 추세 (${String.format("%.1f", recentRate)}%). $stabilityStatus. 가중치 하향 고려"
            }
            TrendDirection.STRONGLY_DOWN -> {
                "급격한 하락 (${String.format("%.1f", recentRate)}%). $stabilityStatus. 가중치 대폭 하향 권장"
            }
        }
    }

    fun printTrendReport(trends: Map<String, PatternTrend>) {
        println("\n============= 패턴 트렌드 분석 보고서 =============")
        println("패턴명                | 전체    | 최근    | 변화   | 방향      | 변동성 | 권장사항")
        println("---------------------|---------|---------|--------|----------|-------|----------")

        trends.entries
            .sortedByDescending { abs(it.value.trendValue) }
            .forEach { (_, trend) ->
                val directionSymbol = when (trend.trendDirection) {
                    TrendDirection.STRONGLY_UP -> "↑↑"
                    TrendDirection.UP -> "↑"
                    TrendDirection.STABLE -> "→"
                    TrendDirection.DOWN -> "↓"
                    TrendDirection.STRONGLY_DOWN -> "↓↓"
                }

                println(String.format(
                    "%-18s | %6.2f%% | %6.2f%% | %+6.2f | %-8s | %5.2f | %s",
                    trend.patternName.take(18),
                    trend.overallSatisfactionRate,
                    trend.recentSatisfactionRate,
                    trend.trendValue,
                    directionSymbol,
                    trend.volatility,
                    trend.recommendation.take(30)
                ))
            }
    }

    fun printWindowAnalysis(analyses: List<WindowAnalysis>, patternName: String) {
        println("\n============= $patternName 이동 윈도우 분석 =============")
        println("윈도우 | 시작회차 | 종료회차 | 만족률  | 위반률")
        println("------|---------|---------|--------|--------")

        analyses.forEach { analysis ->
            println(String.format(
                "%5d | %7d | %7d | %6.2f%% | %6.2f%%",
                analysis.windowSize,
                analysis.startRound,
                analysis.endRound,
                analysis.satisfactionRate,
                analysis.violationRate
            ))
        }
    }

    fun printCorrelations(correlations: List<PatternCorrelation>) {
        println("\n============= 패턴 상관관계 분석 =============")
        println("패턴1                | 패턴2                | 상관계수 | 유형")
        println("---------------------|---------------------|---------|--------")

        correlations.forEach { correlation ->
            println(String.format(
                "%-18s | %-18s | %+7.4f | %s",
                correlation.pattern1.take(18),
                correlation.pattern2.take(18),
                correlation.correlationValue,
                if (correlation.correlationType == "positive") "양의상관" else "음의상관"
            ))
        }
    }
}