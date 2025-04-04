package com.nbogdanov.smartaiplugin

import com.intellij.psi.PsiFile
import com.nbogdanov.smartaiplugin.openai.CONTEXT_WINDOW_LIMIT
import com.nbogdanov.smartaiplugin.statistics.LocalStatistics
import io.kotest.assertions.withClue
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe

/**
 * Let's generate too long content for the given file
 */
fun PsiFile.tooLongContent(): String =
    SIMPLE_CLASS + String((0..CONTEXT_WINDOW_LIMIT * 4).map { 1.toByte() }.toByteArray())

fun LocalStatistics.shouldHaveOnlyCounters(vararg counters: Pair<String, Int>) {
    this.state.dataMap shouldHaveSize counters.size
    counters.forEach { (key, value) ->
        withClue("Expected to have counter $key = $value. Actual counters: ${this.state.dataMap}") {
            this.state.dataMap[key] shouldBe value
        }
    }
}


val SIMPLE_CLASS = """
    public class Test {
    public static final java.lang.String a = "abc";

    private int get1(int i) {
        var str = "REPLACE";
        return 1;
    }

    public java.lang.String calculate() {
        var k = get1(1);
        return "Sdf" + a;
    }
}
""".trimIndent()

fun String.generateLongClass() = this.replace("REPLACE",
    (0..CONTEXT_WINDOW_LIMIT * 4).joinToString { "1" })