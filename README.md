# DASCA
## Installation
### Prerequisites
* Java 8
* Android SDK (to obtain dx.jar)
* Eclipse Neon, including
  * From http://download.scala-ide.org/sdk/lithium/e44/scala211/stable/site : Scala IDE and Scalatest Runner (optional)
  * From http://alchim31.free.fr/m2e-scala/update-site : "Maven for Scala" - Maven Integration for Eclipse
  * From http://download.eclipse.org/releases/neon : m2e - Maven Integration for Eclipse
* CVC3 including the Java bindings for CVC3
* apktool 


### Checkout
Note that this repository imports WALA as a submodule. Thus,
you either need to recursively clone this repository, e.g.,
```
git clone --recursive https://git.logicalhacking.com/DASCA/DASCA.git
```
or execute ``git submodule update --init --recursive`` after 
cloning the repository.


### Resolving external dependencies
* Ensure that the environment variable ``ANDROID_HOME`` is set correctly and that
  the Android SDK has API 19 installed, i.e.,
  ``${ANDROID_HOME}/platforms/android-19/android.jar`` should be a valid path.
* Install ``apktool_2.0.0.jar`` into your local maven repository:

```
cd $(mktemp -d)
wget https://bitbucket.org/iBotPeaches/apktool/downloads/apktool_2.0.0.jar
mvn install:install-file -Dfile=apktool_2.0.0.jar -DgroupId=apktool -DartifactId=apktool -Dpackaging=jar -Dversion=2.0.0
```

### WALA configuration
WALA might needs to know the location of the Java JDK (the current setup is tested with JDK version 6 and 8). This is configured in the ``wala.properties`` file, e.g.
```
cd DASCA/
echo "java_runtime_dir = <PATH-TO-JDK>" >> externals/WALA/com.ibm.wala.core/dat/wala.properties
```
Don't forget to adjust the path to the Java JDK accordingly, i.e., the `<PATH-TO-JDK>` should point to the directory containing the file `rt.lib`.

If `java_runtime_dir` is not configured, WALA will use the JDK-libaries of the JDK used for 
executing WALA. This should work in most of the cases, i.e., providing a better "out-of-the-box"
expierence. 

### How to Compile
First resolve the dependencies using maven:
```
cd src/eu.aniketos.dasca.parent/
mvn -P wala clean install -DskipTests=true -q
```
After this, all projects can be imported into a fresh Eclipse
workspace using `File -> Import -> Maven -> Existing Maven Projects`:
 1. Select the DASCA `src` folder as source for the import
 2. Import all offered projects (WALA and DASCA)

While some Wala projects may contain compilation errors, all DASCA 
projects (i.e., `eu.aniketos.dasca.*`) should compile without errors.

## Team 
Main contact: [Achim D. Brucker](http://www.brucker.ch/)

### Contributors
* Thomas Deuster
* [Michael Herzberg](http://www.dcs.shef.ac.uk/cgi-bin/makeperson?M.Herzberg)
* Tim Herres


## Publications
* Achim D. Brucker and Michael Herzberg. [On the Static Analysis of
  Hybrid Mobile Apps: A Report on the State of Apache Cordova
  Nation.](https://www.brucker.ch/bibliography/download/2016/brucker.ea-cordova-security-2016.pdf)
  In International Symposium on Engineering Secure Software
  and Systems (ESSoS). Lecture Notes in Computer Science (9639), pages
  72-88, Springer-Verlag, 2016.
  https://www.brucker.ch/bibliography/abstract/brucker.ea-cordova-security-2016
  doi: [10.1007/978-3-319-30806-7_5](http://dx.doi.org/10.1007/978-3-319-30806-7_5)
