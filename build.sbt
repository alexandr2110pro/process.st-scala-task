name := "process_st_scala_test"

version := "1.0"

lazy val `process_st_scala_test` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(jdbc,
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42",
  "com.typesafe.play" %% "anorm" % "2.5.1",
  evolutions,
  cache,
  ws,
  specs2 % Test)

libraryDependencies += guice

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  