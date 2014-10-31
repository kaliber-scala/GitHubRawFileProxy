lazy val root = (project in file(".")).enablePlugins(PlayScala)

name := "GitHubRawFileProxy"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.3"

libraryDependencies += ws