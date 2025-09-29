package test

import analyzer.LottoResultCustomAnalyzer
import analyzer.PatternReliabilityAnalyzer
import filtering.ScoreBasedFilter
import model.LottoResult
import scoring.LottoScoringSystem
import trend.PatternTrendAnalyzer

class PatternScoringSystemTest {

    fun generateSampleData(): List<LottoResult> {
        return listOf(
            LottoResult("2024-01-06", 1101, 7, 11, 16, 21, 27, 33, 45),
            LottoResult("2024-01-13", 1102, 2, 9, 16, 25, 26, 40, 42),
            LottoResult("2024-01-20", 1103, 1, 5, 14, 18, 32, 42, 17),
            LottoResult("2024-01-27", 1104, 3, 8, 19, 27, 30, 41, 46),
            LottoResult("2024-02-03", 1105, 6, 13, 25, 31, 36, 43, 11),
            LottoResult("2024-02-10", 1106, 5, 12, 17, 29, 34, 44, 35),
            LottoResult("2024-02-17", 1107, 4, 15, 21, 33, 39, 41, 44),
            LottoResult("2024-02-24", 1108, 8, 10, 20, 27, 33, 38, 45),
            LottoResult("2024-03-02", 1109, 2, 11, 19, 25, 36, 44, 7),
            LottoResult("2024-03-09", 1110, 1, 7, 15, 24, 30, 45, 31)
        )
    }

    fun testPatternReliability() {
        println("========== 패턴 신뢰도 분석 테스트 ==========")

        val data = generateSampleData()
        val analyzer = LottoResultCustomAnalyzer(data)
        val reliabilityAnalyzer = PatternReliabilityAnalyzer(analyzer)

        val reliabilityMap = reliabilityAnalyzer.analyzePatternReliability(data)
        reliabilityAnalyzer.printReliabilityReport(reliabilityMap)

        println("\n가중치가 가장 높은 패턴 Top 5:")
        reliabilityMap.entries
            .sortedByDescending { it.value.weight }
            .take(5)
            .forEachIndexed { index, entry ->
                println("${index + 1}. ${entry.key}: ${String.format("%.2f", entry.value.weight)}")
            }
    }

    fun testScoringSystem() {
        println("\n========== 스코어링 시스템 테스트 ==========")

        val data = generateSampleData()
        val analyzer = LottoResultCustomAnalyzer(data)
        val reliabilityAnalyzer = PatternReliabilityAnalyzer(analyzer)
        val scoringSystem = LottoScoringSystem(analyzer, reliabilityAnalyzer)

        val scores = scoringSystem.calculateScoreBatch(data, data)
        scoringSystem.printScoreReport(scores)

        val statistics = scoringSystem.getScoreStatistics(scores)
        scoringSystem.printStatistics(statistics)

        println("\n개별 패턴 점수 상세 (1103회차):")
        val detailScore = scores.find { it.round == 1103 }
        if (detailScore != null) {
            println("총점: ${String.format("%.2f", detailScore.score)} (${detailScore.grade})")
            println("\n만족한 패턴:")
            detailScore.satisfiedPatterns.forEach {
                println("  ✓ ${it.name}: +${String.format("%.2f", it.weight)}")
            }
            println("\n위반한 패턴:")
            detailScore.violatedPatterns.forEach {
                println("  ✗ ${it.name}: 0.00 (가중치: ${String.format("%.2f", it.weight)})")
            }
        }
    }

    fun testFiltering() {
        println("\n========== 필터링 시스템 테스트 ==========")

        val data = generateSampleData()
        val testCombinations = generateTestCombinations()
        val analyzer = LottoResultCustomAnalyzer(data)
        val reliabilityAnalyzer = PatternReliabilityAnalyzer(analyzer)
        val scoringSystem = LottoScoringSystem(analyzer, reliabilityAnalyzer)
        val filter = ScoreBasedFilter(scoringSystem)

        println("\n임계값별 필터링 결과:")
        val thresholds = listOf(60.0, 70.0, 80.0, 85.0, 90.0)
        val multiResults = filter.filterWithMultipleThresholds(testCombinations, data, thresholds)

        multiResults.forEach { (threshold, result) ->
            println(String.format(
                "임계값 %.0f: %d/%d 통과 (%.2f%%)",
                threshold,
                result.totalPassed,
                result.totalEvaluated,
                result.filteringRate
            ))
        }

        val config = ScoreBasedFilter.FilterConfig(
            minScore = 80.0,
            maxResults = 10,
            includeGrades = setOf("S+", "S", "A+", "A")
        )

        val result = filter.filterByScore(testCombinations, data, config)
        filter.printFilterResult(result, detailed = true)

        val optimal = filter.getOptimalThreshold(testCombinations, data, 50)
        println("\n목표 50개 결과를 위한 최적 임계값: $optimal")
    }

