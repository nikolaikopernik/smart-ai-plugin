class Test {
    private val a = "abc"

    private fun getCounter(i: Int): Int {
        return i + 1
    }

    public fun calculate(): String {
        var k = getCounter(1)
        return "Sdf" + a + k
    }
}