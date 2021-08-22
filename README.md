# FlashTextSpark

A thin Spark wrapper around the FlashTextJava library done by [jasonsperske](https://github.com/jasonsperske/FlashTextJava).
That project was a port of the [flashtext.py](https://github.com/vi3k6i5/flashtext) into Java.

The motivation for this was to run FlashText on Spark to efficiently tag milliions of unstructured documents for matches against a large corpus of keywords (also in the millions).

## Building

Just clone the repo an if you are on UNIX:
```
./gradlew build
```
or on windows:
```
./gradlew.bat build
```
This will bootstrap the project with all the dependencies, just requiring
java 8 to be installed.
