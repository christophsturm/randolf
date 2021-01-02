import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.jfrog.bintray.gradle.BintrayExtension
import info.solidsoft.gradle.pitest.PitestPluginExtension
import org.gradle.api.internal.tasks.testing.TestDescriptorInternal
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val junit5Version = "5.7.0"
val junitPlatformVersion = "1.7.0"
val kotlinVersion = "1.4.21-2"

plugins {
    java
    kotlin("jvm") version "1.4.21-2"
    id("com.github.ben-manes.versions") version "0.36.0"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.5"
    id("info.solidsoft.pitest") version "1.5.2"
}

group = "com.christophsturm"
version = "0.2.0"



repositories {
    //    maven { setUrl("http://dl.bintray.com/kotlin/kotlin-eap") }
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))
    testImplementation("io.strikt:strikt-core:0.28.1")
    testImplementation("dev.minutest:minutest:1.11.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    withType<Test> {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
        afterTest(KotlinClosure2<TestDescriptor, TestResult, Any>({ descriptor, result ->
            val test = descriptor as TestDescriptorInternal
            val classDisplayName =
                if (test.className == test.classDisplayName) test.classDisplayName else "${test.className} [${test.classDisplayName}]"
            val testDisplayName =
                if (test.name == test.displayName) test.displayName else "${test.name} [${test.displayName}]"
            println("\n$classDisplayName > $testDisplayName: ${result.resultType}")
        }))
    }
    create<Jar>("sourceJar") {
        from(sourceSets.main.get().allSource)
        archiveClassifier.set("sources")
    }
}

artifacts {
    add("archives", tasks["jar"])
    add("archives", tasks["sourceJar"])
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourceJar"])
            groupId = project.group as String
            artifactId = "randolf"
            version = project.version as String
        }
    }
}

// BINTRAY_API_KEY= ... ./gradlew clean check publish bintrayUpload
bintray {
    user = "christophsturm"
    key = System.getenv("BINTRAY_API_KEY")
    publish = true
    setPublications("mavenJava")
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "randolf"
        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = project.version as String
        })
    })
}

plugins.withId("info.solidsoft.pitest") {
    configure<PitestPluginExtension> {
        //        verbose.set(true)
        jvmArgs.set(listOf("-Xmx512m"))
        testPlugin.set("junit5")
        avoidCallsTo.set(setOf("kotlin.jvm.internal"))
        targetClasses.set(setOf("randolf.*"))  //by default "${project.group}.*"
        targetTests.set(setOf("randolf.*"))
        threads.set(System.getenv("PITEST_THREADS")?.toInt() ?: Runtime.getRuntime().availableProcessors())
        outputFormats.set(setOf("XML", "HTML"))
        junit5PluginVersion.set("0.12")
        pitestVersion.set("1.6.2")
    }
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    resolutionStrategy {
        componentSelection {
            all {
                val rejected = listOf("alpha", "beta", "rc", "cr", "m", "preview")
                    .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
                    .any { it.matches(candidate.version) }
                if (rejected) {
                    reject("Release candidate")
                }
            }
        }
    }
    // optional parameters
    checkForGradleUpdate = true
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}


