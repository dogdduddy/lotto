package analyzer

import model.LottoResult
import java.time.LocalDate


// 로또 데이터 분석 클래스 (LottoResult용)
class LottoResultAnalyzer {

    // 특정 번호가 포함된 회차 찾기
    fun findRoundsWithNumber(data: List<LottoResult>, number: Int): List<LottoResult> {
        return data.filter { lotto ->
            number in lotto.numbers || number == lotto.bonus
        }
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

    // 특정 년/월별 데이터 가져오기
    fun getDataByYearWithMonth(data: List<LottoResult>, year: Int, month: Int): List<LottoResult> {
        return data.filter { it.year == year && it.month == month }
    }

    // 특정 월 모든 데이터 가져오기
    fun getDataByMonth(data: List<LottoResult>, month: Int): List<LottoResult> {
        return data.filter { it.month == month }
    }

    // 특정 월 모든 데이터 연도별로 정리해서 가져오기
    fun getDataByMonthClassifyYear(data: List<LottoResult>, month: Int): List<Pair<Int, List<LottoResult>>> {
        val dataByMonth = data.filter { it.month == month }
        val result = mutableListOf<Pair<Int, List<LottoResult>>>()

        var baseYear = 2025
        var remainingCount = 0
        val totalCount = dataByMonth.size

        while(totalCount > remainingCount) {
            val temp = dataByMonth.filter { it.year == baseYear }
            result.add(baseYear to temp)
            remainingCount += temp.size
            baseYear -= 1
        }

        return result.toList()
    }

    // 특정 년도 데이터 가져오기
    fun getDataByYear(data: List<LottoResult>, year: Int): List<LottoResult> {
        return data.filter { it.year == year }
    }

    // 년월로 데이터 가져오기 (202508 형식)
    fun getDataByYearMonth(data: List<LottoResult>, yearMonth: Int): List<LottoResult> {
        return data.filter { it.yearMonth == yearMonth }
    }

    // 최근 N회차 데이터 가져오기
    fun getRecentRounds(data: List<LottoResult>, count: Int): List<LottoResult> {
        return data.sortedByDescending { it.round }.take(count)
    }

    // 특정 회차 범위의 데이터 가져오기
    fun getRoundRange(data: List<LottoResult>, startRound: Int, endRound: Int): List<LottoResult> {
        return data.filter { it.round in startRound..endRound }
            .sortedBy { it.round }
    }

    // 월별 당첨번호 통계
    fun getMonthlyNumberFrequency(data: List<LottoResult>, year: Int, month: Int): Map<Int, Int> {
        val monthlyData = getDataByYearWithMonth(data, year, month)
        val frequency = mutableMapOf<Int, Int>()

        monthlyData.forEach { lotto ->
            lotto.numbers.forEach { number ->
                frequency[number] = frequency.getOrDefault(number, 0) + 1
            }
        }

        return frequency.toSortedMap()
    }

    // 년도별 월별 회차 수 계산
    fun getMonthlyRoundCount(data: List<LottoResult>): Map<String, Int> {
        return data.groupBy { it.yearMonthString }
            .mapValues { it.value.size }
            .toSortedMap()
    }

    // 특정 기간의 데이터 가져오기
    fun getDataBetweenDates(
        data: List<LottoResult>,
        startDate: String,
        endDate: String
    ): List<LottoResult> {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)

        return data.filter {
            val date = it.localDate
            !date.isBefore(start) && !date.isAfter(end)
        }
    }

    // 특정 회차 찾기
    fun findByRound(data: List<LottoResult>, round: Int): LottoResult? {
        return data.find { it.round == round }
    }

    // 보너스 번호 통계
    fun getBonusNumberFrequency(data: List<LottoResult>): Map<Int, Int> {
        val frequency = mutableMapOf<Int, Int>()

        data.forEach { lotto ->
            frequency[lotto.bonus] = frequency.getOrDefault(lotto.bonus, 0) + 1
        }

        return frequency.toSortedMap()
    }

    // 가장 많이 나온 번호 TOP N
    fun getTopNumbers(data: List<LottoResult>, topN: Int = 10): List<Pair<Int, Int>> {
        return getNumberFrequency(data).toList()
            .sortedByDescending { it.second }
            .take(topN)
    }

    // 특정 월에 가장 많이 나온 번호 TOP N
    fun getTopNumbersByMonth(data: List<LottoResult>, topN: Int = 10): List<Pair<Int, Int>> {
        return getNumberFrequency(data).toList()
            .sortedByDescending { it.second }
            .take(topN)
    }

    // 가장 적게 나온 번호 TOP N
    fun getLeastFrequentNumbers(data: List<LottoResult>, topN: Int = 10): List<Pair<Int, Int>> {
        return getNumberFrequency(data).toList()
            .sortedBy { it.second }
            .take(topN)
    }
}
