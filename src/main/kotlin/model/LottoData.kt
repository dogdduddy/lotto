package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class LottoData(
    @SerialName("회차") val round: Int,
    @SerialName("번호1") val number1: Int,
    @SerialName("번호2") val number2: Int,
    @SerialName("번호3") val number3: Int,
    @SerialName("번호4") val number4: Int,
    @SerialName("번호5") val number5: Int,
    @SerialName("번호6") val number6: Int,
    @SerialName("보너스") val bonus: Int
) {
    // 편의 프로퍼티: 당첨번호를 리스트로 반환
    val numbers: List<Int>
        get() = listOf(number1, number2, number3, number4, number5, number6)

    // 정렬된 당첨번호
    val sortedNumbers: List<Int>
        get() = numbers.sorted()
}

fun LottoData.toResult(baseRound: Int, baseDate: LocalDate): LottoResult {
    val diff = baseRound - round
    val date = baseDate.minusWeeks(diff.toLong())
    return LottoResult(
        date = date.toString(),
        round = round,
        number1 = number1,
        number2 = number2,
        number3 = number3,
        number4 = number4,
        number5 = number5,
        number6 = number6,
        bonus = bonus
    )
}