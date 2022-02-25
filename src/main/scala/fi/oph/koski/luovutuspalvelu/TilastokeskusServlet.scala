package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{HenkilöRow, OpiskeluoikeusRow}
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresTilastokeskus}
import fi.oph.koski.log.KoskiAuditLogMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_HAKU
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter.NotSuorituksenTyyppi
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryContext, OpiskeluoikeusQueryFilter, QueryOppijaHenkilö}
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.SuorituksenTyyppi.vstvapaatavoitteinenkoulutus
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache, ObservableSupport}
import fi.oph.koski.util.SortOrder.Ascending
import fi.oph.koski.util.{Pagination, PaginationSettings, QueryPagination, Retry}

import javax.servlet.http.HttpServletRequest
import org.json4s.JValue
import org.scalatra.MultiParams
import rx.lang.scala.Observable

class TilastokeskusServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with ObservableSupport with NoCache with Pagination with RequiresTilastokeskus {

  override protected val maxNumberOfItemsPerPage: Int = 1000

  get("/") {
    if (!getOptionalIntegerParam("v").contains(1)) {
      haltWithStatus(KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
    }

    streamResponse(queryOppijat, session)
  }

  private def queryOppijat =
    TilastokeskusQueryContext(request)(session, application)
      .queryLaajoillaHenkilöTiedoilla(multiParams, paginationSettings)
}

case class TilastokeskusQueryContext(request: HttpServletRequest)(implicit koskiSession: KoskiSpecificSession, application: KoskiApplication) extends Logging {
  val exclusionFilters = List(NotSuorituksenTyyppi(vstvapaatavoitteinenkoulutus))

  def queryLaajoillaHenkilöTiedoilla(params: MultiParams, paginationSettings: Option[PaginationSettings]): Either[HttpStatus, Observable[JValue]] = {
    logger(koskiSession).info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))
    OpiskeluoikeusQueryFilter.parse(params)(application.koodistoViitePalvelu, application.organisaatioService, koskiSession).map { filters =>
      AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_HAKU, koskiSession, Map(hakuEhto -> OpiskeluoikeusQueryContext.queryForAuditLog(params))))
      query(filters ::: exclusionFilters, paginationSettings)
    }.map {
      _.map(x => (laajatHenkilötiedotToTilastokeskusHenkilötiedot(x._1), x._2))
       .map(tuple => JsonSerializer.serialize(TilastokeskusOppija(tuple._1, tuple._2.map(_.toOpiskeluoikeusUnsafe))))
    }
  }

  private def query(filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings]): Observable[(LaajatOppijaHenkilöTiedot, List[OpiskeluoikeusRow])] = {
    val groupedByOid = OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, filters, paginationSettings).tumblingBuffer(10)
    val oppijaStream = groupedByOid.flatMap { oppijatJaOidit: Seq[(QueryOppijaHenkilö, List[OpiskeluoikeusRow])] =>
      val oids: List[String] = oppijatJaOidit.map(_._1.oid).toList
      val henkilöt: Map[Oid, LaajatOppijaHenkilöTiedot] = Retry.retryWithInterval(2, 500) {
        application.opintopolkuHenkilöFacade.findMasterOppijat(oids)
      }

      // Huomioi: Tämä flatMappäily purkaa (aiemmin turhaan tehdyn) oppijaryhmittelyn.
      val oppijat: Seq[(LaajatOppijaHenkilöTiedot, List[OpiskeluoikeusRow])] = oppijatJaOidit.flatMap { case (oppijaHenkilö, opiskeluOikeudet) =>
        opiskeluOikeudet.flatMap { oo =>
          henkilöt.get(oppijaHenkilö.oid) match {
            case Some(henkilö) =>
              List((henkilö.copy(linkitetytOidit = oppijaHenkilö.linkitetytOidit), List(oo)))
            case None =>
              logger(koskiSession).warn("Oppijaa " + oppijaHenkilö.oid + " ei löydy henkilöpalvelusta")
              Nil
          }
        }
      }
      Observable.from(oppijat)
    }

    paginationSettings match {
      case None => oppijaStream
      case Some(PaginationSettings(_, size)) => oppijaStream.take(size)
    }
  }

  private def laajatHenkilötiedotToTilastokeskusHenkilötiedot(laajat: LaajatOppijaHenkilöTiedot): TilastokeskusHenkilötiedot = {
    val täydelliset: TäydellisetHenkilötiedot = application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(laajat)

    TilastokeskusHenkilötiedot(
      oid = täydelliset.oid,
      hetu = täydelliset.hetu,
      syntymäaika = täydelliset.syntymäaika,
      etunimet = täydelliset.etunimet,
      kutsumanimi = täydelliset.kutsumanimi,
      sukunimi = täydelliset.sukunimi,
      sukupuoli = laajat.sukupuoli,
      kotikunta = laajat.kotikunta,
      äidinkieli = täydelliset.äidinkieli,
      kansalaisuus = täydelliset.kansalaisuus,
      turvakielto = laajat.turvakielto,
      linkitetytOidit = laajat.linkitetytOidit
    )
  }

}

case class TilastokeskusOppija(
  henkilö: TilastokeskusHenkilötiedot,
  opiskeluoikeudet: Seq[Opiskeluoikeus]
)

case class TilastokeskusHenkilötiedot(
  oid: Henkilö.Oid,
  hetu: Option[Henkilö.Hetu],
  syntymäaika: Option[LocalDate],
  etunimet: String,
  kutsumanimi: String,
  sukunimi: String,
  sukupuoli: Option[String],
  kotikunta: Option[String],
  äidinkieli: Option[Koodistokoodiviite],
  kansalaisuus: Option[List[Koodistokoodiviite]],
  turvakielto: Boolean,
  linkitetytOidit: List[Oid]
)
