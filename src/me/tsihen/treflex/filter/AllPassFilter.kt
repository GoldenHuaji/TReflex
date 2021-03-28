package me.tsihen.treflex.filter

/**
 * 顾名思义，全部通过的过滤器
 */
class AllPassFilter : AbsFilter<Any>() {
    override fun pass(obj: Any?): Boolean = true
}