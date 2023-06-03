package dev.mcmeta.thread_plugin

sealed class ModPlatform(private val prettyName: String) {
    object Common : ModPlatform("common")
    object Fabric : ModPlatform("fabric")
    object Forge : ModPlatform("forge")
    object Quilt : ModPlatform("quilt")
    object Thread : ModPlatform("thread")

    override fun toString(): String {
        return prettyName
    }

    fun isThread(): Boolean {
        return this == Fabric || this == Quilt || this == Thread
    }

    val parent: ModPlatform
        get() = if (this.isThread()) Fabric else this
}
