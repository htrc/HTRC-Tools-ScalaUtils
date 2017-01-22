showCurrentGitBranch

git.useGitDescribe := true

lazy val commonSettings = Seq(
  organization := "org.hathitrust.htrc",
  scalaVersion := "2.12.1",
  scalacOptions ++= Seq(
    "-feature",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-target:jvm-1.7"
  ),
  javacOptions ++= Seq(
    "-source", "1.7",
    "-target", "1.7"
  ),
  resolvers ++= Seq(
    "I3 Repository" at "http://nexus.htrc.illinois.edu/content/groups/public",
    Resolver.mavenLocal
  ),
  publishTo := {
    val nexus = "https://nexus.htrc.illinois.edu/"
    if (isSnapshot.value)
      Some("HTRC Snapshots Repository" at nexus + "content/repositories/snapshots")
    else
      Some("HTRC Releases Repository"  at nexus + "content/repositories/releases")
  },
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  packageOptions in (Compile, packageBin) += Package.ManifestAttributes(
    ("Git-Sha", git.gitHeadCommit.value.getOrElse("N/A")),
    ("Git-Branch", git.gitCurrentBranch.value),
    ("Git-Version", git.gitDescribedVersion.value.getOrElse("N/A")),
    ("Git-Dirty", git.gitUncommittedChanges.value.toString),
    ("Build-Date", new java.util.Date().toString)
  )
)

lazy val `scala-utils` = (project in file(".")).
  enablePlugins(GitVersioning, GitBranchPrompt).
  settings(commonSettings: _*).
  settings(
    name := "scala-utils",
    description :=
      "A set of utility functions and routines that reduce the boilerplate needed " +
      "to accomplish some common tasks in Scala.",
    libraryDependencies ++= Seq(
      "org.scalacheck"    %% "scalacheck"     % "1.13.4"  % "test",
      "org.scalatest"     %% "scalatest"      % "3.0.1"   % "test"
    ),
    crossScalaVersions := Seq("2.12.1", "2.11.8")
  )
