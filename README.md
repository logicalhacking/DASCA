# DASCA

## Installation

### Prerequisites

* Java 8 (Java 9 or later is currently *not* supported)
* Eclipse Oxygen, including the following additional packages:
  * From the Eclipse Marketplace:
    * The Plug-in Development Environment (PDE)
    * JavaScript Development Tools (JSDT)
    * Gradle Integration (Buildship)
  * From the [Scala IDE Update Site](http://scala-ide.org/download/current.html)
    * [Scala IDE and Scalatest Runner (the latter is optional)](http://download.scala-ide.org/sdk/lithium/e44/scala211/stable/site)
* The native libraries and the JNI packages for [CVC3](http://cs.nyu.edu/acsys/cvc3/). 
  On a Debian-based Linux system, you need to install the package `libcvc3-5-jni`.

### Checkout

The repository can be cloned as usual:

``` sh
git clone https://git.logicalhacking.com/DASCA/DASCA.git
```

Note, if you authorized to access the confidential test cases of 
DASCA, you can obtain them by executing

``` sh
git submodule update --init --recursive
```

### WALA configuration (optional)

DASCA (and the underlying WALA setup) is tested with Java version 8.
If DASCA is installed using Java 8, there should be no need for updating
the WALA configuration.

If you experience problems or want to optimize the performance (e.g.,
by analyzing the programs based on a different Java version), you
might need to configure the location of the Java JDK. The JDK used
as part of the static analysis is configured in the file 
`com.logicalhacking.dasca.dataflow/config/main.config`, e.g.

``` sh
cd DASCA/
echo "java_runtime_dir = <PATH-TO-JDK>" >> ./com.logicalhacking.dasca.dataflow/config/main.config
```

Don't forget to adjust the path to the Java JDK accordingly, i.e.,
the `<PATH-TO-JDK>` should point to the directory containing the file
`rt.lib`.

### How to Compile

First check that the variable `JAVA_HOME` is configured correctly, to ensure 
that Java 8 is used, e.g.:

``` sh
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

The project can be compiled using gradle

``` sh
./gradlew clean assemble test
```

### Import into Eclipse

All projects can be imported into a (fresh) Eclipse workspace
using `File -> Import -> Gradle -> Existing Gradle Projects`:

 1. Select the `DASCA` folder as source for the import
 2. Import all offered projects


## Team

Main contact: [Achim D. Brucker](http://www.brucker.ch/)

### Contributors

* Thomas Deuster
* [Michael Herzberg](http://www.dcs.shef.ac.uk/cgi-bin/makeperson?M.Herzberg)
* Tim Herres

## License

This project is licensed under the Eclipse Public License 1.0.

## Master Repository

The master git repository for this project is hosted by the [Software
Assurance & Security Research Team](https://logicalhacking.com) at
<https://git.logicalhacking.com/DASCA/DASCA>.

## Publications

* Achim D. Brucker and Michael Herzberg. [On the Static Analysis of
  Hybrid Mobile Apps: A Report on the State of Apache Cordova
  Nation.](https://www.brucker.ch/bibliography/download/2016/brucker.ea-cordova-security-2016.pdf)
  In International Symposium on Engineering Secure Software
  and Systems (ESSoS). Lecture Notes in Computer Science (9639), pages
  72-88, Springer-Verlag, 2016.
  https://www.brucker.ch/bibliography/abstract/brucker.ea-cordova-security-2016
  doi: [10.1007/978-3-319-30806-7_5](http://dx.doi.org/10.1007/978-3-319-30806-7_5)
