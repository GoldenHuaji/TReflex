package me.tsihen.treflex

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * **THIS FILE IS NOT FOR YOU!!!!!!!!!!**
 */

private val primitiveAndWrap = hashMapOf(
    Integer.TYPE to Integer::class.java,
    java.lang.Long.TYPE to java.lang.Long::class.java,
    Character.TYPE to Character::class.java,
    java.lang.Float.TYPE to java.lang.Float::class.java,
    java.lang.Double.TYPE to java.lang.Double::class.java,
    java.lang.Boolean.TYPE to java.lang.Boolean::class.java,
    java.lang.Byte.TYPE to java.lang.Byte::class.java,
    java.lang.Short.TYPE to java.lang.Short::class.java,
//        Integer.TYPE oppoTo Integer::class.java,
//        java.lang.Long.TYPE oppoTo java.lang.Long::class.java,
//        Character.TYPE oppoTo Character::class.java,
//        java.lang.Float.TYPE oppoTo java.lang.Float::class.java,
//        java.lang.Double.TYPE oppoTo java.lang.Double::class.java,
//        java.lang.Boolean.TYPE oppoTo java.lang.Boolean::class.java,
//        java.lang.Byte.TYPE oppoTo java.lang.Byte::class.java,
//        java.lang.Short.TYPE oppoTo java.lang.Short::class.java,
)

fun isPrimitive(type: Class<*>?): Boolean {
    return primitiveAndWrap.containsKey(type)
}

fun getWrap(type: Class<*>): Class<*> {
    return if (!isPrimitive(type)) type else primitiveAndWrap[type]!!
}

fun match(declaredType: Class<*>, actualType: Class<*>): Boolean {
    return getWrap(actualType).isAssignableFrom(getWrap(declaredType))
}

fun match(c1: Array<Class<*>>, c2: Array<Class<*>>): Boolean {
    if (c1.size == c2.size) {
        for (i in c1.indices) {
            if (!match(c1[i], c2[i])) return false
        }
        return true
    }
    return false
}

infix fun <T, R> T.oppoTo(other: R): Pair<R, T> = other to this

inline fun Class<*>.forEachMethod(action: (Method) -> Unit) {
    var clz: Class<*> = this
    do {
        clz.declaredMethods.forEach {
            it.isAccessible = true
            action(it)
        }
        clz = clz.superclass
    } while (clz.superclass != null)
}

inline fun Class<*>.forEachField(action: (Field) -> Unit) {
    var clz: Class<*> = this
    do {
        clz.declaredFields.forEach {
            it.isAccessible = true
            action(it)
        }
        clz = clz.superclass
    } while (clz.superclass != null)
}

inline fun Class<*>.forEachConstructor(action: (Constructor<*>) -> Unit) {
    var clz: Class<*> = this
    do {
        clz.declaredConstructors.forEach {
            it.isAccessible = true
            action(it)
        }
        clz = clz.superclass
    } while (clz.superclass != null)
}

fun Boolean.toInt() = if (this) 1 else 0

fun getParametersString(vararg clazzes: Class<*>?): String {
    val sb = StringBuilder("(")
    var first = true
    for (clazz in clazzes) {
        if (first) first = false else sb.append(",")
        if (clazz != null) sb.append(clazz.simpleName) else sb.append("null")
    }
    sb.append(")")
    return sb.toString()
}

fun getParametersType(vararg args: Any?): Array<Class<*>?> = Array(args.size) { args[it]?.javaClass }

infix fun Class<*>?.canCastTo(other: Class<*>): Boolean {
//    return (this?.isAssignableFrom(other) ?: true) || primitiveAndWrap[this] == other
    return this == null || match(this, other)
}

infix fun Class<*>?.cannotCastTo(other: Class<*>) = !(this canCastTo other)