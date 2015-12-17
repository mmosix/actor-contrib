resolvers += Resolver.url("actor-sbt-plugins", url("https://dl.bintray.com/actor/sbt-plugins"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.4")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.17")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.1")

addSbtPlugin("im.actor" % "actor-sbt-houserules" % "0.1.1")

libraryDependencies ++= Seq("com.github.os72" % "protoc-jar" % "3.0.0-a3")
