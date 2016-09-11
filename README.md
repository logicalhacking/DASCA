# DASCA
## Installation
### Prerequisites
* Java 8
* Java 6 (core libraries for the WALA analysis)
* Android SDK (to obtain dx.jar)
* Eclipse Neon, including
  * From http://download.scala-ide.org/sdk/lithium/e44/scala211/stable/site
    * Scala IDE
    * Scalatest Runner (optional)
  * From http://alchim31.free.fr/m2e-scala/update-site
    * "Maven for Scala" - Maven Integration for Eclipse
  * From http://download.eclipse.org/releases/neon
    * m2e - Maven Integration for Eclipse
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
WALA needs to know the location of the Java 6 JDK. This is configured in the ``wala.properties`` file, e.g.
```
cd DASCA/
echo "java_runtime_dir = /usr/lib/jvm/java-6-jdk" >> externals/WALA/com.ibm.wala.core/dat/wala.properties
```
Don't forget to adjust the path to the Java 6 JDK accordingly.

### How to Compile
First resolve the dependencies using maven:
```
cd src/eu.aniketos.dasca.parent/
mvn -P wala clean install -DskipTests=true -q
```
After this, all projects can be imported into a fresh Eclipse
workspace using `File -> Import -> Maven -> Existing Maven Projects`:
 1. Select the DASCA ``src`` folder as source for the import
 2. Import all offered projects (WALA and DASCA)

While some Wala projects may contain compilation errors, all DASCA 
projects (i.e., `eu.aniketos.dasca.*`) should compile without errors.

## Troubleshooting
### Unavailable external JARs
The build process for WALA uses Ant scripts to download JAR libraries from third-party web sites, which might
become unavailable. This leads to Maven throwing errors when compiling WALA due to unavailable JAR files, e.g.
```
[ERROR] DASCA/externals/WALA/com.ibm.wala.cast.js.test.data/build.xml:45: Can't get http://ajaxslt.googlecode.com/files/ajaxslt-0-7.tar.gz to DASCA/externals/WALA/com.ibm.wala.cast.js.test.data/temp.folder/ajaxslt-0-7.tar.gz
```
If the affected sub-project is not needed for the DASCA projects (such as the com.ibm.wala.cast.js.test.data
project), the error might be resolved by skipping the compilation of a few WALA projects, e.g.
```
mvn -P wala -pl '!:com.ibm.wala.cast.js.test.data,!:com.ibm.wala.cast.js.html.nu_validator,!:com.ibm.wala.cast.js.test,!:com.ibm.wala.cast.js.rhino.test' install -DskipTests=true -q
```

### WALA throws errors such as ``java.lang.Error: unexpected dynamic invoke type 8``
Make sure the ``wala.properties`` file is correctly configured with the path to the Java 6 JDK. By default,
WALA uses the JDK that is runs with also for the analysis, which is likely to be Java 8.

## Team 
Main contact: [Achim D. Brucker](http://www.brucker.ch/)

### Contributors
* Thomas Deuster
* [Michael Herzberg](http://www.dcs.shef.ac.uk/cgi-bin/makeperson?M.Herzberg)
* Tim Herres


### Publications
* Achim D. Brucker and Michael Herzberg. [On the Static Analysis of
  Hybrid Mobile Apps: A Report on the State of Apache Cordova
  Nation.](https://www.brucker.ch/bibliography/download/2016/brucker.ea-cordova-security-2016.pdf)
  In International Symposium on Engineering Secure Software
  and Systems (ESSoS). Lecture Notes in Computer Science (9639), pages
  72-88, Springer-Verlag, 2016.
  https://www.brucker.ch/bibliography/abstract/brucker.ea-cordova-security-2016
  doi: [10.1007/978-3-319-30806-7_5](http://dx.doi.org/10.1007/978-3-319-30806-7_5)
