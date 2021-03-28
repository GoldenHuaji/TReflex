package me.tsihen.treflex.test

class KtClass : JavaClass {
    fun method3(a: Int, b: Long): String = ""
    private constructor(a: Int): super() {
    }

    @Deprecated("sss")
    constructor(a: CharSequence): super()
}