package fi.oph.koski.mocha

import fi.oph.koski.{KoskiApplicationForTests, SharedJetty}
import fi.oph.koski.util.Files
import org.scalatest.Tag
import org.scalatest.freespec.AnyFreeSpec

class KoskiParallelMochaSpec extends AnyFreeSpec with KoskiCommandLineSpec {
  // Indeksointi lähtee nollasta
  def shardIndex: Int = Integer.parseInt(scala.util.Properties.envOrElse("MOCHA_SHARD_INDEX", "1")) - 1
  "Mocha tests" taggedAs(Tag("parallelmocha")) in {
    val specs = SplitMochaSpecs.takeSpecsForRunner(shardIndex)
    val sharedJetty = new SharedJetty(KoskiApplicationForTests)
    sharedJetty.start()
    // Sleep on workaround CI:llä silloin tällöin esiintyvään "Error: No inspectable targets"-ongelmaan.
    val sleepTimeInMs = 2000
    logger.info(s"Sleeping ${sleepTimeInMs} ms to wait for everything to be ready before starting the tests")
    Thread.sleep(sleepTimeInMs)
    //specFiles parameter makes runner.html to only load given spec.js files
    runTestCommand("mocha-chrome", Seq("scripts/mocha-chrome-test.sh", sharedJetty.baseUrl + s"/test/runner.html?specFiles=$specs"))
  }
}

object SplitMochaSpecs {
  val numberOfRunners = Integer.parseInt(scala.util.Properties.envOrElse("MOCHA_SHARD_TOTAL", "1"))

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
