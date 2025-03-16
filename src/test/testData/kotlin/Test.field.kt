class Test {
    private val CONSTANT = "abc"

    private fun get1(i: Int): Int {
        return i + 1
    }

    public fun calculate(): String {
        var k = get1(1)
        return "Sdf" + CONSTANT + k
    }
}