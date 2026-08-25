package fi.oph.koski.todistus.tiedote

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging

case class SentTiedote(
  oppijanumero: String,
  opiskeluoikeusOid: String,
  idempotencyKey: String,
  todistusBucket: Option[String],
  todistusKey: Option[String],
  kituExamineeDetails: Option[KituExamineeDetails]
)

class MockTiedotuspalveluClient extends TiedotuspalveluClient with Logging {
  /** Onnistuneet lähetykset. */
  @volatile var sentNotifications: List[SentTiedote] = Nil

  /** Kaikki yritykset, myös epäonnistuneet. */
  @volatile var attemptedNotifications: List[SentTiedote] = Nil

  /** Testeille: epäonnistuta N seuraavaa kutsua, jotta job-tason uudelleenyrityspolku voidaan kattaa. */
  @volatile var failNextN: Int = 0

  override def sendKielitutkintoTodistusTiedote(
    oppijanumero: String,
    opiskeluoikeusOid: String,
    idempotencyKey: String,
    todistusBucket: Option[String],
    todistusKey: Option[String],
    kituExamineeDetails: Option[KituExamineeDetails]
  ): Either[HttpStatus, Unit] = {
    logger.info(
      "MockTiedotuspalveluClient: sendKielitutkintoTodistusTiedote " +
        s"oppijanumero=$oppijanumero opiskeluoikeusOid=$opiskeluoikeusOid idempotencyKey=$idempotencyKey " +
        s"todistusBucket=$todistusBucket todistusKey=$todistusKey"
    )
    val tiedote = SentTiedote(
      oppijanumero,
      opiskeluoikeusOid,
      idempotencyKey,
      todistusBucket,
      todistusKey,
      kituExamineeDetails
    )
    synchronized {
      attemptedNotifications = attemptedNotifications :+ tiedote
      if (failNextN > 0) {
        failNextN -= 1
        Left(KoskiErrorCategory.unavailable("MockTiedotuspalveluClient: pyydetty virhe"))
      } else {
        sentNotifications = sentNotifications :+ tiedote
        Right(())
      }
    }
  }

  def reset(): Unit = synchronized {
    sentNotifications = Nil
    attemptedNotifications = Nil
    failNextN = 0
  }
}
