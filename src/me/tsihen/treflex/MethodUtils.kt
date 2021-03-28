@file:JvmName("MethodUtils")

package me.tsihen.treflex

import me.tsihen.treflex.filter.AbsFilter
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.jvm.Throws

/**
 * 请使用更强大的 [getMethods] 或者 [getFirstMethod]
 */
@Deprecated("使用 Filter")
@JvmOverloads
@Throws(NoSuchMethodException::class)
fun findMethod(
    clz: Class<*>,
    name: String? = null,
    findInParent: Boolean = true,
    returnType: Class<*>? = null,
    vararg paramTypes: Class<*>
): Method {
    if (!findInParent && name != null) {
        return clz.getDeclaredMethod(name, *paramTypes)
    } else {
        clz.forEachMethod {
            if (it.name != name && name != null) return@forEachMethod
            if (it.returnType != returnType && returnType != null) return@forEachMethod
            if (!it.parameterTypes.contentEquals(paramTypes)) return@forEachMethod
            return it
        }
    }
    throw NoSuchMethodException("找不到方法${returnType ?: ""} ${name ?: ""}${getParametersString(*paramTypes)} 在 ${clz.simpleName}")
}

/**
 * 在 [clz] 中寻找通过 [filter] 的方法并设为**可访问**
 * @param clz 类
 * @param findInParent 是否在超类中寻找
 * @param filter 过滤器，可以是 [me.tsihen.treflex.filter.MethodFilter]
 * @return 无序的 [List]，长度可以为0
 */
fun getMethods(
    clz: Class<*>,
    findInParent: Boolean = true,
    filter: AbsFilter<Method>
): List<Method> {
    val res = hashSetOf<Method>()
    if (findInParent) clz.forEachMethod {
        if (filter.pass(it)) res.add(it)
    } else {
        clz.declaredMethods.forEach {
            if (filter.pass(it)) {
                it.isAccessible = true
                res.add(it)
            }
        }
    }
    return res.toList()
}

/**
 * 在 [clz] 中寻找第一个通过 [filter] 的方法并设为**可访问**
 * @param clz 类
 * @param findInParent 是否在超类中寻找
 * @param filter 过滤器，可以是 [me.tsihen.treflex.filter.MethodFilter]
 * @return 找到的方法
 * @throws NoSuchMethodException 没有找到
 */
@Throws(NoSuchMethodException::class)
fun getFirstMethod(
    clz: Class<*>,
    findInParent: Boolean = true,
    filter: AbsFilter<Method>
): Method {
    if (findInParent) clz.forEachMethod {
        if (filter.pass(it)) return it
    } else {
        clz.declaredMethods.forEach {
            if (filter.pass(it)) {
                it.isAccessible = true
                return it
            }
        }
    }
    throw NoSuchMethodException("找不到方法 $filter")
}

/**
 * 使用更强大的 [callMethod]
 */
@JvmOverloads
@Throws(NoSuchMethodException::class)
fun callMethod(
    obj: Any,
    name: String? = null,
    args: Array<Any?>,
    types: Array<Class<*>>,
    returnType: Class<*>? = null
): Any? =
    findMethod(obj.javaClass, name = name, returnType = returnType, findInParent = true, paramTypes = *types).invoke(
        obj,
        *args
    )

/**
 * 在 [obj] 中寻找第一个通过 [filter] 、能用 [args] 调用的方法并使用参数 [args] 调用
 * @param obj 对象
 * @param filter 过滤器，可以是 [me.tsihen.treflex.filter.MethodFilter]
 * @return 找到的方法的返回值
 * @throws NoSuchMethodException 没有找到方法
 * @throws InvocationTargetException 该方法抛出异常
 * @throws IllegalAccessException 请见 [Method.invoke]
 * @throws IllegalArgumentException 请见 [Method.invoke]
 * @see [getMethods]，[getFirstField]
 */
@Throws(
    NoSuchMethodException::class,
    IllegalAccessException::class,
    IllegalArgumentException::class,
    InvocationTargetException::class
)
fun callMethod(
    obj: Any,
    filter: AbsFilter<Method>,
    vararg args: Any?
): Any? {
    val argt = getParametersType(*args)
    getMethods(filter = filter, clz = obj.javaClass, findInParent = true).forEach {
        if (it.parameterTypes.size != args.size) return@forEach
        args.indices.forEach Inner@{ i ->
            val a = it.parameterTypes[i]
            val b = argt[i]
            if (b cannotCastTo a) return@forEach
        }
        return it(obj, *args)
    }
    throw NoSuchMethodException("找不到方法 $filter 在 ${obj.javaClass.simpleName} 可以用 ${getParametersString(*argt)} 调用")
}

/**
 * 在 [clz] 中寻找第一个通过 [filter] 、能用 [args] 调用的静态方法并使用参数 [args] 调用
 * @param clz 类
 * @param filter 过滤器，可以是 [me.tsihen.treflex.filter.MethodFilter]
 * @return 找到的方法的返回值
 * @throws NoSuchMethodException 没有找到方法
 * @throws InvocationTargetException 该方法抛出异常
 * @throws IllegalAccessException 请见 [Method.invoke]
 * @throws IllegalArgumentException 请见 [Method.invoke]
 * @see [callMethod]
 */
@Throws(
    NoSuchMethodException::class,
    IllegalAccessException::class,
    IllegalArgumentException::class,
    InvocationTargetException::class
)
fun callStaticMethod(
    clz: Class<*>,
    filter: AbsFilter<Method>,
    vararg args: Any?
): Any {
    getMethods(filter = filter, clz = clz, findInParent = true).forEach {
        if (it.parameterTypes.size != args.size) return@forEach
        args.indices.forEach Inner@{ i ->
            val a = it.parameterTypes[i]
            val b = args[i]?.javaClass
            if (b cannotCastTo a) return@forEach
        }
        return it(null, *args)
    }
    throw NoSuchMethodException("找不到方法 $filter 在 ${clz.simpleName} 可以用 ${getParametersString(*getParametersType(*args))} 调用")
}