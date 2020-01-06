package net.thiki.eclab.yaml2zk

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryNTimes
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@SpringBootApplication
class Yaml2zkBootstrap

fun main(args: Array<String>) {
    runApplication<Yaml2zkBootstrap>(*args)

    //https://www.baeldung.com/jackson

    assert(args.size == 1) {
        "usage: java -jar yaml2zk-xxx.jar file"
    }
    val file = args[0]
    Yaml2Zk().run(file)

}

class Yaml2Zk {
    fun run(file: String) {
        val curator = ZkClientFactory("localhost:2181").build()
        val jacksonObjectMapper = jacksonObjectMapper()

        val yaml = Yaml()
        val prefix = "classpath:"
        val inputStream = if (file.startsWith(prefix)) {
            val realFile = file.substring(prefix.length)
            println("realFile=$realFile.........")
            Yaml2Zk::class.java.getResourceAsStream(realFile)
        } else {
            Files.newInputStream(Paths.get(file))
        }

        val map =
                inputStream.use { ins ->
                    val config = yaml.loadAs(ins, Properties::class.java)
                    PairParser(config).parse()
                }
        val root = "/test"

        var existed = false
        map.forEach { (path, value) ->
            println("$path->$value")
            val stat = curator.checkExists()
                    .forPath("$root/$path")
            if (stat != null){
                println("warning: $path exists already, will be overrided.")
                existed = true
            }
        }

        map.forEach { (path, value) ->
            val stat = curator.checkExists()
                    .forPath("$root/$path")
            if (stat != null){
                curator.setData().forPath("$root/$path", value.toByteArray())
            }else{
                curator.create()
                        .creatingParentsIfNeeded()
                        .forPath("$root/$path", value.toByteArray())
            }
        }
    }
}


class PairParser(private val config: Properties) {
    private val map = mutableMapOf<String, String>()
    fun parse(): Map<String, String> {
        config.forEach { str, property ->
            mapProps(str.toString(), property)
        }
        return map
    }

    fun mapProps(key: String, value: Any) {
        if (value is Map<*, *>) {
            prefixKeys(key, value).forEach { mapProps(it.key, it.value!!) }
        } else {
            map[key] = value.toString()
        }
    }

    fun prefixKeys(key: String, value: Map<*, *>): Map<String, *> {
        return value.mapKeys { key + "/" + it.key.toString() }
    }
}


class ZkClientFactory(private val connectString: String) {

    fun build(): CuratorFramework {
        val retryPolicy = RetryNTimes(5, 1000)
        val client = CuratorFrameworkFactory.newClient(connectString, retryPolicy)
        client.start()
        return client
    }
}