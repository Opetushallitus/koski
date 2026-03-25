package fi.oph.koski.todistus.tiedote

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging

class MockKituClient extends KituClient with Logging {
  @volatile private var httpStatusCode: Option[Int] = None
  private val callCounter = new java.util.concurrent.atomic.AtomicInteger(0)

  def callCount: Int = callCounter.get()

  override def getExamineeDetails(opiskeluoikeusOid: String): Either[HttpStatus, KituExamineeDetails] = {
    callCounter.incrementAndGet()
    logger.info(s"MockKituClient: getExamineeDetails opiskeluoikeusOid=$opiskeluoikeusOid")
    httpStatusCode match {
      case Some(status) =>
        Left(KoskiErrorCategory.unavailable(s"Kitu-kutsu epäonnistui: $status"))
      case None =>
        Right(KituExamineeDetails(
          sukunimi = "Meikäläinen",
          etunimet = "Matti Johannes",
          katuosoite = Some("Esimerkkikatu 123"),
          postinumero = Some("00100"),
          postitoimipaikka = Some("Helsinki"),
          maa = Some(KituKoodiarvo("FIN", "maatjavaltiot1")),
          email = Some("matti.meikalainen@example.com"),
          todistuskieli = Some(KituKoodiarvo("FI", "kieli"))
        ))
    }
  }

  def respondWithHttpStatus(status: Int): Unit = {
    httpStatusCode = Some(status)
  }

  def reset(): Unit = {
    httpStatusCode = None
    callCounter.set(0)
  }
}
