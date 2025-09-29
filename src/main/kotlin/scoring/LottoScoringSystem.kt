package scoring

import analyzer.LottoResultCustomAnalyzer
import analyzer.PatternReliabilityAnalyzer
import model.LottoResult

class LottoScoringSystem(
    private val analyzer: LottoResultCustomAnalyzer,
    private val reliabilityAnalyzer: PatternReliabilityAnalyzer
) {
    data class PatternScore(
        val round: Int,
        val numbers: List<Int>,
        val score: Double,
        val satisfiedPatterns: List<PatternDetail>,
        val violatedPatterns: List<PatternDetail>,
        val grade: String
    )

    data class PatternDetail(
        val name: String,
        val weight: Double,
        val satisfied: Boolean
    )

    data class ScoringConfig(
        val useCustomWeights: Boolean = false,
        val customWeights: Map<String, Double> = emptyMap(),
        val useDynamicWeights: Boolean = false,
        val recentDataCount: Int = 50
    )

    fun calculateScore(
        lotto: LottoResult,
        historicalData: List<LottoResult>,
        config: ScoringConfig = ScoringConfig()
    ): PatternScore {
        val weights = when {
            config.useCustomWeights -> config.customWeights
            config.useDynamicWeights -> calculateDynamicWeights(historicalData, config.recentDataCount)
            else -> getStaticWeights(historicalData)
        }

        val patternResults = evaluateAllPatterns(lotto)
        val satisfiedPatterns = mutableListOf<PatternDetail>()
        val violatedPatterns = mutableListOf<PatternDetail>()

        var totalScore = 0.0
        var maxPossibleScore = 0.0

        patternResults.forEach { (patternName, satisfied) ->
            val weight = weights[patternName] ?: 5.0
            maxPossibleScore += weight

            val detail = PatternDetail(patternName, weight, satisfied)

            if (satisfied) {
                satisfiedPatterns.add(detail)
                totalScore += weight
            } else {
                violatedPatterns.add(detail)
            }
        }

        val normalizedScore = if (maxPossibleScore > 0) {
            (totalScore / maxPossibleScore) * 100
        } else 0.0

        val grade = calculateGrade(normalizedScore)

        return PatternScore(
            round = lotto.round,
            numbers = lotto.numbers,
            score = normalizedScore,
            satisfiedPatterns = satisfiedPatterns.sortedByDescending { it.weight },
            violatedPatterns = violatedPatterns.sortedByDescending { it.weight },
            grade = grade
        )
    }

    fun calculateScoreBatch(
        lottos: List<LottoResult>,
        historicalData: List<LottoResult>,
        config: ScoringConfig = ScoringConfig()
    ): List<PatternScore> {
        return lottos.map { calculateScore(it, historicalData, config) }
    }

    private fun evaluateAllPatterns(lotto: LottoResult): Map<String, Boolean> {
        val patterns = mutableMapOf<String, Boolean>()

        patterns["총합구간(100-175)"] = checkTotalSection(lotto)
        patterns["AC값(7이상)"] = checkAcCalc(lotto)
        patterns["홀짝비율(6:0제외)"] = checkOddOrEvenBias(lotto)
        patterns["고저비율(6:0제외)"] = checkRatioOfHighAndLow(lotto)
        patterns["동일끝수(0-3개)"] = checkFinalNumber(lotto)
        patterns["끝수총합(14-38)"] = checkTotalFinalNumber(lotto)
        patterns["연속번호(0,2연번)"] = checkDiscontinuousOrTwo(lotto)
        patterns["소수(0-3개)"] = checkDecimalCount(lotto)
        patterns["합성수(0-3개)"] = checkCompositeNumber(lotto)
        patterns["완전제곱수(0-2개)"] = checkPerfectSquare(lotto)
        patterns["3,5배수규칙"] = checkMultiple(lotto)
        patterns["쌍수(0-2개)"] = checkDual(lotto)
        patterns["시작끝번호규칙"] = checkRange(lotto)
        patterns["동일구간(3개미만)"] = checkFiveSection(lotto)
        patterns["모서리패턴(1-3개)"] = checkCornerPattern(lotto)
        patterns["삼각패턴(전체선택X)"] = checkTriangle(lotto)
        patterns["개구리패턴(전체선택X)"] = checkFrogPattern(lotto)

        return patterns
    }

    private fun checkTotalSection(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterTotalSection().isNotEmpty() }

    private fun checkAcCalc(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterAcCalc().isNotEmpty() }

    private fun checkOddOrEvenBias(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterOddOrEvenBias().isNotEmpty() }

    private fun checkRatioOfHighAndLow(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterRatioOfHighAndLow().isNotEmpty() }

    private fun checkFinalNumber(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterFinalNumber().isNotEmpty() }

    private fun checkTotalFinalNumber(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterTotalFinalNumber().isNotEmpty() }

    private fun checkDiscontinuousOrTwo(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterDiscontinuousOrTwo().isNotEmpty() }

    private fun checkDecimalCount(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterDecimalCount().isNotEmpty() }

    private fun checkCompositeNumber(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterCompositeNumber().isNotEmpty() }

    private fun checkPerfectSquare(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterPerfectSquare().isNotEmpty() }

    private fun checkMultiple(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterMultiple().isNotEmpty() }

    private fun checkDual(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterDual().isNotEmpty() }

    private fun checkRange(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterRange().isNotEmpty() }

    private fun checkFiveSection(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterFiveSection().isNotEmpty() }

    private fun checkCornerPattern(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterNotCornerPattern().isNotEmpty() }

    private fun checkTriangle(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterTriangle().isNotEmpty() }

    private fun checkFrogPattern(lotto: LottoResult): Boolean =
        analyzer.run { listOf(lotto).filterFrogPattern().isNotEmpty() }

    private fun getStaticWeights(historicalData: List<LottoResult>): Map<String, Double> {
        val reliabilityMap = reliabilityAnalyzer.analyzePatternReliability(historicalData)
        return reliabilityMap.mapValues { it.value.weight }
    }

    private fun calculateDynamicWeights(
        historicalData: List<LottoResult>,
        recentCount: Int
    ): Map<String, Double> {
        val recentData = historicalData.sortedByDescending { it.round }.take(recentCount)
        val recentReliability = reliabilityAnalyzer.analyzePatternReliability(recentData)
        val overallReliability = reliabilityAnalyzer.analyzePatternReliability(historicalData)

        val dynamicWeights = mutableMapOf<String, Double>()

        recentReliability.forEach { (patternName, recent) ->
            val overall = overallReliability[patternName]
            if (overall != null) {
                val adjustmentFactor = recent.satisfactionRate / overall.satisfactionRate
                val baseWeight = overall.weight
                dynamicWeights[patternName] = baseWeight * (0.7 + 0.3 * adjustmentFactor)
            }
        }

        return dynamicWeights
    }

    private fun calculateGrade(score: Double): String {
        return when {
            score >= 95 -> "S+"
            score >= 90 -> "S"
            score >= 85 -> "A+"
            score >= 80 -> "A"
            score >= 75 -> "B+"
            score >= 70 -> "B"
            score >= 65 -> "C+"
            score >= 60 -> "C"
            else -> "D"
        }
    }

    fun printScoreReport(scores: List<PatternScore>, limit: Int = 20) {
        println("\n============= 로또 패턴 스코어링 결과 =============")
        println("회차   | 번호                        | 점수    | 등급 | 만족 | 위반")
        println("------|-----------------------------|---------|----|-----|-----")

        scores.take(limit).forEach { score ->
            val numbersStr = score.numbers.joinToString(", ") { it.toString().padStart(2, '0') }
            println(String.format(
                "%5d | %-27s | %6.2f | %3s | %3d | %3d",
                score.round,
                numbersStr,
                score.score,
                score.grade,
                score.satisfiedPatterns.size,
                score.violatedPatterns.size
            ))
        }

        if (scores.size > limit) {
            println("... ${scores.size - limit}개 더 있음")
        }
    }

    fun getScoreStatistics(scores: List<PatternScore>): ScoreStatistics {
        if (scores.isEmpty()) {
            return ScoreStatistics(0.0, 0.0, 0.0, 0.0, emptyMap())
        }

        val scoreValues = scores.map { it.score }
        val average = scoreValues.average()
        val min = scoreValues.minOrNull() ?: 0.0
        val max = scoreValues.maxOrNull() ?: 0.0
        val median = scoreValues.sorted()[scoreValues.size / 2]

        val gradeDistribution = scores.groupingBy { it.grade }.eachCount()

        return ScoreStatistics(average, min, max, median, gradeDistribution)
    }

    data class ScoreStatistics(
        val averageScore: Double,
        val minScore: Double,
        val maxScore: Double,
        val medianScore: Double,
        val gradeDistribution: Map<String, Int>
    )

    fun printStatistics(statistics: ScoreStatistics) {
        println("\n============= 점수 통계 =============")
        println("평균 점수: ${String.format("%.2f", statistics.averageScore)}")
        println("최소 점수: ${String.format("%.2f", statistics.minScore)}")
        println("최대 점수: ${String.format("%.2f", statistics.maxScore)}")
        println("중간 점수: ${String.format("%.2f", statistics.medianScore)}")
        println("\n등급 분포:")
        statistics.gradeDistribution
            .toSortedMap(compareByDescending { it })
            .forEach { (grade, count) ->
                println("  $grade: ${count}개")
            }
    }
}