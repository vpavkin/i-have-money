import com.trueaccord.scalapb.{ScalaPbPlugin => Protobuf}
import org.flywaydb.sbt.FlywayPlugin._
import sbtdocker.Instructions._

lazy val buildSettings = Seq(
  organization := "ru.pavkin.ihavemoney",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.11.8"
)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Xfatal-warnings",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture"
)

lazy val baseSettings = Seq(
  scalacOptions ++= compilerOptions,
  testOptions in Test += Tests.Argument("-oF"),
  scalacOptions in(Compile, console) := compilerOptions,
  scalacOptions in(Compile, test) := compilerOptions,
  resolvers ++= Seq(
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
    Resolver.jcenterRepo,
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val allSettings = buildSettings ++ baseSettings

lazy val postgreSQLVersion = "9.4-1206-jdbc42"
lazy val funCQRSVersion = "0.4.3"
lazy val shapelessVersion = "2.3.0"
lazy val catsVersion = "0.4.1"
lazy val circeVersion = "0.3.0"
lazy val akkaVersion = "2.4.2"
lazy val akkaPersistenceJDBCVersion = "2.2.15"
lazy val scalaCheckVersion = "1.12.5"
lazy val scalaTestVersion = "2.2.6"

lazy val journal_db_host = sys.props.getOrElse("ihavemoney_writeback_db_host", "127.0.0.1")
lazy val journal_db_port = sys.props.getOrElse("ihavemoney_writeback_db_port", "5432")
lazy val journal_db_name = sys.props.getOrElse("ihavemoney_writeback_db_name", "ihavemoney-write")
lazy val journal_db_user = sys.props.getOrElse("ihavemoney_writeback_db_user", "admin")
lazy val journal_db_password = sys.props.getOrElse("ihavemoney_writeback_db_password", "changeit")

lazy val read_db_host = sys.props.getOrElse("ihavemoney_readback_db_host", "127.0.0.1")
lazy val read_db_port = sys.props.getOrElse("ihavemoney_readback_db_port", "5432")
lazy val read_db_name = sys.props.getOrElse("ihavemoney_readback_db_name", "ihavemoney-read")
lazy val read_db_user = sys.props.getOrElse("ihavemoney_readback_db_user", "admin")
lazy val read_db_password = sys.props.getOrElse("ihavemoney_readback_db_password", "changeit")

lazy val writeback_host = sys.props.getOrElse("ihavemoney_writeback_host", "127.0.0.1")
lazy val writeback_port = sys.props.getOrElse("ihavemoney_writeback_port", "9101")

lazy val readback_host = sys.props.getOrElse("ihavemoney_readback_host", "127.0.0.1")
lazy val readback_port = sys.props.getOrElse("ihavemoney_readback_port", "9201")

lazy val writefront_host = sys.props.getOrElse("ihavemoney_writefront_host", "127.0.0.1")
lazy val writefront_http_port = sys.props.getOrElse("ihavemoney_writefront_http_port", "8101")
lazy val writefront_tcp_port = sys.props.getOrElse("ihavemoney_writefront_tcp_port", "10101")

lazy val readfront_host = sys.props.getOrElse("ihavemoney_readfront_host", "127.0.0.1")
lazy val readfront_http_port = sys.props.getOrElse("ihavemoney_readfront_http_port", "8201")
lazy val readfront_tcp_port = sys.props.getOrElse("ihavemoney_readfront_tcp_port", "10201")

lazy val testDependencies = libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

lazy val protobufSettings = Protobuf.protobufSettings ++
  (Protobuf.runProtoc in Protobuf.protobufConfig := (args =>
    com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray)))

lazy val iHaveMoney = project.in(file("."))
  .settings(buildSettings)
  .aggregate(domain, serialization, writeBackend, writeFrontend, readBackend, readFrontend)
  .dependsOn(domain, serialization, writeBackend, writeFrontend, readBackend, readFrontend)

lazy val domain = project.in(file("domain"))
  .settings(
    moduleName := "domain",
    name := "domain"
  )
  .settings(allSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % shapelessVersion,
      "org.typelevel" %% "cats-core" % catsVersion,
      "io.strongtyped" %% "fun-cqrs-core" % funCQRSVersion
    )
  )
  .settings(testDependencies)

