package backjoon.src

import java.util.*

fun main() {
    val scanner = Scanner(System.`in`)

    print("지불해야 할 총 금액을 입력하세요 (원): ")
    val amount = scanner.nextInt()

    // 화폐 단위
    val denominations = intArrayOf(50000, 10000, 5000, 1000, 500, 100, 50, 10)

    var remainingAmount = amount

    println("\n--- 거스름돈 계산 결과 ---")

    for (denom in denominations) {
        if (remainingAmount == 0) break

        val count = remainingAmount / denom
        if (count > 0) {
            println("${denom}원: ${count}개")
            remainingAmount %= denom
        }
    }

    if (remainingAmount > 0) {
        println("\n주의: ${remainingAmount}원이 부족합니다.")
    } else if (amount == 0) {
        println("지불할 금액이 없습니다.")
    }
}