package fi.oph.koski.opiskeluoikeus

import java.sql.{Date, Timestamp}

import fi.oph.koski.db.Tables._
import fi.oph.koski.db._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter._
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.koski.util.PaginationSettings
import fi.oph.koski.util.QueryPagination.applyPagination
import org.json4s.JValue
import rx.Observable.{create => createObservable}
import rx.Observer
import rx.functions.{Func0, Func2}
import rx.lang.scala.Observable
import rx.observables.SyncOnSubscribe.createStateful
import slick.jdbc.{GetResult, PositionedParameters, SQLActionBuilder}


class OpiskeluoikeusQueryService(val db: DB) extends DatabaseExecutionContext with KoskiDatabaseMethods {
  import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
  implicit private val getQueryOppija = GetResult(r =>
    QueryOppija(
      henkilö = QueryOppijaHenkilö(
        oid = r.nextString,
        linkitetytOidit = Nil
      ),
      opiskeluoikeudet = parseOpiskeluoikeudet(r.nextJson)
    )
  )

  implicit private val getPreQueryResult = GetResult(r =>
    PreQueryRow(
      oppijaOid = r.nextString,
      linkitetytOidit = r.nextArray.toList,
      opiskeluoikeusOidit = r.nextArray.toList
    )
  )

  def oppijaOidsQuery(pagination: Option[PaginationSettings])(implicit user: KoskiSession): Observable[String] = {
    streamQuery(applyPagination(OpiskeluOikeudetWithAccessCheck.map(_.oppijaOid), pagination))
  }

  def muuttuneetOpiskeluoikeudetWithoutAccessCheck(after: Timestamp, afterId: Int, limit: Int): Seq[MuuttunutOpiskeluoikeusRow] = {
    // huom, tässä halutaan myös mitätöidyt, sen takia OpiskeluOikeudet eikä OpiskeluOikeudetWithAccessCheck
    runDbSync(
      OpiskeluOikeudet
        .filter(r => (r.aikaleima === after && r.id > afterId) || (r.aikaleima > after))
        .sortBy(r => (r.aikaleima, r.id))
        .map(r => (r.id, r.aikaleima, r.oppijaOid))
        .take(limit)
        .result
    ).map(MuuttunutOpiskeluoikeusRow.tupled)
  }

  def serverCurrentTimestamp: Timestamp = {
    runDbSync(sql"select current_timestamp".as[Timestamp])(0)
  }

  def kaikkiOppijat(implicit u: KoskiSession): Observable[(OpiskeluoikeusRow, HenkilöRow, Option[HenkilöRow])] = {
    val query = OpiskeluOikeudetWithAccessCheck
      .join(Tables.Henkilöt).on(_.oppijaOid === _.oid)
      .joinLeft(Tables.Henkilöt).on(_._2.masterOid === _.oid)
      .map { case ((oo, h), m) => (oo, h, m) }
    streamQuery(query)
  }

  def mapKaikkiOpiskeluoikeudetSivuittain[A](pageSize: Int, user: KoskiSession)(f: Seq[OpiskeluoikeusRow] => Seq[A]): Observable[A] = {
    processByPage[OpiskeluoikeusRow, A](page => kaikkiOpiskeluoikeudetSivuittain(PaginationSettings(page, pageSize))(user), f)
  }

  private def kaikkiOpiskeluoikeudetSivuittain(pagination: PaginationSettings)(implicit user: KoskiSession): Seq[OpiskeluoikeusRow] = {
    if (!user.hasGlobalReadAccess) throw new RuntimeException("Query does not make sense without global read access")
    // this approach to pagination ("limit 500 offset 176500") is not perfect (the query gets slower as offset
    // increases), but seems tolerable here (with join to henkilot, as in mkQuery below, it's much slower)
    runDbSync(applyPagination(OpiskeluOikeudetWithAccessCheck.sortBy(_.id), pagination).result)
  }

  val defaultPageSize = 1000

