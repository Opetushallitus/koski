package fi.oph.koski.opiskeluoikeus

import java.sql.Timestamp

import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.jsonMethods.{parse => parseJson}
import fi.oph.koski.db.Tables.{HenkilöTable, OpiskeluoikeusTable, _}
import fi.oph.koski.db.{Tables, _}
import fi.oph.koski.henkilo.KoskiHenkilöCache
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter.{SuoritusJsonHaku, _}
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.koski.util.QueryPagination.applyPagination
import fi.oph.koski.util.SortOrder.{Ascending, Descending}
import fi.oph.koski.util.{PaginationSettings, SortOrder}
import rx.Observer
import rx.functions.{Func0, Func2}
import rx.lang.scala.Observable
import rx.Observable.{create => createObservable}
import rx.observables.SyncOnSubscribe.createStateful
import slick.lifted.Query

class OpiskeluoikeusQueryService(val db: DB) extends DatabaseExecutionContext with KoskiDatabaseMethods {
  def oppijaOidsQuery(pagination: Option[PaginationSettings])(implicit user: KoskiSession): Observable[String] = {
    streamingQuery(applyPagination(OpiskeluOikeudetWithAccessCheck.map(_.oppijaOid), pagination))
  }

  def opiskeluoikeusQuery(filters: List[OpiskeluoikeusQueryFilter], sorting: Option[SortOrder], pagination: Option[PaginationSettings])(implicit user: KoskiSession): Observable[(OpiskeluoikeusRow, HenkilöRow, Option[HenkilöRow])] = {
    streamingQuery(mkQuery(filters, sorting, pagination))
  }

  def kaikkiOpiskeluoikeudetSivuittain(pagination: PaginationSettings)(implicit user: KoskiSession): Seq[OpiskeluoikeusRow] = {
    if (!user.hasGlobalReadAccess) throw new RuntimeException("Query does not make sense without global read access")
    // this approach to pagination ("limit 500 offset 176500") is not perfect (the query gets slower as offset
    // increases), but seems tolerable here (with join to henkilot, as in mkQuery below, it's much slower)
    runDbSync(applyPagination(OpiskeluOikeudetWithAccessCheck.sortBy(_.id), pagination).result)
  }

  def mapKaikkiOpiskeluoikeudetSivuittain[A](pageSize: Int, user: KoskiSession)(f: Seq[OpiskeluoikeusRow] => Seq[A]): Observable[A] = {
    processByPage[OpiskeluoikeusRow, A](page => kaikkiOpiskeluoikeudetSivuittain(PaginationSettings(page, pageSize))(user), f)
  }

