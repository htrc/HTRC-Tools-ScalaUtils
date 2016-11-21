# HTRC-Tools-ScalaUtils
This library provides a set of implicit transformations and routines that reduce the boilerplate needed to accomplish some common tasks in Scala.

# Build
* To generate a package that can be referenced from other projects:  
  `sbt test package`  
  then find the result in `target/scala-2.11/` (or similar) folder.

# Usage

To use via Maven:
```
<dependency>
    <groupId>org.hathitrust.htrc</groupId>
    <artifactId>scala-utils.11</artifactId>
    <version>2.0-SNAPSHOT</version>
</dependency>
```

To use via SBT:
`libraryDependencies += "org.hathitrust.htrc" %% "scala-utils" % "2.0-SNAPSHOT"`
