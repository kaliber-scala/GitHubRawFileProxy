// Comment to get more information during initialization
logLevel := Level.Warn

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.14")

resolvers += Resolver.url("heroku-sbt-plugin-releases",
  url("http://dl.bintray.com/heroku/sbt-plugins/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.heroku" % "sbt-heroku" % "1.0.0")
