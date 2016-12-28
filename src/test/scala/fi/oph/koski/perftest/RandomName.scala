package fi.oph.koski.perftest

import scala.io.Source
import scala.util.Random

object RandomName {
  private def randomLines(filename: String) = Random.shuffle(Source.fromFile(filename).getLines().toList)
  private val lastNames = randomLines("src/test/resources/sukunimet.txt")
  private val firstNames = randomLines("src/test/resources/etunimet.txt")
  private var index = 0;
  private def nextFrom(list: List[String]) = this.synchronized {
    index = index + 1
    list.apply(index % list.length)
  }

  def randomFirstName = nextFrom(firstNames)
  def randomLastName = nextFrom(lastNames)
}
