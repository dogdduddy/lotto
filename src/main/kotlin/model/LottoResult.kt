package model

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class LottoResult(
    val date: String,
    val round: Int,
    val number1: Int,
    val number2: Int,
    val number3: Int,
    val number4: Int,
    val number5: Int,
    val number6: Int,
    val bonus: Int
) {
    // 편의 프로퍼티: 당첨번호를 리스트로 반환
    val numbers: List<Int>
        get() = listOf(number1, number2, number3, number4, number5, number6)

    // 정렬된 당첨번호
    val sortedNumbers: List<Int>
        get() = numbers.sorted()

    // String을 LocalDate로 변환
    val localDate: LocalDate
        get() = LocalDate.parse(date)

    // 년도 추출
    val year: Int
        get() = localDate.year

    // 월 추출
    val month: Int
        get() = localDate.monthValue

    // 년월 추출 (202508 형식)
    val yearMonth: Int
        get() = year * 100 + month

    // 년월 문자열 (2025-08 형식)
    val yearMonthString: String
        get() = "${year}-${month.toString().padStart(2, '0')}"

}