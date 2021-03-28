package me.tsihen.treflex.filter

import me.tsihen.treflex.toInt
import java.lang.reflect.Field
import java.lang.reflect.Modifier.*

/**
 * 作用于 [Field] 的过滤器
 * @param name 能够通过过滤的 [Field] 的名称，使用正则表达式
 * @param type 能够通过过滤的 [Field] 的类型，null 表示任何类型
 * @param visibility 能够通过过滤的 [Field] 的可见度，可以是 [PUBLIC] [PROTECTED] [AbsMemberFilter.DEFAULT] [PRIVATE]，指定多个需要使用 or 运算符
 * @param beStatic 能够通过过滤的 [Field] 是否为静态的，[AbsMemberFilter.MUST] [AbsMemberFilter.MUST_NOT] [AbsMemberFilter.EITHER]
 * @param beFinal 能够通过过滤的 [Field] 是否为 final 的，[AbsMemberFilter.MUST] [AbsMemberFilter.MUST_NOT] [AbsMemberFilter.EITHER]
 * @param beTransient 能够通过过滤的 [Field] 是否为 transient 的，[AbsMemberFilter.MUST] [AbsMemberFilter.MUST_NOT] [AbsMemberFilter.EITHER]
 * @param beVolatile 能够通过过滤的 [Field] 是否为 volatile 的，[AbsMemberFilter.MUST] [AbsMemberFilter.MUST_NOT] [AbsMemberFilter.EITHER]
 * @param withAnnotations 能够通过过滤的 [Field] 必须携带的注解，使用 null 或者空集合表示无
 * @param notWithAnnotations 能够通过过滤的 [Field] 禁止携带的注解，使用 null 或者空集合表示无
 */
class FieldFilter @JvmOverloads constructor(
    private val name: Regex = ".*".toRegex(),
    private val type: Class<*>? = null,
    private val visibility: Int = PUBLIC or PROTECTED or PRIVATE or DEFAULT,
    private val beStatic: Int = EITHER,
    private val beFinal: Int = EITHER,
    private val beTransient: Int = EITHER,
    private val beVolatile: Int = EITHER,
    private val withAnnotations: Set<Class<out Annotation>>? = null,
    private val notWithAnnotations: Set<Class<out Annotation>>? = null
) : AbsFieldFilter() {
    override fun pass(obj: Field?): Boolean {
        if (obj == null) return false
        if (!name.matches(obj.name)) return false
        if (type != null && obj.type != type) return false

        val objModifier = obj.modifiers
        if (((objModifier and 0b111) and visibility == 0) && !( visibility and DEFAULT != 0 && isDefault(obj))) return false
        if (beStatic != EITHER && isStatic(objModifier).toInt() xor beStatic == 0) return false
        if (beFinal != EITHER && isFinal(objModifier).toInt() xor beFinal == 0) return false
        if (beTransient != EITHER && isTransient(objModifier).toInt() xor beTransient == 0) return false
        if (beVolatile != EITHER && isVolatile(objModifier).toInt() xor beVolatile == 0) return false
        withAnnotations?.forEach { if (obj.getAnnotation(it) == null) return false }
        notWithAnnotations?.forEach { if (obj.getAnnotation(it) != null) return false }

        return true
    }

    /**
     * 作用于 [Field] 的过滤器
     * @param name 能够通过过滤的 [Field] 的名称，使用正则表达式
     * @param type 能够通过过滤的 [Field] 的类型，null 表示任何类型
     * @param visibility 能够通过过滤的 [Field] 的可见度，可以是 [PUBLIC] [PROTECTED] [AbsMemberFilter.DEFAULT] [PRIVATE]，指定多个需要使用 or 运算符
     * @param beStatic 能够通过过滤的 [Field] 是否为静态的，`true` `false` 或者使用 `null` 表示都可以
     * @param beFinal 能够通过过滤的 [Field] 是否为 final 的，`true` `false` 或者使用 `null` 表示都可以
     * @param beTransient 能够通过过滤的 [Field] 是否为 transient 的，`true` `false` 或者使用 `null` 表示都可以
     * @param beVolatile 能够通过过滤的 [Field] 是否为 volatile 的，`true` `false` 或者使用 `null` 表示都可以
     * @param withAnnotations 能够通过过滤的 [Field] 必须携带的注解，使用 null 或者空集合表示无
     * @param notWithAnnotations 能够通过过滤的 [Field] 禁止携带的注解，使用 null 或者空集合表示无
     */
    @JvmOverloads
    constructor(
        name: String,
        type: Class<*>? = null,
        visibility: Int = PUBLIC or PROTECTED or PRIVATE or DEFAULT,
        beStatic: Boolean? = null,
        beFinal: Boolean? = null,
        beTransient: Boolean? = null,
        beVolatile: Boolean? = null,
        withAnnotations: Set<Class<out Annotation>>? = null,
        notWithAnnotations: Set<Class<out Annotation>>? = null
    ) : this(
        name.toRegex(),
        type, visibility,
        getProperty(beStatic),
        getProperty(beFinal),
        getProperty(beTransient),
        getProperty(beVolatile),
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
        if (beTransient != EITHER) sb.append(if (beTransient == MUST) "transient " else "not-transient ")
        if (beVolatile != EITHER) sb.append(if (beVolatile == MUST) "volatile " else "not-volatile ")
        if (type != null) sb.append(type.simpleName + ' ')
        sb.append(name)
        return sb.toString()
    }
}