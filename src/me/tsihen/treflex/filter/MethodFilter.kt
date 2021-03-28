package me.tsihen.treflex.filter

import me.tsihen.treflex.getParametersString
import me.tsihen.treflex.toInt
import java.lang.reflect.Method
import java.lang.reflect.Modifier.*
/**
 * 作用于 [Method] 的过滤器
 * @param name 能够通过过滤的 [Method] 的名称，使用正则表达式
 * @param returnType 能够通过过滤的 [Method] 的返回值类型，使用 null 表示都可以
 * @param paramTypes 能够通过过滤的 [Method] 的参数类型，使用 null 表示都可以（数组中不能有 null）
 * @param visibility 能够通过过滤的 [Method] 的可见度，可以是 [PUBLIC] [PROTECTED] [AbsMemberFilter.DEFAULT] [PRIVATE]，指定多个需要使用 or 运算符
 * @param beStatic 能够通过过滤的 [Method] 是否为静态的，[AbsMemberFilter.MUST] [AbsMemberFilter.MUST_NOT] [AbsMemberFilter.EITHER]
 * @param beFinal 能够通过过滤的 [Method] 是否为 final 的，[AbsMemberFilter.MUST] [AbsMemberFilter.MUST_NOT] [AbsMemberFilter.EITHER]
 * @param beAbs 能够通过过滤的 [Method] 是否为抽象的，[AbsMemberFilter.MUST] [AbsMemberFilter.MUST_NOT] [AbsMemberFilter.EITHER]
 * @param beNative 能够通过过滤的 [Method] 是否为本地化的，[AbsMemberFilter.MUST] [AbsMemberFilter.MUST_NOT] [AbsMemberFilter.EITHER]
 * @param beSync 能够通过过滤的 [Method] 是否为缝合的，[AbsMemberFilter.MUST] [AbsMemberFilter.MUST_NOT] [AbsMemberFilter.EITHER]
 * @param beStrict 能够通过过滤的 [Method] 是否为严格的，[AbsMemberFilter.MUST] [AbsMemberFilter.MUST_NOT] [AbsMemberFilter.EITHER]
 * @param withAnnotations 能够通过过滤的 [Method] 必须携带的注解，使用 null 或者空集合表示无
 * @param notWithAnnotations 能够通过过滤的 [Method] 禁止携带的注解，使用 null 或者空集合表示无
 */
