package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.Tables.{HenkilöTable, OpiskeluoikeusTable, _}
import fi.oph.koski.db.{Tables, _}
import fi.oph.koski.henkilo.KoskiHenkilöCache
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter.{SuoritusJsonHaku, _}
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.koski.util.SortOrder.{Ascending, Descending}
import fi.oph.koski.util.{PaginationSettings, QueryPagination, ReactiveStreamsToRx, SortOrder}
import rx.lang.scala.Observable
import slick.lifted.Query
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.jsonMethods.{parse => parseJson}

class OpiskeluoikeusQueryService(val db: DB) extends GlobalExecutionContext with KoskiDatabaseMethods with Logging with SerializableTransactions {
  def oppijaOidsQuery(pagination: Option[PaginationSettings])(implicit user: KoskiSession): Observable[String] = {
    streamingQuery(OpiskeluOikeudetWithAccessCheck.map(_.oppijaOid), pagination)
  }

  def streamingQuery(filters: List[OpiskeluoikeusQueryFilter], sorting: Option[SortOrder], pagination: Option[PaginationSettings])(implicit user: KoskiSession): Observable[(OpiskeluoikeusRow, HenkilöRow)] = {
    val query = filters.foldLeft(OpiskeluOikeudetWithAccessCheck.asInstanceOf[Query[OpiskeluoikeusTable, OpiskeluoikeusRow, Seq]] join Tables.Henkilöt on (_.oppijaOid === _.oid)) {
      case (query, OpiskeluoikeusPäättynytAikaisintaan(päivä)) => query.filter(_._1.data.#>>(List("päättymispäivä")) >= päivä.toString)
      case (query, OpiskeluoikeusPäättynytViimeistään(päivä)) => query.filter(_._1.data.#>>(List("päättymispäivä")) <= päivä.toString)
      case (query, OpiskeluoikeusAlkanutAikaisintaan(päivä)) => query.filter(_._1.data.#>>(List("alkamispäivä")) >= päivä.toString)
      case (query, OpiskeluoikeusAlkanutViimeistään(päivä)) => query.filter(_._1.data.#>>(List("alkamispäivä")) <= päivä.toString)
      case (query, SuorituksenTila(tila)) => query.filter(_._1.data.+>("suoritukset").@>(parseJson(s"""[{"tila":{"koodiarvo":"${tila.koodiarvo}"}}]""")))
      case (query, OpiskeluoikeudenTyyppi(tyyppi)) => query.filter(_._1.data.#>>(List("tyyppi", "koodiarvo")) === tyyppi.koodiarvo)
      case (query, SuorituksenTyyppi(tyyppi)) => query.filter(_._1.data.+>("suoritukset").@>(parseJson(s"""[{"tyyppi":{"koodiarvo":"${tyyppi.koodiarvo}"}}]""")))
      case (query, OpiskeluoikeudenTila(tila)) => query.filter(_._1.data.#>>(List("tila", "opiskeluoikeusjaksot", "-1", "tila", "koodiarvo")) === tila.koodiarvo)
      case (query, OpiskeluoikeusQueryFilter.Toimipiste(toimipisteet)) =>
        val matchers = toimipisteet.map { toimipiste =>
          parseJson(s"""[{"toimipiste":{"oid": "${toimipiste.oid}"}}]""")
        }
        query.filter(_._1.data.+>("suoritukset").@>(matchers.bind.any))
      case (query, Luokkahaku(hakusana)) =>
        query.filter({ case t: (Tables.OpiskeluoikeusTable, Tables.HenkilöTable) => t._1.luokka ilike (hakusana + "%")})
      case (query, Nimihaku(hakusana)) =>
        query.filter{ case (_, henkilö) =>
          KoskiHenkilöCache.filterByQuery(hakusana)(henkilö)
        }
      case (query, SuoritusJsonHaku(json)) => query.filter(_._1.data.+>("suoritukset").@>(json))
      case (query, filter) => throw new InvalidRequestException(KoskiErrorCategory.internalError("Hakua ei ole toteutettu: " + filter))
    }

    def ap(tuple: (OpiskeluoikeusTable, HenkilöTable)) = tuple._1.data.#>>(List("alkamispäivä"))
    def luokka(tuple: (OpiskeluoikeusTable, HenkilöTable)) = tuple._1.luokka
    def nimi(tuple: (OpiskeluoikeusTable, HenkilöTable)) = (tuple._2.sukunimi.toLowerCase, tuple._2.etunimet.toLowerCase)
    def nimiDesc(tuple: (OpiskeluoikeusTable, HenkilöTable)) = (tuple._2.sukunimi.toLowerCase.desc, tuple._2.etunimet.toLowerCase.desc)

    val sorted = sorting match {
      case None => query
      case Some(Ascending("oppijaOid")) => query.sortBy(_._2.oid)
      case Some(Ascending("nimi")) => query.sortBy(nimi)
      case Some(Descending("nimi")) => query.sortBy(nimiDesc)
      case Some(Ascending("alkamispäivä")) => query.sortBy(tuple => (ap(tuple), nimi(tuple)))
      case Some(Descending("alkamispäivä")) => query.sortBy(tuple => (ap(tuple).desc, nimiDesc(tuple)))
      case Some(Ascending("luokka")) => query.sortBy(tuple => (luokka(tuple), nimi(tuple)))
      case Some(Descending("luokka")) => query.sortBy(tuple => (luokka(tuple).desc, nimiDesc(tuple)))
      case s => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Epäkelpo järjestyskriteeri: " + s))
    }

    streamingQuery(sorted, pagination)
  }

  private def streamingQuery[E, U, C[_]](query: Query[E, U, C], pagination: Option[PaginationSettings]) = {
    import ReactiveStreamsToRx._
    val paginated = QueryPagination.applyPagination(query, pagination)
    // Note: it won't actually stream unless you use both `transactionally` and `fetchSize`. It'll collect all the data into memory.
    db.stream(paginated.result.transactionally.withStatementParameters(fetchSize = 1000)).publish.refCount
  }

}
