name := "Sudoku"
version := "0.0.1"

scalaVersion := "3.8.2"

// MUnit
libraryDependencies += "org.scalameta" %% "munit" % "1.2.2" % Test
testFrameworks += new TestFramework("munit.Framework")
