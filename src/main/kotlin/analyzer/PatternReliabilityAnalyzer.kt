package analyzer

import model.LottoResult

class PatternReliabilityAnalyzer(private val analyzer: LottoResultCustomAnalyzer) {

    data class PatternReliability(
        val patternName: String,
        val satisfactionRate: Double,
        val weight: Double,
        val totalSamples: Int,
        val satisfiedCount: Int
    )

    fun analyzePatternReliability(data: List<LottoResult>): Map<String, PatternReliability> {
        val reliabilityMap = mutableMapOf<String, PatternReliability>()

        reliabilityMap["총합구간(100-175)"] = calculatePatternReliability(
            "총합구간(100-175)",
            data
        ) { listOf(it).filterTotalSection().isNotEmpty() }

        reliabilityMap["AC값(7이상)"] = calculatePatternReliability(
            "AC값(7이상)",
            data
        ) { listOf(it).filterAcCalc().isNotEmpty() }

        reliabilityMap["홀짝비율(6:0제외)"] = calculatePatternReliability(
            "홀짝비율(6:0제외)",
            data
        ) { listOf(it).filterOddOrEvenBias().isNotEmpty() }

        reliabilityMap["고저비율(6:0제외)"] = calculatePatternReliability(
            "고저비율(6:0제외)",
            data
        ) { listOf(it).filterRatioOfHighAndLow().isNotEmpty() }

        reliabilityMap["동일끝수(0-3개)"] = calculatePatternReliability(
            "동일끝수(0-3개)",
            data
        ) { listOf(it).filterFinalNumber().isNotEmpty() }

        reliabilityMap["끝수총합(14-38)"] = calculatePatternReliability(
            "끝수총합(14-38)",
            data
        ) { listOf(it).filterTotalFinalNumber().isNotEmpty() }

        reliabilityMap["연속번호(0,2연번)"] = calculatePatternReliability(
            "연속번호(0,2연번)",
            data
        ) { listOf(it).filterDiscontinuousOrTwo().isNotEmpty() }

        reliabilityMap["소수(0-3개)"] = calculatePatternReliability(
            "소수(0-3개)",
            data
        ) { listOf(it).filterDecimalCount().isNotEmpty() }

        reliabilityMap["합성수(0-3개)"] = calculatePatternReliability(
            "합성수(0-3개)",
            data
        ) { listOf(it).filterCompositeNumber().isNotEmpty() }

        reliabilityMap["완전제곱수(0-2개)"] = calculatePatternReliability(
            "완전제곱수(0-2개)",
            data
        ) { listOf(it).filterPerfectSquare().isNotEmpty() }

        reliabilityMap["3,5배수규칙"] = calculatePatternReliability(
            "3,5배수규칙",
            data
        ) { listOf(it).filterMultiple().isNotEmpty() }

        reliabilityMap["쌍수(0-2개)"] = calculatePatternReliability(
            "쌍수(0-2개)",
            data
        ) { listOf(it).filterDual().isNotEmpty() }

        reliabilityMap["시작끝번호규칙"] = calculatePatternReliability(
            "시작끝번호규칙",
            data
        ) { listOf(it).filterRange().isNotEmpty() }

        reliabilityMap["동일구간(3개미만)"] = calculatePatternReliability(
            "동일구간(3개미만)",
            data
        ) { listOf(it).filterFiveSection().isNotEmpty() }

        reliabilityMap["모서리패턴(1-3개)"] = calculatePatternReliability(
            "모서리패턴(1-3개)",
            data
        ) { listOf(it).filterNotCornerPattern().isNotEmpty() }

        reliabilityMap["삼각패턴(전체선택X)"] = calculatePatternReliability(
            "삼각패턴(전체선택X)",
            data
        ) { listOf(it).filterTriangle().isNotEmpty() }

        reliabilityMap["개구리패턴(전체선택X)"] = calculatePatternReliability(
            "개구리패턴(전체선택X)",
            data
        ) { listOf(it).filterFrogPattern().isNotEmpty() }

        return reliabilityMap
    }

