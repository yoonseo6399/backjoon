import java.util.*
import kotlin.math.abs

fun main() {
    print("화학 반응식을 입력하세요 (예: H2 + O2 = H2O): ")
    val input = readLine() ?: return

    val (left, right) = input.replace(" ", "").split("=")
    val leftCompounds = left.split("+")
    val rightCompounds = right.split("+")

    val allCompounds = leftCompounds + rightCompounds
    val elements = mutableSetOf<String>()
    val compoundElementCounts = allCompounds.map { parseCompound(it) }

    // 원소 리스트 작성
    for (compound in compoundElementCounts) {
        elements.addAll(compound.keys)
    }
    val elementList = elements.toList()

    // 원소별 방정식 행렬 만들기
    val rows = elementList.size
    val cols = allCompounds.size

    // 행렬: 각 원소별 각 화합물에 포함된 원자 수 (우변은 음수)
    val matrix = Array(rows) { DoubleArray(cols) }
    for (i in elementList.indices) {
        val element = elementList[i]
        for (j in compoundElementCounts.indices) {
            val count = compoundElementCounts[j].getOrDefault(element, 0)
            matrix[i][j] = if (j < leftCompounds.size) count.toDouble() else -count.toDouble()
        }
    }

    // 계수 구하기 (가우스 소거법 확장 - 동차방정식 최소 정수해)
    val coeffs = solveHomogeneous(matrix) ?: run {
        println("해를 구할 수 없습니다.")
        return
    }

    // 최소공배수로 정수 변환
    val denominators = coeffs.map { frac ->
        val d = frac.denominator
        d
    }
    val lcmDenominator = denominators.fold(1L) { acc, d -> lcm(acc, d.toLong()) }

    val finalCoeffs = coeffs.map { frac ->
        ((frac.numerator.toLong() * (lcmDenominator / frac.denominator.toLong())).toInt())
    }

    // 출력
    val leftStr = leftCompounds.indices.joinToString(" + ") {
        "${finalCoeffs[it]}${leftCompounds[it]}"
    }
    val rightStr = rightCompounds.indices.joinToString(" + ") {
        "${finalCoeffs[it + leftCompounds.size]}${rightCompounds[it]}"
    }

    println("균형 잡힌 반응식: $leftStr = $rightStr")
}

fun parseCompound(compound: String): Map<String, Int> {
    val pattern = Regex("([A-Z][a-z]?)(\\d*)")
    val matches = pattern.findAll(compound)
    val result = mutableMapOf<String, Int>()
    for (m in matches) {
        val element = m.groupValues[1]
        val count = m.groupValues[2].ifEmpty { "1" }.toInt()
        result[element] = result.getOrDefault(element, 0) + count
    }
    return result
}

data class Fraction(val numerator: Int, val denominator: Int) {
    init {
        require(denominator != 0)
    }

    fun simplify(): Fraction {
        val g = gcd(numerator, denominator)
        return Fraction(numerator / g, denominator / g)
    }

    operator fun times(other: Int): Fraction = Fraction(numerator * other, denominator)
    operator fun div(other: Int): Fraction = Fraction(numerator, denominator * other)
}

fun gcd(a: Int, b: Int): Int {
    if (b == 0) return kotlin.math.abs(a)
    return gcd(b, a % b)
}

fun gcd(a: Long, b: Long): Long {
    if (b == 0L) return kotlin.math.abs(a)
    return gcd(b, a % b)
}

fun lcm(a: Long, b: Long): Long {
    return abs(a * b) / gcd(a, b)
}

fun solveHomogeneous(matrix: Array<DoubleArray>): List<Fraction>? {
    val rows = matrix.size
    val cols = matrix[0].size

    val m = Array(rows) { DoubleArray(cols) }
    for (i in 0 until rows) {
        for (j in 0 until cols) {
            m[i][j] = matrix[i][j]
        }
    }

    // 가우스 소거법 (행렬 크기가 작으므로 단순 구현)
    var rank = 0
    val where = IntArray(cols) { -1 }

    for (col in 0 until cols) {
        var sel = -1
        for (row in rank until rows) {
            if (kotlin.math.abs(m[row][col]) > 1e-9) {
                sel = row
                break
            }
        }
        if (sel == -1) continue

        for (j in col until cols) {
            val tmp = m[rank][j]
            m[rank][j] = m[sel][j]
            m[sel][j] = tmp
        }

        where[col] = rank

        val pivot = m[rank][col]
        for (j in col until cols) {
            m[rank][j] /= pivot
        }

        for (row in 0 until rows) {
            if (row != rank && abs(m[row][col]) > 1e-9) {
                val c = m[row][col]
                for (j in col until cols) {
                    m[row][j] -= c * m[rank][j]
                }
            }
        }
        rank++
    }

    // 자유 변수로 계수 설정: 마지막 변수를 1로 놓고 뒤에서부터 계산
    val res = DoubleArray(cols)
    for (i in cols - 1 downTo 0) {
        if (where[i] == -1) {
            res[i] = 1.0
        } else {
            var valSum = 0.0
            val row = where[i]
            for (j in i + 1 until cols) {
                valSum += m[row][j] * res[j]
            }
            res[i] = -valSum
        }
    }

    // Double -> Fraction 변환 (근사)
    return res.map { doubleToFraction(it) }
}

// Double 값을 근사 분수로 변환
fun doubleToFraction(value: Double, maxDenominator: Int = 1000): Fraction {
    var x = value
    var a = kotlin.math.floor(x).toInt()
    if (abs(x - a) < 1e-9) return Fraction(a, 1)

    var numerator0 = 1
    var denominator0 = 0
    var numerator1 = a
    var denominator1 = 1

    while (denominator1 <= maxDenominator) {
        x = 1.0 / (x - a)
        a = kotlin.math.floor(x).toInt()

        val numerator2 = a * numerator1 + numerator0
        val denominator2 = a * denominator1 + denominator0

        if (denominator2 > maxDenominator) break

        numerator0 = numerator1
        denominator0 = denominator1
        numerator1 = numerator2
        denominator1 = denominator2
    }
    return Fraction(numerator1, denominator1).simplify()
}
