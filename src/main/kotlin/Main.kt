import analyzer.LottoDataAnalyzer
import analyzer.LottoResultAnalyzer
import analyzer.LottoResultCustomAnalyzer
import manager.LottoDataManager


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

    if (lottoResults.isNotEmpty()) {
        println("=== LottoResult 사용 예시 ===")
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


    }

    if (lottoData.isNotEmpty()) {
        println("\n=== LottoData 사용 예시 ===")

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

        // 2025.09.18
//        val filteredLottoData = lottoData.take(lottoData.size)
//        val frequency = dataAnalyzer.getNumberFrequency(filteredLottoData)
//        println("${filteredLottoData.last().round}회 ~ ${filteredLottoData.first().round}회")
//        frequency.toList().sortedWith(compareBy({it.second})).reversed()
//            .forEach { (i, n) ->
//                println("${if (i > 9) i else " $i"}번 출현 횟수: ${n}회")
//            }
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

        val result = customAnalyzer.getTotalFinalNumberPattern(lottoResults)
        println(result.size)

    }
}