
# Usage


```
Usage: yaml2zk [-hV] [-c=<connectString>] [-r=<root>] <file>
Copy the contents of a yaml file into a zookeeper cluster as central
configurations.
      <file>          The yaml file.
                      If it starts with 'classpath:', the app will search the
                        file as a resource in the classpath.
  -c, --connect-string=<connectString>
                      The connect-string of the zookeeper cluster.
                      default='localhost:2181'
  -h, --help          Show this help message and exit.
  -r, --root=<root>   The root of the znode.
                      default='/test'
  -V, --version       Print version information and exit.
```
