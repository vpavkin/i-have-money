import com.trueaccord.scalapb.{ScalaPbPlugin => Protobuf}
import org.flywaydb.sbt.FlywayPlugin._

lazy val buildSettings = Seq(
  organization := "ru.pavkin.ihavemoney",
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

lazy val db_host = sys.props.getOrElse("ihavemoney.writeback.db.host", "127.0.0.1")
lazy val db_port = sys.props.getOrElse("ihavemoney.writeback.db.port", "5432")
lazy val db_name = sys.props.getOrElse("ihavemoney.writeback.db.name", "ihavemoney")
lazy val db_user = sys.props.getOrElse("ihavemoney.writeback.db.user", "admin")
lazy val db_password = sys.props.getOrElse("ihavemoney.writeback.db.password", "changeit")

lazy val testDependencies = libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

lazy val protobufSettings = Protobuf.protobufSettings ++
  (Protobuf.runProtoc in Protobuf.protobufConfig := (args =>
    com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray)))

lazy val iHaveMoney = project.in(file("."))
  .settings(buildSettings)
  .aggregate(domain, writeBackend)
  .dependsOn(domain, writeBackend)

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

lazy val writeBackend = project.in(file("write-backend"))
  .settings(
    moduleName := "write-backend",
    name := "write-backend"
  )
  .settings(allSettings: _*)
  .settings(protobufSettings: _*)
  .settings(flywaySettings: _*)
  .settings(
    flywayUrl := s"jdbc:postgresql://$db_host:$db_port/$db_name",
    flywayUser := db_user,
    flywayPassword := db_password,
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
  .settings(mainClass in assembly := Some("ru.pavkin.ihavemoney.writeback.Application"))
  .enablePlugins(DockerPlugin)
  .settings(Seq(
    dockerfile in docker := {
      val artifact: File = assembly.value
      val artifactTargetPath = artifact.name
      val applicationConf = "application.conf"
      val resources = (resourceDirectory in Compile).value / applicationConf
      val entry = Seq(
        "java",
        s"-Dihavemoney.writeback.db.user=$db_user",
        s"-Dihavemoney.writeback.db.password=$db_password",
        s"-Dihavemoney.writeback.db.host=$db_host",
        s"-Dihavemoney.writeback.db.port=$db_port",
        s"-Dihavemoney.writeback.db.name=$db_name",
        s"-Dconfig.file=$applicationConf",
        "-jar",
        artifactTargetPath
      )
      new Dockerfile {
        from("java:8")
        add(artifact, artifactTargetPath)
        add(resources, applicationConf)
        entryPoint(entry: _*)
      }
    }))
  .dependsOn(domain)
