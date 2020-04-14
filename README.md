# HTRC-Tools-ScalaUtils
This library provides a set of implicit transformations and routines that reduce the boilerplate 
needed to accomplish some common tasks in Scala.

# Build
* To generate a package that can be referenced from other projects:  
  `sbt package`  
  then find the result in `target/scala-2.12/` (or similar) folder.

# Usage

## SBT
`libraryDependencies += "org.hathitrust.htrc" %% "scala-utils" % "2.11"`

## Maven

**Scala 2.11.x:**
```
<dependency>
    <groupId>org.hathitrust.htrc</groupId>
    <artifactId>scala-utils_2.11</artifactId>
    <version>2.11</version>
</dependency>
```

**Scala 2.12.x:**
```
<dependency>
    <groupId>org.hathitrust.htrc</groupId>
    <artifactId>scala-utils_2.12</artifactId>
    <version>2.11</version>
</dependency>
```

