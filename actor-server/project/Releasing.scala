package im.actor

import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport._
import sbt._
import Keys._
import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin.autoImport._

object Releasing {
  lazy val releaseSettings = Seq(
    releaseCommitMessage := s"chore(server): setting version to ${(version in ThisBuild).value}",
    releaseTagName := s"server/v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}",
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      ReleaseStep(
        action = { state =>
          val extracted = Project extract state
          if (sys.env.isDefinedAt("TEAMCITY_VERSION")) {
            println(s"##teamcity[buildNumber '${extracted.get(version)}']")
          }
          extracted.runAggregated(packageBin in Debian in extracted.get(thisProjectRef), state)
        }
      ),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
}