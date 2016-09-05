package fi.oph.koski.perftest

import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.schema.Opiskeluoikeus
import fi.oph.koski.tiedonsiirto.ExamplesTiedonsiirto
import org.scalatra.test.ClientResponse

import scala.util.Random

object TiedonsiirtoFixtureDataInserter extends FixtureDataInserter {
  lazy val opiskeluoikeudet = List.fill(3)(List(ExamplesTiedonsiirto.opiskeluoikeus, ExamplesTiedonsiirto.failingOpiskeluoikeus)).flatten
  def opiskeluoikeudet(x: Int) = Random.shuffle(opiskeluoikeudet)
  override def defaultUser = MockUsers.stadinAmmattiopistoPalvelukäyttäjä
  override def handleResponse(response: ClientResponse, oikeus: Opiskeluoikeus) = {
    if(response.status != 200 && response.status != 403) {
      super.handleResponse(response, oikeus)
    }
  }
}
