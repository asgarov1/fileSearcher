# File Searcher

## Motivation:
Windows 10 currently doesn't have a reliable search option available.
When I search for a specific file (like a Java class I wrote) it doesn't find it - but this program does :)

### To run:
 - make sure you have Java 9+
 - run from the same directory as the jar file: 
`java --module-path javafx-sdk-11.0.2/lib --add-modules=javafx.controls -jar com.asgarov.finder.jar`
   
Since javafx dependencies are provided the above command will work for any Java 9+ version

##### Explaining the run command: 
_this is known as a **top-down** modularization approach, where we have modularized our app, but we 
don't modularize the dependencies, instead we turn them into automatic modules by adding them
explicitly to the module-path._

- so `java -jar "name of the jar"` runs the jar
- `--module-path "place where dependencies are" --add-modules="name of the module to add"` puts dependencies on module-path

#### To compile
`javac -d out/production/findFile src/main/java/com/asgarov/finder/*.java 
src/main/java/com/asgarov/finder/helper/*.java src/main/java/com/asgarov/finder/service/*.java 
src/main/java/com/asgarov/finder/util/*.java src/main/java/module-info.java
`

#### To jar

First compile the project and then run:

`jar -cvfe com.asgarov.finder.jar com.asgarov.finder.FinderApplication -C out/production/findFile .
`