# DASCA
## Installation
### Prerequisites
* Java 8
* Java 6 (core libraries for the WALA analysis)
* Android SDK (to obtain dx.jar)
* Eclipse Mars, including
  * Scala IDE
  * Scala Maven Plugin (http://scala-ide.org/docs/tutorials/m2eclipse/)
  * m2e plugin 
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
wget https://bitbucket.org/iBotPeaches/apktool/downloads/apktool_2.0.0.jar
mvn install:install-file -Dfile=apktool_2.0.0.jar -DgroupId=apktool \
    -DartifactId=apktool -Dpackaging=jar -Dversion=2.0.0
```

### How to Compile
First resolve the dependencies using maven:
```
cd DASCA/src/eu.aniketos.dasca.parent/
mvn -P wala clean install -DskipTests=true -q
```
After this, all projects can be imported into a fresh Eclipse
workspace using `File -> Import -> Maven -> Existing Maven Projects`:
 1. Select the DASCA ``src`` folder as source for the import
 2. Import all offered projects (WALA and DASCA)
While some Wala projects may contain compilation errors, all DASCA 
projects (i.e., `eu.aniketos.dasca.*`) should compile without errors.

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
