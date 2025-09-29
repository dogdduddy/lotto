package example

import analyzer.LottoResultCustomAnalyzer
import analyzer.PatternReliabilityAnalyzer
import filtering.ScoreBasedFilter
import model.LottoResult
import scoring.LottoScoringSystem
import trend.PatternTrendAnalyzer

class UsageExample {

    fun demonstrateBasicUsage() {
        println("============= 로또 패턴 스코어링 시스템 사용 예제 =============\n")

        val historicalData = loadHistoricalData()
        println("1. 데이터 로드 완료: ${historicalData.size}개 회차\n")

        val analyzer = LottoResultCustomAnalyzer(historicalData)
        val reliabilityAnalyzer = PatternReliabilityAnalyzer(analyzer)
        val scoringSystem = LottoScoringSystem(analyzer, reliabilityAnalyzer)
        val filter = ScoreBasedFilter(scoringSystem)
        val trendAnalyzer = PatternTrendAnalyzer(analyzer, reliabilityAnalyzer)

        println("2. 패턴 신뢰도 분석")
        val reliability = reliabilityAnalyzer.analyzePatternReliability(historicalData)
        println("   - 분석된 패턴 수: ${reliability.size}개")
        println("   - 평균 신뢰도: ${String.format("%.2f", reliability.values.map { it.satisfactionRate }.average())}%\n")

        println("3. 새로운 번호 조합 생성 및 스코어링")
        val newCombinations = generateNewCombinations()
        val scores = scoringSystem.calculateScoreBatch(newCombinations, historicalData)

        println("   생성된 조합 수: ${newCombinations.size}개")
        println("   평균 점수: ${String.format("%.2f", scores.map { it.score }.average())}점\n")

        println("4. 점수 기반 필터링 (80점 이상)")
        val filterConfig = ScoreBasedFilter.FilterConfig(
            minScore = 80.0,
            maxResults = 10,
            includeGrades = setOf("S+", "S", "A+", "A")
        )
        val filterResult = filter.filterByScore(newCombinations, historicalData, filterConfig)

        println("   필터링 결과:")
        println("   - 통과한 조합: ${filterResult.totalPassed}개")
        println("   - 필터링 비율: ${String.format("%.2f", filterResult.filteringRate)}%\n")

        println("5. 상위 5개 조합 출력")
        filterResult.filteredScores.take(5).forEachIndexed { index, score ->
            println("   ${index + 1}. [${score.numbers.joinToString(", ")}] - ${String.format("%.2f", score.score)}점 (${score.grade})")
        }

        println("\n6. 최근 트렌드 분석")
        val trends = trendAnalyzer.analyzePatternTrends(historicalData, recentCount = 50)
        val topTrends = trends.entries
            .sortedByDescending { Math.abs(it.value.trendValue) }
            .take(3)

        println("   주요 변화 패턴:")
        topTrends.forEach { (name, trend) ->
            val symbol = if (trend.trendValue > 0) "↑" else "↓"
            println("   - $name: ${String.format("%+.2f", trend.trendValue)}% $symbol")
        }
    }

    fun demonstrateAdvancedFeatures() {
        println("\n============= 고급 기능 사용 예제 =============\n")

        val historicalData = loadHistoricalData()
        val analyzer = LottoResultCustomAnalyzer(historicalData)
        val reliabilityAnalyzer = PatternReliabilityAnalyzer(analyzer)
        val scoringSystem = LottoScoringSystem(analyzer, reliabilityAnalyzer)
        val filter = ScoreBasedFilter(scoringSystem)

        println("1. 동적 가중치 적용")
        val dynamicConfig = LottoScoringSystem.ScoringConfig(
            useDynamicWeights = true,
            recentDataCount = 30
        )

        val testLotto = LottoResult("2024-12-01", 9999, 5, 12, 18, 25, 33, 41, 7)
        val staticScore = scoringSystem.calculateScore(testLotto, historicalData)
        val dynamicScore = scoringSystem.calculateScore(testLotto, historicalData, dynamicConfig)

        println("   테스트 조합: [${testLotto.numbers.joinToString(", ")}]")
        println("   - 정적 가중치 점수: ${String.format("%.2f", staticScore.score)}")
        println("   - 동적 가중치 점수: ${String.format("%.2f", dynamicScore.score)}\n")

        println("2. 특정 패턴 필수/제외 필터링")
        val combinations = generateNewCombinations()

        val requiredConfig = ScoreBasedFilter.FilterConfig(
            minScore = 70.0,
            requirePatterns = setOf("홀짝비율(6:0제외)", "고저비율(6:0제외)"),
            excludePatterns = setOf("시작끝번호규칙")
        )

        val requiredResult = filter.filterByScore(combinations, historicalData, requiredConfig)
        println("   필수 패턴: 홀짝비율, 고저비율")
        println("   제외 패턴: 시작끝번호규칙")
        println("   결과: ${requiredResult.totalPassed}/${combinations.size} 통과\n")

        println("3. 최적 임계값 찾기")
        val targetCount = 50
        val optimalThreshold = filter.getOptimalThreshold(combinations, historicalData, targetCount)
        println("   목표 결과 수: ${targetCount}개")
        println("   최적 임계값: ${String.format("%.1f", optimalThreshold)}점\n")

        println("4. 패턴별 점수 기여도 분석")
        val sampleScore = scoringSystem.calculateScore(combinations.first(), historicalData)
        val contributions = sampleScore.satisfiedPatterns
            .sortedByDescending { it.weight }
            .take(5)

        println("   상위 5개 기여 패턴:")
        contributions.forEach { pattern ->
            val contribution = (pattern.weight / sampleScore.score) * 100
            println("   - ${pattern.name}: ${String.format("%.1f", contribution)}%")
        }
    }

