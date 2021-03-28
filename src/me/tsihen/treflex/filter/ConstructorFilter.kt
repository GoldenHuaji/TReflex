package me.tsihen.treflex.filter

import me.tsihen.treflex.getParametersString
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

/**
 * 作用于 [Constructor] 的过滤器
 * @param paramTypes 能通过过滤的 [Constructor] 的参数类型
 * @param visibility 能通过过滤的 [Constructor] 的可见度，参见 [Modifier.PUBLIC] 以及 [AbsMemberFilter.DEFAULT]，指明多个可见度需要使用 or 运算符
 * @param declaredClass 能通过过滤的 [Constructor] 被定义的类，使用 null 表示任何类
 * @param withAnnotations 能通过过滤的 [Constructor] 必须携带的注解，使用 null 或者空集合表示无
 * @param notWithAnnotations 能通过过滤的 [Constructor] 禁止携带的注解，使用 null 或者空集合表示无
 */
class ConstructorFilter @JvmOverloads constructor(
    private val paramTypes: Array<Class<*>>? = null,
    private val visibility: Int = Modifier.PUBLIC or Modifier.PROTECTED or Modifier.PRIVATE or DEFAULT,
    private val declaredClass: Class<*>? = null,
    private val withAnnotations: Set<Class<out Annotation>>? = null,
    private val notWithAnnotations: Set<Class<out Annotation>>? = null
) : AbsConstructorFilter() {
    override fun pass(obj: Constructor<*>?): Boolean {
        if (obj == null) return false

        val objModifier = obj.modifiers
        if (((objModifier and 0b111) and visibility == 0) && !(visibility and DEFAULT != 0 && isDefault(obj))) return false
        if (declaredClass != null && obj.declaringClass != declaredClass) return false
        if (paramTypes != null && !paramTypes.contentEquals(obj.parameterTypes)) return false
        withAnnotations?.forEach { if (obj.getAnnotation(it) == null) return false }
        notWithAnnotations?.forEach { if (obj.getAnnotation(it) != null) return false }

        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        val v = StringBuilder()
        if (visibility and Modifier.PUBLIC != 0) v.append("public/")
        if (visibility and Modifier.PROTECTED != 0) v.append("protected/")
        if (visibility and DEFAULT != 0) v.append("default/")
        if (visibility and Modifier.PRIVATE != 0) v.append("private/")
        v.deleteCharAt(v.length - 1)
        v.append(' ')
        sb.append(v.toString())
        if (declaredClass != null) sb.append(declaredClass.simpleName + ' ')
        sb.append(paramTypes?.let { getParametersString(*it) } ?: "(...)")
        return sb.toString()
    }
}