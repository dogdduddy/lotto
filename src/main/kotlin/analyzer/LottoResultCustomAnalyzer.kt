package analyzer

import model.LottoResult
import kotlin.collections.get

// 로또 데이터 분석 클래스 (LottoResult용)
class LottoResultCustomAnalyzer(val data: List<LottoResult>) {

    fun filterNumbersLessThan(map: Map<Int, Int>, max: Int) : List<Pair<Int, Int>> {
        val list = mutableListOf<Pair<Int, Int>>()
        map.entries.filter { it.value <= max }.forEach {
            list.add(it.key to it.value)
        }
        return list
    }

    fun numbersToExclude(map: Map<Int, Int>, max: Int) : List<Int> {
        val list = mutableListOf<Int>()
        map.entries.filter { it.value >= max }.forEach {
            list.add(it.key)
        }
        return list
    }

    // 특정 회차 데이터 가져오기
    fun getLottoWithRound(round: Int): LottoResult? {
        return data.firstOrNull { it.round == round }
    }

    // 특정 회차 범위의 데이터 가져오기
    fun getRoundRange(startRound: Int, endRound: Int): List<LottoResult> {
        return data.filter { it.round in startRound..endRound }
            .sortedBy { it.round }
    }

    // 번호별 출현 횟수 계산
    fun getNumberFrequency(data: List<LottoResult>): Map<Int, Int> {
        val frequency = mutableMapOf<Int, Int>()

        data.forEach { lotto ->
            lotto.numbers.forEach { number ->
                frequency[number] = frequency.getOrDefault(number, 0) + 1
            }
        }

        return frequency.toSortedMap()
    }

    /**
     * @param round : 시작 회차
     * @param count : 제외 숫자를 지정할 회차 범위
     * @param max : 제외할 뽑힌 횟수 커트라인
     */
    fun getCoreNumbers(round: Int, count: Int, max: Int): Map<Int, Int> {
        val sortedData = data.sortedByDescending { it.round }.take(count)
        val numberFrequency = getNumberFrequency(sortedData)

        val numberExcludeList = mutableSetOf<Int>()

        (0..5).forEach {
            val currentRound = round - it
            val temp = getRoundRange(currentRound - count, currentRound)
            val frequency = getNumberFrequency(temp)
            val exclude = numbersToExclude(frequency, max)
            numberExcludeList.addAll(exclude)
        }

        return numberFrequency.filter { it.key !in numberExcludeList }
    }

    /**
     * 전체 패턴 검사 필터
     */
    fun filterAllPattern(list: List<LottoResult>): List<LottoResult> {
        return list.filterNotCornerPattern()
            .filterTotalSection()
            .filterAcCalc()
            .filterOddOrEvenBias()
            .filterRatioOfHighAndLow()
            .filterFinalNumber()
            .filterTotalFinalNumber()
            .filterDiscontinuousOrTwo()
            .filterDecimalCount()
            .filterCompositeNumber()
            .filterPerfectSquare()
            .filterMultiple()
            .filterDual()
            .filterRange()
            .filterFiveSection()
            .filterNotCornerPattern()
            .filterTriangle()
            .filterFrogPattern()
    }

    /**
     * 1. 총합구간 설정
     *   확률 77%
     * - 100 ~ 175
     */
    object TotalSection {
        const val MIN_COUNT = 100
        const val MAX_COUNT = 175
    }

    fun getTotalSection(list: List<LottoResult>) = list.filterTotalSection()

    fun List<LottoResult>.filterTotalSection(): List<LottoResult> {
        return this.filter {
            it.numbers.sum() in TotalSection.MIN_COUNT..TotalSection.MAX_COUNT
        }
    }

    /**
     * 2. AC Calc Pattern
     *    확률 84%
     * - 7이상인 경우 사용
     */
    object AC {
        const val AC_MAX_COUNT = 7
        const val AC_NUMBER_COUNT = 6
    }

