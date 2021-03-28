package me.tsihen.treflex.filter

abstract class AbsFilter<in T> {
    abstract fun pass(obj: T?): Boolean
    operator fun <R : T> plus(other: AbsFilter<R>): AbsFilter<R> = object : AbsFilter<R>() {
        override fun pass(obj: R?): Boolean = this@AbsFilter.pass(obj) && other.pass(obj)
        override fun toString(): String {
            return "${this@AbsFilter} && $other"
        }
    }
}