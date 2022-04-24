import sbt._

object Dependencies {
  lazy val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % "3.0.8" % Test,
    // for law testing cats based typeclasses
    // "org.typelevel" %% "cats-laws" % "2.0.0" % Test,
    // "org.typelevel" %% "cats-testkit-scalatest" % "2.1.5"% Test,
    // "com.github.alexarchambault" %% "scalacheck-shapeless_1.15" % "1.3.0" % Test
  )

  // val circeVersion = "0.13.0"
  // val pureconfigVersion = "0.15.0"
  // val catsVersion = "2.2.0"
  val sparkVersion = "3.2.1"
  val postgresVersion = "42.2.2" 

  lazy val core = Seq(
    // cats FP libary
    // "org.typelevel" %% "cats-core" % catsVersion,

    // support for JSON formats
    // "io.circe" %% "circe-core" % circeVersion,
    // "io.circe" %% "circe-generic" % circeVersion,
    // "io.circe" %% "circe-parser" % circeVersion,
    // "io.circe" %% "circe-literal" % circeVersion,

    // support for typesafe configuration
    // "com.github.pureconfig" %% "pureconfig" % pureconfigVersion,

    // parallel collections
    // "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",

    // spark
    "org.apache.spark" %% "spark-sql" % sparkVersion % Provided, // for submiting spark app as a job to cluster
    // "org.apache.spark" %% "spark-sql" % sparkVersion, // for simple standalone spark app

    // postgres database
    "org.postgresql" % "postgresql" % postgresVersion,

    // logging
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )
}
