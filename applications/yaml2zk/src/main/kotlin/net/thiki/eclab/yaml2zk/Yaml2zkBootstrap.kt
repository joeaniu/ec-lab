package net.thiki.eclab.yaml2zk

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryNTimes
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
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
            arity = "0..1",
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
    private var connectString: String = "localhost:2181"

    @Option(
            names = ["-f", "--force-override"],
            description = [
                "By default, the program will exit when encounter any existed znode and print these znodes' path.",
                "If -f is set, the program will override the znodes with warnings."
            ]
    )
    private var forceOverride: Boolean = false

    @Option(
            names = ["-s", "--spring-multiple-profiles-support"],
            description = [
                "Support multiple profiles in one application.yaml file."
            ]
    )
    private var springBootSupport: Boolean = false


    override fun call(): Int{
        assert(file != null){
            "Must specify the file in the first parameter."
        }
        val curator = ZkClientFactory(connectString).build()

        val inputStream = if (file!!.startsWith("classpath:")) {
            val realFile = file!!.substring("classpath:".length)
            println("realFile=$realFile.........")
            Yaml2Zk::class.java.getResourceAsStream(realFile)
        } else {
            Files.newInputStream(Paths.get(file))
        }

        val yaml = Yaml(Constructor(Properties::class.java))
                inputStream.use { ins ->
                    yaml.loadAll(ins).forEach { config ->
                        config as Properties
                        val map = PairParser(config).parse()
                        val code = writeADocument(map, curator)
                        if (code < 0) return code
                    }
                }

        return 0
    }

    private fun writeADocument(map: Map<String, String>, curator: CuratorFramework): Int {

        val theRoot = if (springBootSupport && map.containsKey("spring/profiles")){
            "$root-${map["spring/profiles"]}"
        }else{
            root
        }

        println("\n\n")
        println("root=$theRoot")
        println("printing the contents: ")
        map.forEach { (path, value) ->
            println("$path->$value")
        }

        println("\n\n")
        var existed = false
        map.forEach { (path, value) ->
            val stat = curator.checkExists()
                    .forPath("$theRoot/$path")
            if (stat != null) {
                println("warning: $path exists already, will be overridden.")
                existed = true
            }
        }

        if (!forceOverride && existed) {
            println("There are some path already exists, nothing changes.")
            return -99
        }

        map.forEach { (path, value) ->
            val stat = curator.checkExists()
                    .forPath("$theRoot/$path")
            if (stat != null) {
                curator.setData().forPath("$theRoot/$path", value.toByteArray())
            } else {
                curator.create()
                        .creatingParentsIfNeeded()
                        .forPath("$theRoot/$path", value.toByteArray())
            }
        }
        println("\n\n")
        println("done.")
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