# 🎯 로또 패턴 분석 시스템 사용 가이드

## 📁 프로그램 실행 방법

### 1. 기본 실행
```bash
# IntelliJ IDEA에서 Main.kt 실행
# 또는 터미널에서:
./gradlew run
```

### 2. 예제 실행
```bash
# 사용 예제 실행
kotlin src/main/kotlin/example/UsageExample.kt
```

## 🎲 주요 기능 사용법

### 1. 기본 데이터 로드
```kotlin
val dataManager = LottoDataManager()
val lottoResults = dataManager.loadLottoResultsFromFile("data/lotto_result.json")
val customAnalyzer = LottoResultCustomAnalyzer(lottoResults)
```

### 2. 패턴 신뢰도 분석
```kotlin
val reliabilityAnalyzer = PatternReliabilityAnalyzer(customAnalyzer)
val reliability = reliabilityAnalyzer.analyzePatternReliability(lottoResults)
reliabilityAnalyzer.printReliabilityReport(reliability)
```

### 3. 번호 조합 점수 계산
```kotlin
val scoringSystem = LottoScoringSystem(customAnalyzer, reliabilityAnalyzer)

// 단일 번호 점수 계산
val testLotto = LottoResult("", 0, 1, 6, 8, 25, 38, 43, 25)
val score = scoringSystem.calculateScore(testLotto, lottoResults)
println("점수: ${score.score}, 등급: ${score.grade}")
```

### 4. 스마트 필터링 시스템
```kotlin
val filter = ScoreBasedFilter(scoringSystem)

// 기본 필터링 (기본값 사용)
val result = filter.filterByScore(combinations, lottoResults)

// 맞춤 설정 필터링
val config = ScoreBasedFilter.FilterConfig(
    minScore = 85.0,           // 최소 점수
    maxResults = 50,           // 최대 결과 수
    includeGrades = setOf("S+", "S", "A+", "A"),  // 포함할 등급
    requirePatterns = setOf("홀짝비율(6:0제외)"),   // 필수 패턴
    excludePatterns = setOf("시작끝번호규칙")        // 제외 패턴
)
val result = filter.filterByScore(combinations, lottoResults, config)
```

### 5. 최적 임계값 자동 계산
```kotlin
// 원하는 결과 수에 맞는 최적 점수 임계값 찾기
val optimalThreshold = filter.getOptimalThreshold(combinations, lottoResults, 50)
val result = filter.filterByScore(combinations, lottoResults,
    ScoreBasedFilter.FilterConfig(minScore = optimalThreshold))
```

### 6. 트렌드 분석
```kotlin
val trendAnalyzer = PatternTrendAnalyzer(customAnalyzer, reliabilityAnalyzer)
val trends = trendAnalyzer.analyzePatternTrends(lottoResults, recentCount = 50)

// 상승/하락 트렌드 확인
trends.forEach { (patternName, trend) ->
    val symbol = if (trend.trendValue > 0) "↑" else "↓"
    println("$patternName: ${trend.trendValue}% $symbol")
}
```

## 🎯 실전 활용 시나리오

### 시나리오 1: 다음 회차 예측용 번호 선정
```kotlin
// 1. 번호 조합 생성 (1~45에서 6개 선택의 모든 조합 또는 일부)
val combinations = generateCombinations()

// 2. 다단계 필터링
val step1 = filter.filterByScore(combinations, lottoResults,
    ScoreBasedFilter.FilterConfig(minScore = 75.0))

val step2 = filter.filterByScore(step1.results, lottoResults,
    ScoreBasedFilter.FilterConfig(minScore = 85.0, maxResults = 10))

// 3. 최종 추천 번호 출력
step2.filteredScores.forEach { score ->
    println("[${score.numbers.joinToString(", ")}] - ${score.score}점")
}
```

### 시나리오 2: 과거 당첨 번호 검증
```kotlin
// 특정 회차의 당첨 번호가 얼마나 좋은 점수를 받는지 확인
val winningNumber = customAnalyzer.getLottoWithRound(1188)
winningNumber?.let {
    val score = scoringSystem.calculateScore(it, lottoResults)
    println("${it.round}회 당첨번호 점수: ${score.score}점 (${score.grade})")
}
```

