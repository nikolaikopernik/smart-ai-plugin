class Test {
    private val a = "abc"

    private fun get1(i: Int): Int {
        return i + 1
    }

    public fun calculate(): String {
        var incrementValue = get1(1)
        return "Sdf" + a + incrementValue
    }
}