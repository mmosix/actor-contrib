import dsl._
import Keys._
import im.actor.Releasing

scalaVersion := "2.11.7"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

val sdkVersion = "1.0.73"

libraryDependencies ++= Seq(
  "im.actor.server" % "actor-server-sdk" % sdkVersion,
  "im.actor.server" % "actor-testkit" % sdkVersion % "test"
)

enablePlugins(JavaServerAppPackaging)
enablePlugins(JDebPackaging)

name := "actor"

maintainer := "Actor LLC <oss@actor.im>"
packageSummary := "Messaging platform server"
packageDescription := "Open source messaging platform for team communications"
version in Debian := version.value
debianPackageDependencies in Debian ++= Seq("java8-runtime-headless")

rpmVendor := "actor"

daemonUser in Linux := "actor"
daemonGroup in Linux := (daemonUser in Linux).value

bashScriptExtraDefines += """addJava "-Dactor.home=${app_home}/..""""
bashScriptExtraDefines += """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml""""
bashScriptExtraDefines += """addJava -javaagent:${app_home}/../lib/org.aspectj.aspectjweaver-1.8.7.jar"""

dockerExposedPorts := Seq(9070, 9080, 9090)
packageName in Docker := "server"
version in Docker := version.value
dockerRepository := Some("actor")
dockerUpdateLatest := true

// Enterprise goes here:

import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

libraryDependencies ++= Seq(
  "com.codereligion" % "bugsnag-logback" % "1.1.0",
  "com.sksamuel.elastic4s" %% "elastic4s-core" % "1.7.4",
  "org.nibor.autolink" % "autolink" % "0.2.0"
)

PB.protobufSettings

//PB.javaConversions in PB.protobufConfig := true,
libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.5.17" % PB.protobufConfig

PB.runProtoc in PB.protobufConfig := (args =>
  com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray))

Releasing.releaseSettings

pomExtra in Global :=
  <url>https://actor.im</url>
    <licenses>
      <license>
        <name>MIT</name>
        <url>http://www.opensource.org/licenses/MIT</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:github.com/actorapp/actor-platform.git</connection>
      <developerConnection>scm:git:git@github.com:actorapp/actor-platform.git</developerConnection>
      <url>github.com/(your repository url)</url>
    </scm>
    <developers>
      <developer>
        <id>prettynatty</id>
        <name>Andrey Kuznetsov</name>
        <url>https://github.com/prettynatty</url>
      </developer>
    </developers>
