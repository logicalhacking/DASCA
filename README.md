# DASCA
## Installation
### Prerequisites
* Java 8 (Java 9 or later is currently *not* supported)
* Android SDK (to obtain dx.jar)
* Eclipse Oxygen, including
  * The Plug-in Development Environment (PDE)
  * JavaScript Development Tools (JSDT)
  * [Scala IDE and Scalatest Runner (the latter is optional)](http://download.scala-ide.org/sdk/lithium/e44/scala211/stable/site)
  * ["Maven for Scala" - Maven Integration for Eclipse](http://alchim31.free.fr/m2e-scala/update-site)
  * m2e - Maven Integration for Eclipse
* [CVC3](http://cs.nyu.edu/acsys/cvc3/) including the Java bindings for CVC3
* [apktool](https://ibotpeaches.github.io/Apktool/)


### Checkout
Note that this repository imports [WALA](http://wala.sf.net) as a submodule. Thus,
you either need to recursively clone this repository, e.g.,
```
git clone --recursive https://git.logicalhacking.com/DASCA/DASCA.git
```
or execute ``git submodule update --init --recursive`` after 
cloning the repository.


### Resolving external dependencies
* Ensure that the environment variable `ANDROID_HOME` is set correctly and that
  the Android SDK has API 19 installed, i.e.,
  `${ANDROID_HOME}/platforms/android-19/android.jar` should be a valid path.
* Install ``apktool_2.3.0.jar`` into your local maven repository:

```
cd $(mktemp -d)
wget https://bitbucket.org/iBotPeaches/apktool/downloads/apktool_2.3.0.jar
mvn install:install-file -Dfile=apktool_2.3.0.jar -DgroupId=apktool -DartifactId=apktool -Dpackaging=jar -Dversion=2.3.0
```

### WALA configuration
DASCA (and the underlying WALA setup) is tested with Java version 8.
If DASCA is installed using Java 8, there should be no need for updating 
the WALA configuration. 

If you experience problems or want to optimize the performance (e.g., 
by analyzing the programs based on a different Java version), you 
might need to configure the location of the Java JDK. The JDK used
as part of the static analysis is configured in the `wala.properties` 
file, e.g.
```
cd DASCA/
echo "java_runtime_dir = <PATH-TO-JDK>" >> externals/WALA/com.ibm.wala.core/dat/wala.properties
```
Don't forget to adjust the path to the Java JDK accordingly, i.e.,
the `<PATH-TO-JDK>` should point to the directory containing the file
`rt.lib`.

### How to Compile
First check that the variable `JAVA_HOME` is configured correctly, e.g.:
```
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
```

Second, resolve the dependencies using maven:
```
cd src/com.logicalhacking.dasca.parent/
mvn -P wala clean verify -DskipTests=true 
```

After this, all projects can be imported into a fresh Eclipse
workspace using `File -> Import -> Maven -> Existing Maven Projects`:
 1. Select the DASCA `src` folder as source for the import
 2. Import all offered projects (WALA and DASCA)

While some WALA projects may contain compilation errors, all DASCA 
projects (i.e., `com.logicalhacking.dasca.*`) should compile without errors.

## Team 
Main contact: [Achim D. Brucker](http://www.brucker.ch/)

### Contributors
* Thomas Deuster
* [Michael Herzberg](http://www.dcs.shef.ac.uk/cgi-bin/makeperson?M.Herzberg)
* Tim Herres

## License
This project is licensed under the Eclipse Public License 1.0. 

## Publications
* Achim D. Brucker and Michael Herzberg. [On the Static Analysis of
  Hybrid Mobile Apps: A Report on the State of Apache Cordova
  Nation.](https://www.brucker.ch/bibliography/download/2016/brucker.ea-cordova-security-2016.pdf)
  In International Symposium on Engineering Secure Software
  and Systems (ESSoS). Lecture Notes in Computer Science (9639), pages
  72-88, Springer-Verlag, 2016.
  https://www.brucker.ch/bibliography/abstract/brucker.ea-cordova-security-2016
  doi: [10.1007/978-3-319-30806-7_5](http://dx.doi.org/10.1007/978-3-319-30806-7_5)
