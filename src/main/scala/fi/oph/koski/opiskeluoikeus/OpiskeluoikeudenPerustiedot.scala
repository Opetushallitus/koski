package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.{HttpStatus, HttpStatusException, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.{KoskiSession, RequiresAuthentication}
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException}
import fi.oph.koski.util.{ListPagination, PaginatedResponse, Pagination, PaginationSettings}
import fi.oph.scalaschema.annotation.Description
import OpiskeluoikeusQueryFilter._
import com.sksamuel.elastic4s.ElasticClient
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusSortOrder.{Ascending, Descending}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.searches.RichSearchResponse
import fi.oph.koski.json.Json

import scala.concurrent.Future

case class OpiskeluoikeudenPerustiedot(
  henkilö: NimitiedotJaOid,
  oppilaitos: Oppilaitos,
  @Description("Opiskelijan opiskeluoikeuden alkamisaika joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  alkamispäivä: Option[LocalDate],
  tyyppi: Koodistokoodiviite,
  suoritukset: List[SuorituksenPerustiedot],
  @KoodistoUri("virtaopiskeluoikeudentila")
  @KoodistoUri("koskiopiskeluoikeudentila")
  tila: Koodistokoodiviite,
  @Description("Luokan tai ryhmän tunniste, esimerkiksi 9C")
  luokka: Option[String]
)

case class SuorituksenPerustiedot(
  @Description("Suorituksen tyyppi, jolla erotellaan eri koulutusmuotoihin (perusopetus, lukio, ammatillinen...) ja eri tasoihin (tutkinto, tutkinnon osa, kurssi, oppiaine...) liittyvät suoritukset")
  @KoodistoUri("suorituksentyyppi")
  @Hidden
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: KoulutusmoduulinPerustiedot,
  @Description("Tieto siitä mihin osaamisalaan/osaamisaloihin oppijan tutkinto liittyy")
  @KoodistoUri("osaamisala")
  @OksaUri(tunnus = "tmpOKSAID299", käsite = "osaamisala")
  osaamisala: Option[List[Koodistokoodiviite]] = None,
  @Description("Tieto siitä mihin tutkintonimikkeeseen oppijan tutkinto liittyy")
  @KoodistoUri("tutkintonimikkeet")
  @OksaUri("tmpOKSAID588", "tutkintonimike")
  tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  toimipiste: OrganisaatioWithOid
)

case class KoulutusmoduulinPerustiedot(
  tunniste: KoodiViite
)

object KoulutusmoduulinPerustiedot {

}

class OpiskeluoikeudenPerustiedotRepository(henkilöRepository: HenkilöRepository, opiskeluoikeusRepository: OpiskeluoikeusQueryService, koodisto: KoodistoViitePalvelu, es: ElasticClient) {
  def find(filters: List[OpiskeluoikeusQueryFilter], sorting: OpiskeluoikeusSortOrder, pagination: PaginationSettings)(implicit session: KoskiSession): List[OpiskeluoikeudenPerustiedot] = {

    val execute: Future[RichSearchResponse] = es.execute {
      search("koski") start (pagination.page * pagination.size) limit (pagination.size)
    }

    val a: RichSearchResponse = execute.await


    a.hits.map{ hit =>
      println(Json.writePretty(Json.parse(hit.sourceAsString)))
      Json.read[OpiskeluoikeudenPerustiedot](hit.sourceAsString)
    }.toList
    /*val perustiedotObservable = opiskeluoikeusRepository.streamingQuery(filters, Some(sorting), Some(pagination)).map {
      case (opiskeluoikeusRow, henkilöRow) =>
        val nimitiedotJaOid = henkilöRow.toNimitiedotJaOid
        val oo = opiskeluoikeusRow.toOpiskeluoikeus
        val suoritukset: List[SuorituksenPerustiedot] = oo.suoritukset
          .filterNot(_.isInstanceOf[PerusopetuksenVuosiluokanSuoritus])
          .map { suoritus =>
            val (osaamisala, tutkintonimike) = suoritus match {
              case s: AmmatillisenTutkinnonSuoritus => (s.osaamisala, s.tutkintonimike)
              case s: NäyttötutkintoonValmistavanKoulutuksenSuoritus => (s.osaamisala, s.tutkintonimike)
              case _ => (None, None)
            }
            SuorituksenPerustiedot(suoritus.tyyppi, KoulutusmoduulinPerustiedot(suoritus.koulutusmoduuli.tunniste), osaamisala, tutkintonimike, suoritus.toimipiste)
          }
        OpiskeluoikeudenPerustiedot(nimitiedotJaOid, oo.oppilaitos, oo.alkamispäivä, oo.tyyppi, suoritukset, oo.tila.opiskeluoikeusjaksot.last.tila, opiskeluoikeusRow.luokka)
    }
    perustiedotObservable.toBlocking.toList*/
  }
}

class OpiskeluoikeudenPerustiedotServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Pagination {
  private val repository = new OpiskeluoikeudenPerustiedotRepository(application.henkilöRepository, application.opiskeluoikeusQueryRepository, application.koodistoViitePalvelu, application.es)
  get("/") {
    renderEither({
      val sort = params.get("sort").map {
        str => str.split(":") match {
          case Array(key: String, "asc") => Ascending(key)
          case Array(key: String, "desc") => Descending(key)
          case xs => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Invalid sort param. Expected key:asc or key: desc"))
        }
      }.getOrElse(Ascending("nimi"))

      OpiskeluoikeusQueryFilter.parse(params.toList)(application.koodistoViitePalvelu, application.organisaatioRepository, koskiSession) match {
        case Right(filters) =>
          val result: List[OpiskeluoikeudenPerustiedot] = repository.find(filters, sort, paginationSettings)(koskiSession)
          Right(PaginatedResponse(Some(paginationSettings), result, result.length))
        case Left(HttpStatus(404, _)) =>
          Right(PaginatedResponse(None, List[OpiskeluoikeudenPerustiedot](), 0))
        case e @ Left(_) => e
      }
    })
  }
}