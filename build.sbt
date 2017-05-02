
lazy val root = (project in file("."))
  .enablePlugins(PlayScala, sbtdocker.DockerPlugin, DockerComposePlugin)
  .settings(rootSettings: _*)
  .settings(herokuSettings: _*)
  .settings(dockerSettings: _*)

lazy val rootSettings = Seq(
  organization := "net.kaliber",
  name := "GitHubRawFileProxy",

  scalaVersion := "2.11.8",
  libraryDependencies += ws
)

lazy val herokuSettings = Seq(
  herokuAppName in Compile := "github-file-proxy-production",

  herokuProcessTypes in Compile := Map(
    "web" -> "target/universal/stage/bin/githubrawfileproxy -Dhttp.port=$PORT -Dallowed.accessTokens=$ALLOWED_ACCESSTOKENS"
  )
)

lazy val dockerSettings = Seq(
  mainClass in assembly := Some("play.core.server.ProdServerStart"),

  fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value),

  assemblyJarName in assembly := s"${name.value}-${version.value}.jar",

  assemblyMergeStrategy in assembly := {
    case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
    case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },

  //assembly. It brings in signpost-commonshttp4, which leads to commons-logging. This conflicts with jcl-over-slf4j, which re-implements the logging API
  libraryDependencies ~= { _ map {
    case m if m.organization == "com.typesafe.play" =>
      m.exclude("commons-logging", "commons-logging").
        exclude("com.typesafe.play", "sbt-link")
    case m => m
  }},

  dockerfile in docker := {
    // The assembly task generates a fat JAR file
    val artifact = assembly.value
    val artifactTargetPath = s"/app/${artifact.name}"

    //You can start this container with
    // docker run --name resource-api-test -p 9000:9000 -v {configDir}:/configs -v {logdir}:/logs -e JAVA_OPTS='-Dconfig.file=/configs/{environment}.conf' {dockerImageName}

    new Dockerfile {
      from("java")
      maintainer("Pointlogic")
      add(artifact, artifactTargetPath)
      volume("/configs", "/logs")
      entryPointShell("exec", "java", "$JAVA_OPTS", "-jar", artifactTargetPath, "$@")
    }
  },

  // exposing the play ports
  dockerExposedPorts in docker := Seq(9000, 9443),

  buildOptions in docker := BuildOptions(cache = false),

  imageNames in docker := dockerImageNames.value,

  dockerImageCreationTask := docker.value
)

lazy val overrideDockerImageNames = settingKey[Option[Seq[ImageName]]]("get the docker image name")
overrideDockerImageNames := None

lazy val dockerImageNames = taskKey[Seq[ImageName]]("get the docker image name")
dockerImageNames := {
  val fallback = s"${organization.value}/${name.value}-${version.value}:latest"

  overrideDockerImageNames.value match {
    case Some(names) => names
    case _ => Seq(ImageName(fallback))
  }
}
