package fi.oph.koski.mocha

import fi.oph.koski.api.SharedJetty
import fi.oph.koski.util.Files
import org.scalatest.Tag


trait KoskiParallelMochaSpec extends KoskiCommandLineSpec {
  def runnerNumber: Int
  "Mocha tests" taggedAs(ParallelMochaTag) in {
    SplitMochaSpecs.takeSpecsForRunner(runnerNumber)
    SharedJetty.start
    runTestCommand("mocha-chrome", Seq("scripts/mocha-chrome-test.sh", SharedJetty.baseUrl + "/test/runner.html"))
  }
}

object ParallelMochaTag extends Tag("parallelmocha")

class KoskiMochaSpecRunner0 extends KoskiParallelMochaSpec {
  def runnerNumber: Int = 0
}

class KoskiMochaSpecRunner1 extends KoskiParallelMochaSpec {
  def runnerNumber: Int = 1
}

class KoskiMochaSpecRunner2 extends KoskiParallelMochaSpec {
  def runnerNumber: Int = 2
}

class KoskiMochaSpecRunner3 extends KoskiParallelMochaSpec {
  def runnerNumber: Int = 3
}

class KoskiMochaSpecRunner4 extends KoskiParallelMochaSpec {
  def runnerNumber: Int = 4
}

object SplitMochaSpecs {
  val numberOfRunners = 4 //indexing starts from 0

  def takeSpecsForRunner(number: Int)= {
    val runnerFile = Files
      .asString("web/test/runner.html").getOrElse(throw new RuntimeException("/web/test/runner.html not found"))
      .split("\n")

    val divided = divideSpecs(runnerFile.filter(specFiles))
    val specsForRunner = divided.get(number).getOrElse(throw new RuntimeException(s"Did not found any spec for runner number $number"))

    println(divided)

    val newFileContent = runnerFile
      .filterNot(specFiles)
      .flatMap(insertSpecFiles(specsForRunner))
      .mkString("\n")

    Files.writeFile("web/test/runner.html", newFileContent)
  }

  private def divideSpecs(specs: Seq[String]) = specs
    .sorted
    .zipWithIndex.map { case (spec, index) => (index % numberOfRunners, spec)}
    .groupBy(_._1)
    .mapValues(_.map(_._2))

  private def specFiles(s: String) = s.startsWith("<script src=\"spec/")

  private def insertSpecFiles(importSpecFiles: Seq[String])(s: String) = {
    if (s.contains("insert specs here")) importSpecFiles else List(s)
  }
}

