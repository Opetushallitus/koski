package fi.oph.koski.schema

import java.io.File
import fi.oph.koski.json.JsonFiles
import fi.oph.scalaschema._
import org.json4s.JsonAST.JBool
import scala.collection.immutable.LazyList

object DeserializationPerfTester extends App {

  // scala.io.StdIn.readLine("Press enter to continue (for VisualVM)")

  implicit val deserializationContext = ExtractionContext(KoskiSchema.schemaFactory).copy(validate = false)

  def doOne(filename: String) = {
    val json = JsonFiles.readFile(filename).removeField {
      case ("ignoreJsonEquality", JBool(true)) => true
      case _ => false
    }
    SchemaValidatingExtractor.extract[Oppija](json) match {
      case Left(e) => println(s"Error deserializing ${filename} -> ${e}")
      case Right(_) => // println(s"OK ${filename}")
    }
  }

  val files = new File("src/test/resources/backwardcompatibility")
    .listFiles(_.getName.endsWith(".json"))
    .toList
    .map(_.getAbsolutePath)
    .sorted
  val filesRepeating = LazyList.continually(LazyList.from(files)).flatten

  val count = 1
  var started = System.currentTimeMillis()
  var secondStarted: Long = -1
  println("Starting")
  filesRepeating.take(count).foreach { filename =>
    doOne(filename)
    if (secondStarted == -1) {
      val firstTook = System.currentTimeMillis - started
      println(s"First deserialization took $firstTook ms")
      // scala.io.StdIn.readLine("Press enter to continue (for VisualVM)")
      println("Continuing with remaining deserializations")
      secondStarted = System.currentTimeMillis
    }
  }
  val restTook = System.currentTimeMillis - secondStarted
  println(s"Remaining ${count-1} deserializations took ${restTook} ms, ${(count-1)/(restTook/1000.0)}/s")
}
