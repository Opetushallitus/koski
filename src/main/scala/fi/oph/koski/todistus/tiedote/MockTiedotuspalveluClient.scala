package fi.oph.koski.todistus.tiedote

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging

class MockTiedotuspalveluClient extends TiedotuspalveluClient with Logging {
  @volatile var sentNotifications: List[(String, String, String, String)] = Nil

  override def sendKielitutkintoTodistusTiedote(
    oppijanumero: String,
    idempotencyKey: String,
    todistusBucket: String,
    todistusKey: String
  ): Either[HttpStatus, Unit] = {
    logger.info(s"MockTiedotuspalveluClient: sendKielitutkintoTodistusTiedote oppijanumero=$oppijanumero idempotencyKey=$idempotencyKey todistusBucket=$todistusBucket todistusKey=$todistusKey")
    synchronized {
      sentNotifications = sentNotifications :+ (oppijanumero, idempotencyKey, todistusBucket, todistusKey)
    }
    Right(())
  }

  def reset(): Unit = synchronized {
    sentNotifications = Nil
  }
}