lazy val serialization = project.in(file("serialization"))
  .settings(
    moduleName := "serialization",
    name := "serialization"
  )
  .settings(allSettings: _*)
  .settings(protobufSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence" % akkaVersion
    )
  )
  .dependsOn(domain)

lazy val writeBackend = project.in(file("write-backend"))
  .settings(
    moduleName := "write-backend",
    name := "write-backend"
  )
  .settings(allSettings: _*)
  .settings(flywaySettings: _*)
  .settings(
    flywayUrl := s"jdbc:postgresql://$journal_db_host:$journal_db_port/$journal_db_name",
    flywayUser := journal_db_user,
    flywayPassword := journal_db_password,
    flywayBaselineOnMigrate := true,
    flywayLocations := Seq("filesystem:write-backend/src/main/resources/db/migrations")
  )
  .settings(
    libraryDependencies ++= Seq(
      "io.strongtyped" %% "fun-cqrs-akka" % funCQRSVersion,
      "com.github.dnvriend" %% "akka-persistence-jdbc" % akkaPersistenceJDBCVersion,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
      "com.typesafe.akka" %% "akka-distributed-data-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaVersion,
      "org.postgresql" % "postgresql" % postgreSQLVersion
    )
  )
  .settings(testDependencies)
  .settings(
    mainClass in assembly := Some("ru.pavkin.ihavemoney.writeback.Application"),
    assemblyJarName in assembly := "writeback.jar"
  )
  .enablePlugins(DockerPlugin)
  .settings(Seq(
    dockerfile in docker := {
      val artifact: File = assembly.value
      val artifactTargetPath = artifact.name
      val applicationConf = "application.conf"
      val resources = (resourceDirectory in Compile).value / applicationConf
      val entry = Seq(
        "java",
        s"-Dconfig.file=$applicationConf",
        "-jar",
        artifactTargetPath
      )
      new Dockerfile {
        from("java:8")
        env(
          "ihavemoney_writeback_host" → "127.0.0.1",
          "ihavemoney_writeback_port" → "9101",
          "ihavemoney_writeback_db_host" → "127.0.0.1",
          "ihavemoney_writeback_db_port" → "5432",
          "ihavemoney_writeback_db_name" → "ihavemoney-write",
          "ihavemoney_writeback_db_user" → "admin",
          "ihavemoney_writeback_db_password" → "changeit"
        )
        copy(artifact, artifactTargetPath)
        copy(resources, applicationConf)
        addInstruction(Raw("expose", s"$$ihavemoney_writeback_port"))
        entryPoint(entry: _*)
      }
    },
    imageNames in docker := Seq(
      ImageName(s"ihavemoney/${name.value}:latest")
    )
  ))
  .dependsOn(domain, serialization)

lazy val writeFrontend = project.in(file("write-frontend"))
  .settings(
    moduleName := "write-frontend",
    name := "write-frontend"
  )
  .settings(allSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.5.3"
    )
  )
  .settings(testDependencies)
  .settings(
    mainClass in assembly := Some("ru.pavkin.ihavemoney.writefront.Application"),
    assemblyJarName in assembly := "writefront.jar"
  )
  .enablePlugins(DockerPlugin)
  .settings(Seq(
    dockerfile in docker := {
      val artifact: File = assembly.value
      val artifactTargetPath = artifact.name
      val applicationConf = "application.conf"
      val resources = (resourceDirectory in Compile).value / applicationConf
      val entry = Seq(
        "java",
        s"-Dihavemoney_writefront_host=$writefront_host",
        s"-Dihavemoney_writefront_http_port=$writefront_http_port",
        s"-Dihavemoney_writefront_tcp_port=$writefront_tcp_port",
        s"-Dihavemoney_writeback_host=$writeback_host",
        s"-Dihavemoney_writeback_port=$writeback_port",
        s"-Dconfig.file=$applicationConf",
        "-jar",
        artifactTargetPath
      )
      new Dockerfile {
        from("java:8")
        copy(artifact, artifactTargetPath)
        copy(resources, applicationConf)
        expose(writefront_http_port.toInt)
        expose(writefront_tcp_port.toInt)
        entryPoint(entry: _*)
      }
    }))
  .dependsOn(domain, serialization)

