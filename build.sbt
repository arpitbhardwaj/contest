ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.7"

lazy val akkaVersion = "2.6.18"
lazy val slickVersion = "3.3.3"
lazy val akkaHttpVersion = "10.2.6"

lazy val commonSettings = Seq(
  //scalaVersion := "2.13.0",

  libraryDependencies ++= Seq(
    "org.scalactic"            %% "scalactic"                 % "3.2.9",
    "ch.qos.logback"            % "logback-classic"           % "1.2.10",
    "org.scalatest"            %% "scalatest"                 % "3.2.9"               % Test,
    "com.softwaremill.macwire" %% "macros"                    % "2.5.2"               % Provided
  )
)

lazy val databaseDependencies = Seq(
  "com.typesafe.slick"         %% "slick"                     % slickVersion,
  "com.typesafe.slick"         %% "slick-hikaricp"            % slickVersion,
  "org.postgresql"              % "postgresql"                % "42.3.1",
)

lazy val akkaDependencies = Seq(
  "com.typesafe.akka"          %% "akka-slf4j"                % akkaVersion,
  "com.typesafe.akka"          %% "akka-stream"               % akkaVersion,
  "com.typesafe.akka"          %% "akka-actor-typed"          % akkaVersion,
  "com.typesafe.akka"          %% "akka-http"                 % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-http-core"            % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-http-spray-json"      % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-stream-testkit"       % akkaVersion           % Test,
  "com.typesafe.akka"          %% "akka-http-testkit"         % akkaHttpVersion       % Test,
  "com.typesafe.akka"          %% "akka-actor-testkit-typed"  % "2.6.18"               % Test,
)

lazy val userService = (project in file("userService"))
  .settings(
    name := "user_service",
    commonSettings,
    libraryDependencies ++= akkaDependencies ++ databaseDependencies
  )

lazy val root = (project in file("."))
  .settings(
    name := "contest",
    idePackagePrefix := Some("com.ab")
  )