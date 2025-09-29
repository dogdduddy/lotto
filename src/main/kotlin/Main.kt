import analyzer.LottoDataAnalyzer
import analyzer.LottoResultAnalyzer
import analyzer.LottoResultCustomAnalyzer
import manager.LottoDataManager
import model.LottoResult


//fun createLottoFile() {
//    // 로또 날짜 변환 기준
//    val baseRound = 1187
//    val baseDate = LocalDate.of(2025, 8, 30)
//    // 파일에서 데이터 로드
//    val lottoData = dataManager.loadLottoDataFromFile("/Users/jim/IdeaProjects/lotto_/src/main/kotlin/lotto_data.json")
//    val lottoResult = lottoData.map { it.toResult(baseRound = baseRound, baseDate = baseDate) }
//    // 데이터 가공 후 저장
//    dataManager.saveLottoResultsToFile(lottoResult, "/Users/jim/IdeaProjects/lotto_/src/main/kotlin/lotto_result.json")
//}

// 사용 예시
fun main() {

    val dataManager = LottoDataManager()
    val resultAnalyzer = LottoResultAnalyzer()
    val dataAnalyzer = LottoDataAnalyzer()
//     리소스에서 데이터 로드 (src/main/resources 폴더)
     val lottoResults = dataManager.loadLottoResultsFromFile("/Users/jim/IdeaProjects/lotto_/src/main/kotlin/data/lotto_result.json")
     val lottoData = dataManager.loadLottoDataFromFile("/Users/jim/IdeaProjects/lotto_/src/main/kotlin/data/lotto_data.json")

    val customAnalyzer = LottoResultCustomAnalyzer(lottoResults)

//
//        val selectedMoth = 9
//        // 모든 회차 중 월 데이터
//        val augustClassifyYear = resultAnalyzer.getDataByMonthClassifyYear(lottoResults, selectedMoth)
//
//        val map = mutableMapOf<Int, Int>()
//
//        augustClassifyYear.forEach { (year, list) ->
//            val result = resultAnalyzer.getTopNumbers(list, 10)
//            println("[ $year.0$selectedMoth ]")
//            result.forEachIndexed { i, s ->
//                map[s.first] = map.getOrDefault(s.first, 0) + s.second
//
//                val temp = s.first.toString()
//                val number = if (temp.length == 1) " $temp" else temp
//                println("Top ${i + 1}  |  $number  | cnt ${s.second}")
//            }
//        }
//
//
//        // 줄바꿈
//        println()
//
//        // Top 5만 모아서 다시 Top 5 선정
//        println("${selectedMoth}월별 Top 5만 모아서, 다시 Top 5 선정")
//        val sortedMap = map.entries.filter { it.value > 0 }.sortedByDescending { it.value }.take(5)
//        sortedMap.forEachIndexed { i, s ->
//            val temp = s.key.toString()
//            val number = if (temp.length == 1) " $temp" else temp
//            println("Top ${i + 1}   |  $number  |  cnt ${s.value}")
//        }
//
//        // 줄바꿈
//        println()
//
//        // 전체 8월 데이터 들 중 Top 5 선정
//        val august = resultAnalyzer.getDataByMonth(lottoResults, selectedMoth)
//        val augustTop5 = resultAnalyzer.getTopNumbers(august, 5)
//        println("전체 ${selectedMoth}월 데이터 중 Top 5")
//        augustTop5.forEachIndexed { i, s ->
//            val temp = s.first.toString()
//            val number = if (temp.length == 1) " $temp" else temp
//            println("Top ${i + 1}  |  $number  | cnt ${s.second}")
//        }

//        val a = resultAnalyzer.getRecentRounds(lottoResults, 10)


        val a = customAnalyzer.getCoreNumbers(1188, 9, 3)
        /*
        println("총 개수 : ${a.size}")
        a.forEach {
            val temp = it.key.toString()
            val number = if (temp.length == 1) " $temp" else temp
            println("$number  |  cnt ${it.value}")
        }

        println("번호 리스트 : ${a.map { it.key }}")

         */

//        // 번호별 데이터 추출
//        a.entries.forEachIndexed { i, s ->
//            val temp = s.key.toString()
//            val number = if (temp.length == 1) " $temp" else temp
//            println("Top ${i + 1}   |  $number  |  cnt ${s.value}")
//        }


    if (lottoResults.isNotEmpty()) {
        println("=== LottoResult 사용 예시 ===")
        // 최근 10회차
//        val recent10 = dataAnalyzer.getRecentRounds(lottoData, 11).subList(1, 11)
//        println("최근 10회차: ${recent10.map { "${it.round}회" }}")
//        recent10.forEach {
//            var text = buildString {
//                it.numbers.forEach { number ->
//                    append("  ${if (number > 9) number else "$number "}  ")
//                }
//            }
//
//            println("${it.round}  $text")
//        }

//        // 번호 통계
//        val frequency = dataAnalyzer.getNumberFrequency(recent10)
//        frequency.forEach {
//            println("${it.key}번 출현 횟수: ${it.value}회")
//        }

        // 2025.09.18 (번호별 나온 횟수, 그래프로 표시)
//        val filteredLottoData = lottoData
//        val frequency = dataAnalyzer.getNumberFrequency(filteredLottoData)
////        println("${filteredLottoData.last().round}회 ~ ${filteredLottoData.first().round}회")
//
//        val max = frequency.values.max()
//        val unit = max / 20
//
//        val unitOfMap = mutableMapOf<Int, Int>()
//
//        frequency.forEach { (n, v) ->
//            unitOfMap[n] = v / unit
//        }
//
//        (1..45).forEach {
//            print(if (it > 9) "$it " else " $it ")
//        }
//        println()
//        for (i in 20 downTo 1) {
//            unitOfMap.forEach { (n, v) ->
//                print(if (v >= i) " * " else "   ")
//            }
//            println()
//        }

//
//        val a = customAnalyzer.getCornerPattern()
//        a.forEach {
//            println("${it.key}회  출현 횟수: ${it.value}개")
//        }
//
//        repeat(7) { cnt ->
//            val count = a.count { it.value == cnt }
//            println("${cnt}개 등장 $${count}회")
//        }

        val lottoList = customAnalyzer.getRoundRange(1180, 1189)
        lottoList.forEach { customAnalyzer.validationPatternOfRound(it) }

//        val numberList = (listOf<Int>() + (1..45)).toTypedArray()
//        val list = combination(numberList, 6).map { it.toLottoResult() }
//        println(list.size)
//        val result = customAnalyzer.filterAllPattern(list)
//        println(result.size)
    }
}

fun List<Int>.toLottoResult(): LottoResult = LottoResult(
    date = "",
    round = 0,
    number1 = this[0] ?: 0,
    number2 = this[1] ?: 0,
    number3 = this[2] ?: 0,
    number4 = this[3] ?: 0,
    number5 = this[4] ?: 0,
    number6 = this[5] ?: 0,
    bonus = 0,
)

fun <T> combination(elements: Array<T>, r: Int): List<List<T>> {
    val n = elements.size
    val results = mutableListOf<List<T>>() // 모든 경우의 수

    val result = elements.sliceArray(0 until r)

    fun recursionCombination(depth: Int = 0, index: Int = 0) {
        if (depth == r) {
            results.add(result.toList())
            return
        }

        for (i in index until n) {
            result[depth] = elements[i]
            recursionCombination(depth + 1, i + 1)
        }
    }

    recursionCombination()
    return results
}