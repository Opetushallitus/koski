package fi.oph.tor.arvosana

import com.typesafe.config.Config
import fi.oph.tor.arvosana.ArviointiasteikkoRepository.{example, asteikkoFromKoodit}
import fi.oph.tor.http.Http
import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._
import fi.oph.tor.koodisto.KoodistoViittaus

trait ArviointiasteikkoRepository {
  def getArviointiasteikko(koodisto: KoodistoViittaus): Option[Arviointiasteikko]
  def getAll: List[Arviointiasteikko] = List(getArviointiasteikko(example)).flatten
}

object ArviointiasteikkoRepository {
  def apply(config: Config) = {
    if (config.hasPath("opintopolku.virkailija.url")) {
      new KoodistoArviointiasteikkoRepository(config.getString("opintopolku.virkailija.url") + "/koodisto-service")
    } else {
      new MockArviointiasteikkoRepository
    }
  }

  protected[arvosana] def asteikkoFromKoodit(koodisto: KoodistoViittaus, koodit: List[KoodistoKoodi]) = {
    Arviointiasteikko(koodisto, koodit.map(koodi => Arvosana(koodi.koodiUri, koodi.metadata.flatMap(_.nimi).headOption.getOrElse(koodi.koodiUri))).sortBy(_.id))
  }

  val example = KoodistoViittaus("ammatillisenperustutkinnonarviointiasteikko", 1) // TODO: <- get rid of fixed data
}

class MockArviointiasteikkoRepository extends ArviointiasteikkoRepository {
  override def getArviointiasteikko(koodisto: KoodistoViittaus) = {
    val koodit = Json.readFile("src/main/resources/mockdata/koodisto/ammatillisenperustutkinnonarviointiasteikko.json").extract[List[KoodistoKoodi]]
    Some(asteikkoFromKoodit(koodisto, koodit))
  }
}

class KoodistoArviointiasteikkoRepository(koodistoRoot: String) extends ArviointiasteikkoRepository {
  val http = Http()
  override def getArviointiasteikko(koodisto: KoodistoViittaus) = {
    http.apply(koodistoRoot + "/rest/codeelement/codes/" + koodisto.koodistoUri + "/" + koodisto.versio)(Http.parseJsonOptional[List[KoodistoKoodi]]).map(asteikkoFromKoodit(koodisto, _))
  }
}

case class KoodistoKoodi(koodiUri: String, metadata: List[KoodistoMetadata], versio: Int)
case class KoodistoMetadata(nimi: Option[String], lyhytNimi: Option[String], kieli: Option[String])