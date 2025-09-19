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
     * 기본 패턴 검사
     * 1. 모서리 패턴 포함 필터
     * 2. 삼각 패턴 포함 제거 필터
     * 3. 개구리 패턴 포함 제거 필터
     */

    fun filterBasicPattern(list: List<LottoResult>): List<LottoResult> {
        return list.filterNotCornerPattern()
            .filterTriangle()
            .filterFrogPattern()
    }

    /**
     * 총합구간 설정
     *
     * - 100 ~ 175
     * 확률 77%
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
     * AC Calc Pattern
     * - 7이상인 경우 사용
     * 확률 84%
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
     * 홀수 또는 짝수 편향
     *
     * - 홀수
     * - 짝수
     *  확률 97%
     */

    fun getOddOrEvenBias(list: List<LottoResult>) = list.filterOddOrEvenBias()

    fun List<LottoResult>.filterOddOrEvenBias(): List<LottoResult> {
        return this.filter {
            !it.numbers.all { it % 2 == 0 }
                    && !it.numbers.all { it % 2 != 0 }
        }
    }

    /**
     * 고저 비율
     *
     * - 고저비율 6:0, 0:6 제외
     * - 조합번호 6개중 23을 기준으로 23미만 (1~22) 저비율, 23이상(23~45) 고비율이라고 합니다.
     * - 조합번호를 모두 '고' 혹은 '저' 비율로 조합하는 경우, 역대 1등 당첨번호 통계상 나올 확률이 3% 미만이기에 조합을 모두 '고' 혹은 '저' 비율로 선택하는 것은 추천하지 않습니다.
     *  확률 97%
     */

    object HighAndLow {
        const val BOUNDARY = 23
    }

    fun getRatioOfHighAndLow(list: List<LottoResult>) = list.filterRatioOfHighAndLow()

    private fun List<LottoResult>.filterRatioOfHighAndLow(): List<LottoResult> {
        return this.filter {
            !it.numbers.all { n -> n < HighAndLow.BOUNDARY }
                    && !it.numbers.all { n -> n >= HighAndLow.BOUNDARY }
        }
    }

    /**
     * 동일 끝수 포함
     *
     * - 동일 끝수 0 ~ 3개 포함
     * - 끝수란 끝자리수를 말하며 당첨번호를 십자리와 단자리로 나눈후 단자리에 해당하는 숫자를 끝수라고 부릅니다. (예:42인경우 끝자리수는 2)
     * - 끝수가 같은 번호들은 동일 끝수라고 표현합니다.
     * - 역대 1등 당첨번호 통계상 같은 끝수가 4개이상 나올 확률은 1% 미만이기에 같은 끝수를 4개이상 조합번호를 만든 것은 추천하지 않습니다.
     * 확률 99.5%
     */

    object FinalNumber {
        const val MIN_COUNT = 0
        const val MAX_COUNT = 3

        const val TOTAL_MIN_COUNT = 15
        const val TOTAL_MAX_COUNT = 38
    }

    fun getFinalNumberPattern(list: List<LottoResult>) = list.filterFinalNumber()

    private fun List<LottoResult>.filterFinalNumber(): List<LottoResult> {
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
     * 끝수 총합
     * - 끝수 총합 범위 :  15 ~ 38 구간
     * - 끝수란 조합번호의 끝자리수를 말합니다. 예를 들어 로또번호 42인경우 2가 끝수에 해당하며, 단 자리수(1~9)인 경우는 자신수가 끝수에 해당합니다.
     * - 끝수 총합구간은 로또분석가들이 생각하는 범위는 14~38이며, 역대 1등 당첨번호에 끝수 합을 보았을때 15~35구간이 나오는 확률이 90%이기에 로또타파에서는 15~38구간을 추천합니다.
     */

    fun getTotalFinalNumberPattern(list: List<LottoResult>) = list.filterTotalFinalNumber()

    private fun List<LottoResult>.filterTotalFinalNumber(): List<LottoResult> {
        return this.filter {
            val sum = it.numbers.sumOf { n -> n % 10  }
            sum >= FinalNumber.TOTAL_MIN_COUNT && sum <= FinalNumber.TOTAL_MAX_COUNT
        }
    }

    /**
     * 모서리 패턴
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
    private fun List<LottoResult>.filterNotCornerPattern(): List<LottoResult> {
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

    private fun List<LottoResult>.filterTriangle(): List<LottoResult> {
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

    private fun List<LottoResult>.filterFrogPattern(): List<LottoResult> {
        return this.filter {
            !it.numbers.all { n -> n in leftAlignFrog } && !it.numbers.all { n -> n in rightAlignFrog }
        }
    }
}