    fun demonstrateRealWorldScenario() {
        println("\n============= 실전 시나리오 예제 =============\n")

        val historicalData = loadHistoricalData()
        val analyzer = LottoResultCustomAnalyzer(historicalData)
        val reliabilityAnalyzer = PatternReliabilityAnalyzer(analyzer)
        val scoringSystem = LottoScoringSystem(analyzer, reliabilityAnalyzer)
        val filter = ScoreBasedFilter(scoringSystem)
        val trendAnalyzer = PatternTrendAnalyzer(analyzer, reliabilityAnalyzer)

        println("시나리오: 다음 회차 당첨 번호 예측을 위한 후보 선정\n")

        println("1단계: 최근 패턴 트렌드 분석")
        val trends = trendAnalyzer.analyzePatternTrends(historicalData, recentCount = 20)
        val strongPatterns = trends.filter {
            it.value.trendDirection == PatternTrendAnalyzer.TrendDirection.STRONGLY_UP ||
            it.value.trendDirection == PatternTrendAnalyzer.TrendDirection.UP
        }.keys

        println("   상승 트렌드 패턴: ${strongPatterns.size}개 확인\n")

        println("2단계: 후보 조합 생성 (1000개)")
        val candidates = generateSmartCombinations(1000, historicalData)
        println("   생성 완료\n")

        println("3단계: 다단계 필터링")

        val step1Config = ScoreBasedFilter.FilterConfig(
            minScore = 75.0,
            maxResults = 500
        )
        val step1Result = filter.filterByScore(candidates, historicalData, step1Config)
        println("   - 1차 필터(75점 이상): ${step1Result.totalPassed}개 통과")

        val step2Config = ScoreBasedFilter.FilterConfig(
            minScore = 80.0,
            maxResults = 100,
            includeGrades = setOf("S+", "S", "A+", "A")
        )
        val step2Result = filter.filterByScore(
            step1Result.filteredScores.map {
                LottoResult("2024-12-01", it.round,
                    it.numbers[0], it.numbers[1], it.numbers[2],
                    it.numbers[3], it.numbers[4], it.numbers[5], 1)
            },
            historicalData,
            step2Config
        )
        println("   - 2차 필터(80점 + A등급 이상): ${step2Result.totalPassed}개 통과")

        val finalConfig = ScoreBasedFilter.FilterConfig(
            minScore = 85.0,
            maxResults = 10,
            requirePatterns = strongPatterns
        )
        val finalResult = filter.filterByScore(
            step2Result.filteredScores.map {
                LottoResult("2024-12-01", it.round,
                    it.numbers[0], it.numbers[1], it.numbers[2],
                    it.numbers[3], it.numbers[4], it.numbers[5], 1)
            },
            historicalData,
            finalConfig
        )
        println("   - 최종 필터(85점 + 트렌드 패턴): ${finalResult.filteredScores.size}개 선정\n")

        println("4단계: 최종 추천 번호")
        finalResult.filteredScores.take(5).forEachIndexed { index, score ->
            println("   추천 ${index + 1}: [${score.numbers.joinToString(", ") { it.toString().padStart(2, '0') }}]")
            println("          점수: ${String.format("%.2f", score.score)}점 (${score.grade}) - 만족 패턴 ${score.satisfiedPatterns.size}개")
        }
    }

    private fun loadHistoricalData(): List<LottoResult> {
        return (1..100).map { round ->
            val numbers = generateRandomNumbers()
            LottoResult(
                "2024-01-01",
                round,
                numbers[0], numbers[1], numbers[2],
                numbers[3], numbers[4], numbers[5],
                (1..45).random()
            )
        }
    }

    private fun generateNewCombinations(): List<LottoResult> {
        return (1..200).map { i ->
            val numbers = generateRandomNumbers()
            LottoResult(
                "2024-12-01",
                5000 + i,
                numbers[0], numbers[1], numbers[2],
                numbers[3], numbers[4], numbers[5],
                (1..45).random()
            )
        }
    }

    private fun generateSmartCombinations(count: Int, historicalData: List<LottoResult>): List<LottoResult> {
        val combinations = mutableListOf<LottoResult>()
        val analyzer = LottoResultCustomAnalyzer(historicalData)
        val frequency = analyzer.getNumberFrequency(historicalData.takeLast(20))
        val hotNumbers = frequency.entries.sortedByDescending { it.value }.take(15).map { it.key }

        repeat(count) { i ->
            val numbers = mutableSetOf<Int>()
            numbers.addAll(hotNumbers.shuffled().take(3))
            while (numbers.size < 6) {
                numbers.add((1..45).random())
            }
            val sorted = numbers.sorted()

            combinations.add(
                LottoResult(
                    "2024-12-01",
                    6000 + i,
                    sorted[0], sorted[1], sorted[2],
                    sorted[3], sorted[4], sorted[5],
                    (1..45).random()
                )
            )
        }
        return combinations
    }

    private fun generateRandomNumbers(): List<Int> {
        return (1..45).shuffled().take(6).sorted()
    }
}

fun main() {
    val example = UsageExample()

    example.demonstrateBasicUsage()
    example.demonstrateAdvancedFeatures()
    example.demonstrateRealWorldScenario()
}