    fun getAcCalc(list: List<LottoResult>): List<LottoResult> {
        return list.filterAcCalc()
    }

    fun List<LottoResult>.filterAcCalc(): List<LottoResult> {
        return this.filter { getAcValue(it) >= AC.AC_MAX_COUNT }
    }

    private fun getAcValue(lotto: LottoResult): Int {
        val numSet = mutableSetOf<Int>()
        val input = lotto.numbers

        for (i in 0 until input.size - 1) {
            for (j in i + 1 until input.size) {
                val temp = if (input[i] > input[j]) {
                    input[i] - input[j]
                } else {
                    input[j] - input[i]
                }
                numSet.add(temp)
            }
        }

        return numSet.count() - (AC.AC_NUMBER_COUNT - 1)
    }

    /**
     * 3. 홀수 또는 짝수 편향
     *   확률 97%
     * - 홀수
     * - 짝수
     */

    fun getOddOrEvenBias(list: List<LottoResult>) = list.filterOddOrEvenBias()

    fun List<LottoResult>.filterOddOrEvenBias(): List<LottoResult> {
        return this.filter {
            !it.numbers.all { it % 2 == 0 }
                    && !it.numbers.all { it % 2 != 0 }
        }
    }

    /**
     * 4. 고저 비율
     *   확률 97%
     * - 고저비율 6:0, 0:6 제외
     * - 조합번호 6개중 23을 기준으로 23미만 (1~22) 저비율, 23이상(23~45) 고비율이라고 합니다.
     * - 조합번호를 모두 '고' 혹은 '저' 비율로 조합하는 경우, 역대 1등 당첨번호 통계상 나올 확률이 3% 미만이기에 조합을 모두 '고' 혹은 '저' 비율로 선택하는 것은 추천하지 않습니다.
     */

    object HighAndLow {
        const val BOUNDARY = 23
    }

    fun getRatioOfHighAndLow(list: List<LottoResult>) = list.filterRatioOfHighAndLow()

    fun List<LottoResult>.filterRatioOfHighAndLow(): List<LottoResult> {
        return this.filter {
            !it.numbers.all { n -> n < HighAndLow.BOUNDARY }
                    && !it.numbers.all { n -> n >= HighAndLow.BOUNDARY }
        }
    }

    /**
     * 5. 동일 끝수 포함
     *   확률 99.5%
     * - 동일 끝수 0 ~ 3개 포함
     * - 끝수란 끝자리수를 말하며 당첨번호를 십자리와 단자리로 나눈후 단자리에 해당하는 숫자를 끝수라고 부릅니다. (예:42인경우 끝자리수는 2)
     * - 끝수가 같은 번호들은 동일 끝수라고 표현합니다.
     * - 역대 1등 당첨번호 통계상 같은 끝수가 4개이상 나올 확률은 1% 미만이기에 같은 끝수를 4개이상 조합번호를 만든 것은 추천하지 않습니다.
     */

    object FinalNumber {
        const val MIN_COUNT = 0
        const val MAX_COUNT = 3

        const val TOTAL_MIN_COUNT = 14
        const val TOTAL_MAX_COUNT = 38
    }

    fun getFinalNumberPattern(list: List<LottoResult>) = list.filterFinalNumber()

    fun List<LottoResult>.filterFinalNumber(): List<LottoResult> {
        return this.filter {
            val map = mutableMapOf<Int, Int>()
            it.numbers.forEach { n -> map[n.getFinalNumber()] = map.getOrDefault(n.getFinalNumber(), 0) + 1 }
            val max = map.values.max()

            max >= FinalNumber.MIN_COUNT && max <= FinalNumber.MAX_COUNT
        }
    }

    private fun Int.getFinalNumber(): Int {
        return this % 10
    }