class MethodFilter @JvmOverloads constructor(
    private val name: Regex = ".*".toRegex(),
    private val returnType: Class<*>? = null,
    private val paramTypes: Array<Class<*>>? = null,
    private val visibility: Int = PUBLIC or PROTECTED or PRIVATE or DEFAULT,
    private val beStatic: Int = EITHER,
    private val beFinal: Int = EITHER,
    private val beAbs: Int = EITHER,
    private val beNative: Int = EITHER,
    private val beSync: Int = EITHER,
    private val beStrict: Int = EITHER,
    private val withAnnotations: Set<Class<out Annotation>>? = null,
    private val notWithAnnotations: Set<Class<out Annotation>>? = null
) : AbsMethodFilter() {
    override fun pass(obj: Method?): Boolean {
        if (obj == null) return false
        if (!name.matches(obj.name)) return false
        if (returnType != null && obj.returnType != returnType) return false

        val objModifier = obj.modifiers
        if (((objModifier and 0b111) and visibility == 0) && !(visibility and DEFAULT != 0 && isDefault(obj))) return false
        if (beStatic != EITHER && isStatic(objModifier).toInt() xor beStatic == 0) return false
        if (beFinal != EITHER && isFinal(objModifier).toInt() xor beFinal == 0) return false
        if (beAbs != EITHER && isAbstract(objModifier).toInt() xor beAbs == 0) return false
        if (beNative != EITHER && isNative(objModifier).toInt() xor beNative == 0) return false
        if (beSync != EITHER && isSynchronized(objModifier).toInt() xor beSync == 0) return false
        if (beStrict != EITHER && isStrict(objModifier).toInt() xor beStrict == 0) return false
        withAnnotations?.forEach { if (obj.getAnnotation(it) == null) return false }
        notWithAnnotations?.forEach { if (obj.getAnnotation(it) != null) return false }

        if (paramTypes != null && !obj.parameterTypes.contentEquals(paramTypes)) return false
        return true
    }

    /**
     * 作用于 [Method] 的过滤器
     * @param name 能够通过过滤的 [Method] 的名称，使用正则表达式
     * @param returnType 能够通过过滤的 [Method] 的返回值类型，使用 null 表示都可以
     * @param paramTypes 能够通过过滤的 [Method] 的参数类型，使用 null 表示都可以（数组中不能有 null）
     * @param visibility 能够通过过滤的 [Method] 的可见度，可以是 [PUBLIC] [PROTECTED] [AbsMemberFilter.DEFAULT] [PRIVATE]，指定多个需要使用 or 运算符
     * @param beStatic 能够通过过滤的 [Method] 是否为静态的，`true` `false` 或者使用 `null` 表示都可以
     * @param beFinal 能够通过过滤的 [Method] 是否为 final 的，`true` `false` 或者使用 `null` 表示都可以
     * @param beAbs 能够通过过滤的 [Method] 是否为抽象的，`true` `false` 或者使用 `null` 表示都可以
     * @param beNative 能够通过过滤的 [Method] 是否为本地化的，`true` `false` 或者使用 `null` 表示都可以
     * @param beSync 能够通过过滤的 [Method] 是否为缝合的，`true` `false` 或者使用 `null` 表示都可以
     * @param beStrict 能够通过过滤的 [Method] 是否为严格的，`true` `false` 或者使用 `null` 表示都可以
     * @param withAnnotations 能够通过过滤的 [Method] 必须携带的注解，使用 null 或者空集合表示无
     * @param notWithAnnotations 能够通过过滤的 [Method] 禁止携带的注解，使用 null 或者空集合表示无
     */
    @JvmOverloads
    constructor(
        name: String,
        returnType: Class<*>? = null,
        paramTypes: Array<Class<*>>? = null,
        visibility: Int = PUBLIC or PROTECTED or PRIVATE or DEFAULT,
        beStatic: Boolean? = null,
        beFinal: Boolean? = null,
        beAbs: Boolean? = null,
        beNative: Boolean? = null,
        beSync: Boolean? = null,
        beStrict: Boolean? = null,
        withAnnotations: Set<Class<out Annotation>>? = null,
        notWithAnnotations: Set<Class<out Annotation>>? = null
    ) : this(
        name.toRegex(),
        returnType, paramTypes, visibility,
        getProperty(beStatic),
        getProperty(beFinal),
        getProperty(beAbs),
        getProperty(beNative),
        getProperty(beSync),
        getProperty(beStrict),
        withAnnotations,
        notWithAnnotations
    )

    override fun toString(): String {
        val sb = StringBuilder()
        val v = StringBuilder()
        if (visibility and PUBLIC != 0) v.append("public/")
        if (visibility and PROTECTED != 0) v.append("protected/")
        if (visibility and DEFAULT != 0) v.append("default/")
        if (visibility and PRIVATE != 0) v.append("private/")
        v.deleteCharAt(v.length - 1)
        v.append(' ')
        sb.append(v.toString())
        if (beStatic != EITHER) sb.append(if (beStatic == MUST) "static " else "not-static ")
        if (beFinal != EITHER) sb.append(if (beFinal == MUST) "final " else "not-final ")
        if (beAbs != EITHER) sb.append(if (beAbs == MUST) "abstract " else "not-abstract ")
        if (beNative != EITHER) sb.append(if (beNative == MUST) "native " else "not-native ")
        if (beSync != EITHER) sb.append(if (beSync == MUST) "synchronized " else "not-synchronized ")
        if (beStrict != EITHER) sb.append(if (beStrict == MUST) "strictfp " else "not-strictfp ")
        if (returnType != null) sb.append(returnType.simpleName + ' ')
        sb.append(name)
        sb.append(paramTypes?.let { getParametersString(*it) } ?: "(...)")
        return sb.toString()
    }
}
