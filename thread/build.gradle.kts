plugins {
    id("ellemes.gradle.mod").apply(false)
    id("thread-plugin")
}

dependencies {
    modImplementation(mod.fabricApi().full())
}
