resolvers += "Flyway" at "http://flywaydb.org/repo"

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "3.2.1")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.3.0")
addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.24")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")

libraryDependencies ++= Seq(
  "com.github.os72" % "protoc-jar" % "3.0.0-b2.1"
)
