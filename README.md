# DASCA
## Installation
### Prerequisites
* Java 8
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
git clone --recursive https://github.com/DASPA/DASCA.git
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
* Michael Herzberg
* Tim Herres
