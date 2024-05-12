[![Scala CI](https://github.com/htrc/HTRC-Tools-ScalaUtils/actions/workflows/ci.yml/badge.svg)](https://github.com/htrc/HTRC-Tools-ScalaUtils/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/htrc/HTRC-Tools-ScalaUtils/branch/develop/graph/badge.svg?token=2AU0ZY0EPL)](https://codecov.io/gh/htrc/HTRC-Tools-ScalaUtils)
[![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/htrc/HTRC-Tools-ScalaUtils?include_prereleases&sort=semver)](https://github.com/htrc/HTRC-Tools-ScalaUtils/releases/latest)

# HTRC-Tools-ScalaUtils
This library provides a set of implicit transformations and routines that reduce the boilerplate 
needed to accomplish some common tasks in Scala.

# Build
* To generate a package that can be referenced from other projects:  
  `sbt package`  
  then find the result in `target/scala-2.13/` (or similar) folder.

# Usage

## SBT
`libraryDependencies += "org.hathitrust.htrc" %% "scala-utils" % VERSION`

## Maven

**Scala 2.12.x:**
```
<dependency>
    <groupId>org.hathitrust.htrc</groupId>
    <artifactId>scala-utils_2.12</artifactId>
    <version>VERSION</version>
</dependency>
```

**Scala 2.13.x:**
```
<dependency>
    <groupId>org.hathitrust.htrc</groupId>
    <artifactId>scala-utils_2.13</artifactId>
    <version>VERSION</version>
</dependency>
```