    fun testTrendAnalysis() {
        println("\n========== 패턴 트렌드 분석 테스트 ==========")

        val data = generateSampleData()
        val analyzer = LottoResultCustomAnalyzer(data)
        val reliabilityAnalyzer = PatternReliabilityAnalyzer(analyzer)
        val trendAnalyzer = PatternTrendAnalyzer(analyzer, reliabilityAnalyzer)

        val trends = trendAnalyzer.analyzePatternTrends(data, recentCount = 5)
        trendAnalyzer.printTrendReport(trends)

        println("\n총합구간 패턴 이동 윈도우 분석:")
        val windowAnalyses = trendAnalyzer.analyzeMovingWindow(
            data,
            "총합구간(100-175)",
            listOf(3, 5, 7)
        )
        trendAnalyzer.printWindowAnalysis(windowAnalyses, "총합구간(100-175)")

        println("\n패턴 간 상관관계 분석 (상관계수 0.5 이상):")
        val correlations = trendAnalyzer.analyzePatternCorrelations(data, threshold = 0.5)
        if (correlations.isNotEmpty()) {
            trendAnalyzer.printCorrelations(correlations.take(10))
        } else {
            println("상관계수 0.5 이상인 패턴 쌍이 없습니다.")
        }
    }

    fun testPerformance() {
        println("\n========== 성능 테스트 ==========")

        val data = generateSampleData()
        val largeCombinations = generateLargeCombinations(10000)
        val analyzer = LottoResultCustomAnalyzer(data)
        val reliabilityAnalyzer = PatternReliabilityAnalyzer(analyzer)
        val scoringSystem = LottoScoringSystem(analyzer, reliabilityAnalyzer)
        val filter = ScoreBasedFilter(scoringSystem)

        val startTime = System.currentTimeMillis()
        val result = filter.filterByScore(
            largeCombinations,
            data,
            ScoreBasedFilter.FilterConfig(minScore = 80.0, maxResults = 1000)
        )
        val endTime = System.currentTimeMillis()

        println("처리된 조합 수: ${result.totalEvaluated}")
        println("통과한 조합 수: ${result.totalPassed}")
        println("처리 시간: ${endTime - startTime}ms")
        println("초당 처리량: ${(result.totalEvaluated * 1000) / (endTime - startTime)}개/초")
    }

    fun testBackTesting() {
        println("\n========== 백테스팅 테스트 ==========")

        val data = generateSampleData()
        val analyzer = LottoResultCustomAnalyzer(data)
        val reliabilityAnalyzer = PatternReliabilityAnalyzer(analyzer)
        val scoringSystem = LottoScoringSystem(analyzer, reliabilityAnalyzer)

        val historicalScores = data.map { lotto ->
            val score = scoringSystem.calculateScore(lotto, data)
            Triple(lotto.round, score.score, score.grade)
        }

        println("역대 당첨번호 점수 분포:")
        val gradeGroups = historicalScores.groupBy { it.third }
        gradeGroups.forEach { (grade, list) ->
            val avgScore = list.map { it.second }.average()
            println("$grade 등급: ${list.size}개 (평균 ${String.format("%.2f")}점)")
        }

        val avgScore = historicalScores.map { it.second }.average()
        val minScore = historicalScores.minByOrNull { it.second }
        val maxScore = historicalScores.maxByOrNull { it.second }

        println("\n통계:")
        println("평균 점수: ${String.format("%.2f", avgScore)}")
        println("최고 점수: ${maxScore?.first}회차 - ${String.format("%.2f", maxScore?.second ?: 0.0)}점")
        println("최저 점수: ${minScore?.first}회차 - ${String.format("%.2f", minScore?.second ?: 0.0)}점")

        val above80 = historicalScores.count { it.second >= 80.0 }
        println("\n80점 이상: $above80/${historicalScores.size} (${(above80 * 100) / historicalScores.size}%)")
    }

    private fun generateTestCombinations(): List<LottoResult> {
        val combinations = mutableListOf<LottoResult>()
        var round = 2000

        for (i in 1..100) {
            val numbers = generateRandomNumbers()
            combinations.add(
                LottoResult(
                    "2024-12-01",
                    round++,
                    numbers[0], numbers[1], numbers[2],
                    numbers[3], numbers[4], numbers[5],
                    (1..45).random()
                )
            )
        }
        return combinations
    }

    private fun generateLargeCombinations(count: Int): List<LottoResult> {
        val combinations = mutableListOf<LottoResult>()
        var round = 10000

        repeat(count) {
            val numbers = generateRandomNumbers()
            combinations.add(
                LottoResult(
                    "2024-12-01",
                    round++,
                    numbers[0], numbers[1], numbers[2],
                    numbers[3], numbers[4], numbers[5],
                    (1..45).random()
                )
            )
        }
        return combinations
    }

    private fun generateRandomNumbers(): List<Int> {
        return (1..45).shuffled().take(6).sorted()
    }

    fun runAllTests() {
        testPatternReliability()
        testScoringSystem()
        testFiltering()
        testTrendAnalysis()
        testPerformance()
        testBackTesting()
    }
}

fun main() {
    val test = PatternScoringSystemTest()
    test.runAllTests()
}