    /**
     * 6. 끝수 총합
     *   확률 95%
     * - 끝수 총합 범위 :  14 ~ 38 구간
     * - 끝수란 조합번호의 끝자리수를 말합니다. 예를 들어 로또번호 42인경우 2가 끝수에 해당하며, 단 자리수(1~9)인 경우는 자신수가 끝수에 해당합니다.
     * - 끝수 총합구간은 로또분석가들이 생각하는 범위는 14~38이며, 역대 1등 당첨번호에 끝수 합을 보았을때 15~35구간이 나오는 확률이 90%이기에 로또타파에서는 15~38구간을 추천합니다.
     */

    fun getTotalFinalNumberPattern(list: List<LottoResult>) = list.filterTotalFinalNumber()

    fun List<LottoResult>.filterTotalFinalNumber(): List<LottoResult> {
        return this.filter {
            val sum = it.numbers.sumOf { n -> n % 10  }
            sum >= FinalNumber.TOTAL_MIN_COUNT && sum <= FinalNumber.TOTAL_MAX_COUNT
        }
    }

    /**
     * 7. 연속번호 없음 및 2연속번호 적용
     *   확률 98.5%
     * - 연속번호란 로또 당첨번호 숫자중 1,2,3 이런식으로 연속적으로 등장하는 번호는 연속번호라고 합니다.
     * 역대 1등 당첨번호 통계를 보변 연속번호가 없거나, 2연번이 나오는 확률이 90% 이상이기에 로또타파에서는 연번이 없거나 2연번이 존재하는 조합을 추천합니다.
     */

    fun getDiscontinuousOrTwo(list: List<LottoResult>) = list.filterDiscontinuousOrTwo()

    fun List<LottoResult>.filterDiscontinuousOrTwo(): List<LottoResult> {
        return this.filter {
            val count = getMaxConsecutive(it)
            count == 0 || count == 1
        }
    }

    private fun getMaxConsecutive(lotto: LottoResult): Int {
        var max = 0
        var previous = -1

        lotto.numbers.forEach {
            if (it == previous + 1) max++
            else max = 0
            previous = it
        }

        return max
    }

    /**
     * 8. 소수 0 ~ 3개 포함
     *    확률 94%
     * 45개 로또번호중 소수에 해당하는 수는 2,3,5,7,11,13,17,19,23,29,31,37,41,43 이며 총 14개수입니다.
     * 조합번호중 소수를 4개이상 포함하는 경우, 역대 1등 당첨번호 통계상 나올 확률은 1%미만이기에 추천하지 않습니다.
     */
    object Decimal {
        const val MAX_COUNT = 3
    }

    val decimal = listOf(2,3,5,7,11,13,17,19,23,29,31,37,41,43)

    fun getDecimalCount(list: List<LottoResult>) = list.filterDecimalCount()

    fun List<LottoResult>.filterDecimalCount(): List<LottoResult> {
        return this.filter {
            it.numbers.count { n -> n in decimal } <= Decimal.MAX_COUNT
        }
    }

    /**
     * 9. 합성수 0 ~ 3개 포함
     *    확률 88%
     * 합성수란 소수와 3의 배수를 제외한 숫자를 말합니다.
     * 45개 로또번호중 합성수는 1,4,8,10,14,16,20,22,25,26,28,32,34,35,38,40,44 이며 총 17개수입니다.
     */

    object CompositeNumber {
        const val MAX_COUNT = 3
    }

    val compositeNumber = listOf(1,4,8,10,14,16,20,22,25,26,28,32,34,35,38,40,44)

    fun getCompositeNumber(list: List<LottoResult>) = list.filterCompositeNumber()

    fun List<LottoResult>.filterCompositeNumber(): List<LottoResult> {
        return this.filter {
            it.numbers.count { n -> n in compositeNumber } <= CompositeNumber.MAX_COUNT
        }
    }

    /**
     * 10. 완전제곱수 0 ~ 2개 포함
     *   확률 97%
     * 완전제곱수란 자기자신를 곱한 수를 말합니다.
     * 45개 로또번호중 완전 제곱수는 1,4,9,16,25,36 이며 총 6개수입니다.
     */

