package fi.oph.koski.ovara

import com.typesafe.config.Config
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http.{UriInterpolator, runIO}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.log.Logging

import scala.util.control.NonFatal

object OvaraClient extends Logging {
  def apply(config: Config): OvaraClient = {
    config.getString("opintopolku.virkailija.url") match {
      case "mock" =>
        logger.info("Using mock Ovara integration")
        MockOvaraClient
      case "" =>
        logger.info("Ovara integration disabled")
        EmptyOvaraClient
      case _ =>
        logger.info("Using remote Ovara integration")
        new RemoteOvaraClient(config)
    }
  }
}

trait OvaraClient {
  def fetchOpiskelijavalintatiedot(oppijaOid: String): Either[HttpStatus, Option[OvaraOpiskelijavalintatieto]]
}

object EmptyOvaraClient extends OvaraClient {
  override def fetchOpiskelijavalintatiedot(oppijaOid: String): Either[HttpStatus, Option[OvaraOpiskelijavalintatieto]] = Right(None)
}

object MockOvaraClient extends OvaraClient {
  private val mockData: Map[String, Either[HttpStatus, Option[OvaraOpiskelijavalintatieto]]] = Map(
    KoskiSpecificMockOppijat.ammattilainen.oid -> Right(Some(
      OvaraOpiskelijavalintatieto(
        oppijanumero = KoskiSpecificMockOppijat.ammattilainen.oid,
        hetu = None,
        syntymaaika = None,
        sukunimi = None,
        etunimet = None,
        hakemukset = List(
          OvaraHakemus(
            hakemusOid = "1.2.246.562.11.00000000000001049800",
            haku = OvaraHaku(
              oid = "1.2.246.562.29.00000000000000005467",
              nimi = OvaraNimi(fi = Some("Yhteishaku kevät 2024"), sv = Some("Gemensam ansökan våren 2024"), en = None)
            ),
            haunKohdejoukko = Some("haunkohdejoukko_12#1"),
            hakutapa = Some("hakutapa_01#1"),
            hakutoiveet = List(
              OvaraHakutoive(
                hakukohde = OvaraOrganisaatio(
                  oid = "1.2.246.562.20.00000000000000005476",
                  nimi = OvaraNimi(fi = Some("Tietotekniikan koulutusohjelma"), sv = None, en = None)
                ),
                tarjoaja = Some(OvaraOrganisaatio(
                  oid = "1.2.246.562.10.42160341923",
                  nimi = OvaraNimi(fi = Some("Esimerkkioppilaitos"), sv = None, en = None)
                )),
                koulutuksenAlkamiskausiUri = Some("kausi_s#1"),
                koulutuksenAlkamisvuosi = Some("2024"),
                valinnanTila = Some("HYVAKSYTTY"),
                vastaanotonTila = Some("VASTAANOTTANUT_SITOVASTI"),
                ilmoittautumisenTila = Some("LASNA")
              )
            )
          )
        )
      )
    )),
    KoskiSpecificMockOppijat.koululainen.oid -> Left(KoskiErrorCategory.unavailable.ovara()),
    KoskiSpecificMockOppijat.amis.oid -> Right(Some(
      OvaraOpiskelijavalintatieto(
        oppijanumero = KoskiSpecificMockOppijat.amis.oid,
        hetu = None,
        syntymaaika = None,
        sukunimi = None,
        etunimet = None,
        hakemukset = List(
          OvaraHakemus(
            hakemusOid = "1.2.246.562.11.00000000000001234570",
            haku = OvaraHaku(
              oid = "1.2.246.562.29.00000000000000005467",
              nimi = OvaraNimi(fi = Some("Yhteishaku kevät 2024"), sv = None, en = None)
            ),
            haunKohdejoukko = Some("INVALID"),
            hakutapa = None,
            hakutoiveet = List(
              OvaraHakutoive(
                hakukohde = OvaraOrganisaatio(
                  oid = "1.2.246.562.20.00000000000000005476",
                  nimi = OvaraNimi(fi = Some("Tietotekniikan koulutusohjelma"), sv = None, en = None)
                ),
                tarjoaja = None,
                koulutuksenAlkamiskausiUri = None,
                koulutuksenAlkamisvuosi = None,
                valinnanTila = None,
                vastaanotonTila = None,
                ilmoittautumisenTila = None
              )
            )
          )
        )
      )
    )),
    KoskiSpecificMockOppijat.lukiolainen.oid -> Right(Some(
      OvaraOpiskelijavalintatieto(
        oppijanumero = KoskiSpecificMockOppijat.lukiolainen.oid,
        hetu = None,
        syntymaaika = None,
        sukunimi = None,
        etunimet = None,
        hakemukset = List(
          OvaraHakemus(
            hakemusOid = "1.2.246.562.11.00000000000001234571",
            haku = OvaraHaku(
              oid = "1.2.246.562.29.00000000000000005467",
              nimi = OvaraNimi(fi = Some("Yhteishaku kevät 2024"), sv = None, en = None)
            ),
            haunKohdejoukko = None,
            hakutapa = None,
            hakutoiveet = List(
              OvaraHakutoive(
                hakukohde = OvaraOrganisaatio(
                  oid = "1.2.246.562.20.00000000000000005476",
                  nimi = OvaraNimi(fi = Some("Tietotekniikan koulutusohjelma"), sv = None, en = None)
                ),
                tarjoaja = None,
                koulutuksenAlkamiskausiUri = None,
                koulutuksenAlkamisvuosi = None,
                valinnanTila = Some("TUNTEMATON_TILA"),
                vastaanotonTila = None,
                ilmoittautumisenTila = None
              )
            )
          )
        )
      )
    )),
    KoskiSpecificMockOppijat.masterYlioppilasJaAmmattilainen.oid -> Right(Some(
      OvaraOpiskelijavalintatieto(
        oppijanumero = KoskiSpecificMockOppijat.masterYlioppilasJaAmmattilainen.oid,
        hetu = None,
        syntymaaika = None,
        sukunimi = None,
        etunimet = None,
        hakemukset = List(
          OvaraHakemus(
            hakemusOid = "1.2.246.562.11.00000000000001234569",
            haku = OvaraHaku(
              oid = "1.2.246.562.29.00000000000000005467",
              nimi = OvaraNimi(fi = Some("Yhteishaku kevät 2024"), sv = None, en = None)
            ),
            haunKohdejoukko = Some("haunkohdejoukko_12#1"),
            hakutapa = Some("hakutapa_01#1"),
            hakutoiveet = List(
              OvaraHakutoive(
                hakukohde = OvaraOrganisaatio(
                  oid = "1.2.246.562.20.00000000000000005476",
                  nimi = OvaraNimi(fi = Some("Tietotekniikan koulutusohjelma"), sv = None, en = None)
                ),
                tarjoaja = None,
                koulutuksenAlkamiskausiUri = None,
                koulutuksenAlkamisvuosi = None,
                valinnanTila = Some("HYVAKSYTTY"),
                vastaanotonTila = None,
                ilmoittautumisenTila = None
              )
            )
          )
        )
      )
    ))
  )

  override def fetchOpiskelijavalintatiedot(oppijaOid: String): Either[HttpStatus, Option[OvaraOpiskelijavalintatieto]] =
    mockData.getOrElse(oppijaOid, Right(None))
}

class RemoteOvaraClient(config: Config) extends OvaraClient with Logging {
  private val http: Http = VirkailijaHttpClient(
    ServiceConfig.apply(config, "opintopolku.virkailija"),
    "/ovara-backend",
    preferGettingCredentialsFromSecretsManager = true
  )

  override def fetchOpiskelijavalintatiedot(oppijaOid: String): Either[HttpStatus, Option[OvaraOpiskelijavalintatieto]] =
    try {
      Right(runIO(http.get(uri"/ovara-backend/api/opiskelijavalintatiedot?ovara_oppijanumero=${oppijaOid}")(Http.parseJsonOptional[OvaraOpiskelijavalintatieto])))
    } catch {
      case NonFatal(e) =>
        logger.error(e)("Failed to fetch opiskelijavalintatiedot from Ovara")
        Left(KoskiErrorCategory.unavailable.ovara())
    }
}