  private def mkQuery(filters: List[OpiskeluoikeusQueryFilter], sorting: Option[SortOrder], pagination: Option[PaginationSettings])(implicit user: KoskiSession) = {
    val baseQuery = OpiskeluOikeudetWithAccessCheck
      .join(Tables.Henkilöt).on(_.oppijaOid === _.oid)
      .joinLeft(Tables.Henkilöt).on(_._2.masterOid === _.oid)
      .map(stuff => (stuff._1._1, stuff._1._2, stuff._2))

    val query = filters.foldLeft(baseQuery) {
      case (query, OpiskeluoikeusPäättynytAikaisintaan(päivä)) => query.filter(_._1.data.#>>(List("päättymispäivä")) >= päivä.toString)
      case (query, OpiskeluoikeusPäättynytViimeistään(päivä)) => query.filter(_._1.data.#>>(List("päättymispäivä")) <= päivä.toString)
      case (query, OpiskeluoikeusAlkanutAikaisintaan(päivä)) => query.filter(_._1.data.#>>(List("tila", "opiskeluoikeusjaksot", "0", "alku")) >= päivä.toString)
      case (query, OpiskeluoikeusAlkanutViimeistään(päivä)) => query.filter(_._1.data.#>>(List("tila", "opiskeluoikeusjaksot", "0", "alku")) <= päivä.toString)
      case (query, OpiskeluoikeudenTyyppi(tyyppi)) => query.filter(_._1.koulutusmuoto === tyyppi.koodiarvo)
      case (query, OneOfOpiskeluoikeudenTyypit(tyypit)) => query.filter(_._1.koulutusmuoto inSet (tyypit.map(_.tyyppi.koodiarvo)))
      case (query, SuorituksenTyyppi(tyyppi)) => query.filter(_._1.data.+>("suoritukset").@>(parseJson(s"""[{"tyyppi":{"koodiarvo":"${tyyppi.koodiarvo}"}}]""")))
      case (query, OpiskeluoikeudenTila(tila)) => query.filter(_._1.data.#>>(List("tila", "opiskeluoikeusjaksot", "-1", "tila", "koodiarvo")) === tila.koodiarvo)
      case (query, OpiskeluoikeusQueryFilter.Toimipiste(toimipisteet)) =>
        val matchers = toimipisteet.map { toimipiste =>
          parseJson(s"""[{"toimipiste":{"oid": "${toimipiste.oid}"}}]""")
        }
        query.filter(_._1.data.+>("suoritukset").@>(matchers.bind.any))
      case (query, Luokkahaku(hakusana)) =>
        query.filter({ case t: (OpiskeluoikeusTable, HenkilöTable, _) => t._1.luokka ilike (hakusana + "%") })
      case (query, Nimihaku(hakusana)) =>
        query.filter { case (_, henkilö, _) =>
          KoskiHenkilöCache.filterByQuery(hakusana)(henkilö)
        }
      case (query, IdHaku(ids)) => query.filter(_._1.id inSetBind ids)
      case (query, OppijaOidHaku(oids)) => query.filter { case (_, henkilö, masterHenkilö) => (henkilö.oid inSetBind oids) || masterHenkilö.map(_.oid inSetBind oids).getOrElse(false) }
      case (query, SuoritusJsonHaku(json)) => query.filter(_._1.data.+>("suoritukset").@>(json))
      case (query, MuuttunutEnnen(aikaleima)) => query.filter(_._1.aikaleima < Timestamp.from(aikaleima))
      case (query, MuuttunutJälkeen(aikaleima)) => query.filter(_._1.aikaleima >= Timestamp.from(aikaleima))
      case (query, filter) => throw new InvalidRequestException(KoskiErrorCategory.internalError("Hakua ei ole toteutettu: " + filter))
    }

    def alkamispäivä(tuple: (OpiskeluoikeusTable, HenkilöTable, Rep[Option[HenkilöTable]])) = tuple._1.data.#>>(List("alkamispäivä"))
    def luokka(tuple: (OpiskeluoikeusTable, HenkilöTable, Rep[Option[HenkilöTable]])) = tuple._1.luokka
    def nimi(tuple: (OpiskeluoikeusTable, HenkilöTable, Rep[Option[HenkilöTable]])) = (tuple._2.sukunimi.toLowerCase, tuple._2.etunimet.toLowerCase)
    def nimiDesc(tuple: (OpiskeluoikeusTable, HenkilöTable, Rep[Option[HenkilöTable]])) = (tuple._2.sukunimi.toLowerCase.desc, tuple._2.etunimet.toLowerCase.desc)

    val sorted = sorting match {
      case None => query
      case Some(Ascending("id")) => query.sortBy(_._1.id)
      case Some(Ascending("oppijaOid")) => query.sortBy { case (oo, henkilö, masterHenkilö) => (masterHenkilö.map(_.oid).getOrElse(henkilö.oid), oo.id) }
      case Some(Ascending("nimi")) => query.sortBy(nimi)
      case Some(Descending("nimi")) => query.sortBy(nimiDesc)
      case Some(Ascending("alkamispäivä")) => query.sortBy(tuple => (alkamispäivä(tuple), nimi(tuple)))
      case Some(Descending("alkamispäivä")) => query.sortBy(tuple => (alkamispäivä(tuple).desc, nimiDesc(tuple)))
      case Some(Ascending("luokka")) => query.sortBy(tuple => (luokka(tuple), nimi(tuple)))
      case Some(Descending("luokka")) => query.sortBy(tuple => (luokka(tuple).desc, nimiDesc(tuple)))
      case s => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Epäkelpo järjestyskriteeri: " + s))
    }

    applyPagination(sorted, pagination)
  }

  private def processByPage[A, B](loadRows: Int => Seq[A], processRows: Seq[A] => Seq[B]): Observable[B] = {
    import rx.lang.scala.JavaConverters._
    def loadRowsInt(page: Int): (Seq[B], Int, Boolean) = {
      val rows = loadRows(page)
      (processRows(rows), page, rows.isEmpty)
    }
    createObservable(createStateful[(Seq[B], Int, Boolean), Seq[B]](
      (() => loadRowsInt(0)): Func0[_ <: (Seq[B], Int, Boolean)],
      ((state, observer) => {
        val (loadResults, page, done) = state
        observer.onNext(loadResults)
        if (done) {
          observer.onCompleted()
          (Nil, 0, true)
        } else {
          loadRowsInt(page + 1)
        }
      }): Func2[_ >: (Seq[B], Int, Boolean), _ >: Observer[_ >: Seq[B]], _ <: (Seq[B], Int, Boolean)]
    )).asScala.flatMap(Observable.from(_))
  }
}