    object PerfectSquare  {
        const val MAX_COUNT = 2
    }

    val perfectSquare = listOf(1,4,9,16,25,36)

    fun getPerfectSquare(list: List<LottoResult>) = list.filterPerfectSquare()

    fun List<LottoResult>.filterPerfectSquare(): List<LottoResult> {
        return this.filter {
            it.numbers.count { n -> n in perfectSquare } <= PerfectSquare.MAX_COUNT
        }
    }

    /**
     * 11. 3, 5의 배수
     *   확률 84%
     * 배수란 로또번호 45개중 3과 5의 배수를 말합니다.
     * 3의 배수는 3,6,9,12,15,18,21,24,27,30,33,36,39,42,45 총 15개수가 존재하며, 5의 배수는 5,10,15,25,30,35,40,45 총 9개가 존재합니다.
     * 로또 조합시 3의 배수는 0개 ~ 3개까지 포함, 5의 배수는 0개 ~ 2개까지만 포함하는 것을 추천합니다.
     */

    object Multiple {
        const val THREE = 3
        const val FIVE = 5

        const val THREE_MAX_COUNT = 3
        const val FIVE_MAX_COUNT = 2
    }

    fun getMultiple(list: List<LottoResult>) = list.filterMultiple()

    fun List<LottoResult>.filterMultiple(): List<LottoResult> {
        return this.filterMultipleThree().filterMultipleFive()
    }

    private fun List<LottoResult>.filterMultipleThree(): List<LottoResult> {
        return this.filter {
            it.numbers.count { n -> n % Multiple.THREE == 0 } <= Multiple.THREE_MAX_COUNT
        }
    }

    private fun List<LottoResult>.filterMultipleFive(): List<LottoResult> {
        return this.filter {
            it.numbers.count { n -> n % Multiple.FIVE == 0 } <= Multiple.FIVE_MAX_COUNT
        }
    }


    /**
     * 12. 쌍수 0 ~ 2개 포함
     *    확률 99%
     * 쌍수란 로또번호중 앞뒤가 같은 수를 말하며, 쌍수에 해당하는 수는 11,22,33,44가 있습니다.
     * 역대 로또 1등 당첨번호 통계상 쌍수가 3개 이상 나오는 경우 1% 미만이기에 로또번호 조합시 쌍수를 0개 ~ 2개까지만 포함하는것을 추천합니다.
     */

    object Dual {
        const val MAX_COUNT = 2
    }

    val dual = listOf(11,22,33,44)

    fun getDual(list: List<LottoResult>) = list.filterDual()

    fun List<LottoResult>.filterDual(): List<LottoResult> {
        return this.filter {
            it.numbers.count { n -> n in dual } <= Dual.MAX_COUNT
        }
    }

    /**
     * 13. 시작번호 14이상 끝번호 30이하 제외
     *   확률 80%
     */

    object Range {
        const val START_NUMBER = 14
        const val END_NUMBER = 30
    }

    fun test(list: List<LottoResult>) = getRange(list).size / 1189.0

    fun getRange(list: List<LottoResult>) = list.filterRange()

    fun List<LottoResult>.filterRange(): List<LottoResult> {
        return this.filter {
            it.number1 < Range.START_NUMBER
                    && it.number6 > Range.END_NUMBER
        }
    }

    /**
     * 14. 동일구간 3개이상 제외
     *   확률 94%
     * 동일구간이란 45개 숫자를 10단위로 나누어 공색기준으로 보면 1~10, 11~20, 21~30, 31~40, 41~45 나눈 5구간을 말합니다.
     * 역대 1등 로또 당첨번호중 동일구간에 4개이상 번호가 포함된 조합은 5%미만이기에 동일구간에 4개이상 번호를 포함하는것은 추천하지 않습니다.
     */

    object FiveSection {
        const val MAX_COUNT = 4
    }