    private fun calculatePatternReliability(
        patternName: String,
        data: List<LottoResult>,
        patternCheck: (LottoResult) -> Boolean
    ): PatternReliability {
        val satisfiedCount = data.count(patternCheck)
        val totalSamples = data.size
        val satisfactionRate = (satisfiedCount.toDouble() / totalSamples) * 100
        val weight = satisfactionRate / 10.0

        return PatternReliability(
            patternName = patternName,
            satisfactionRate = satisfactionRate,
            weight = weight,
            totalSamples = totalSamples,
            satisfiedCount = satisfiedCount
        )
    }

    private fun List<LottoResult>.filterTotalSection(): List<LottoResult> = with(analyzer) { this@filterTotalSection.filterTotalSection() }
    private fun List<LottoResult>.filterAcCalc(): List<LottoResult> = with(analyzer) { this@filterAcCalc.filterAcCalc() }
    private fun List<LottoResult>.filterOddOrEvenBias(): List<LottoResult> = with(analyzer) { this@filterOddOrEvenBias.filterOddOrEvenBias() }
    private fun List<LottoResult>.filterRatioOfHighAndLow(): List<LottoResult> = with(analyzer) { this@filterRatioOfHighAndLow.filterRatioOfHighAndLow() }
    private fun List<LottoResult>.filterFinalNumber(): List<LottoResult> = with(analyzer) { this@filterFinalNumber.filterFinalNumber() }
    private fun List<LottoResult>.filterTotalFinalNumber(): List<LottoResult> = with(analyzer) { this@filterTotalFinalNumber.filterTotalFinalNumber() }
    private fun List<LottoResult>.filterDiscontinuousOrTwo(): List<LottoResult> = with(analyzer) { this@filterDiscontinuousOrTwo.filterDiscontinuousOrTwo() }
    private fun List<LottoResult>.filterDecimalCount(): List<LottoResult> = with(analyzer) { this@filterDecimalCount.filterDecimalCount() }
    private fun List<LottoResult>.filterCompositeNumber(): List<LottoResult> = with(analyzer) { this@filterCompositeNumber.filterCompositeNumber() }
    private fun List<LottoResult>.filterPerfectSquare(): List<LottoResult> = with(analyzer) { this@filterPerfectSquare.filterPerfectSquare() }
    private fun List<LottoResult>.filterMultiple(): List<LottoResult> = with(analyzer) { this@filterMultiple.filterMultiple() }
    private fun List<LottoResult>.filterDual(): List<LottoResult> = with(analyzer) { this@filterDual.filterDual() }
    private fun List<LottoResult>.filterRange(): List<LottoResult> = with(analyzer) { this@filterRange.filterRange() }
    private fun List<LottoResult>.filterFiveSection(): List<LottoResult> = with(analyzer) { this@filterFiveSection.filterFiveSection() }
    private fun List<LottoResult>.filterNotCornerPattern(): List<LottoResult> = with(analyzer) { this@filterNotCornerPattern.filterNotCornerPattern() }
    private fun List<LottoResult>.filterTriangle(): List<LottoResult> = with(analyzer) { this@filterTriangle.filterTriangle() }
    private fun List<LottoResult>.filterFrogPattern(): List<LottoResult> = with(analyzer) { this@filterFrogPattern.filterFrogPattern() }

    fun printReliabilityReport(reliabilityMap: Map<String, PatternReliability>) {
        println("\n============= 패턴별 신뢰도 분석 결과 =============")
        println("패턴명                    | 만족률    | 가중치  | 샘플수")
        println("------------------------|----------|--------|--------")

        reliabilityMap.entries
            .sortedByDescending { it.value.satisfactionRate }
            .forEach { (_, reliability) ->
                println(String.format(
                    "%-20s | %6.2f%% | %6.2f | %6d",
                    reliability.patternName,
                    reliability.satisfactionRate,
                    reliability.weight,
                    reliability.totalSamples
                ))
            }
    }

    fun analyzeRecentTrends(
        data: List<LottoResult>,
        recentCount: Int = 50
    ): Map<String, Double> {
        val recentData = data.sortedByDescending { it.round }.take(recentCount)
        val recentReliability = analyzePatternReliability(recentData)
        val allReliability = analyzePatternReliability(data)

        val trendMap = mutableMapOf<String, Double>()

        recentReliability.forEach { (patternName, recent) ->
            val overall = allReliability[patternName]
            if (overall != null) {
                val trend = recent.satisfactionRate - overall.satisfactionRate
                trendMap[patternName] = trend
            }
        }

        return trendMap
    }
}