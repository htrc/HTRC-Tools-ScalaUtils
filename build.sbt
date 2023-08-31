showCurrentGitBranch

inThisBuild(Seq(
  organization := "org.hathitrust.htrc",
  organizationName := "HathiTrust Research Center",
  organizationHomepage := Some(url("https://www.hathitrust.org/htrc")),
  scalaVersion := "2.13.11",
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-language:postfixOps",
    "-language:implicitConversions"
  ),
  resolvers ++= Seq(
    Resolver.mavenLocal,
    "HTRC Nexus Repository" at "https://nexus.htrc.illinois.edu/repository/maven-public"
  ),
  externalResolvers := Resolver.combineDefaultResolvers(resolvers.value.toVector, mavenCentral = false),
  Compile / packageBin / packageOptions += Package.ManifestAttributes(
    ("Git-Sha", git.gitHeadCommit.value.getOrElse("N/A")),
    ("Git-Branch", git.gitCurrentBranch.value),
    ("Git-Version", git.gitDescribedVersion.value.getOrElse("N/A")),
    ("Git-Dirty", git.gitUncommittedChanges.value.toString),
    ("Build-Date", new java.util.Date().toString)
  ),
  dynverSonatypeSnapshots := true,
  versionScheme := Some("semver-spec"),
  crossScalaVersions := Seq("2.13.11", "2.12.18")
))

lazy val publishSettings = Seq(
  publishTo := {
    val nexus = "https://nexus.htrc.illinois.edu/"
    if (isSnapshot.value)
      Some("HTRC Snapshots Repository" at nexus + "repository/snapshots")
    else
      Some("HTRC Releases Repository"  at nexus + "repository/releases")
  },
  // force to run 'test' before 'package' and 'publish' tasks
  publish := (publish dependsOn Test / test).value,
  Keys.`package` := (Compile / Keys.`package` dependsOn Test / test).value,
  credentials += Credentials(
    "Sonatype Nexus Repository Manager", // realm
    "nexus.htrc.illinois.edu", // host
    "drhtrc", // user
    sys.env.getOrElse("HTRC_NEXUS_DRHTRC_PWD", "abc123") // password
  )
)

lazy val ammoniteSettings = Seq(
  libraryDependencies +=
    {
      val version = scalaBinaryVersion.value match {
        case "2.10" => "1.0.3"
        case "2.11" => "1.6.7"
        case _ ⇒  "2.5.9"
      }
      "com.lihaoyi" % "ammonite" % version % Test cross CrossVersion.full
    },
  Test / sourceGenerators += Def.task {
    val file = (Test / sourceManaged).value / "amm.scala"
    IO.write(file, """object amm extends App { ammonite.AmmoniteMain.main(args) }""")
    Seq(file)
  }.taskValue,
  connectInput := true,
  outputStrategy := Some(StdoutOutput)
)

lazy val `scala-utils` = (project in file("."))
  .enablePlugins(GitVersioning, GitBranchPrompt)
  .settings(publishSettings)
  .settings(ammoniteSettings)
  .settings(
    name := "scala-utils",
    description :=
      "A set of utility functions and routines that reduce the boilerplate needed " +
      "to accomplish some common tasks in Scala.",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules"  %% "scala-collection-compat"  % "2.11.0",
      "org.scalacheck"          %% "scalacheck"               % "1.17.0"    % Test,
      "org.scalatest"           %% "scalatest"                % "3.2.16"    % Test,
      "org.scalatestplus"       %% "scalacheck-1-15"          % "3.2.11.0"  % Test
    ),
  )
