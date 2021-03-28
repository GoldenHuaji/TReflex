@file:JvmName("ClassUtils")
package me.tsihen.treflex

import me.tsihen.treflex.exception.NoClassLoaderFoundException
import kotlin.jvm.Throws

/**
 * 加载 [Class]
 * @param loader 用于加载 [Class] 的 [ClassLoader]
 * @param name 所需类的全名
 * @param initialize 如果为 `true`，这个类将会被加载
 * @return 被加载的类
 * @throws ClassNotFoundException 找不到类
 */
@JvmOverloads
@Throws(ClassNotFoundException::class)
fun getClass(loader: ClassLoader, name: String, initialize: Boolean = true): Class<*> {
    return try {
        Class.forName(name, initialize, loader)
    } catch (e: ClassNotFoundException) {
        loader.loadClass(name)
    }
}

/**
 * 使用默认的 [ClassLoader] 加载 [Class]
 * @param name 所需类的全名
 * @param initialize 如果为 `true`，这个类将会被加载
 * @return 被加载的类
 * @throws ClassNotFoundException 找不到类
 * @throws NoClassLoaderFoundException 无法获取 [ClassLoader]
 */
@JvmOverloads
@Throws(ClassNotFoundException::class, NoClassLoaderFoundException::class)
fun getClass(name: String, initialize: Boolean = true): Class<*> {
    val ctxLoader = Thread.currentThread().contextClassLoader ?: System::class.java.classLoader ?: throw NoClassLoaderFoundException("无法获取 ClassLoader")
    return getClass(ctxLoader, name, initialize)
}