package fi.oph.koski.valpas.hakukooste

import com.typesafe.config.Config
import fi.oph.koski.healthcheck.HealthMonitoring
import fi.oph.koski.healthcheck.Subsystem.Ovara
import fi.oph.koski.henkilo.OpintopolkuHenkilöFacade
import fi.oph.koski.http.Http.{StringToUriConverter, parseJsonWithDeserialize, unsafeRetryingClient}
import fi.oph.koski.http.{Http, HttpException, HttpStatus, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{BlankLocalizedString, BlankableLocalizedString, Koodistokoodiviite, LocalizedString}
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö
import fi.oph.koski.valpas.oppija.ValpasErrorCategory
import org.json4s.{DefaultFormats, Formats, JValue}

import java.net.URLEncoder
import java.time.{LocalDateTime, OffsetDateTime, ZoneId}
import scala.concurrent.duration.DurationInt
import scala.util.Try

case class OvaraNimi(fi: Option[String], sv: Option[String], en: Option[String])

case class OvaraHakutoiveResponse(
  hakukohdeOid: String,
  hakukohdeNimi: OvaraNimi,
  organisaatioNimi: OvaraNimi,
  hakukohdeOrganisaatio: String,
  koulutusNimi: OvaraNimi,
  koulutusOid: Option[String],
  hakutoivenumero: Int,
  pisteet: Option[BigDecimal],
  alinHyvaksyttyPistemaara: Option[BigDecimal],
  alinValintaPistemaara: Option[Int],
  valintatila: Option[String], // Enum
  varasijanumero: Option[Int],
  vastaanottotieto: Option[String], // Enum
  ilmoittautumistila: Option[String], // Enum
  harkinnanvaraisuus: Option[String],
  hakukohdeKoulutuskoodi: Option[Seq[Koodistokoodiviite]],
)

case class OvaraHakukoosteResponse(
  oppijaOid: ValpasHenkilö.Oid,
  hakuOid: String,
  aktiivinenHaku: Option[Boolean],
  hakemusOid: String,
  hakemusUrl: String,
  hakutapa: Koodistokoodiviite,
  hakutyyppi: Option[Koodistokoodiviite], // TODO: kenttä poistettu Ovarasta?
  haunAlkamispaivamaara: String,
  hakuNimi: OvaraNimi,
  email: String,
  lahiosoite: String,
  postinumero: String,
  postitoimipaikka: Option[String],
  maa: Option[Koodistokoodiviite],
  matkapuhelin: String,
  huoltajanNimi: Option[String], // TODO: kenttä poistettu Ovarasta?
  huoltajanPuhelinnumero: Option[String], // TODO: kenttä poistettu Ovarasta?
  huoltajanSahkoposti: Option[String], // TODO: kenttä poistettu Ovarasta?
  hakutoiveet: Seq[OvaraHakutoiveResponse],
  hakemuksenMuokkauksenAikaleima: Option[String],
)

class OvaraHakukoosteService(
  valpasLocalizationRepository: LocalizationRepository,
  valpasOpintopolkuHenkilöFacade: OpintopolkuHenkilöFacade,
  koodistoViitePalvelu: KoodistoViitePalvelu,
  config: Config,
  healthMonitoring: HealthMonitoring,
) extends ValpasHakukoosteService
  with Logging
  with Timing {

  protected val localizationRepository = valpasLocalizationRepository
  protected val opintopolkuHenkilöFacade = valpasOpintopolkuHenkilöFacade

  private val baseUrl = "/ovara-backend"

  private val totalTimeout = config.getInt("valpas.hakukoosteTimeoutSeconds").seconds

  private val http = {
    val (connectTimeout, headerTimeout) = totalTimeout match {
      case t if t >= 4.seconds => (totalTimeout - 3.seconds, totalTimeout - 2.seconds)
      case _ => (1.second, 2.seconds)
    }

    val client = unsafeRetryingClient(
      baseUrl, clientBuilder => clientBuilder
        .withConnectTimeout(connectTimeout)
        .withResponseHeaderTimeout(headerTimeout)
        .withRequestTimeout(totalTimeout)
    )

    VirkailijaHttpClient(
      ServiceConfig.apply(config, "opintopolku.virkailija"),
      baseUrl,
      client,
      preferGettingCredentialsFromSecretsManager = true
    )
  }

  def getHakukoosteet(
    oppijaOids: Set[ValpasHenkilö.Oid],
    ainoastaanAktiivisetHaut: Boolean,
    errorClue: String,
  ): Either[HttpStatus, Seq[Hakukooste]] =
    if (oppijaOids.isEmpty) {
      Right(Seq.empty)
    } else {
      implicit val formats: Formats = DefaultFormats.preservingEmptyValues

      def deserialize(parsedJson: JValue): Either[HttpStatus, Seq[Hakukooste]] =
        Try(parsedJson.extract[Seq[OvaraHakukoosteResponse]]).toEither
          .left.map { e =>
            logger.error(s"Error deserializing JSON response from Ovara for $errorClue: $e")
            ValpasErrorCategory.badGateway.ovara()
          }
          .flatMap(convertResponses(_, errorClue))

      val decoder = parseJsonWithDeserialize(deserialize) _
      val timedBlockname = if (oppijaOids.size == 1) "getHakukoosteetOvaraSingle" else "getHakukoosteetOvaraMultiple"

      timed(timedBlockname, 10) {
        val ioResult = if (oppijaOids.size == 1) {
          val encodedOid = URLEncoder.encode(oppijaOids.head, "UTF-8")
          val aktiivisetParam = if (ainoastaanAktiivisetHaut) "&ovara_vain_aktiiviset=true" else ""
          http.get(
            s"$baseUrl/api/valpas?ovara_oppijanumero=$encodedOid$aktiivisetParam".toUri,
            timeout = totalTimeout,
          )(decoder)
        } else {
          val encoder = json4sEncoderOf[Seq[ValpasHenkilö.Oid]]
          val postQueryParams = if (ainoastaanAktiivisetHaut) "?ovara_vain_aktiiviset=true" else ""
          http.post(
            s"$baseUrl/api/valpas$postQueryParams".toUri,
            oppijaOids.toSeq,
            timeout = totalTimeout,
          )(encoder)(decoder)
        }

        Http.runIO(
          ioResult
            .map { response =>
              healthMonitoring.setSubsystemStatus(Ovara, operational = response.isRight)
              response
            }
            .handleError {
              case e: HttpException =>
                healthMonitoring.setSubsystemStatus(Ovara, operational = false)
                logger.error(e)(s"Bad response from Ovara for $errorClue: $e")
                Left(ValpasErrorCategory.unavailable.ovara())
              case e: Exception =>
                healthMonitoring.setSubsystemStatus(Ovara, operational = false)
                logger.error(e)(s"Error fetching hakukoosteet from Ovara for $errorClue: $e")
                Left(ValpasErrorCategory.internalError())
            }
        )
      }
    }

  private def convertResponses(responses: Seq[OvaraHakukoosteResponse], errorClue: String): Either[HttpStatus, Seq[Hakukooste]] =
    responses.foldLeft[Either[HttpStatus, Seq[Hakukooste]]](Right(Seq.empty)) { (acc, response) =>
      acc.flatMap(hakukoosteet => toHakukooste(response, errorClue).map(hakukoosteet :+ _))
    }

  private def toHakukooste(response: OvaraHakukoosteResponse, errorClue: String): Either[HttpStatus, Hakukooste] =
    for {
      hakutapa <- validateKoodiviite(response.hakutapa, errorClue)
      hakutyyppi <- validateOptionalKoodiviite(response.hakutyyppi, errorClue)
      maa <- validateOptionalKoodiviite(response.maa, errorClue)
      hakutoiveet <- convertHakutoiveet(response.hakutoiveet, errorClue)
      haunAlkamispaivamaara <- parseDateToDateTime(response.haunAlkamispaivamaara, errorClue)
    } yield Hakukooste(
      oppijaOid = response.oppijaOid,
      hakuOid = response.hakuOid,
      aktiivinenHaku = response.aktiivinenHaku,
      hakemusOid = response.hakemusOid,
      hakemusUrl = response.hakemusUrl,
      hakutapa = hakutapa,
      hakutyyppi = hakutyyppi,
      haunAlkamispaivamaara = haunAlkamispaivamaara,
      hakuNimi = toLocalizedString(response.hakuNimi),
      email = response.email,
      lahiosoite = response.lahiosoite,
      postinumero = response.postinumero,
      postitoimipaikka = response.postitoimipaikka,
      maa = maa,
      matkapuhelin = response.matkapuhelin,
      huoltajanNimi = response.huoltajanNimi,
      huoltajanPuhelinnumero = response.huoltajanPuhelinnumero,
      huoltajanSähkoposti = response.huoltajanSahkoposti,
      hakutoiveet = hakutoiveet,
      hakemuksenMuokkauksenAikaleima = response.hakemuksenMuokkauksenAikaleima.flatMap { s =>
        Try(OffsetDateTime.parse(s).atZoneSameInstant(ZoneId.of("Europe/Helsinki")).toLocalDateTime).toOption
      },
    )

  private def convertHakutoiveet(hakutoiveet: Seq[OvaraHakutoiveResponse], errorClue: String): Either[HttpStatus, Seq[Hakutoive]] =
    hakutoiveet.foldLeft[Either[HttpStatus, Seq[Hakutoive]]](Right(Seq.empty)) { (acc, t) =>
      acc.flatMap(toiveet => toHakutoive(t, errorClue).map(toiveet :+ _))
    }

  private def toHakutoive(t: OvaraHakutoiveResponse, errorClue: String): Either[HttpStatus, Hakutoive] = {
    // Valppaan käyttötapauksessa eli 2. asteen koulutuksissa ei pitäisi olla kuin yksi koulutuskoodi:
    val koulutusKoodiResult: Either[HttpStatus, Option[Koodistokoodiviite]] =
      t.hakukohdeKoulutuskoodi.flatMap(_.headOption) match {
        case Some(kv) => validateKoodiviite(kv, errorClue).map(Some(_))
        case None => Right(None)
      }
    koulutusKoodiResult.map { koulutusKoodi =>
      Hakutoive(
        hakukohdeOid = t.hakukohdeOid,
        hakukohdeNimi = toLocalizedString(t.hakukohdeNimi),
        organisaatioNimi = toLocalizedString(t.organisaatioNimi),
        hakukohdeOrganisaatio = t.hakukohdeOrganisaatio,
        koulutusNimi = toLocalizedString(t.koulutusNimi),
        koulutusOid = t.koulutusOid,
        hakutoivenumero = t.hakutoivenumero,
        pisteet = t.pisteet,
        alinHyvaksyttyPistemaara = t.alinHyvaksyttyPistemaara,
        valintatila = t.valintatila,
        varasijanumero = t.varasijanumero,
        vastaanottotieto = t.vastaanottotieto,
        ilmoittautumistila = t.ilmoittautumistila,
        harkinnanvaraisuus = t.harkinnanvaraisuus,
        hakukohdeKoulutuskoodi = koulutusKoodi,
      )
    }
  }

  private def validateKoodiviite(kv: Koodistokoodiviite, errorClue: String): Either[HttpStatus, Koodistokoodiviite] =
    koodistoViitePalvelu.validate(kv.koodistoUri, kv.koodiarvo) match {
      case Some(validated) => Right(validated)
      case None =>
        logger.error(s"Invalid koodiviite ${kv.koodistoUri}/${kv.koodiarvo} from Ovara for $errorClue")
        Left(ValpasErrorCategory.badGateway.ovara())
    }

  private def validateOptionalKoodiviite(kv: Option[Koodistokoodiviite], errorClue: String): Either[HttpStatus, Option[Koodistokoodiviite]] =
    kv match {
      case None => Right(None)
      case Some(koodiviite) => validateKoodiviite(koodiviite, errorClue).map(Some(_))
    }

  private def parseDateToDateTime(dateStr: String, errorClue: String): Either[HttpStatus, LocalDateTime] =
    Try(LocalDateTime.parse(dateStr)).toEither.left.map { e =>
      logger.error(s"Invalid haunAlkamispaivamaara '$dateStr' from Ovara for $errorClue: ${e.getMessage}")
      ValpasErrorCategory.badGateway.ovara()
    }

  private def toLocalizedString(nimi: OvaraNimi): BlankableLocalizedString = LocalizedString.sanitize(Map(
    "fi" -> nimi.fi.getOrElse(""),
    "sv" -> nimi.sv.getOrElse(""),
    "en" -> nimi.en.getOrElse("")
  )).getOrElse(BlankLocalizedString())
}
