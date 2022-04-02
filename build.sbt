ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.7"

lazy val akkaVersion = "2.6.18"
lazy val slickVersion = "3.3.3"
lazy val akkaHttpVersion = "10.2.6"
lazy val leveldbVersion = "0.12"
lazy val leveldbjniVersion = "1.8"
lazy val postgresVersion = "42.2.2"
lazy val cassandraVersion = "0.91"
lazy val json4sVersion = "3.2.11"
lazy val protobufVersion = "3.6.1"

// some libs are available in Bintray's JCenter
//resolvers += Resolver.jcenterRepo

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalactic"            %% "scalactic"                 % "3.2.9",
    "ch.qos.logback"            % "logback-classic"           % "1.2.10",
    "org.scalatest"            %% "scalatest"                 % "3.2.9"               % Test,
    "com.softwaremill.macwire" %% "macros"                    % "2.5.2"               % Provided
  )
)
lazy val rootDependencies = Seq(
  //for core akka
  "com.typesafe.akka"          %% "akka-actor"                % akkaVersion,
  "com.typesafe.akka"          %% "akka-testkit"              % akkaVersion,

  //for akka streams
  "com.typesafe.akka"          %% "akka-stream"               % akkaVersion,
  "com.typesafe.akka"          %% "akka-stream-testkit"       % akkaVersion           % Test,

  // for akka remoting
  "com.typesafe.akka"          %% "akka-remote"               % akkaVersion,

  //for udp aeron
  //"io.aeron" % "aeron-driver" % "1.37.0",
  //"io.aeron" % "aeron-client" % "1.37.0",

  // for akka clustering
  "com.typesafe.akka"          %% "akka-cluster"              % akkaVersion,
  "com.typesafe.akka"          %% "akka-cluster-sharding"     % akkaVersion,
  "com.typesafe.akka"          %% "akka-cluster-tools"        % akkaVersion,

  //for akka persistence
  "com.typesafe.akka"          %% "akka-persistence"          % akkaVersion,

  // local levelDB stores
  "org.iq80.leveldb"            % "leveldb"                   % leveldbVersion,
  "org.fusesource.leveldbjni"   % "leveldbjni-all"            % leveldbjniVersion,

  /*
  // JDBC with PostgreSQL
  "org.postgresql" % "postgresql" % postgresVersion,
  "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.4.0",

  // Cassandra
  "com.typesafe.akka" %% "akka-persistence-cassandra" % cassandraVersion,
  "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % cassandraVersion % Test,
  */

  // Google Protocol Buffers
  "com.google.protobuf" % "protobuf-java"  % protobufVersion,

  "org.scalatest"              %% "scalatest"                 % "3.2.9"               % Test
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

lazy val contestService = (project in file("contestService"))
  .settings(
    name := "contest_service",
    commonSettings,
    libraryDependencies ++= akkaDependencies ++ databaseDependencies
  )

lazy val root = (project in file("."))
  .settings(
    name := "contest",
    idePackagePrefix := Some("com.ab"),
    libraryDependencies ++= rootDependencies

  )
