package fi.oph.koski.todistus.tiedote

import com.typesafe.config.Config
import fi.oph.koski.http.HttpStatus

object TiedotuspalveluClient {
  def apply(config: Config): TiedotuspalveluClient = {
    if (config.getString("tiedote.baseUrl") == "mock") {
      new MockTiedotuspalveluClient
    } else {
      new RemoteTiedotuspalveluClient(config)
    }
  }
}

trait TiedotuspalveluClient {
  def sendKielitutkintoTodistusTiedote(
    oppijanumero: String,
    idempotencyKey: String
  ): Either[HttpStatus, Unit]
}
