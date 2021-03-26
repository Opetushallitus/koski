package fi.oph.koski.henkilo

import java.time.LocalDate
import com.typesafe.config.Config
import fi.oph.koski.http.Http._
import fi.oph.koski.http._
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.Koodistokoodiviite
import scalaz.Nondeterminism
import scalaz.concurrent.Task

case class OppijanumeroRekisteriClient(config: Config) {
  def findOrCreate(createUserInfo: UusiOppijaHenkilö): Task[Either[HttpStatus, SuppeatOppijaHenkilöTiedot]] = oidServiceHttp.post(uri"/oppijanumerorekisteri-service/s2s/findOrCreateHenkiloPerustieto", createUserInfo)(json4sEncoderOf[UusiOppijaHenkilö]) {
    case (x, data, _) if x <= 201 => Right(JsonSerializer.parse[OppijaNumerorekisteriPerustiedot](data, ignoreExtras = true))
    case (400, error, _) => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.virheelliset(error))
    case (status, text, uri) => throw HttpStatusException(status, text, uri)
  }.flatMap {
    case Right(o) => findSlaveOids(o.oidHenkilo).map(o.toSuppeaOppijaHenkilö).map(Right(_))
    case Left(status) => Task.now(status).map(Left(_))
  }

  private val oidServiceHttp = VirkailijaHttpClient(makeServiceConfig(config), "/oppijanumerorekisteri-service")

  private def makeServiceConfig(config: Config) = ServiceConfig.apply(config, "authentication-service", "authentication-service.virkailija", "opintopolku.virkailija")

  private def henkilöByOid[T](oid: String) = oidServiceHttp.get[T](uri"/oppijanumerorekisteri-service/henkilo/$oid")(_)

  def findKäyttäjäByOid(oid: String): Task[Option[KäyttäjäHenkilö]] = henkilöByOid(oid)(Http.parseJsonOptional[KäyttäjäHenkilö])

  def findOppijatNoSlaveOids(oids: Seq[Oid]): Task[List[SuppeatOppijaHenkilöTiedot]] =
    findOnrOppijatByOids(oids).map(_.map(_.toSuppeaOppijaHenkilö(Nil)))

  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): Task[List[Oid]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/s2s/changedSince/$since?offset=$offset&amount=$amount")(Http.parseJson[List[String]])

  def findOppijaByOid(oid: Oid): Task[Option[LaajatOppijaHenkilöTiedot]] =
    henkilöByOid(oid)(Http.parseJsonOptional[OppijaNumerorekisteriOppija])
      .map(_.toSeq).flatMap(withSlaveOids(_).map(_.headOption))

  def findOppijaByHetu(hetu: String): Task[Option[LaajatOppijaHenkilöTiedot]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/hetu=$hetu")(Http.parseJsonOptional[OppijaNumerorekisteriOppija])
      .flatMap(withSlaveOids(_).map(_.headOption))

  def findMasterOppija(oid: String): Task[Option[LaajatOppijaHenkilöTiedot]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/$oid/master")(Http.parseJsonOptional[OppijaNumerorekisteriOppija])
      .flatMap(withSlaveOids(_).map(_.headOption))

  def findMasterOppijat(oids: List[String]): Task[Map[String, LaajatOppijaHenkilöTiedot]] =
    oidServiceHttp.post(uri"/oppijanumerorekisteri-service/henkilo/masterHenkilosByOidList", oids)(json4sEncoderOf[List[String]])(Http.parseJson[Map[String, OppijaNumerorekisteriOppija]])
    .map(_.mapValues(_.toOppijaHenkilö(Nil)))

  def findOppijatByHetusNoSlaveOids(hetus: Seq[String]): Task[List[SuppeatOppijaHenkilöTiedot]] =
    oidServiceHttp.post(uri"/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloHetuList", hetus)(json4sEncoderOf[Seq[String]])(Http.parseJson[List[OppijaNumerorekisteriPerustiedot]])
      .map(_.map(_.toSuppeaOppijaHenkilö(Nil)))

  def findSlaveOids(masterOid: String): Task[List[String]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/$masterOid/slaves")(Http.parseJson[List[OppijaNumerorekisteriSlave]]).map(_.map(_.oidHenkilo))

  private def findOnrOppijatByOids(oids: Seq[Oid]): Task[List[OppijaNumerorekisteriPerustiedot]] =
    oidServiceHttp.post(uri"/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloOidList", oids)(json4sEncoderOf[Seq[String]])(Http.parseJson[List[OppijaNumerorekisteriPerustiedot]])

  private def withSlaveOids(onrOppijat: Iterable[OppijaNumerorekisteriOppija]): Task[List[LaajatOppijaHenkilöTiedot]] =
    Nondeterminism[Task].gather(onrOppijat.toSeq.map(complementWithSlaveOids))

  private def complementWithSlaveOids(onrOppija: OppijaNumerorekisteriOppija): Task[LaajatOppijaHenkilöTiedot] =
    findSlaveOids(onrOppija.oidHenkilo).map(onrOppija.toOppijaHenkilö)
}

case class KäyttäjäHenkilö(oidHenkilo: String, sukunimi: String, etunimet: String, asiointiKieli: Option[Kieli])
case class OppijaNumerorekisteriSlave(oidHenkilo: String)

case class OppijaNumerorekisteriPerustiedot(
  oidHenkilo: String,
  sukunimi: String,
  etunimet: String,
  kutsumanimi: Option[String],
  hetu: Option[String],
  syntymaaika: Option[LocalDate],
  aidinkieli: Option[Kieli],
  kansalaisuus: Option[List[Kansalaisuus]],
  modified: Long,
  turvakielto: Option[Boolean],
  sukupuoli: Option[String],
  kotikunta: Option[String],
  yksiloity: Option[Boolean],
  yksiloityVTJ: Option[Boolean]
) {
  def toSuppeaOppijaHenkilö(linkitetytOidit: List[String]) = SuppeatOppijaHenkilöTiedot(
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
    sukupuoli = sukupuoli,
    linkitetytOidit = linkitetytOidit
  )
}

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
  turvakielto: Option[Boolean],
  sukupuoli: Option[String],
  kotikunta: Option[String],
  yksiloity: Option[Boolean],
  yksiloityVTJ: Option[Boolean],
  yhteystiedotRyhma: Seq[OppijaNumerorekisteriYhteystiedotRyhma]
) {
  def toOppijaHenkilö(linkitetytOidit: List[String]) = LaajatOppijaHenkilöTiedot(
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
    sukupuoli = sukupuoli,
    linkitetytOidit = linkitetytOidit,
    vanhatHetut = kaikkiHetut.getOrElse(Nil).filterNot(hetu.contains),
    kotikunta = kotikunta,
    yksilöity = yksiloity.exists(identity) || yksiloityVTJ.exists(identity),
    yhteystiedot = yhteystiedotRyhma.map(_.toYhteystiedot)
  )
}
case class UusiOppijaHenkilö(hetu: Option[String], sukunimi: String, etunimet: String, kutsumanimi: String, henkiloTyyppi: String = "OPPIJA")

case class Kansalaisuus(kansalaisuusKoodi: String)
case class Kieli(kieliKoodi: String)

case class OppijaNumerorekisteriYhteystiedotRyhma(
  id: Int,
  readOnly: Boolean,
  ryhmaAlkuperaTieto: String,
  ryhmaKuvaus: String,
  yhteystieto: Seq[OppijaNumerorekisteriYhteystieto]
) {
  def toYhteystiedot: Yhteystiedot = Yhteystiedot(
    alkuperä = Koodistokoodiviite(ryhmaAlkuperaTieto, "yhteystietojenalkupera"),
    tyyppi = Koodistokoodiviite(ryhmaKuvaus, "yhteystietotyypit"),
    sähköposti = yhteystietoarvo("YHTEYSTIETO_SAHKOPOSTI"),
    puhelinnumero = yhteystietoarvo("YHTEYSTIETO_PUHELINNUMERO"),
    matkapuhelinnumero = yhteystietoarvo("YHTEYSTIETO_MATKAPUHELINNUMERO"),
    katuosoite = yhteystietoarvo("YHTEYSTIETO_KATUOSOITE"),
    kunta = yhteystietoarvo("YHTEYSTIETO_KUNTA"),
    postinumero = yhteystietoarvo("YHTEYSTIETO_POSTINUMERO"),
    kaupunki = yhteystietoarvo("YHTEYSTIETO_KAUPUNKI"),
    maa = yhteystietoarvo("YHTEYSTIETO_MAA"),
  )

  def yhteystietoarvo(tyyppi: String): Option[String] = yhteystieto
    .find(yt => yt.yhteystietoTyyppi == tyyppi)
    .map(yt => yt.yhteystietoArvo)
}

case class OppijaNumerorekisteriYhteystieto(
  yhteystietoArvo: String,
  yhteystietoTyyppi: String
)
