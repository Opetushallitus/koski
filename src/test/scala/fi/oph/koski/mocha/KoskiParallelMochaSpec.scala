package fi.oph.koski.mocha

import fi.oph.koski.{KoskiApplicationForTests, SharedJetty}
import fi.oph.koski.util.Files
import org.scalatest.Tag
import org.scalatest.freespec.AnyFreeSpec

trait KoskiParallelMochaSpec extends AnyFreeSpec with KoskiCommandLineSpec {
  def runnerNumber: Int
  "Mocha tests" taggedAs(Tag("parallelmocha")) in {
    val specs = SplitMochaSpecs.takeSpecsForRunner(runnerNumber)
    val sharedJetty = new SharedJetty(KoskiApplicationForTests)
    sharedJetty.start()
    //specFiles parameter makes runner.html to only load given spec.js files
    runTestCommand("mocha-chrome", Seq("scripts/mocha-chrome-test.sh", sharedJetty.baseUrl + s"/test/runner.html?specFiles=$specs"))
  }
}

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
  val numberOfRunners = 5

  def takeSpecsForRunner(number: Int)= {
    val runnerFile = Files
      .asString("web/test/runner.html").getOrElse(throw new RuntimeException("/web/test/runner.html not found"))
      .split("\n")

    val filenames = runnerFile.filter(javascriptSpecFilename).map(cleanFilename)
    val filenamesByRunnerNumber = divideSpecs(filenames)
    val filenamesForRunner = filenamesByRunnerNumber.getOrElse(number, throw new RuntimeException(s"No specs found for ParallelMochaRunner $number. Is there enough specs for every runner?"))

    println(
      s"Runner $number executing ${filenamesForRunner.size} specs\n" +
      filenamesForRunner.mkString("\n")
    )

    filenamesForRunner.mkString(",")
  }

  private def divideSpecs(specs: Seq[String]) = specs
    .zipWithIndex.map { case (spec, index) => (index % numberOfRunners, spec)}
    .groupBy(_._1)
    .mapValues(_.map(_._2))

  private def javascriptSpecFilename(str: String) = str.contains("spec/") && str.contains(".js")
  private def cleanFilename(str: String) = str.trim.replace(",", "").replace("\"","")
}
