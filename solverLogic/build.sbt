name := "Sudoku"
version := "0.0.1"

scalaVersion := "3.8.2"

// MUnit
libraryDependencies += "org.scalameta" %% "munit" % "1.2.2" % Test
testFrameworks += new TestFramework("munit.Framework")

Compile / unmanagedSourceDirectories ++= Seq(
  (Compile / sourceDirectory).value / "java",
  (Compile / sourceDirectory).value / "scala"
)
Compile / compile := (Compile / compile).dependsOn(Compile / compile).value
Compile / classDirectory := target.value / "classes"
