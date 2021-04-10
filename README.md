# File Searcher

## Motivation:
Unfortunately Windows 10 currently doesn't have a reliable search option available.
When I search for a specific file it doesn't find it - but this program does :)

Written with Java 11 (fx version).

### To run:
 - make sure you have Java 9+
 - run from the same directory as the jar file: 
`java --module-path javafx-sdk-11.0.2/lib --add-modules=javafx.controls -jar com.asgarov.finder.jar`
   
#### To compile
`javac com/asgarov/finder/*.java com/asgarov/finder/helper/*.java com/asgarov/finder/service/*.java com/asgarov/finder/util/*.java module-info.java`

#### To jar
`jar -cvfe com.asgarov.finder.jar com.asgarov.finder.Main -C src/ .`