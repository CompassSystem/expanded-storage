plugins {
    id("ellemes.gradle.mod")
    id("java")
}


allprojects {
    sourceSets {
        getByName("main") {
            java.setSrcDirs(listOf("src/main/java", "src/conlib/java"))
            resources.setSrcDirs(listOf("src/main/resources", "src/conlib/resources"))
        }
    }
}
