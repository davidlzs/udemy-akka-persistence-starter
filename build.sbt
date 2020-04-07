name := "udemy-akka-persistence-starter"

version := "0.1"

scalaVersion := "2.12.7"
lazy val akkaVersion = "2.6.4" // must be 2.5.13 so that it's compatible with the stores plugins (JDBC and Cassandra)
lazy val leveldbVersion = "0.7"
lazy val leveldbjniVersion = "1.8"
lazy val postgresVersion = "42.2.2"
lazy val cassandraVersion = "1.0.0-RC1"
lazy val protobufVersion = "3.6.1"
lazy val akkaHttpVersion = "10.1.10"

// some libs are available in Bintray's JCenter
resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(

  // HTTP
  "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,

  "com.typesafe.akka"          %% "akka-actor"       % akkaVersion,
  "com.typesafe.akka"          %% "akka-persistence" % akkaVersion,

  "com.typesafe.akka"          %% "akka-actor-typed" % akkaVersion,

  // local levelDB stores
  "org.iq80.leveldb"            % "leveldb"          % leveldbVersion,
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % leveldbjniVersion,

  // JDBC with PostgreSQL 
  "org.postgresql" % "postgresql" % postgresVersion,
  "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.4.0",

  // Cassandra
  "com.typesafe.akka" %% "akka-persistence-cassandra" % cassandraVersion,
  "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % cassandraVersion % Test,

  // Google Protocol Buffers
  "com.google.protobuf" % "protobuf-java"  % protobufVersion,

  // Play
  "com.typesafe.play" %% "play" % "2.8.0",

  // scala logging
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.slf4j"      %  "slf4j-api"       %  "1.7.28",
  "ch.qos.logback" %  "logback-classic" % "1.2.3",

  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion

)
