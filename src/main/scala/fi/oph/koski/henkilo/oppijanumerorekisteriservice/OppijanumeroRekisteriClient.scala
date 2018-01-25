package fi.oph.koski.henkilo.oppijanumerorekisteriservice

import java.time.LocalDate

import com.typesafe.config.Config
import fi.oph.koski.henkilo.RemoteOpintopolkuHenkilöFacade
import fi.oph.koski.henkilo.kayttooikeusservice.Käyttäjätiedot
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpStatusException, KoskiErrorCategory, VirkailijaHttpClient}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.TäydellisetHenkilötiedot

import scalaz.concurrent.Task

case class OppijanumeroRekisteriClient(config: Config) {
  def findOrCreate(createUserInfo: UusiHenkilö) = oidServiceHttp.post(uri"/oppijanumerorekisteri-service/s2s/findOrCreateHenkiloPerustieto", createUserInfo)(json4sEncoderOf[UusiHenkilö]) {
    case (x, data, _) if x <= 201 => Right(JsonSerializer.parse[OppijaNumerorekisteriOppija](data, ignoreExtras = true).toOppijaHenkilö)
    case (400, error, _) => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.virheelliset(error))
    case (status, text, uri) => throw HttpStatusException(status, text, uri)
  }

  private val oidServiceHttp = VirkailijaHttpClient(RemoteOpintopolkuHenkilöFacade.makeServiceConfig(config), "/oppijanumerorekisteri-service", config.getBoolean("authentication-service.useCas"))

  def findKäyttäjäByOid(oid: String) = oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/$oid")(Http.parseJsonOptional[KäyttäjäHenkilö])

  def findYhteystiedot(oid: String) = oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/${oid}/yhteystiedot/yhteystietotyyppi2")(Http.parseJson[Yhteystiedot])
  def findOppijatByOids(oids: List[Oid]): Task[List[OppijaHenkilö]] =
    oidServiceHttp.post(uri"/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloOidList", oids)(json4sEncoderOf[List[String]])(Http.parseJson[List[OppijaNumerorekisteriOppija]]).map(_.map(_.toOppijaHenkilö))

  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): Task[List[Oid]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/s2s/changedSince/$since?offset=$offset&amount=$amount")(Http.parseJson[List[String]])

  def findOppijaByHetu(hetu: String): Task[Option[OppijaHenkilö]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/hetu=$hetu")(Http.parseJsonOptional[OppijaNumerorekisteriOppija]).map(_.map(_.toOppijaHenkilö))

  def findMasterOppija(oid: String): Task[Option[OppijaHenkilö]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/$oid/master")(Http.parseJsonOptional[OppijaNumerorekisteriOppija]).map(_.map(_.toOppijaHenkilö))
}

case class KäyttäjäHenkilö(oidHenkilo: String, sukunimi: String, etunimet: String, kutsumanimi: String, kayttajatiedot: Option[Käyttäjätiedot], asiointiKieli: Option[Kieli])
case class OppijaNumerorekisteriOppija(oidHenkilo: String, sukunimi: String, etunimet: String, kutsumanimi: String, hetu: Option[String], syntymaaika: Option[LocalDate], aidinkieli: Option[Kieli], kansalaisuus: Option[List[Kansalaisuus]], modified: Long) {
  def toOppijaHenkilö = OppijaHenkilö(oidHenkilo, sukunimi, etunimet, kutsumanimi, hetu, syntymaaika, aidinkieli.map(_.kieliKoodi), kansalaisuus.map(_.map(_.kansalaisuusKoodi)), modified)
}
case class UusiHenkilö(hetu: Option[String], sukunimi: String, etunimet: String, kutsumanimi: String, henkiloTyyppi: String, kayttajatiedot: Option[Käyttäjätiedot])
object UusiHenkilö {
  def palvelu(nimi: String) = UusiHenkilö(None, nimi, "_", "_", "PALVELU", Some(Käyttäjätiedot(Some(nimi))))
  def oppija(hetu: Option[String], sukunimi: String, etunimet: String, kutsumanimi: String) = UusiHenkilö(hetu, sukunimi, etunimet, kutsumanimi, "OPPIJA", None)
}
case class Yhteystiedot(sahkoposti: String)
case class Kansalaisuus(kansalaisuusKoodi: String)
case class Kieli(kieliKoodi: String)
case class OppijaHenkilö(oidHenkilo: String, sukunimi: String, etunimet: String, kutsumanimi: String, hetu: Option[String], syntymaika: Option[LocalDate], aidinkieli: Option[String], kansalaisuus: Option[List[String]], modified: Long) {
  def toTäydellisetHenkilötiedot = TäydellisetHenkilötiedot(oidHenkilo, etunimet, kutsumanimi, sukunimi)
}
