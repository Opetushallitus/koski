package fi.oph.koski.todistus.tiedote

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging

import scala.jdk.CollectionConverters._

class MockKituClient extends KituClient with Logging {
  @volatile private var httpStatusCode: Option[Int] = None
  private val callCounter = new java.util.concurrent.atomic.AtomicInteger(0)
  private val endpointCalls = new java.util.concurrent.ConcurrentLinkedQueue[String]()
  private val lähdejärjestelmänIdCalls = new java.util.concurrent.ConcurrentLinkedQueue[String]()
  private val opiskeluoikeusOidCalls = new java.util.concurrent.ConcurrentLinkedQueue[String]()

  def callCount: Int = callCounter.get()
  def calledEndpoints: List[String] = endpointCalls.asScala.toList
  def calledLähdejärjestelmänIds: List[String] = lähdejärjestelmänIdCalls.asScala.toList
  def calledOpiskeluoikeusOids: List[String] = opiskeluoikeusOidCalls.asScala.toList

  override def getExamineeDetails(lähdejärjestelmänId: String): Either[HttpStatus, KituExamineeDetails] = {
    callCounter.incrementAndGet()
    endpointCalls.add(KituClient.examineeDetailsEndpoint(lähdejärjestelmänId))
    lähdejärjestelmänIdCalls.add(lähdejärjestelmänId)
    logger.info(s"MockKituClient: getExamineeDetails lähdejärjestelmänId=$lähdejärjestelmänId")
    response
  }

  override def getExamineeDetailsByOpiskeluoikeusOid(opiskeluoikeusOid: String): Either[HttpStatus, KituExamineeDetails] = {
    callCounter.incrementAndGet()
    endpointCalls.add(KituClient.examineeDetailsByOpiskeluoikeusOidEndpoint(opiskeluoikeusOid))
    opiskeluoikeusOidCalls.add(opiskeluoikeusOid)
    logger.info(s"MockKituClient: getExamineeDetails opiskeluoikeusOid=$opiskeluoikeusOid")
    response
  }

  private def response: Either[HttpStatus, KituExamineeDetails] = {
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
    endpointCalls.clear()
    lähdejärjestelmänIdCalls.clear()
    opiskeluoikeusOidCalls.clear()
  }
}
