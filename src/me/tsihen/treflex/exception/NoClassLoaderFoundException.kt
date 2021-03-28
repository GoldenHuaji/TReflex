package me.tsihen.treflex.exception

class NoClassLoaderFoundException @JvmOverloads constructor(desc: String = "") : ReflectiveOperationException(desc) {
}