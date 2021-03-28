@file:JvmName("FieldUtils")
package me.tsihen.treflex

import me.tsihen.treflex.filter.AbsFilter
import me.tsihen.treflex.filter.AbsMemberFilter.Companion.MUST
import me.tsihen.treflex.filter.AbsMemberFilter.Companion.MUST_NOT
import me.tsihen.treflex.filter.FieldFilter
import java.lang.reflect.Field
import java.lang.reflect.Modifier.*
import kotlin.jvm.Throws

private val staticFilter = FieldFilter(beStatic = MUST)
private val notStaticFilter = FieldFilter(beStatic = MUST_NOT)

/**
 * 寻找 [clz] 中能通过 [filter] 筛选的字段，并设置为**可访问**
 * @param clz 哪个 [Class]
 * @param findInParent 是否在超类中寻找
 * @param filter 过滤器，可以使 [me.tsihen.treflex.filter.FieldFilter]
 * @return 一个**无序的**字段列表。如果长度为0，则表示没有找到任何字段
 */
@JvmOverloads
fun getFields(
    clz: Class<*>,
    findInParent: Boolean = true,
    filter: AbsFilter<Field>
): List<Field> {
    val res = mutableSetOf<Field>()
    if (findInParent) {
        clz.forEachField {
            if (filter.pass(it)) res.add(it)
        }
    } else {
        clz.declaredFields.forEach {
            if (filter.pass(it)) {
                it.isAccessible = true
                res.add(it)
            }
        }
    }
    return res.toList()
}

/**
 * 寻找 [clz] 中第一个通过 [filter] 筛选的字段，并设置为**可访问**
 * @param clz 哪个 [Class]
 * @param findInParent 是否在超类中寻找
 * @param filter 过滤器，可以使 [me.tsihen.treflex.filter.FieldFilter]
 * @return 字段。找不到时会抛出异常
 * @throws NoSuchFieldException 找不到字段
 */
@JvmOverloads
@Throws(NoSuchFieldException::class)
fun getFirstField(
    clz: Class<*>,
    findInParent: Boolean = true,
    filter: AbsFilter<Field>
): Field {
    if (findInParent) clz.forEachField {
        if (filter.pass(it)) return it
    } else {
        clz.declaredFields.forEach {
            if (filter.pass(it)) {
                it.isAccessible = true
                return it
            }
        }
    }
    throw NoSuchFieldException("找不到字段 $filter 在 ${clz.simpleName}")
}

/**
 * 读取 [obj] 中第一个通过 [filter] 筛选的字段的值
 * @param obj 对象
 * @param findInParent 是否在超类中寻找
 * @param filter 过滤器，可以使 [me.tsihen.treflex.filter.FieldFilter]
 * @return 字段的值。找不到时会抛出异常
 * @throws NoSuchFieldException 找不到字段
 * @throws IllegalAccessException 请见 [Field.get]
 * @throws IllegalArgumentException 请见 [Field.get]
 */
@JvmOverloads
@Throws(NoSuchFieldException::class, IllegalArgumentException::class, IllegalAccessException::class)
fun readField(
    obj: Any,
    findInParent: Boolean = true,
    filter: AbsFilter<Field>
): Any? = getFirstField(obj.javaClass, findInParent, filter + notStaticFilter)[obj]

/**
 * 把 [obj] 中第一个通过 [filter] 筛选的字段的值更改为 [value]。会自动**装箱和拆箱**
 * @param obj 对象
 * @param findInParent 是否在超类中寻找
 * @param filter 过滤器，可以使 [me.tsihen.treflex.filter.FieldFilter]
 * @param value 值
 * @throws NoSuchFieldException 找不到字段
 * @throws IllegalAccessException 请见 [Field.set]
 * @throws IllegalArgumentException 请见 [Field.set]
 */
@JvmOverloads
@Throws(NoSuchFieldException::class, IllegalArgumentException::class, IllegalAccessException::class)
fun writeField(
    obj: Any,
    findInParent: Boolean = true,
    filter: AbsFilter<Field>,
    value: Any?
) {
    val f = getFirstField(obj.javaClass, findInParent, filter + notStaticFilter)
    f[obj] = value
}

/**
 * 读取 [clz] 中第一个通过 [filter] 筛选的静态字段的值
 * @param filter 过滤器，可以使 [me.tsihen.treflex.filter.FieldFilter]
 * @return 字段的值。找不到时会抛出异常
 * @throws NoSuchFieldException 找不到字段
 * @throws IllegalAccessException 请见 [Field.get]
 * @throws IllegalArgumentException 请见 [Field.get]
 * @see [readField]
 */
@Throws(NoSuchFieldException::class, IllegalArgumentException::class, IllegalAccessException::class)
fun readStaticField(
    clz: Class<*>,
    filter: AbsFilter<Field>
): Any? {
    return getFirstField(clz, true, filter + staticFilter)[null]
}

/**
 * 把 [clz] 中第一个通过 [filter] 筛选的静态字段的值更改为 [value]。会自动**装箱和拆箱**
 *
 * 如果是**静态常量**，也会强制修改
 * @param filter 过滤器，可以使 [me.tsihen.treflex.filter.FieldFilter]
 * @param value 值
 * @throws NoSuchFieldException 找不到字段
 * @throws IllegalAccessException 请见 [Field.set]
 * @throws IllegalArgumentException 请见 [Field.set]
 * @see writeField
 */
@Throws(NoSuchFieldException::class, IllegalArgumentException::class, IllegalAccessException::class)
fun writeStaticField(
    clz: Class<*>,
    filter: AbsFilter<Field>,
    value: Any?
) {
    getFields(filter = filter + staticFilter, clz = clz, findInParent = true).forEach { f ->
        val mod = f.modifiers
        if (isFinal(mod)) {
            val fMod = Field::class.java.getDeclaredField("modifiers")
            fMod.isAccessible = true
            fMod[f] = mod and FINAL.inv()
            f[null] = value
            fMod[f] = mod
        } else {
            f[null] = value
        }
        return
    }
    throw NoSuchFieldException("找不到静态字段 $filter 在 ${clz.simpleName} ")
}
