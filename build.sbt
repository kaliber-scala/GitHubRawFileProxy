lazy val root = (project in file(".")).enablePlugins(PlayScala)

name := "GitRawFileProxy"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies += ws

herokuAppName in Compile := "github-file-proxy-production"

herokuProcessTypes in Compile := Map(
  "web" -> "target/universal/stage/bin/githubrawfileproxy -Dhttp.port=$PORT -Dallowed.accessTokens=$ALLOWED_ACCESSTOKENS"
)
