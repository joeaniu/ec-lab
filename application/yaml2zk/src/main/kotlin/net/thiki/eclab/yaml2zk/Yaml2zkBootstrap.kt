package net.thiki.eclab.yaml2zk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Yaml2zkBootstrap

fun main(args: Array<String>) {
	runApplication<Yaml2zkBootstrap>(*args)

}
