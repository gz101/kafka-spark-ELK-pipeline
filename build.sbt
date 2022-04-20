import Dependencies._

ThisBuild / scalaVersion := "2.13.6"
ThisBuild / semanticdbEnabled := true // enable SemanticDB
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision // use Scalafix compatible version

lazy val root = (project in file(".")).
  settings(
    name := "stream-handler",
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-language:postfixOps",
      "-language:higherKinds", // HKT required for Monads and other HKT types
      "-Wunused", // for scalafix
    ),
    libraryDependencies ++= Dependencies.core ++ Dependencies.scalaTest,
    assembly / mainClass := Some("StreamHandler"),
    assembly / assemblyJarName := "2022SpringSparkJob.jar",
    assembly / test := {},
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case "application.conf"            => MergeStrategy.concat
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },
    // see shading feature at https://github.com/sbt/sbt-assembly#shading
    assembly / assemblyShadeRules := Seq(
      ShadeRule.rename("shapeless.**" -> "shadeshapeless.@1").inAll
    )
  )
