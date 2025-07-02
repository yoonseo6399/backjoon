import java.util.Scanner
import kotlin.collections.filter

//TIP 코드를 <b>실행</b>하려면 <shortcut actionId="Run"/>을(를) 누르거나
// 에디터 여백에 있는 <icon src="AllIcons.Actions.Execute"/> 아이콘을 클릭하세요.
fun main() {
    /*
    12 7 9 47 5
    13 8 11 48 6
    21 10 26 49 7
    48 14 28 50 8
    52 20 32 51 9
    */


    val s = Scanner(System.`in`)
    val n = s.nextLine().toInt()
    val arr = mutableListOf<MutableList<Int>>()
    for (i in 1..n) {
        val s = s.nextLine().split(" ")
        arr.add(s.map { it.toInt() }.toMutableList())
    }
    println()
    arr.forEach {
        println(it)
    }
    val bindex = mutableMapOf<Int,Int>()
    var totalb = 0
    var result = 0
    for ((i,e) in arr.withIndex()) {
        val index = n - i -1
        val ele = arr[index]
        println(ele.max())
        bindex[index] = ele.indexOf(ele.max())
        if(index == n-1) continue
        println("index #${index+1} resulted $totalb")



        result = arr[index+1].lastOrNull { e ->
            if(totalb==5) true
            if(e >= bindex[index]!!) totalb++
            false
        } ?: continue
    }
    println(result)
}