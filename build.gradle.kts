
plugins {
}

group = "net.thiki.eclab"
version = "0.0.1-SNAPSHOT"


repositories {
	mavenCentral()
}

dependencies {

}

tasks.withType<Test> {
	useJUnitPlatform()
}