### 시나리오 3: 패턴별 성능 분석
```kotlin
// 최근 트렌드와 전체 데이터 비교
val recentTrends = reliabilityAnalyzer.analyzeRecentTrends(lottoResults, 30)
recentTrends.forEach { (pattern, change) ->
    println("$pattern: ${if(change > 0) "+" else ""}${change}% 변화")
}
```

## 📊 결과 해석 가이드

### 점수 체계
- **S+ (95점 이상)**: 매우 우수한 조합
- **S (90-94점)**: 우수한 조합
- **A+ (85-89점)**: 좋은 조합
- **A (80-84점)**: 양호한 조합
- **B+ (75-79점)**: 보통 조합
- **B (70-74점)**: 기준 이하
- **C (70점 미만)**: 권장하지 않음

### 주요 패턴 설명
- **홀짝비율**: 홀수/짝수 번호의 균형
- **고저비율**: 낮은 번호(1-22)/높은 번호(23-45)의 균형
- **AC값**: 번호 간 차이의 복잡도
- **연속번호**: 연속된 번호의 개수
- **구간분포**: 1-9, 10-19, 20-29, 30-39, 40-45 구간별 분포

## 🔧 고급 설정

### 동적 가중치 사용
```kotlin
val dynamicConfig = LottoScoringSystem.ScoringConfig(
    useDynamicWeights = true,
    recentDataCount = 30  // 최근 30회차 데이터 기반
)
val score = scoringSystem.calculateScore(lotto, lottoResults, dynamicConfig)
```

### 효과성 분석
```kotlin
val effectiveness = filter.analyzeFilterEffectiveness(
    testData = testCombinations,
    historicalData = lottoResults,
    actualWinningNumbers = actualWinners,
    thresholds = listOf(70.0, 75.0, 80.0, 85.0, 90.0)
)
filter.printEffectivenessReport(effectiveness)
```

## 📝 주요 클래스 설명

### LottoDataManager
- **역할**: JSON 파일에서 로또 데이터 로드/저장
- **주요 메서드**: `loadLottoResultsFromFile()`, `saveLottoResultsToFile()`

### LottoResultCustomAnalyzer
- **역할**: 로또 번호 패턴 분석 및 필터링
- **주요 메서드**: `filterTotalSection()`, `filterAcCalc()`, `filterOddOrEvenBias()` 등

### PatternReliabilityAnalyzer
- **역할**: 각 패턴의 신뢰도 및 가중치 계산
- **주요 메서드**: `analyzePatternReliability()`, `analyzeRecentTrends()`

### LottoScoringSystem
- **역할**: 번호 조합에 대한 종합 점수 계산
- **주요 메서드**: `calculateScore()`, `calculateScoreBatch()`

### ScoreBasedFilter
- **역할**: 점수 기반 필터링 및 최적화
- **주요 메서드**: `filterByScore()`, `getOptimalThreshold()`, `analyzeFilterEffectiveness()`

### PatternTrendAnalyzer
- **역할**: 패턴별 트렌드 분석
- **주요 메서드**: `analyzePatternTrends()`, `getPatternTrendSummary()`

## 💡 팁과 권장사항

### 1. 데이터 준비
- 최소 100회차 이상의 과거 데이터 사용 권장
- 데이터는 JSON 형태로 `data/` 폴더에 저장

### 2. 필터링 전략
- 점진적 필터링: 낮은 임계값 → 높은 임계값 순으로 적용
- 조합 수가 많을 때는 `maxResults`로 제한
- 특정 패턴을 중시한다면 `requirePatterns` 활용

### 3. 성능 최적화
- 대량의 조합 처리 시 `calculateScoreBatch()` 사용
- 필터링 결과를 단계별로 저장하여 재사용

### 4. 결과 검증
- 과거 당첨 번호로 시스템 성능 검증
- 다양한 임계값으로 테스트하여 최적값 찾기

## 🚀 시작하기

1. **프로젝트 클론 및 설정**
   ```bash
   git clone [repository-url]
   cd lotto_
   ```

2. **데이터 준비**
   - `data/lotto_result.json` 파일에 과거 로또 데이터 준비

3. **첫 실행**
   ```kotlin
   // Main.kt 또는 UsageExample.kt 실행
   ```

4. **결과 확인**
   - 콘솔에서 분석 결과 및 추천 번호 확인

이제 프로그램을 활용하여 로또 번호 분석을 시작해보세요! 🎯