import org.scalajs.linker.interface.ModuleSplitStyle

val sbtPlugin = true
val scala3Version = "3.3.1"
val http4sVersion = "0.23.24"
val circeVersion = "0.14.6"

lazy val root = project
  .in(file("."))
  .settings(
    name := "fullstack-scala3",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
  )
  .aggregate(server, client)

lazy val server = project // Step 1: We create this block
  .in(file("./server"))
  .settings(
    name := "server",
    Compile / run / fork := true,
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.slf4j" % "slf4j-simple" % "2.0.9",
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )

lazy val client = project
  .in(file("./client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := scala3Version,
    Compile / run / fork := true,
    // Tell Scala.js that this is an application with a main method
    scalaJSUseMainModuleInitializer := true,

    /* Configure Scala.js to emit modules in the optimal way to
     * connect to Vite's incremental reload.
     * - emit ECMAScript modules
     * - emit as many small modules as possible for classes in the "client" package
     * - emit as few (large) modules as possible for all other classes
     *   (in particular, for the standard library)
     */
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("client"))
        )
    },

    /* Depend on the scalajs-dom library.
     * It provides static types for the browser DOM APIs.
     */
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.4.0",
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion
    )
  )