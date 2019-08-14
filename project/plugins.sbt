logLevel := Level.Warn

addSbtPlugin("com.typesafe.sbt"       % "sbt-git"             % "1.0.0")
addSbtPlugin("com.typesafe.sbt"       % "sbt-native-packager" % "1.3.25")
addSbtPlugin("com.eed3si9n"           % "sbt-assembly"        % "0.14.10")
addSbtPlugin("org.wartremover"        % "sbt-wartremover"     % "2.4.2")
addSbtPlugin("io.crashbox"            % "sbt-gpg"             % "0.2.0")