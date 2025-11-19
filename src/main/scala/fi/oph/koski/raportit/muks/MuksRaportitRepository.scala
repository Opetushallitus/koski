package fi.oph.koski.raportit.muks

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.util.DateOrdering.sqlDateOrdering
import slick.jdbc.{GetResult, SQLActionBuilder}

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

case class MuksRaportitRepository(raportointiDatabase: RaportointiDatabase) extends QueryMethods with RaportointikantaTableQueries {
  val db: DB = raportointiDatabase.db
  private val defaultTimeout = 5.minutes

  def suoritustiedot(
    oppilaitosOids: Set[Organisaatio.Oid],
    alku: LocalDate,
    loppu: LocalDate,
  ): Seq[MuksRaporttiRows] = {
    val opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet =
      opiskeluoikeusAikajaksotPaatasonSuorituksetResult(
        oppilaitosOids,
        alku,
        loppu,
      )

    val opiskeluoikeusOids = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.map(_.opiskeluoikeusOid)
    val päätasonSuoritusIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.map(_.päätasonSuoritusId)
    val aikajaksoIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_.aikajaksoIds)

    val opiskeluoikeudet = runDbSync(
      ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result,
      timeout = defaultTimeout,
    )

    val aikajaksot = runDbSync(
      ROpiskeluoikeusAikajaksot.filter(_.id inSet aikajaksoIds).result,
      timeout = defaultTimeout,
    ).groupBy(_.opiskeluoikeusOid)

    val paatasonSuoritukset = runDbSync(
      RPäätasonSuoritukset.filter(_.päätasonSuoritusId inSet päätasonSuoritusIds).result,
      timeout = defaultTimeout,
    ).groupBy(_.opiskeluoikeusOid)

    val osasuoritukset = runDbSync(
      ROsasuoritukset.filter(_.päätasonSuoritusId inSet päätasonSuoritusIds).result,
      timeout = defaultTimeout,
    ).groupBy(_.päätasonSuoritusId)

    val henkilot = runDbSync(
      RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct).result,
      timeout = defaultTimeout,
    ).groupBy(_.oppijaOid).view.mapValues(_.head).toMap

    opiskeluoikeudet.foldLeft[Seq[MuksRaporttiRows]](Seq.empty) {
      combineOpiskeluoikeusWith(_, _, aikajaksot, paatasonSuoritukset, osasuoritukset, henkilot)
    }
  }

  private def combineOpiskeluoikeusWith(
    acc: Seq[MuksRaporttiRows],
    opiskeluoikeus: ROpiskeluoikeusRow,
    aikajaksot: Map[String, Seq[ROpiskeluoikeusAikajaksoRow]],
    päätasonSuoritukset: Map[String, Seq[RPäätasonSuoritusRow]],
    osasuoritukset: Map[Long, Seq[ROsasuoritusRow]],
    henkilot: Map[String, RHenkilöRow],
  ) = {
    päätasonSuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).map(päätasonsuoritus =>
      MuksRaporttiRows(
        opiskeluoikeus,
        henkilot(opiskeluoikeus.oppijaOid),
        aikajaksot.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).sortBy(_.alku)(sqlDateOrdering),
        päätasonsuoritus,
        osasuoritukset.getOrElse(päätasonsuoritus.päätasonSuoritusId, Nil),
      )
    ) ++ acc
  }
  private def opiskeluoikeusAikajaksotPaatasonSuorituksetResult(
    oppilaitosOids: Set[String],
    alku: LocalDate,
    loppu: LocalDate,
  ): Seq[OpiskeluoikeusPtsAikajaksot] =
    runDbSync(
      opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitosOids.toSeq, alku, loppu).as[OpiskeluoikeusPtsAikajaksot],
      timeout = defaultTimeout,
    )

  implicit val getResult: GetResult[OpiskeluoikeusPtsAikajaksot] = GetResult[OpiskeluoikeusPtsAikajaksot](pr =>
      OpiskeluoikeusPtsAikajaksot(
        pr.nextString(),
        pr.nextLong(),
        pr.nextArray(),
      ))

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(
    oppilaitosOids: Seq[String],
    alku: LocalDate,
    loppu: LocalDate,
  ): SQLActionBuilder = {
    sql"""
      SELECT
        oo.opiskeluoikeus_oid,
        pts.paatason_suoritus_id,
        array_agg(aikajakso.id)
      FROM r_opiskeluoikeus oo
      JOIN r_paatason_suoritus pts ON pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      JOIN r_opiskeluoikeus_aikajakso aikajakso ON aikajakso.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      WHERE
        oo.oppilaitos_oid = any($oppilaitosOids) AND
        pts.suorituksen_tyyppi = 'muukuinsaanneltykoulutus' AND
        aikajakso.alku <= $loppu AND
        aikajakso.loppu >= $alku
      GROUP BY
        oo.opiskeluoikeus_oid,
        pts.paatason_suoritus_id
    """
  }

  case class OpiskeluoikeusPtsAikajaksot(
    opiskeluoikeusOid: String,
    päätasonSuoritusId: Long,
    aikajaksoIds: Seq[Long],
  )
}

case class MuksRaporttiRows(
  opiskeluoikeus: ROpiskeluoikeusRow,
  henkilö: RHenkilöRow,
  aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow],
  päätasonSuoritus: RPäätasonSuoritusRow,
  osasuoritukset: Seq[ROsasuoritusRow],
)