lazy val readBackend = project.in(file("read-backend"))
  .settings(
    moduleName := "read-backend",
    name := "read-backend"
  )
  .settings(allSettings: _*)
  .settings(flywaySettings: _*)
  .settings(
    flywayUrl := s"jdbc:postgresql://$read_db_host:$read_db_port/$read_db_name",
    flywayUser := read_db_user,
    flywayPassword := read_db_password,
    flywayBaselineOnMigrate := true,
    flywayLocations := Seq("filesystem:read-backend/src/main/resources/db/migrations")
  )
  .settings(
    libraryDependencies ++= Seq(
      "io.strongtyped" %% "fun-cqrs-akka" % funCQRSVersion,
      "com.github.dnvriend" %% "akka-persistence-jdbc" % akkaPersistenceJDBCVersion,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
      "com.typesafe.akka" %% "akka-distributed-data-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaVersion,
      "org.postgresql" % "postgresql" % postgreSQLVersion
    )
  )
  .settings(testDependencies)
  .settings(
    mainClass in assembly := Some("ru.pavkin.ihavemoney.readback.Application"),
    assemblyJarName in assembly := "readback.jar"
  )
  .enablePlugins(DockerPlugin)
  .settings(Seq(
    dockerfile in docker := {
      val artifact: File = assembly.value
      val artifactTargetPath = artifact.name
      val applicationConf = "application.conf"
      val resources = (resourceDirectory in Compile).value / applicationConf
      val entry = Seq(
        "java",
        s"-Dihavemoney_writeback_db_user=$journal_db_user",
        s"-Dihavemoney_writeback_db_password=$journal_db_password",
        s"-Dihavemoney_writeback_db_host=$journal_db_host",
        s"-Dihavemoney_writeback_db_port=$journal_db_port",
        s"-Dihavemoney_writeback_db_name=$journal_db_name",
        s"-Dihavemoney_readback_db_user=$read_db_user",
        s"-Dihavemoney_readback_db_password=$read_db_password",
        s"-Dihavemoney_readback_db_host=$read_db_host",
        s"-Dihavemoney_readback_db_port=$read_db_port",
        s"-Dihavemoney_readback_db_name=$read_db_name",
        s"-Dconfig.file=$applicationConf",
        "-jar",
        artifactTargetPath
      )
      new Dockerfile {
        from("java:8")
        copy(artifact, artifactTargetPath)
        copy(resources, applicationConf)
        expose(readback_port.toInt)
        entryPoint(entry: _*)
      }
    }))
  .dependsOn(domain, serialization)

lazy val readFrontend = project.in(file("read-frontend"))
  .settings(
    moduleName := "read-frontend",
    name := "read-frontend"
  )
  .settings(allSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-remote" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.5.3"
    )
  )
  .settings(testDependencies)
  .settings(
    mainClass in assembly := Some("ru.pavkin.ihavemoney.readfront.Application"),
    assemblyJarName in assembly := "readfront.jar"
  )
  .enablePlugins(DockerPlugin)
  .settings(Seq(
    dockerfile in docker := {
      val artifact: File = assembly.value
      val artifactTargetPath = artifact.name
      val applicationConf = "application.conf"
      val resources = (resourceDirectory in Compile).value / applicationConf
      val entry = Seq(
        "java",
        s"-Dihavemoney_readfront_host=$readfront_host",
        s"-Dihavemoney_readfront_http_port=$readfront_http_port",
        s"-Dihavemoney_readfront_tcp_port=$readfront_tcp_port",
        s"-Dihavemoney_readback_host=$readback_host",
        s"-Dihavemoney_readback_port=$readback_port",
        s"-Dconfig.file=$applicationConf",
        "-jar",
        artifactTargetPath
      )
      new Dockerfile {
        from("java:8")
        copy(artifact, artifactTargetPath)
        copy(resources, applicationConf)
        expose(readfront_http_port.toInt)
        expose(readfront_tcp_port.toInt)
        entryPoint(entry: _*)
      }
    }))
  .dependsOn(domain, serialization)