    val firstSection = (1..10)
    val secondSection = (11..20)
    val thirdSection = (21..30)
    val fourthSection = (31..40)
    val fifthSection = (41..45)

    fun getFiveSection(list: List<LottoResult>) = list.filterFiveSection()

    fun List<LottoResult>.filterFiveSection(): List<LottoResult> {
        return this.filter {
            it.numbers.count { n -> n in firstSection } < FiveSection.MAX_COUNT
                    && it.numbers.count { n -> n in secondSection } < FiveSection.MAX_COUNT
                    && it.numbers.count { n -> n in thirdSection } < FiveSection.MAX_COUNT
                    && it.numbers.count { n -> n in fourthSection } < FiveSection.MAX_COUNT
                    && it.numbers.count { n -> n in fifthSection } < FiveSection.MAX_COUNT
        }
    }

    /**
     * 15. 모서리 패턴
     *
     * - 좌측상단 : 1,2,8,9
     * - 우측상단 : 6,7,13,14
     * - 좌측하단 : 29,30,36,37
     * - 우측하단 : 34,35,41,42
     *
     */
    val leftTopCorner = listOf(1, 2, 8, 9)
    val rightTopCorner = listOf(6, 7, 13, 14)
    val leftBottomCorner = listOf(29, 30, 36, 37, 43, 44)
    val rightBottomCorner = listOf(34, 35, 41, 42)

    object Corner {
        const val MIN_COUNT = 1
        const val MAX_COUNT = 3
    }

    /**
     * 해당 영역 숫자 포함 개수
     * @return Map < Round, Count >
     */
    fun getCornerPattern(): Map<Int, Int> {
        val frequency = mutableMapOf<Int, Int>()

        data.forEach {
            frequency[it.round] = it.numbers.count { it in (leftTopCorner + rightTopCorner + leftBottomCorner + rightBottomCorner) }
        }

        return frequency.toMap()
    }

    /**
     * 모서리 패턴에 부합하지 않는 데이터 필터링
     */
    fun List<LottoResult>.filterNotCornerPattern(): List<LottoResult> {
        return this.filter {
            val cnt = it.numbers.count { n -> n in (leftTopCorner + rightTopCorner + leftBottomCorner + rightBottomCorner) }
            cnt in Corner.MIN_COUNT..Corner.MAX_COUNT
        }
    }

