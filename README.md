# DASCA
## Installation
### Prerequisites
* Java 8
* CVC3 including the Java bindings for CVC3
* Android SDK (do obtain dx.jar)

### Checkout
Note that this repository imports WALA as a submodule. Thus,
you either need to recursively clone this repository, e.g., 
```
git clone --recursive https://github.com/DASPA/DASCA.git
```
or execute ``git submodule update --init --recursive`` after 
cloning the repository.

Next, you need to copy ``dx.jar`` from the Android SDK into
the WALA project ``com.ibm.wala.dalvik.test'', e.g.:
```
mkdir -p DASCA/externals/WALA/com.ibm.wala.dalvik.test/lib
cp $ANDROID_HOME/build-tools/20.0.0/lib/dx.jar DASCA/externals/WALA/com.ibm.wala.dalvik.test/lib
```

### How to Compile
First resolve the dependencies using maven and initialise the 
Eclipse project structure:
```
cd DASCA/src/eu.aniketos.dasca.parent/
mvn -P wala clean install -DskipTests=true -q
mvn -P dasca clean eclipse:clean 
mvn -P dasca eclipse:eclipse
```
After this, all projects can be imported into a fresh Eclipse
workspace using `File -> Import -> Existing Projects into Workspace`.
Please import them in the following order:
1. Import all WALA projects (``externals/WALA/``) into Eclipse. 
2. Import all DASCA projects (``src/``) into Eclipse.
While several Wala projects may contain compilation errors, all DASCA 
projects (i.e., `eu.aniketos.dasca.*`) should compile without errors.

## Team 
Main contact: [Achim D. Brucker](http://www.brucker.ch/)

### Contributors
* Thomas Deuster
* Michael Herzberg
* Tim Herres
