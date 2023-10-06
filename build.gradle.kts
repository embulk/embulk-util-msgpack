plugins {
    id("java")
    id("maven-publish")
    id("signing")

    // For Scala-based tests came with msgpack-core embedded
    id("scala")
    // It expects that a property "com.github.maiflai.gradle-scalatest.mode" is set in gradle.properties.
    id("com.github.maiflai.scalatest") version "0.32"
}

repositories {
    mavenCentral()
}

group = "org.embulk"
version = "0.8.24-SNAPSHOT"
description = "Utility with the MessagePack format for Embulk plugins"

sourceSets {
    test {
        withConvention(ScalaSourceSet::class) {
            scala {
                setSrcDirs(listOf("src/test/scala"))
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:unchecked")
    options.compilerArgs.add("-Xlint:deprecation")
    options.encoding = "UTF-8"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }

    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnly("org.embulk:embulk-spi:0.11")

    testImplementation("org.embulk:embulk-spi:0.11")

    testImplementation("junit:junit:4.13.2")

    // For Scala-based tests came with msgpack-core embedded
    testImplementation("com.novocode:junit-interface:0.11")
    testImplementation("org.scala-lang:scala-library:2.12.13")
    testImplementation("org.scalatest:scalatest_2.12:3.2.8")
    testImplementation("org.scalacheck:scalacheck_2.12:1.15.4")
    testImplementation("org.xerial:xerial-core_2.12:3.6.0")
    testImplementation("org.msgpack:msgpack:0.6.12")
    testImplementation("commons-codec:commons-codec:1.12")
    testImplementation("com.typesafe.akka:akka-actor_2.12:2.5.23")
    testRuntimeOnly("com.vladsch.flexmark:flexmark-all:0.35.10")
}

tasks.javadoc {
    title = "Utility with the MessagePack format for Embulk plugins"
    options {
        locale = "en_US"
        encoding = "UTF-8"
        overview = "src/main/html/overview.html"
        (this as StandardJavadocDocletOptions).apply {
            links("https://docs.oracle.com/javase/8/docs/api/")
        }
    }
}

tasks.jar {
    metaInf {
        from(rootProject.file("LICENSE"))
        from(rootProject.file("NOTICE"))
    }
}

tasks.named<Jar>("sourcesJar") {
    metaInf {
        from(rootProject.file("LICENSE"))
        from(rootProject.file("NOTICE"))
    }
}

tasks.named<Jar>("javadocJar") {
    metaInf {
        from(rootProject.file("LICENSE"))
        from(rootProject.file("NOTICE"))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name

            from(components["java"])  // Must be 'components["java"]'. The dependency modification works only for it.
            // javadocJar and sourcesJar are added by java.withJavadocJar() and java.withSourcesJar() above.
            // See: https://docs.gradle.org/current/javadoc/org/gradle/api/plugins/JavaPluginExtension.html

            pom {  // https://central.sonatype.org/pages/requirements.html
                packaging = "jar"
                name.set(project.name)
                description.set(project.description)
                url.set("https://www.embulk.org/")

                licenses {
                    license {
                        // http://central.sonatype.org/pages/requirements.html#license-information
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        name.set("Dai MIKURUBE")
                        email.set("dmikurube@treasure-data.com")
                        roles.set(setOf("fork maintainer"))
                    }
                }

                contributors {
                    contributor {
                        name.set("Sadayuki Furuhashi")
                        email.set("frsyuki@users.sourceforge.jp")
                        url.set("https://github.com/frsyuki")
                        roles.set(setOf("original developer"))
                    }
                    contributor {
                        name.set("Muga Nishizawa")
                        email.set("muga.nishizawa@gmail.com")
                        url.set("https://github.com/muga")
                        roles.set(setOf("original developer"))
                    }
                    contributor {
                        name.set("Tsuyoshi Ozawa")
                        email.set("ozawa.tsuyoshi@gmail.com")
                        url.set("https://github.com/oza")
                        roles.set(setOf("original developer"))
                    }
                    contributor {
                        name.set("Mitsunori Komatsu")
                        email.set("komamitsu@gmail.com")
                        url.set("https://github.com/komamitsu")
                        roles.set(setOf("original developer"))
                    }
                    contributor {
                        name.set("Taro L. Saito")
                        email.set("leo@xerial.org")
                        url.set("https://github.com/xerial")
                        roles.set(setOf("original developer"))
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/embulk/embulk-util-msgpack.git")
                    developerConnection.set("scm:git:git@github.com:embulk/embulk-util-msgpack.git")
                    url.set("https://github.com/embulk/embulk-util-msgpack")
                }
            }
        }
    }

    repositories {
        maven {  // publishMavenPublicationToMavenCentralRepository
            name = "mavenCentral"
            if (project.version.toString().endsWith("-SNAPSHOT")) {
                url = uri("https://oss.sonatype.org/content/repositories/snapshots")
            } else {
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            }

            credentials {
                username = project.findProperty("ossrhUsername")?.toString() ?: ""
                password = project.findProperty("ossrhPassword")?.toString() ?: ""
            }
        }
    }
}

signing {
    if (project.hasProperty("signingKey") && project.hasProperty("signingPassword")) {
        logger.lifecycle("Signing with an in-memory key.")
        useInMemoryPgpKeys(project.property("signingKey") as String, project.property("signingPassword") as String)
    }
    sign(publishing.publications["maven"])
}

tasks.scalatest {
    var scalatestWorkingDir = file("${buildDir}/scalatest")
    var scalatestTargetDir = file("${buildDir}/scalatest/target")
    doFirst {
        scalatestWorkingDir.mkdirs()
        scalatestTargetDir.mkdirs()
    }
    workingDir(scalatestWorkingDir)
}

tasks.check {
    dependsOn(":scalatest")
}