    /**
     * 삼각 패턴
     *
     * - 좌측 상단 중심 삼각형
     * - 우측 상단 중심 상각형
     * - 좌측 하단 중심 삼각형
     * - 우측 하단 중심 삼각형
     */
    val leftTopTriangle = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 17, 18, 19, 22, 23, 24, 25, 29, 30, 31, 36, 37, 43)
    val rightTopTriangle = (1..7) + (9..14) + (17..21) + (25..28) + (33..35) + (41..42)
    val leftBottomTriangle = listOf(1, 8, 9, 15, 16, 17, 22, 23, 24, 25, 29, 30, 31, 32, 33, 36, 37, 38, 39, 40, 41, 43, 44, 45)
    val rightBottomTriangle = listOf(7) + (13..14) + (19..21) + (25..28) + (31..35) + (37..42) + (43..45)

    fun getTriangleType(list: List<LottoResult>): Map<Int, String> {
        var result = mutableMapOf<Int, String>()
        list.forEach { lotto ->
            if (lotto.numbers.all { n -> n in leftTopTriangle }) {
                result[lotto.round] = "Left Top  "
            }
            if (lotto.numbers.all { n -> n in rightTopTriangle }) {
                result[lotto.round] = result.getOrDefault(lotto.round, "") + "Right Top  "
            }
            if (lotto.numbers.all { n -> n in leftBottomTriangle }) {
                result[lotto.round] = result.getOrDefault(lotto.round, "") + "Left Bottom  "
            }
            if (lotto.numbers.all { n -> n in rightBottomTriangle }) {
                result[lotto.round] = result.getOrDefault(lotto.round, "") + "Right Bottom  "
            }
        }

        return result
    }

    fun List<LottoResult>.filterTriangle(): List<LottoResult> {
        return this.filter {
            !it.numbers.all { n -> n in leftTopTriangle }
                    && !it.numbers.all { n -> n in rightTopTriangle }
                    && !it.numbers.all { n -> n in leftBottomTriangle }
                    && !it.numbers.all { n -> n in rightBottomTriangle }
        }
    }

    /**
     * 개구리 패턴
     * - 좌측 정렬
     * - 우측 정렬
     */
    val leftAlignFrog = listOf(1, 2, 4, 5, 8, 9, 11, 12, 15, 16, 18, 19, 22, 23, 25, 26, 29, 30, 32, 33, 36, 37, 39, 40, 43, 44)
    val rightAlignFrog = listOf(3, 4, 6, 7, 10, 11, 13, 14, 17, 18, 20, 21, 24, 25, 27, 28, 31, 32, 34, 35, 38, 39, 41, 42, 45)

    fun getFrogPattern(list: List<LottoResult>): Map<Int, String> {
        var result = mutableMapOf<Int, String>()
        list.forEach { lotto ->
            if (lotto.numbers.all { n -> n in leftAlignFrog }) {
                result[lotto.round] = "Left Frog  "
            }
            if (lotto.numbers.all { n -> n in rightAlignFrog }) {
                result[lotto.round] = result.getOrDefault(lotto.round, "") + "Right Frog  "
            }
        }
        return result
    }

    fun List<LottoResult>.filterFrogPattern(): List<LottoResult> {
        return this.filter {
            !it.numbers.all { n -> n in leftAlignFrog } && !it.numbers.all { n -> n in rightAlignFrog }
        }
    }

    /**
     * 특정 회차에 일치하는 패턴 출력
     */
    fun validationPatternOfRound(lotto: LottoResult) {
        val list = listOf(lotto)
        val inconsistentPatternList = mutableListOf<String>()

        if (list.filterTotalSection().isEmpty()) inconsistentPatternList.add("총합구간")
        if (list.filterAcCalc().isEmpty()) inconsistentPatternList.add("AC")
        if (list.filterOddOrEvenBias().isEmpty()) inconsistentPatternList.add("홀수")
        if (list.filterRatioOfHighAndLow().isEmpty()) inconsistentPatternList.add("고저")
        if (list.filterFinalNumber().isEmpty()) inconsistentPatternList.add("동일 끝수")
        if (list.filterTotalFinalNumber().isEmpty()) inconsistentPatternList.add("끝수 총합")
        if (list.filterDiscontinuousOrTwo().isEmpty()) inconsistentPatternList.add("연속번호")
        if (list.filterDecimalCount().isEmpty()) inconsistentPatternList.add("소수")
        if (list.filterCompositeNumber().isEmpty()) inconsistentPatternList.add("합성수")
        if (list.filterPerfectSquare().isEmpty()) inconsistentPatternList.add("완전제곱수")
        if (list.filterMultiple().isEmpty()) inconsistentPatternList.add("3|5 배수")
        if (list.filterDual().isEmpty()) inconsistentPatternList.add("쌍수")
        if (list.filterRange().isEmpty()) inconsistentPatternList.add("시작끝번호")
        if (list.filterFiveSection().isEmpty()) inconsistentPatternList.add("동일 구간")
        if (list.filterNotCornerPattern().isEmpty()) inconsistentPatternList.add("모서리")
        if (list.filterTriangle().isEmpty()) inconsistentPatternList.add("삼각")
        if (list.filterFrogPattern().isEmpty()) inconsistentPatternList.add("개구리")

        println("${lotto.round}회 필터 비일치 개수 : ${inconsistentPatternList.size}")
        if (inconsistentPatternList.isNotEmpty())
            println(inconsistentPatternList.joinToString(", "))
    }
}