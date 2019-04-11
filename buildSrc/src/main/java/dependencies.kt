@file:Suppress("ClassName", "unused")

object Build {
    const val applicationId = "com.ivianuu.list.sample"
    const val buildToolsVersion = "28.0.3"
    const val compileSdk = 28
    const val minSdk = 16
    const val targetSdk = 28

    const val versionCode = 1
    const val versionName = "0.0.1"
}

object Publishing {
    const val groupId = "com.ivianuu.list"
    const val vcsUrl = "https://github.com/IVIanuu/list"
    const val version = "${Build.versionName}-dev-20"
}

object Versions {
    const val androidGradlePlugin = "3.3.0"
    const val androidxAppCompat = "1.0.2"
    const val androidxRecyclerView = "1.0.0"
    const val androidxTestJunit = "1.0.0"
    const val bintray = "1.8.4"
    const val closeable = "0.0.1-dev-2"
    const val junit = "4.12"
    const val kotlin = "1.3.21"
    const val mavenGradle = "2.1"
    const val roboelectric = "4.0.2"
    const val stdlibx = "0.0.1-dev-5"
}

object Deps {
    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.androidGradlePlugin}"

    const val androidxAppCompat = "androidx.appcompat:appcompat:${Versions.androidxAppCompat}"
    const val androidxRecyclerView =
        "androidx.recyclerview:recyclerview:${Versions.androidxRecyclerView}"
    const val androidxTestJunit = "androidx.test.ext:junit:${Versions.androidxTestJunit}"

    const val bintrayGradlePlugin =
        "com.jfrog.bintray.gradle:gradle-bintray-plugin:${Versions.bintray}"

    const val closeable = "com.ivianuu.closeable:closeable:${Versions.closeable}"

    const val junit = "junit:junit:${Versions.junit}"

    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"

    const val mavenGradlePlugin =
        "com.github.dcendents:android-maven-gradle-plugin:${Versions.mavenGradle}"

    const val roboelectric = "org.robolectric:robolectric:${Versions.roboelectric}"

    const val stdlibx = "com.ivianuu.stdlibx:stdlibx:${Versions.stdlibx}"

}