package fi.oph.koski.henkilo

import java.time.{Instant, LocalDateTime, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter

import fi.oph.koski.henkilo.Hetu.checkChars

import scala.util.Random

object HetuGenerator {
  private val hetuFormatter = DateTimeFormatter.ofPattern("ddMMyy")

  def generateHetu = {
    val date = hetuFormatter.format(randomDate)
    val randomPart = s"${Random.nextInt(10)}${Random.nextInt(10)}${Random.nextInt(10)}"
    s"$date-$randomPart${checkChars(Math.round((s"$date$randomPart".toInt / 31.0 % 1) * 31).toInt)}"
  }

  private def randomDate = {
    val beforeY2K = ZonedDateTime.of(1999, 12, 31, 0, 0, 0, 0, ZoneId.systemDefault).toInstant.toEpochMilli
    def randomInstant = (Random.nextDouble * beforeY2K).toLong
    LocalDateTime.ofInstant(Instant.ofEpochMilli(randomInstant), ZoneId.systemDefault)
  }
}
