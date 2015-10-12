name := """consumer-stack-service"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

resolvers ++= Seq(
	"The New Motion Public Repo" at "http://nexus.thenewmotion.com/content/groups/public/"
)

libraryDependencies ++= Seq(
	"com.thenewmotion.akka" %% "akka-rabbitmq" % "1.2.4",
	"org.mongodb" %% "casbah" % "2.8.2"
)

