import com.trueaccord.scalapb.{ScalaPbPlugin => Protobuf}

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
lazy val akkaHttpCorsVersion = "0.1.1"
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
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "io.strongtyped" %% "fun-cqrs-test-kit" % funCQRSVersion % "test"
)

lazy val protobufSettings = Protobuf.protobufSettings ++
  (Protobuf.runProtoc in Protobuf.protobufConfig := (args =>
    com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray)))

lazy val iHaveMoney = project.in(file("."))
  .settings(buildSettings)
  .aggregate(domainJVM, serialization, writeBackend, writeFrontend, readBackend, frontendProtocolJVM, readFrontend, jsApp)

lazy val domain = crossProject.in(file("domain"))
  .settings(
    moduleName := "domain",
    name := "domain"
  )
  .settings(allSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.chuusai" %%% "shapeless" % shapelessVersion,
      "org.typelevel" %%% "cats-core" % catsVersion
    )
  )
  .jvmSettings(libraryDependencies ++= Seq(
    "io.strongtyped" %% "fun-cqrs-core" % funCQRSVersion
  ))
  .jvmSettings(testDependencies)

lazy val domainJVM = domain.jvm
lazy val domainJS = domain.js

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
  .dependsOn(domainJVM)

lazy val writeBackend = project.in(file("write-backend"))
  .settings(
    moduleName := "write-backend",
    name := "write-backend"
  )
  .settings(allSettings: _*)
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
    mainClass in assembly := Some("ru.pavkin.ihavemoney.writeback.WriteBackend"),
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
  .dependsOn(domainJVM, serialization)

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
      "ch.megard" %% "akka-http-cors" % akkaHttpCorsVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.5.3"
    )
  )
  .settings(testDependencies)
  .settings(
    mainClass in assembly := Some("ru.pavkin.ihavemoney.writefront.WriteFrontend"),
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
        s"-Dconfig.file=$applicationConf",
        "-jar",
        artifactTargetPath
      )
      new Dockerfile {
        from("java:8")
        env(
          "ihavemoney_writeback_host" → "127.0.0.1",
          "ihavemoney_writeback_port" → "9101",
          "ihavemoney_writefront_host" → "127.0.0.1",
          "ihavemoney_writefront_http_port" → "8101",
          "ihavemoney_writefront_tcp_port" → "10101"
        )
        copy(artifact, artifactTargetPath)
        copy(resources, applicationConf)
        addInstruction(Raw("expose", s"$$ihavemoney_writefront_tcp_port"))
        addInstruction(Raw("expose", s"$$ihavemoney_writefront_http_port"))
        entryPoint(entry: _*)
      }
    },
    imageNames in docker := Seq(
      ImageName(s"ihavemoney/${name.value}:latest")
    )
  ))
  .dependsOn(domainJVM, frontendProtocolJVM, serialization)

lazy val readBackend = project.in(file("read-backend"))
  .settings(
    moduleName := "read-backend",
    name := "read-backend"
  )
  .settings(allSettings: _*)
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
    mainClass in assembly := Some("ru.pavkin.ihavemoney.readback.ReadBackend"),
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
        s"-Dconfig.file=$applicationConf",
        "-jar",
        artifactTargetPath
      )
      new Dockerfile {
        from("java:8")
        env(
          "ihavemoney_writeback_db_user" → "admin",
          "ihavemoney_writeback_db_password" → "changeit",
          "ihavemoney_writeback_db_host" → "127.0.0.1",
          "ihavemoney_writeback_db_port" → "5432",
          "ihavemoney_writeback_db_name" → "ihavemoney-write",
          "ihavemoney_readback_db_user" → "admin",
          "ihavemoney_readback_db_password" → "changeit",
          "ihavemoney_readback_db_host" → "127.0.0.1",
          "ihavemoney_readback_db_port" → "5432",
          "ihavemoney_readback_db_name" → "ihavemoney-read",
          "ihavemoney_readback_host" → "127.0.0.1",
          "ihavemoney_readback_port" → "9201"
        )
        copy(artifact, artifactTargetPath)
        copy(resources, applicationConf)
        addInstruction(Raw("expose", s"$$ihavemoney_readback_port"))
        entryPoint(entry: _*)
      }
    },
    imageNames in docker := Seq(
      ImageName(s"ihavemoney/${name.value}:latest")
    )
  ))
  .dependsOn(domainJVM, serialization)

