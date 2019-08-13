showCurrentGitBranch

git.useGitDescribe := true

lazy val commonSettings = Seq(
  organization := "org.hathitrust.htrc",
  organizationName := "HathiTrust Research Center",
  organizationHomepage := Some(url("https://www.hathitrust.org/htrc")),
  scalaVersion := "2.12.9",
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-language:postfixOps",
    "-language:implicitConversions"
  ),
  externalResolvers ++= Seq(
    Resolver.defaultLocal,
    Resolver.mavenLocal,
    "HTRC Nexus Repository" at "http://nexus.htrc.illinois.edu/content/groups/public"
  ),
  packageOptions in (Compile, packageBin) += Package.ManifestAttributes(
    ("Git-Sha", git.gitHeadCommit.value.getOrElse("N/A")),
    ("Git-Branch", git.gitCurrentBranch.value),
    ("Git-Version", git.gitDescribedVersion.value.getOrElse("N/A")),
    ("Git-Dirty", git.gitUncommittedChanges.value.toString),
    ("Build-Date", new java.util.Date().toString)
  ),
  publishTo := {
    val nexus = "https://nexus.htrc.illinois.edu/"
    if (isSnapshot.value)
      Some("HTRC Snapshots Repository" at nexus + "content/repositories/snapshots")
    else
      Some("HTRC Releases Repository"  at nexus + "content/repositories/releases")
  },
  wartremoverErrors ++= Warts.unsafe.diff(Seq(
    Wart.DefaultArguments,
    Wart.NonUnitStatements
  )),
  // force to run 'test' before 'package' and 'publish' tasks
  publish := (publish dependsOn Test / test).value,
  Keys.`package` := (Compile / Keys.`package` dependsOn Test / test).value
)

lazy val `scala-utils` = (project in file("."))
  .enablePlugins(GitVersioning, GitBranchPrompt)
  .settings(commonSettings)
  .settings(
    name := "scala-utils",
    description :=
      "A set of utility functions and routines that reduce the boilerplate needed " +
      "to accomplish some common tasks in Scala.",
    libraryDependencies ++= Seq(
      "org.scalacheck"    %% "scalacheck"     % "1.14.0"  % Test,
      "org.scalatest"     %% "scalatest"      % "3.0.8"   % Test
    ),
    crossScalaVersions := Seq("2.12.9", "2.11.12")
  )
