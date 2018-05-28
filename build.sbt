
val json4sNative = "org.json4s" %% "json4s-native" % "3.5.2"
val proj4j = "io.jeo" % "jeo" % "0.7"
val graphhopper = "com.graphhopper" % "graphhopper" % "0.7.0"
val kdtree = "com.thesamet" %% "kdtree" % "1.0.4"
val scalatic = "org.scalactic" %% "scalactic" % "3.0.0"
val scalatest = "org.scalatest" %% "scalatest" % "3.0.0" % "test"

def scalaSources(base: File): PathFinder = (base / "src") ** "*.scala"
//
val akkaV = "2.4.19"
val sprayV = "1.3.3"

enablePlugins(JavaServerAppPackaging)

val commonSettings = Seq(
  resolvers += DefaultMavenRepository,
  scalaVersion := "2.11.11",
  version := "1.0"
)

lazy val fuzzyClustering = (project in file("fuzzy-clustering")).
  settings(
    commonSettings,
    name := "fuzzy-clustering",
    libraryDependencies ++= Seq(
      scalatest
    ),
    mainClass in assembly := Some("sg.beeline.clustering.FuzzyDistanceClusterApp")
  )

lazy val routing = (project in file("routing"))
  .settings(
    commonSettings,
    name := "routing",
    libraryDependencies ++= Seq(
      json4sNative,
      proj4j,
      graphhopper,
      kdtree,
      scalatic,
      scalatest
    ) ++ Seq(
      "org.postgresql" % "postgresql" % "42.1.3",
      "com.typesafe.akka"   %% "akka-http" % "10.0.9",
      "com.typesafe.akka"   %% "akka-http-spray-json" % "10.0.9",
      "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
      "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
      "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5",
      "com.typesafe.slick" %% "slick" % "3.2.1",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1"
    )
  )

lazy val root = (project in file("."))
  .aggregate(fuzzyClustering, routing)
  .settings(
    commonSettings,
    name := "beeline-routing",
    mainClass in assembly := Some("sg.beeline.BeelineRoutingApp")
  )

fork in run := true

