package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresVirkailijaOrPalvelukäyttäjä}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.util.UuidUtils
import fi.oph.scalaschema.annotation.{Description, Discriminator, SyntheticProperty, Title}
import org.json4s.jackson.JsonMethods

import java.time.{LocalDateTime, OffsetDateTime}
import java.util.TimeZone
import scala.language.implicitConversions

object MassaluovutusServletUrls {
  def root(rootUrl: String): String = s"$rootUrl/api/massaluovutus"
  def query(rootUrl: String, queryId: String): String = s"${root(rootUrl)}/$queryId"
  def file(rootUrl: String, queryId: String, fileKey: String): String = s"${root(rootUrl)}/$queryId/$fileKey"
}

class MassaluovutusServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with JsonMethods with NoCache
{
  val massaluovutukset: MassaluovutusService = application.massaluovutusService
  val rootUrl: String = application.config.getString("koski.root.url")

  post("/") {
    withJsonBody { body =>
      renderEither {
        application
          .validatingAndResolvingExtractor
          .extract[MassaluovutusQueryParameters](strictDeserialization)(body)
          .flatMap(massaluovutukset.add)
          .map(q => QueryResponse(rootUrl, q, session))
      }
    } (parseErrorHandler = jsonErrorHandler)
  }

  get("/:id") {
    renderEither {
      UuidUtils.optionFromString(getStringParam("id"))
        .toRight(KoskiErrorCategory.badRequest.queryParam("Epävalidi tunniste"))
        .flatMap(massaluovutukset.get)
        .map(q => QueryResponse(rootUrl, q, session))
    }
  }

  get("/:id/:file") {
    UuidUtils.optionFromString(getStringParam("id"))
      .toRight(KoskiErrorCategory.badRequest.queryParam("Epävalidi tunniste"))
      .flatMap(massaluovutukset.get)
      .flatMap {
        case q: QueryWithResultFiles =>
          massaluovutukset.getDownloadUrl(q, getStringParam("file"))
        case _ =>
          Left(KoskiErrorCategory.badRequest("Tulostiedostot eivät ole vielä ladattavissa"))
      }
      .fold(renderStatus, redirect)
  }

  private def jsonErrorHandler(status: HttpStatus) = {
    haltWithStatus(status)
  }
}

trait QueryResponse {
  @Description("Massaluovutuksen tunniste")
  def queryId: String
  @Description("Massaluovutuksen tila")
  @Discriminator
  @SyntheticProperty
  def status: String
  @Description("Massaluovutuksen luoman käyttäjän oid")
  def requestedBy: String
  @Description("Määrittää tehtävän massaluovutuksen tyypin sekä sen parametrit.")
  def query: MassaluovutusQueryParameters
  @Description("Massaluovutuksen luontiaika")
  def createdAt: OffsetDateTime
}

@Title("Odottava kysely")
@Description("Massaluovutuskysely on luotu, mutta sen käsittelyä ei ole vielä aloitettu.")
case class PendingQueryResponse(
  queryId: String,
  requestedBy: String,
  query: MassaluovutusQueryParameters,
  createdAt: OffsetDateTime,
  @Description("Osoite josta kyselyn tilaa voi kysellä")
  resultsUrl: String,
  @EnumValues(Set(QueryState.pending))
  status: String = QueryState.pending,
) extends QueryResponse

case class RunningQueryResponse(
  queryId: String,
  requestedBy: String,
  query: MassaluovutusQueryParameters,
  createdAt: OffsetDateTime,
  @Description("Massaluovutuskyselyn käsittelyn aloitusaika")
  startedAt: OffsetDateTime,
  @Description("Lista osoitteista, joista valmistuneet tulostiedostot voi ladata. Tiedostojen määrä kasvaa kyselyn edetessä.")
  files: List[String],
  @Description("Osoite josta kyselyn tilaa voi kysellä")
  resultsUrl: String,
  @Description("Tietoa kyselyn etenemisestä")
  progress: Option[QueryProgress],
  @EnumValues(Set(QueryState.running))
  status: String = QueryState.running,
) extends QueryResponse

