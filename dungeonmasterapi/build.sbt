val Http4sVersion = "0.21.0-M1"
val CirceVersion = "0.11.1"
val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"
val AwscalaVersion = "0.8.+"
val TypeSafeConfigVersion = "1.4.0"
val scalaTestVersion = "0.3.0"

resolvers += Resolver.sonatypeRepo("public")
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

//addCompilerPlugin("io.tryp" % "splain" % "0.5.1" cross CrossVersion.patch)

lazy val main = (project in file("."))
  .settings(
    organization := "com.dungeonMaster",
    name := "dungeonmasterapi",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    libraryDependencies += "com.typesafe" % "config" % "1.4.0",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
      "com.typesafe" % "config" % TypeSafeConfigVersion,
      "org.scalatest" %% "scalatest" % "3.1.0" % "test",
      "org.scalactic" %% "scalactic" % "3.1.0",
      "org.scalacheck" %% "scalacheck" % "1.14.1" % "test",
      "org.scalamock" %% "scalamock" % "4.4.0" % Test
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.0")
  )

libraryDependencies += "org.specs2" %% "specs2-core" % "4.8.1"
libraryDependencies += "io.circe" %% "circe-generic" % "0.12.3"
libraryDependencies += "com.github.seratch" %% "awscala" % "0.8.3"


scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings",
)
