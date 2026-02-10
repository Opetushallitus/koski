package fi.oph.koski.todistus.tiedote

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging

class MockTiedotuspalveluClient extends TiedotuspalveluClient with Logging {
  @volatile var sentNotifications: List[(String, String)] = Nil

  override def sendKielitutkintoTodistusTiedote(
    oppijanumero: String,
    idempotencyKey: String
  ): Either[HttpStatus, Unit] = {
    logger.info(s"MockTiedotuspalveluClient: sendKielitutkintoTodistusTiedote oppijanumero=$oppijanumero idempotencyKey=$idempotencyKey")
    synchronized {
      sentNotifications = sentNotifications :+ (oppijanumero, idempotencyKey)
    }
    Right(())
  }

  def reset(): Unit = synchronized {
    sentNotifications = Nil
  }
}
