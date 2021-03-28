@file:JvmName("ConstructorUtils")

package me.tsihen.treflex

import me.tsihen.treflex.filter.AbsFilter
import java.lang.reflect.Constructor
import kotlin.jvm.Throws

/**
 * 依据 [types] 来在 [clz] 中寻找 [Constructor] ，并设置为**可访问**。超类也会被考虑。
 * @param clz 在哪个 [Class] 中寻找
 * @param types 可变参数，构造函数的参数类型
 * @return 找到的构造函数
 * @throws NoSuchMethodException 找不到 [Constructor]
 * @throws SecurityException 安全违规
 */
@Throws(NoSuchMethodException::class, SecurityException::class)
fun getConstructor(
    clz: Class<*>,
    vararg types: Class<*>
): Constructor<*> {
    val r = clz.getDeclaredConstructor(*types)
    r.isAccessible = true
    return r
}

/**
 * 在 [clz] 中寻找第一个通过 [filter] 并且能使用 [args] 调用的 [Constructor]，并使用 [args] 调用。超类也会被考虑。
 * @param clz 在哪个 [Class] 中寻找
 * @param filter 过滤器，可以使用 [me.tsihen.treflex.filter.ConstructorFilter]
 * @return [clz] 的新实例
 * @throws NoSuchMethodException 找不到 [Constructor]
 * @throws SecurityException 安全违规
 */
@Throws(NoSuchMethodException::class, SecurityException::class)
fun callConstructor(
    clz: Class<*>,
    filter: AbsFilter<Constructor<*>>,
    vararg args: Any?
): Any {
    val argt = getParametersType(*args)
    clz.forEachConstructor {
        if (!filter.pass(it)) return@forEachConstructor
        if (argt.size != it.parameterTypes.size) return@forEachConstructor
        args.indices.forEach Inner@{ i ->
            val a = it.parameterTypes[i]
            val b = argt[i]
            if (b cannotCastTo a) return@forEachConstructor
        }
        return it.newInstance(*args)
    }
    throw NoSuchMethodException("找不到构造方法 $filter 在 ${clz.simpleName} 可以被 ${getParametersString(*argt)} 调用")
}