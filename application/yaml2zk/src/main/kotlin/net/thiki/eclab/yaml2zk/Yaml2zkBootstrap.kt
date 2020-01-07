package net.thiki.eclab.yaml2zk

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryNTimes
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.yaml.snakeyaml.Yaml
import picocli.CommandLine
import picocli.CommandLine.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Callable


@SpringBootApplication
class Yaml2zkBootstrap{

    companion object{

        @JvmStatic
        fun main(args: Array<String>) {
//            runApplication<Yaml2zkBootstrap>(*args)

            CommandLine(Yaml2Zk()).execute(*args)
        }
    }
}

@Command(name = "yaml2zk", mixinStandardHelpOptions = true, version = ["1.0"],
        description = ["Copy the contents of a yaml file into a zookeeper cluster as central configurations. "])
class Yaml2Zk: Callable<Int> {

    @Parameters(
            index = "0",
            description = [
                "The yaml file.",
                "If it starts with 'classpath:', the app will search the file as a resource in the classpath."
            ]
    )
    private var file: String? = null

    @Option(
            names = ["-r", "--root"],
            description = [
                "The root of the znode.",
                "default='/test'"
            ]
    )
    private var root: String = "/test"

    @Option(
            names = ["-c", "--connect-string"],
            description = [
                "The connect-string of the zookeeper cluster.",
                "default='localhost:2181'"
            ]
    )
    private var connectString = "localhost:2181"

    override fun call(): Int{
        assert(file != null){
            "Must specify the file in the first parameter."
        }
        val curator = ZkClientFactory(connectString).build()

        val yaml = Yaml()
        val prefix = "classpath:"
        val inputStream = if (file!!.startsWith(prefix)) {
            val realFile = file!!.substring(prefix.length)
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

        var existed = false
        map.forEach { (path, value) ->
            println("$path->$value")
            val stat = curator.checkExists()
                    .forPath("$root/$path")
            if (stat != null){
                println("warning: $path exists already, will be overridden.")
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
        return 0
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
            map[key.replace(".", "/")] = value.toString()
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