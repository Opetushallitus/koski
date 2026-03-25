package fi.oph.koski.todistus.tiedote

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.http.HttpStatus

object TiedotuspalveluClient {
  def apply(config: Config): TiedotuspalveluClient = {
    if (config.getString("tiedote.baseUrl") == "mock") {
      if (Environment.isServerEnvironment(config)) {
        throw new IllegalStateException("MockTiedotuspalveluClient ei ole sallittu palvelinympäristössä – aseta tiedote.baseUrl konfiguraatioon")
      }
      new MockTiedotuspalveluClient
    } else {
      new RemoteTiedotuspalveluClient(config)
    }
  }
}

trait TiedotuspalveluClient {
  def sendKielitutkintoTodistusTiedote(
    oppijanumero: String,
    idempotencyKey: String,
    todistusBucket: Option[String],
    todistusKey: Option[String],
    kituExamineeDetails: Option[KituExamineeDetails]
  ): Either[HttpStatus, Unit]
}
