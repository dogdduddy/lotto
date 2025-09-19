package analyzer

import model.LottoData

// 로또 데이터 분석 클래스 (LottoData용)
class LottoDataAnalyzer {

    // 특정 번호가 포함된 회차 찾기
    fun findRoundsWithNumber(data: List<LottoData>, number: Int): List<LottoData> {
        return data.filter { lotto ->
            number in lotto.numbers || number == lotto.bonus
        }
    }

    // 번호별 출현 횟수 계산
    fun getNumberFrequency(data: List<LottoData>): Map<Int, Int> {
        val frequency = mutableMapOf<Int, Int>()

        data.forEach { lotto ->
            lotto.numbers.forEach { number ->
                frequency[number] = frequency.getOrDefault(number, 0) + 1
            }
        }

        return frequency.toSortedMap()
    }

    // 최근 N회차 데이터 가져오기
    fun getRecentRounds(data: List<LottoData>, count: Int): List<LottoData> {
        return data.sortedByDescending { it.round }.take(count)
    }

    // 특정 회차 범위의 데이터 가져오기
    fun getRoundRange(data: List<LottoData>, startRound: Int, endRound: Int): List<LottoData> {
        return data.filter { it.round in startRound..endRound }
            .sortedBy { it.round }
    }

    // 특정 회차 찾기
    fun findByRound(data: List<LottoData>, round: Int): LottoData? {
        return data.find { it.round == round }
    }

    // 보너스 번호 통계
    fun getBonusNumberFrequency(data: List<LottoData>): Map<Int, Int> {
        val frequency = mutableMapOf<Int, Int>()

        data.forEach { lotto ->
            frequency[lotto.bonus] = frequency.getOrDefault(lotto.bonus, 0) + 1
        }

        return frequency.toSortedMap()
    }

    // 가장 많이 나온 번호 TOP N
    fun getTopNumbers(data: List<LottoData>, topN: Int = 10): List<Pair<Int, Int>> {
        return getNumberFrequency(data).toList()
            .sortedByDescending { it.second }
            .take(topN)
    }

    // 가장 적게 나온 번호 TOP N
    fun getLeastFrequentNumbers(data: List<LottoData>, topN: Int = 10): List<Pair<Int, Int>> {
        return getNumberFrequency(data).toList()
            .sortedBy { it.second }
            .take(topN)
    }
}