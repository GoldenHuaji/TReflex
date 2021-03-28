package me.tsihen.treflex.test

import me.tsihen.treflex.*
import me.tsihen.treflex.filter.AbsMemberFilter.Companion.DEFAULT
import me.tsihen.treflex.filter.AbsMemberFilter.Companion.MUST_NOT
import me.tsihen.treflex.filter.ConstructorFilter
import me.tsihen.treflex.filter.FieldFilter
import me.tsihen.treflex.filter.MethodFilter
import java.lang.reflect.Modifier.PRIVATE

fun main() {
    try {
        val ktClz = getClass("me.tsihen.treflex.test.KtClass")
        val jvClz = getClass("me.tsihen.treflex.test.JavaClass")
        val jvIns = jvClz.newInstance()
        val methodUtilClz = getClass("me.tsihen.treflex.MethodUtils")
//        writeField(jvClz.newInstance(),
//                FieldFilter(
//                        visibility = DEFAULT,
//                        type = Character.TYPE,
//                        name = "field0".toRegex()
//                ),
//                '2'
//        )
        /*
        // pass
        readStaticField(
            jvClz,
            filter = FieldFilter(
                visibility = DEFAULT,
                type = Character.TYPE
            )
        ).print()
         */
//        writeField(jvIns, false, FieldFilter(visibility = DEFAULT, type = Character.TYPE, beStatic = MUST_NOT), 'l')
//        readField(jvIns, false, FieldFilter(visibility = DEFAULT, type = Character.TYPE, beStatic = MUST_NOT)).print()
//        writeStaticField(
//            jvClz,
//            FieldFilter(
//                visibility = DEFAULT,
//                type = Character.TYPE
//            ),
//            '7'
//        )
//        readStaticField(
//            jvClz,
//            FieldFilter(
//                visibility = DEFAULT,
//                type = Character.TYPE
//            )
//        ).print()
//        callMethod(
//            jvIns,
//            MethodFilter(name = "method2"),
//            "something"
//        ).print()
//        callConstructor(
//            ktClz,
//            ConstructorFilter(
//                visibility = PRIVATE
//            ),
//            0
//        )
        callConstructor(
            ktClz,
            ConstructorFilter(
                notWithAnnotations = hashSetOf(Deprecated::class.java),
            ),
            "baba"
        )
//        val i = 100000
//        ktTest(i)
//        ktTest2(i)
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

fun Any?.print() = println(this)

fun ktTest(i: Any) = i.javaClass.simpleName.print() // Integer 包装类型
fun ktTest2(i: Int) = i.javaClass.simpleName.print() // int 基本类型