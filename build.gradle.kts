import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.dokka") version "1.8.20"
    id("maven-publish")
    signing
}

apply(plugin = "signing")

group = "com.orbitasolutions"
version = "0.2.8"

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.macos_arm64)
                implementation(compose.desktop.macos_x64)
                implementation(compose.desktop.windows_x64)
                implementation("org.apache.commons:commons-text:1.10.0")
                implementation("com.google.code.gson:gson:2.10.1")
                implementation("net.minidev:json-smart:2.5.0")
                implementation("com.jayway.jsonpath:json-path:2.8.0")
                implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.2.2")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "com.orbitasolutions.geleia.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "geleia"
            packageVersion = "1.0.0"
            macOS {
                dmgPackageVersion = "1.0.0"
                pkgPackageVersion = "1.0.0"
                dockName = "NCMusicDesktop"
                packageBuildVersion = "1.0.0"
                dmgPackageBuildVersion = "1.0.0"
                pkgPackageBuildVersion = "1.0.0"
                iconFile.set(project.file("icons/icon.png"))
            }

            windows {
                packageVersion = "1.0.0"
                msiPackageVersion = "1.0.0"
                exePackageVersion = "1.0.0"
                iconFile.set(project.file("icons/icon.ico"))
            }
        }
    }
}

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    val dokkaHtml by tasks.getting(DokkaTask::class)

    dependsOn(tasks.named("jvmJar"))
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

tasks {
    withType<GenerateMavenPom> {
        updatePom(pom)
    }
    task("publishGprPublicationToGeleiaRepository").outputs.cacheIf { true }
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        withType<MavenPublication> {
            updatePom(pom)
            the<SigningExtension>().sign(this)
            artifact(javadocJar)
        }
    }

    repositories {
        maven {
            name = "sonatypeStaging"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials(PasswordCredentials::class)
        }
    }
}

fun updatePom(pom: MavenPom?) {
    if (pom != null) {
        with(pom) {
            val projectGitUrl = "https://github.com/leodouglas/geleia"
            name.set(rootProject.name)
            packaging = "jar"
            description.set(
                "A general-purpose multiplatform client.,"
            )
            url.set(projectGitUrl)
            inceptionYear.set("2023")
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("orbitasolutions.com")
                    name.set("Leo Douglas Padilha")
                    email.set("leodouglas@gmail.com")
                    url.set("https://www.orbitasolutions.com")
                }
            }
            issueManagement {
                system.set("GitHub")
                url.set("$projectGitUrl/issues")
            }
            scm {
                connection.set("scm:git:$projectGitUrl")
                developerConnection.set("scm:git:$projectGitUrl")
                url.set(projectGitUrl)
            }

        }
    }

}

signing {
    sign(configurations.archives.get())
}