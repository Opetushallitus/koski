package fi.oph.koski.todistus.tiedote

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging

case class SentTiedote(
  oppijanumero: String,
  idempotencyKey: String,
  todistusBucket: Option[String],
  todistusKey: Option[String],
  kituExamineeDetails: Option[KituExamineeDetails]
)

class MockTiedotuspalveluClient extends TiedotuspalveluClient with Logging {
  @volatile var sentNotifications: List[SentTiedote] = Nil

  override def sendKielitutkintoTodistusTiedote(
    oppijanumero: String,
    idempotencyKey: String,
    todistusBucket: Option[String],
    todistusKey: Option[String],
    kituExamineeDetails: Option[KituExamineeDetails]
  ): Either[HttpStatus, Unit] = {
    logger.info(s"MockTiedotuspalveluClient: sendKielitutkintoTodistusTiedote oppijanumero=$oppijanumero idempotencyKey=$idempotencyKey todistusBucket=$todistusBucket todistusKey=$todistusKey")
    synchronized {
      sentNotifications = sentNotifications :+ SentTiedote(oppijanumero, idempotencyKey, todistusBucket, todistusKey, kituExamineeDetails)
    }
    Right(())
  }

  def reset(): Unit = synchronized {
    sentNotifications = Nil
  }
}