lazy val frontendProtocol = crossProject.in(file("frontend-protocol"))
  .settings(
    moduleName := "frontend-protocol",
    name := "frontend-protocol"
  )
  .settings(allSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "io.circe" %%% "circe-core" % circeVersion,
    "io.circe" %%% "circe-generic" % circeVersion,
    "io.circe" %%% "circe-parser" % circeVersion
  ))
  .dependsOn(domain)

lazy val frontendProtocolJS = frontendProtocol.js
lazy val frontendProtocolJVM = frontendProtocol.jvm

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
      "ch.megard" %% "akka-http-cors" % akkaHttpCorsVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.5.3"
    ),
    testDependencies
  )
  .settings(
    mainClass in assembly := Some("ru.pavkin.ihavemoney.readfront.ReadFrontend"),
    assemblyJarName in assembly := "readfront.jar"
  )
  .enablePlugins(DockerPlugin)
  .settings(Seq(
    dockerfile in docker := {
      val artifact: File = assembly.value
      val artifactTargetPath = artifact.name
      val applicationConf = "application.conf"
      val resourceFiles = (resources in Compile).value
      val entry = Seq(
        "java",
        s"-Dconfig.file=$applicationConf",
        "-jar",
        artifactTargetPath
      )
      new Dockerfile {
        from("java:8")
        env(
          "ihavemoney_readback_host" → "127.0.0.1",
          "ihavemoney_readback_port" → "9201",
          "ihavemoney_readfront_host" → "127.0.0.1",
          "ihavemoney_readfront_http_port" → "8201",
          "ihavemoney_readfront_tcp_port" → "10201",
          "ihavemoney_writefront_host" → "127.0.0.1",
          "ihavemoney_writefront_port" → "8101"
        )
        copy(artifact, artifactTargetPath)
        resourceFiles.foreach(copy(_, "/"))
        addInstruction(Raw("expose", s"$$ihavemoney_readfront_tcp_port"))
        addInstruction(Raw("expose", s"$$ihavemoney_readfront_http_port"))
        entryPoint(entry: _*)
      }
    },
    imageNames in docker := Seq(
      ImageName(s"ihavemoney/${name.value}:latest")
    )
  ))
  .settings(
    (resourceGenerators in Compile) +=
      (packageJSDependencies in Compile in jsApp,
        fastOptJS in Compile in jsApp,
        packageScalaJSLauncher in Compile in jsApp)
        .map((f1, f2, f3) => Seq(f1, f2.data, f3.data)).taskValue
  )
  .dependsOn(domainJVM, serialization, frontendProtocolJVM)

lazy val jsApp = project.in(file("js-app"))
  .settings(
    moduleName := "js-app",
    name := "js-app"
  )
  .settings(allSettings: _*)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % "0.11.0",
      "com.github.japgolly.scalajs-react" %%% "extra" % "0.11.0",
      "com.github.japgolly.scalacss" %%% "core" % "0.4.1",
      "com.github.japgolly.scalacss" %%% "ext-react" % "0.4.1",
      "org.querki" %%% "jquery-facade" % "1.0-RC3",
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion
    ),
    jsDependencies ++= Seq(
      "org.webjars.bower" % "react" % "15.0.1" / "react-with-addons.js"
        minified "react-with-addons.min.js"
        commonJSName "React",

      "org.webjars.bower" % "react" % "15.0.1" / "react-dom.js"
        minified "react-dom.min.js"
        dependsOn "react-with-addons.js"
        commonJSName "ReactDOM",

      "org.webjars" % "jquery" % "2.2.3" / "jquery.js"
        minified "jquery.min.js",

      "org.webjars" % "bootstrap" % "3.3.6" / "bootstrap.js"
        minified "bootstrap.min.js"
        dependsOn "jquery.js"
    ),
    persistLauncher in Compile := true
  )
  .dependsOn(frontendProtocolJS)

lazy val tests = project.in(file("tests"))
  .settings(
    description := "Tests",
    name := "tests"
  )
  .settings(allSettings: _*)
  .settings(testDependencies)
  .settings(
    fork := true
  )
  .dependsOn(
    domainJVM, serialization, writeBackend, writeFrontend, readBackend, frontendProtocolJVM, readFrontend
  )