  def opiskeluoikeusQuery(
    filters: List[OpiskeluoikeusQueryFilter],
    paginationSettings: Option[PaginationSettings]
  )(implicit u: KoskiSession): Observable[QueryOppija] = {
    val pagination = paginationSettings.getOrElse(PaginationSettings(0, defaultPageSize))
    val q: PreQuery = preQuery(filters, pagination)
    streamAction(query2(q, filters)).map { row => row.copy(
      henkilö = row.henkilö.copy(linkitetytOidit = q.linkitetytOidit(row.henkilö.oid))
    )}
  }

  private def preQuery(filters: List[OpiskeluoikeusQueryFilter], pagination: PaginationSettings)(implicit u: KoskiSession) = {
    PreQuery(runDbSync(concat(sql"""
      SELECT h.oid, array_agg(distinct s.oid) as linkitetytOidit, array_agg(oo.oid) as opiskeluoikeusOidit
      FROM henkilo h
      LEFT JOIN henkilo s ON s.master_oid = h.oid
      JOIN opiskeluoikeus oo ON oo.oppija_oid = h.oid
      WHERE h.master_oid IS NULL AND NOT oo.mitatoity """,
      queryFilters(filters),
      sql"""
      GROUP BY h.oid
      ORDER BY lower(h.sukunimi), lower(h.etunimet)
      LIMIT ${pagination.size} OFFSET ${pagination.page * pagination.size}
    """).as[PreQueryRow]))
  }

  private def query2(oppijat: PreQuery, filters: List[OpiskeluoikeusQueryFilter])(implicit u: KoskiSession) = {
    concat(sql"""
    SELECT COALESCE(s.master_oid, oo.oppija_oid) AS oid, json_agg(json_build_object('data', oo.data, 'oidVersionumeroAikaleima', row_to_json((SELECT a FROM (SELECT oo.oid, oo.aikaleima, oo.versionumero) a)) :: jsonb)) AS opiskeluoikeudet
    FROM opiskeluoikeus oo
    JOIN henkilo h ON h.oid = oo.oppija_oid
    LEFT OUTER JOIN henkilo s ON oo.oppija_oid = s.oid AND s.master_oid IS NOT NULL
    WHERE oo.oid IN (#${mkString(oppijat.opiskeluoikeusOidit)}) OR (oo.oppija_oid IN (#${mkString(oppijat.kaikkiLinkitetytOidit)})""",
    queryFilters(filters), sql""")
    GROUP BY COALESCE(s.master_oid, oo.oppija_oid)
    ORDER BY max(lower(h.sukunimi)), max(lower(h.etunimet));
    """).as[QueryOppija]
  }

  private def queryFilters(filters: List[OpiskeluoikeusQueryFilter])(implicit u: KoskiSession) = filters.foldLeft(accessCheck) {
    case (q, OpiskeluoikeusPäättynytAikaisintaan(päivä)) =>
      concat(q, sql" AND oo.paattymispaiva >= ${Date.valueOf(päivä)}")
    case (q, OpiskeluoikeusPäättynytViimeistään(päivä)) =>
      concat(q, sql" AND oo.paattymispaiva <= ${Date.valueOf(päivä)}")
    case (q, OpiskeluoikeusAlkanutAikaisintaan(päivä)) =>
      concat(q, sql" AND oo.alkamispaiva >= ${Date.valueOf(päivä)}")
    case (q, OpiskeluoikeusAlkanutViimeistään(päivä)) =>
      concat(q, sql" AND oo.alkamispaiva <= ${Date.valueOf(päivä)}")
    case (q, OpiskeluoikeudenTyyppi(tyyppi)) =>
      concat(q, sql" AND oo.koulutusmuoto = ${tyyppi.koodiarvo}")
    case (q, OneOfOpiskeluoikeudenTyypit(tyypit)) =>
      concat(q, sql" AND oo.koulutusmuoto IN (#${mkString(tyypit.map(_.tyyppi.koodiarvo))})")
    case (q, OpiskeluoikeudenTila(tila)) =>
      concat(q, sql" AND oo.data -> 'tila' -> 'opiskeluoikeusjaksot' -> -1 -> 'tila' ->> 'koodiarvo' = ${tila.koodiarvo}")
    case (q, OppijaOidHaku(oids)) =>
      concat(q, sql" AND oo.oppija_oid IN (#${mkString(oids)})")
    case (q, MuuttunutEnnen(aikaleima)) =>
      concat(q, sql" AND oo.aikaleima < ${Timestamp.from(aikaleima)}")
    case (q, MuuttunutJälkeen(aikaleima)) =>
      concat(q, sql" AND oo.aikaleima >= ${Timestamp.from(aikaleima)}")
    case (q, SuorituksenTyyppi(tyyppi)) =>
      concat(q, sql""" AND (oo.data -> 'suoritukset') @> '[{"tyyppi":{"koodiarvo":"#${tyyppi.koodiarvo}"}}]'""")
    case (q, filter) => throw new InvalidRequestException(KoskiErrorCategory.internalError("Hakua ei ole toteutettu: " + filter))
  }