case class FailedQueryResponse(
  queryId: String,
  requestedBy: String,
  query: MassaluovutusQueryParameters,
  createdAt: OffsetDateTime,
  @Description("Massaluovutuksen käsittelyn aloitusaika")
  startedAt: OffsetDateTime,
  @Description("Massaluovutuksen epäonnistumisen aika")
  finishedAt: OffsetDateTime,
  @Description("Lista ennen kyselyn epäonnistumista saaduista tulostiedostoista.")
  files: List[String],
  @EnumValues(Set(QueryState.failed))
  status: String = QueryState.failed,
  @Description("Mahdollinen vihje, miten epäonnistuneen kyselyn voisi yrittää korjata kyselyä muuttamalla.")
  hint: Option[String],
  @Description("Alkuperäinen virheviesti. Nähtävissä vain pääkäyttäjän oikeuksilla.")
  error: Option[String],
) extends QueryResponse

case class CompleteQueryResponse(
  queryId: String,
  requestedBy: String,
  query: MassaluovutusQueryParameters,
  createdAt: OffsetDateTime,
  @Description("Massaluovutuksen käsittelyn aloitusaika")
  startedAt: OffsetDateTime,
  @Description("Massaluovutuksen valmistumisaika")
  finishedAt: OffsetDateTime,
  @Description("Lista osoitteista, joista tulostiedostot voi ladata. Tiedostojen määrä riippuu kyselyn tyypistä.")
  files: List[String],
  @Description(s"Tiedostojen avaamiseen tarvittava salasana. Käytössä vain xlsx-tiedostojen (${QueryFormat.xlsx}) kanssa.")
  password: Option[String],
  @Description(s"Viimeisin opiskeluoikeuspäivitysten vastaanottoaika.")
  sourceDataUpdatedAt: Option[OffsetDateTime],
  @EnumValues(Set(QueryState.complete))
  status: String = QueryState.complete,
) extends QueryResponse

object QueryResponse {
  def apply(rootUrl: String, query: Query, session: KoskiSpecificSession): QueryResponse = query match {
    case q: PendingQuery => PendingQueryResponse(
      queryId = q.queryId,
      requestedBy = q.userOid,
      query = q.query,
      createdAt = q.createdAt,
      resultsUrl = q.externalResultsUrl(rootUrl),
    )
    case q: RunningQuery => RunningQueryResponse(
      queryId = q.queryId,
      requestedBy = q.userOid,
      query = q.query,
      createdAt = q.createdAt,
      startedAt = q.startedAt,
      files = q.filesToExternal(rootUrl),
      resultsUrl = q.externalResultsUrl(rootUrl),
      progress = q.progress,
    )
    case q: FailedQuery => FailedQueryResponse(
      queryId = q.queryId,
      requestedBy = q.userOid,
      query = q.query,
      createdAt = q.createdAt,
      startedAt = q.startedAt,
      finishedAt = q.finishedAt,
      files = q.filesToExternal(rootUrl),
      hint = failedQueryHint(q, rootUrl),
      error = if (session.hasGlobalReadAccess) Some(q.error) else None,
    )
    case q: CompleteQuery => CompleteQueryResponse(
      queryId = q.queryId,
      requestedBy = q.userOid,
      query = q.query,
      createdAt = q.createdAt,
      startedAt = q.startedAt,
      finishedAt = q.finishedAt,
      files = q.filesToExternal(rootUrl),
      password = q.meta.flatMap(_.password),
      sourceDataUpdatedAt = q.meta.flatMap(_.raportointikantaGeneratedAt),
    )
  }

  implicit def toOffsetDateTime(dt: LocalDateTime): OffsetDateTime =
    dt.atZone(TimeZone.getDefault.toZoneId).toOffsetDateTime

  implicit def toOffsetDateTime(dt: Option[LocalDateTime]): Option[OffsetDateTime] =
    dt.map(toOffsetDateTime)

  def failedQueryHint(q: FailedQuery, rootUrl: String): Option[String] =
    if (q.error.contains("Your proposed upload exceeds the maximum allowed size")) {
      val suggestions = (
        List("rajaa kysely lyhyemmälle aikavälille") ++
        (q.query match {
          case p: PartitionSupport =>
            val formats = p.partitionFormats.mkString(" tai ")
            val helpUrl = s"$rootUrl/dokumentaatio/rajapinnat/massaluovutus/koulutuksenjarjestajat"
            List(s"käytä tulostiedostojen ositusta asettalla format-kenttään $formats (kts. tarkemmat ohjeet $helpUrl)")
          case _ => List()
        })
      ).mkString("; ")
      Some(s"Kyselystä syntyneen tulostiedoston koko kasvoi liian suureksi. Ehdotuksia kyselyn korjaamiseksi: $suggestions")
    } else {
      None
    }
}
