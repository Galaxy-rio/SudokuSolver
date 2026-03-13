import xsbt.trace.enable
name := "libsudoku"
version := "0.0.1"

scalaVersion := "3.8.2"

Compile / compileOrder := CompileOrder.ScalaThenJava
javacOptions ++= Seq(
    "-g",
    "-deprecation",
)

// MUnit
libraryDependencies += "org.scalameta" %% "munit" % "1.2.2" % Test
testFrameworks += new TestFramework("munit.Framework")

enablePlugins(AssemblyPlugin)
