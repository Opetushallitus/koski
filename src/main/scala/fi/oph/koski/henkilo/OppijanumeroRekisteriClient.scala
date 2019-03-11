package fi.oph.koski.henkilo

import java.time.LocalDate

import com.typesafe.config.Config
import fi.oph.koski.http.Http._
import fi.oph.koski.http._
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Henkilö.Oid
import scalaz.Nondeterminism
import scalaz.concurrent.Task

case class OppijanumeroRekisteriClient(config: Config) {
  def findOrCreate(createUserInfo: UusiOppijaHenkilö): Task[Either[HttpStatus, OppijaHenkilö]] = oidServiceHttp.post(uri"/oppijanumerorekisteri-service/s2s/findOrCreateHenkiloPerustieto", createUserInfo)(json4sEncoderOf[UusiOppijaHenkilö]) {
    case (x, data, _) if x <= 201 => Right(JsonSerializer.parse[OppijaNumerorekisteriOppija](data, ignoreExtras = true))
    case (400, error, _) => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.virheelliset(error))
    case (status, text, uri) => throw HttpStatusException(status, text, uri)
  }.flatMap {
    case Right(o) => toOppijaHenkilö(o).map(Right(_))
    case Left(status) => Task.now(status).map(Left(_))
  }

  private val oidServiceHttp = VirkailijaHttpClient(makeServiceConfig(config), "/oppijanumerorekisteri-service", config.getBoolean("authentication-service.useCas"))

  private def makeServiceConfig(config: Config) = ServiceConfig.apply(config, "authentication-service", "authentication-service.virkailija", "opintopolku.virkailija")

  def findKäyttäjäByOid(oid: String) = oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/$oid")(Http.parseJsonOptional[KäyttäjäHenkilö])

  def findTyöSähköpostiosoitteet(organisaatioOid: String, käyttöikeusRyhmä: String): Task[List[String]] =
    oidServiceHttp.post(uri"/oppijanumerorekisteri-service/s2s/henkilo/yhteystiedot", YhteystiedotHaku(List(organisaatioOid), List(käyttöikeusRyhmä)))(json4sEncoderOf[YhteystiedotHaku])(Http.parseJson[List[Yhteystiedot]])
        .map(_.flatMap { yhteystiedot =>
          yhteystiedot.yhteystiedotRyhma
            .filter(työOsoite)
            .flatMap(_.yhteystieto.find(_.yhteystietoTyyppi == "YHTEYSTIETO_SAHKOPOSTI").map(_.yhteystietoArvo))
        })

  def findOppijatNoSlaveOids(oids: List[Oid]): Task[List[OppijaHenkilö]] =
    findOnrOppijatByOids(oids).map(_.map(_.toOppijaHenkilö(Nil)))

  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): Task[List[Oid]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/s2s/changedSince/$since?offset=$offset&amount=$amount")(Http.parseJson[List[String]])

  def findOppijaByOid(oid: Oid): Task[Option[OppijaHenkilö]] =
    findOnrOppijatByOids(List(oid)).flatMap(toOppijaHenkilöt).map(_.headOption)

  def findOppijaByHetu(hetu: String): Task[Option[OppijaHenkilö]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/hetu=$hetu")(Http.parseJsonOptional[OppijaNumerorekisteriOppija])
      .flatMap(toOppijaHenkilöt(_).map(_.headOption))

  def findMasterOppija(oid: String): Task[Option[OppijaHenkilö]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/$oid/master")(Http.parseJsonOptional[OppijaNumerorekisteriOppija])
      .flatMap(toOppijaHenkilöt(_).map(_.headOption))

  def findMasterOppijat(oids: List[String]): Task[Map[String, OppijaHenkilö]] =
    oidServiceHttp.post(uri"/oppijanumerorekisteri-service/henkilo/masterHenkilosByOidList", oids)(json4sEncoderOf[List[String]])(Http.parseJson[Map[String, OppijaNumerorekisteriOppija]])
    .map(_.mapValues(_.toOppijaHenkilö(Nil)))

  def findOppijatByHetusNoSlaveOids(hetus: List[String]): Task[List[OppijaHenkilö]] =
    oidServiceHttp.post(uri"/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloHetuList", hetus)(json4sEncoderOf[List[String]])(Http.parseJson[List[OppijaNumerorekisteriOppija]])
      .map(_.map(_.toOppijaHenkilö(Nil)))

  def findSlaveOids(masterOid: String): Task[List[String]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/$masterOid/slaves")(Http.parseJson[List[OppijaNumerorekisteriSlave]]).map(_.map(_.oidHenkilo))

  private def findOnrOppijatByOids(oids: List[Oid]): Task[List[OppijaNumerorekisteriOppija]] =
    oidServiceHttp.post(uri"/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloOidList", oids)(json4sEncoderOf[List[String]])(Http.parseJson[List[OppijaNumerorekisteriOppija]])

  private def toOppijaHenkilöt(onrOppijat: Iterable[OppijaNumerorekisteriOppija]): Task[List[OppijaHenkilö]] =
    Nondeterminism[Task].gather(onrOppijat.toSeq.map(toOppijaHenkilö))

  private def toOppijaHenkilö(onrOppija: OppijaNumerorekisteriOppija): Task[OppijaHenkilö] =
    findSlaveOids(onrOppija.oidHenkilo).map(onrOppija.toOppijaHenkilö)

  private def työOsoite(yhteystiedotRyhmä: YhteystiedotRyhmä): Boolean =
    yhteystiedotRyhmä.ryhmaKuvaus == "yhteystietotyyppi2"
}

case class KäyttäjäHenkilö(oidHenkilo: String, sukunimi: String, etunimet: String, asiointiKieli: Option[Kieli])
case class OppijaNumerorekisteriSlave(oidHenkilo: String)

case class OppijaNumerorekisteriOppija(
  oidHenkilo: String,
  sukunimi: String,
  etunimet: String,
  kutsumanimi: Option[String],
  hetu: Option[String],
  syntymaaika: Option[LocalDate],
  aidinkieli: Option[Kieli],
  kansalaisuus: Option[List[Kansalaisuus]],
  kaikkiHetut: Option[List[String]],
  modified: Long,
  turvakielto: Option[Boolean]
) {
  def toOppijaHenkilö(linkitetytOidit: List[String]) = OppijaHenkilö(
    oid = oidHenkilo,
    sukunimi = sukunimi,
    etunimet = etunimet,
    kutsumanimi = kutsumanimi.getOrElse(etunimet.trim.split(" ").head),
    hetu = hetu,
    syntymäaika = syntymaaika,
    äidinkieli = aidinkieli.map(_.kieliKoodi),
    kansalaisuus = kansalaisuus.map(_.map(_.kansalaisuusKoodi)),
    modified = modified,
    turvakielto = turvakielto.getOrElse(false),
    linkitetytOidit = linkitetytOidit,
    vanhatHetut = kaikkiHetut.getOrElse(Nil).filterNot(hetu.contains)
  )
}
case class UusiOppijaHenkilö(hetu: Option[String], sukunimi: String, etunimet: String, kutsumanimi: String, henkiloTyyppi: String = "OPPIJA")

case class YhteystiedotHaku(organisaatioOids: List[String], kayttoOikeusRyhmaNimet: List[String], duplikaatti: Boolean = false, passivoitu: Boolean = false)
case class Yhteystiedot(yhteystiedotRyhma: List[YhteystiedotRyhmä])
case class YhteystiedotRyhmä(ryhmaKuvaus: String, yhteystieto: List[Yhteystieto])
case class Yhteystieto(yhteystietoTyyppi: String, yhteystietoArvo: String)

case class Kansalaisuus(kansalaisuusKoodi: String)
case class Kieli(kieliKoodi: String)
