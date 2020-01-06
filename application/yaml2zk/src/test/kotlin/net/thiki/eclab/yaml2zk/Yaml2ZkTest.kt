package net.thiki.eclab.yaml2zk

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class Yaml2ZkTest{

    @Test
    @Disabled("depends on the zookeeper server.")
    fun testYaml2Zk(){
        val cut = Yaml2Zk()

//        cut.run("path_to/test.yaml")
        cut.run("classpath:/test.yaml")
    }
}