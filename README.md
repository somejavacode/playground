# playground 

just an unsorted collection of some java command line tools, utilities, benchmarks and experiments.
ufw is the micro-framework for common code.

all done with focus on minimal size and minimal dependencies (currently none) and efficiency.

## usage hints

this is all rather raw.

```
mvn clean compile
set CLASSPATH=target/classes
java PakPacker
```

using jar file
```
mvn clean package
java -cp playground-1.0-SNAPSHOT.jar PakPacker
```

## useful tools

PakPacker:

can unpack/pack chromium pak (aka resource obfuscation) format.

it might help to control all the (shady) things chromium is doing (without your consent)

content is extracted to directory with resource id as filename.
you need to look into content to find out what to find out file type.
might be improved in future by "guessing" synthetic extensions.
