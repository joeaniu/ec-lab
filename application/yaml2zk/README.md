

# Prerequisites

* A JDK installation above 8

# Installation

* download or clone the source code
* build the source
```
./gradlew build
```

* copy and unzip the program

```bash
cd yaml2zk/build/distributions
tar vxf *tar
cp -rf yaml2zk-boot-0.0.1-SNAPSHOT $target
cd $target/bin
# run the program and show the help
./yaml2zk --help
```


# Usage


```
Usage: yaml2zk [-fhV] [-c=<connectString>] [-r=<root>] [<file>]
Copy the contents of a yaml file into a zookeeper cluster as central
configurations.
      [<file>]           The yaml file.
                         If it starts with 'classpath:', the app will search
                           the file as a resource in the classpath.
  -c, --connect-string=<connectString>
                         The connect-string of the zookeeper cluster.
                         default='localhost:2181'
  -f, --force-override   By default, the program will exit when encounter any
                           existed znode and print these znodes' path.
                         If -f is set, the program will override the znodes
                           with warnings.
  -h, --help             Show this help message and exit.
  -r, --root=<root>      The root of the znode.
                         default='/test'
  -V, --version          Print version information and exit.
```
