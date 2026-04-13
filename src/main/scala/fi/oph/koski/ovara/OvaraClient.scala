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
  def fetchOpiskelijavalintatiedot(oppijaOid: String): Either[HttpStatus, List[OvaraOpiskelijavalintatieto]]
}

object EmptyOvaraClient extends OvaraClient {
  override def fetchOpiskelijavalintatiedot(oppijaOid: String): Either[HttpStatus, List[OvaraOpiskelijavalintatieto]] = Right(List.empty)
}

object MockOvaraClient extends OvaraClient {
  private val mockData: Map[String, Either[HttpStatus, List[OvaraOpiskelijavalintatieto]]] = Map(
    KoskiSpecificMockOppijat.ammattilainen.oid -> Right(List(
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
            haunKohdejoukko = "haunkohdejoukko_12#1",
            hakutapa = "hakutapa_01#1",
            hakutoiveet = List(
              OvaraHakutoive(
                hakukohde = OvaraOrganisaatio(
                  oid = "1.2.246.562.20.00000000000000005476",
                  nimi = OvaraNimi(fi = Some("Tietotekniikan koulutusohjelma"), sv = None, en = None)
                ),
                tarjoaja = OvaraOrganisaatio(
                  oid = "1.2.246.562.10.42160341923",
                  nimi = OvaraNimi(fi = Some("Esimerkkioppilaitos"), sv = None, en = None)
                ),
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
    KoskiSpecificMockOppijat.koululainen.oid -> Left(KoskiErrorCategory.unavailable.ovara())
  )

  override def fetchOpiskelijavalintatiedot(oppijaOid: String): Either[HttpStatus, List[OvaraOpiskelijavalintatieto]] =
    mockData.getOrElse(oppijaOid, Right(List.empty))
}

class RemoteOvaraClient(config: Config) extends OvaraClient with Logging {
  private val http: Http = VirkailijaHttpClient(
    ServiceConfig.apply(config, "opintopolku.virkailija"),
    "/ovara-backend",
    preferGettingCredentialsFromSecretsManager = true
  )

  override def fetchOpiskelijavalintatiedot(oppijaOid: String): Either[HttpStatus, List[OvaraOpiskelijavalintatieto]] =
    try {
      Right(runIO(http.get(uri"/ovara-backend/api/opiskelijavalintatiedot?ovara_oppijanumerot=${oppijaOid}")(Http.parseJson[List[OvaraOpiskelijavalintatieto]])))
    } catch {
      case NonFatal(e) =>
        logger.error(e)("Failed to fetch opiskelijavalintatiedot from Ovara")
        Left(KoskiErrorCategory.unavailable.ovara())
    }
}
