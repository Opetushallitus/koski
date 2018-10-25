package fi.oph.koski.henkilo

import java.time.LocalDate

import com.typesafe.config.Config
import fi.oph.koski.http.Http._
import fi.oph.koski.http._
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Henkilö.Oid
import scalaz.concurrent.Task

case class OppijanumeroRekisteriClient(config: Config) {
  def findOrCreate(createUserInfo: UusiOppijaHenkilö) = oidServiceHttp.post(uri"/oppijanumerorekisteri-service/s2s/findOrCreateHenkiloPerustieto", createUserInfo)(json4sEncoderOf[UusiOppijaHenkilö]) {
    case (x, data, _) if x <= 201 => Right(JsonSerializer.parse[OppijaNumerorekisteriOppija](data, ignoreExtras = true).toOppijaHenkilö)
    case (400, error, _) => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.virheelliset(error))
    case (status, text, uri) => throw HttpStatusException(status, text, uri)
  }

  private val oidServiceHttp = VirkailijaHttpClient(makeServiceConfig(config), "/oppijanumerorekisteri-service", config.getBoolean("authentication-service.useCas"))

  private def makeServiceConfig(config: Config) = ServiceConfig.apply(config, "authentication-service", "authentication-service.virkailija", "opintopolku.virkailija")

  def findKäyttäjäByOid(oid: String) = oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/$oid")(Http.parseJsonOptional[KäyttäjäHenkilö])

  def findSähköpostit(organisaatioOid: String, käyttöikeusRyhmä: String): Task[List[String]] =
    oidServiceHttp.post(uri"/oppijanumerorekisteri-service/s2s/henkilo/yhteystiedot", YhteystiedotHaku(List(organisaatioOid), List(käyttöikeusRyhmä)))(json4sEncoderOf[YhteystiedotHaku])(Http.parseJson[List[Yhteystiedot]])
        .map(_.flatMap(_.yhteystiedotRyhma.flatMap(_.yhteystieto.find(_.yhteystietoTyyppi == "YHTEYSTIETO_SAHKOPOSTI").map(_.yhteystietoArvo))))

  def findOppijatByOids(oids: List[Oid]): Task[List[OppijaHenkilö]] =
    oidServiceHttp.post(uri"/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloOidList", oids)(json4sEncoderOf[List[String]])(Http.parseJson[List[OppijaNumerorekisteriOppija]]).map(_.map(_.toOppijaHenkilö))

  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): Task[List[Oid]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/s2s/changedSince/$since?offset=$offset&amount=$amount")(Http.parseJson[List[String]])

  def findOppijaByHetu(hetu: String): Task[Option[OppijaHenkilö]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/hetu=$hetu")(Http.parseJsonOptional[OppijaNumerorekisteriOppija]).map(_.map(_.toOppijaHenkilö))

  def findMasterOppija(oid: String): Task[Option[OppijaHenkilö]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/$oid/master")(Http.parseJsonOptional[OppijaNumerorekisteriOppija]).map(_.map(_.toOppijaHenkilö))

  def findOppijatByHetus(hetus: List[String]): Task[List[OppijaHenkilö]] =
    oidServiceHttp.post(uri"/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloHetuList", hetus)(json4sEncoderOf[List[String]])(Http.parseJson[List[OppijaNumerorekisteriOppija]]).map(_.map(_.toOppijaHenkilö))
}

case class KäyttäjäHenkilö(oidHenkilo: String, sukunimi: String, etunimet: String, asiointiKieli: Option[Kieli])
case class OppijaNumerorekisteriOppija(
  oidHenkilo: String,
  sukunimi: String,
  etunimet: String,
  kutsumanimi: String,
  hetu: Option[String],
  syntymaaika: Option[LocalDate],
  aidinkieli: Option[Kieli],
  kansalaisuus: Option[List[Kansalaisuus]],
  modified: Long,
  turvakielto: Option[Boolean]
) {
  def toOppijaHenkilö = OppijaHenkilö(oidHenkilo, sukunimi, etunimet, kutsumanimi, hetu, syntymaaika, aidinkieli.map(_.kieliKoodi), kansalaisuus.map(_.map(_.kansalaisuusKoodi)), modified, turvakielto.getOrElse(false))
}
case class UusiOppijaHenkilö(hetu: Option[String], sukunimi: String, etunimet: String, kutsumanimi: String, henkiloTyyppi: String = "OPPIJA")

case class YhteystiedotHaku(organisaatioOids: List[String], kayttoOikeusRyhmaNimet: List[String], duplikaatti: Boolean = false, passivoitu: Boolean = false)
case class Yhteystiedot(yhteystiedotRyhma: List[YhteystiedotRyhmä])
case class YhteystiedotRyhmä(yhteystieto: List[Yhteystieto])
case class Yhteystieto(yhteystietoTyyppi: String, yhteystietoArvo: String)

case class Kansalaisuus(kansalaisuusKoodi: String)
case class Kieli(kieliKoodi: String)
