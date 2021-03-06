package net.thiki.eclab.yaml2zk

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import picocli.CommandLine

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class Yaml2ZkTest {

    @Test
    @Disabled("depends on the zookeeper server.")
    fun testYaml2Zk() {
        val cut = Yaml2Zk()

//        cut.run("path_to/test.yaml")
        CommandLine(cut).execute("-f", "classpath:/test.yaml", "-r=/test")
    }

    @Test
    fun testCommandLine() {
        CommandLine(Yaml2Zk()).execute("--help")

    }


    @Test
    @Disabled("depends on the zookeeper server.")
    fun testMultipleDocument() {
        val cut = Yaml2Zk()

//        cut.run("path_to/test.yaml")
        CommandLine(cut).execute("-sf", "classpath:/multiprofiles.yaml", "-r=/config/test")
    }

}