  private def accessCheck(implicit u: KoskiSession) = {
    val query = if (u.hasGlobalReadAccess || u.hasGlobalKoulutusmuotoReadAccess) {
      sql""
    } else {
      val oppilaitosOidit = mkString(u.organisationOids(AccessType.read))
      val varhaiskasvatusOikeudet = u.varhaiskasvatusKäyttöoikeudet.filter(_.organisaatioAccessType.contains(AccessType.read))
      sql"""
      AND (
        oo.oppilaitos_oid IN (#$oppilaitosOidit) OR
        oo.sisaltava_opiskeluoikeus_oppilaitos_oid IN (#$oppilaitosOidit) OR
        (oo.oppilaitos_oid IN (#${mkString(varhaiskasvatusOikeudet.map(_.ulkopuolinenOrganisaatio.oid))}) AND oo.koulutustoimija_oid IN (#${mkString(varhaiskasvatusOikeudet.map(_.koulutustoimija.oid))}))
      )
      """
    }

    if (u.hasKoulutusmuotoRestrictions) {
      concat(query, sql" AND oo.koulutusmuoto IN (#${mkString(u.allowedOpiskeluoikeusTyypit)})")
    } else {
      query
    }
  }

  private def concat(parts: SQLActionBuilder*) = SQLActionBuilder(
    queryParts = parts.flatMap(_.queryParts),
    unitPConv = (p: Unit, pp: PositionedParameters) => parts.foreach(_.unitPConv(p, pp))
  )

  private def parseOpiskeluoikeudet(json: JValue): List[KoskeenTallennettavaOpiskeluoikeus] = {
    JsonSerializer.extract[List[QueryOpiskeluoikeus]](json)
      .map { case QueryOpiskeluoikeus(OidVersionumeroAikaleima(oid, versionumero, aikaleima), data) =>
        data.withOidAndVersion(Some(oid), Some(versionumero)).withAikaleima(Some(aikaleima.toLocalDateTime))
      }.sortBy(_.oid)
  }

  private def mkString(xs: Iterable[String]) = xs.mkString("'", "','", "'")

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

case class PreQuery(rows: Seq[PreQueryRow]) {
  private val slaveOidit: Map[String, List[String]] = rows.flatMap(o => o.linkitetytOidit.flatMap(Option(_)) match {
    case Nil => Nil
    case xs => List(o.oppijaOid -> xs)
  }).toMap

  val opiskeluoikeusOidit: Seq[String] = rows.flatMap(_.opiskeluoikeusOidit)

  def kaikkiLinkitetytOidit: Set[String] = slaveOidit.values.flatten.toSet
  def linkitetytOidit(oid: String): List[String] = slaveOidit.get(oid).toList.flatten
}

case class PreQueryRow(oppijaOid: String, linkitetytOidit: List[String], opiskeluoikeusOidit: List[String])
case class MuuttunutOpiskeluoikeusRow(id: Int, aikaleima: Timestamp, oppijaOid: String)
case class QueryOppija(henkilö: QueryOppijaHenkilö, opiskeluoikeudet: List[KoskeenTallennettavaOpiskeluoikeus])
case class QueryOppijaHenkilö(oid: Oid, linkitetytOidit: List[Oid])
case class QueryOpiskeluoikeus(oidVersionumeroAikaleima: OidVersionumeroAikaleima, data: KoskeenTallennettavaOpiskeluoikeus)
case class OidVersionumeroAikaleima(oid: String, versionumero: Int, aikaleima: Timestamp)
