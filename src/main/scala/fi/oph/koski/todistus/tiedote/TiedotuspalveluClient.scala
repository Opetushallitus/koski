package fi.oph.koski.todistus.tiedote

import fi.oph.koski.http.HttpStatus

trait TiedotuspalveluClient {
  def sendKielitutkintoTodistusTiedote(
    oppijanumero: String,
    idempotencyKey: String
  ): Either[HttpStatus, Unit]
}
