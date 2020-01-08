import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("org.springframework.boot") version "2.2.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.spring") version "1.3.61"
    application
}

group = "net.thiki"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

application {
    mainClassName = "net.thiki.eclab.yaml2zk.Yaml2zkBootstrap"
}

val developmentOnly: Configuration by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(developmentOnly)
    }
}

repositories {
    mavenCentral()
}

//extra["slf4j.version"] = "1.7.20"
val curatorVersion = "4.0.1"
val zookeeperVersion = "3.5.5"


dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.10.0")

    implementation("org.apache.zookeeper:zookeeper:${zookeeperVersion}") {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
    }
    implementation("org.apache.curator:curator-x-async:${curatorVersion}")
    implementation("org.apache.curator:curator-x-discovery:${curatorVersion}")

    implementation("info.picocli:picocli:4.1.4")

    val log4j2Version = "2.11.1"
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version"){
        //exclusions for avoiding conflict.
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "ch.qos.logback", module = "log4j-to-slf4j")
    }
    implementation("org.apache.logging.log4j:log4j-jul:$log4j2Version")
    implementation("org.slf4j:jul-to-slf4j:1.7.25")
}

configurations{
    implementation{
        exclude(group =  "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
