package me.tsihen.treflex.filter

import me.tsihen.treflex.toInt
import java.lang.reflect.Member

abstract class AbsMemberFilter<T: Member> : AbsFilter<T>(){
    companion object {
        const val MUST = 0b0 // 0

        const val MUST_NOT = 0b1 // 1

        const val EITHER = 0b10 // 10

        /**
         * For visibilities, like[java.lang.reflect.Modifier.PUBLIC]
         */
        const val DEFAULT = 0b1000

        @JvmStatic
        protected fun getProperty(b: Boolean?) = if (b == null) EITHER else (!b).toInt()

        @JvmStatic
        protected fun isDefault(m: Member) = m.modifiers and 0b111 == 0
    }
}