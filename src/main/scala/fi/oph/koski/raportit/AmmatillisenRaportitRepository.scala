package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.db.DB
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.util.DateOrdering.sqlDateOrdering

import slick.jdbc.GetResult
import scala.concurrent.duration.DurationInt

case class AmmatillisenRaportitRepository(db: DB) extends QueryMethods with RaportointikantaTableQueries {

  private val defaultTimeout = 10.minutes

  private type OpiskeluoikeusOid = String
  private type SisältyvOpiskeluoikeuteenOid = String
  private type AikajaksoId = Long
  private type PäätasonSuoritusId = Long
  private type OppijaOid = String

  def suoritustiedot(oppilaitos: Organisaatio.Oid, koulutusmuoto: String, suorituksenTyyppi: String, alku: LocalDate, loppu: LocalDate) = {
    val opiskeluoikeusAikajaksotVarsinaisetPäätasonSuoritukset = opiskeluoikeusAikajaksotPäätasonSuorituksetQuery(oppilaitos, koulutusmuoto, suorituksenTyyppi, alku, loppu)
    val opiskeluoikeusAikajaksotNäyttötutkintoonValmistavatPäätasonSuoritukset = opiskeluoikeusAikajaksotPäätasonSuorituksetQuery(oppilaitos, koulutusmuoto, "nayttotutkintoonvalmistavakoulutus", alku, loppu)
    val masterOpiskeluoikeusOids = (opiskeluoikeusAikajaksotVarsinaisetPäätasonSuoritukset ++ opiskeluoikeusAikajaksotNäyttötutkintoonValmistavatPäätasonSuoritukset).map(_._1)

    val sisältyvätOpiskeluoikeusAikajaksotPäätasonSuoritukset = sisältyvätOpiskeluoikeusAikajaksotPäätasonSuorituksetQuery(masterOpiskeluoikeusOids)
    val sisältyvätOpiskeluoikeusOids = sisältyvätOpiskeluoikeusAikajaksotPäätasonSuoritukset.map(_._1)
    val sisältyvätOpiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet sisältyvätOpiskeluoikeusOids).result, timeout = defaultTimeout)
    val sisältyvätOpiskeluoikeudetGrouped = sisältyvätOpiskeluoikeudet.groupBy(_.sisältyyOpiskeluoikeuteenOid.get)

    val opiskeluoikeusAikajaksotNäyttötutkintoonValmistavatPäätasonSuorituksetJoillaPariPäätasonSuorituksissaTaiSisältyvissäOpiskeluoikeuksissa =
      opiskeluoikeusAikajaksotNäyttötutkintoonValmistavatPäätasonSuoritukset.filter(nvp => {
        opiskeluoikeusAikajaksotVarsinaisetPäätasonSuoritukset.exists(_._1 == nvp._1) || sisältyvätOpiskeluoikeudetGrouped.contains(nvp._1)
      }
    )
    val opiskeluoikeusAikajaksotPäätasonSuoritukset = opiskeluoikeusAikajaksotVarsinaisetPäätasonSuoritukset ++ opiskeluoikeusAikajaksotNäyttötutkintoonValmistavatPäätasonSuorituksetJoillaPariPäätasonSuorituksissaTaiSisältyvissäOpiskeluoikeuksissa

    val päätasonSuoritusIds = opiskeluoikeusAikajaksotPäätasonSuoritukset.flatMap(_._2) ++ sisältyvätOpiskeluoikeusAikajaksotPäätasonSuoritukset.flatMap(_._2)
    val aikajaksoIds = opiskeluoikeusAikajaksotPäätasonSuoritukset.flatMap(_._3) ++ sisältyvätOpiskeluoikeusAikajaksotPäätasonSuoritukset.flatMap(_._3)

    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet masterOpiskeluoikeusOids).result, timeout = defaultTimeout) ++ sisältyvätOpiskeluoikeudet
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.id inSet aikajaksoIds).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)
    val päätasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.päätasonSuoritusId inSet päätasonSuoritusIds).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)
    val osasuoritukset = runDbSync(ROsasuoritukset.filter(_.päätasonSuoritusId inSet päätasonSuoritusIds).result, timeout = defaultTimeout).groupBy(_.päätasonSuoritusId)
    val henkilöt = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid)).result, timeout = defaultTimeout).groupBy(_.oppijaOid).view.mapValues(_.head).toMap

    opiskeluoikeudet.flatMap { opiskeluoikeus =>
      päätasonSuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).map { päätasonSuoritus =>
        (
          opiskeluoikeus,
          henkilöt(opiskeluoikeus.oppijaOid),
          aikajaksot.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Seq.empty).map(_.truncateToDates(alku, loppu)).sortBy(_.alku)(sqlDateOrdering),
          päätasonSuoritus,
          sisältyvätOpiskeluoikeudetGrouped.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Seq.empty),
          osasuoritukset.getOrElse(päätasonSuoritus.päätasonSuoritusId, Seq.empty)
        )
      }
    }
}

  private def opiskeluoikeusAikajaksotPäätasonSuorituksetQuery(oppilaitosOid: String, koulutusmuoto: String, suorituksenTyyppi: String, alku: LocalDate, loppu: LocalDate) = {
    implicit val getResult = GetResult[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])](r => (r.nextString(), r.nextArray(), r.nextArray()))
    runDbSync(opiskeluoikeusAikajaksotPäätasonSuorituksetSQL(oppilaitosOid, koulutusmuoto, suorituksenTyyppi, alku, loppu).as[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])], timeout = defaultTimeout)
  }

  private def opiskeluoikeusAikajaksotPäätasonSuorituksetSQL(oppilaitosOid: String, koulutusmuoto: String, suorituksenTyyppi: String, alku: LocalDate, loppu: LocalDate) = {
    sql"""
      select
        oo.opiskeluoikeus_oid,
        array_agg(pts.paatason_suoritus_id),
        array_agg(aikaj.id)
      from r_opiskeluoikeus oo
      join r_paatason_suoritus pts
        on pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      join r_opiskeluoikeus_aikajakso aikaj
        on aikaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      where
        oo.oppilaitos_oid = $oppilaitosOid and
        oo.koulutusmuoto = $koulutusmuoto and
        pts.suorituksen_tyyppi = $suorituksenTyyppi and
        aikaj.alku <= $loppu and aikaj.loppu >= $alku
      group by oo.opiskeluoikeus_oid
       """
  }

  private def sisältyvätOpiskeluoikeusAikajaksotPäätasonSuorituksetQuery(masterOids: Seq[OpiskeluoikeusOid]) = {
    implicit val getResult = GetResult[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])](r => (r.nextString(), r.nextArray(), r.nextArray()))
    runDbSync(sisältyvätOpiskeluoikeusAikajaksotPäätasonSuorituksetSQL(masterOids).as[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])], timeout = defaultTimeout)
  }

  private def sisältyvätOpiskeluoikeusAikajaksotPäätasonSuorituksetSQL(masterOids: Seq[OpiskeluoikeusOid]) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    sql"""
      select
        oo.opiskeluoikeus_oid,
        array_agg(pts.paatason_suoritus_id),
        array_agg(aikaj.id)
      from r_opiskeluoikeus oo
      join r_paatason_suoritus pts
        on pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      join r_opiskeluoikeus_aikajakso aikaj
        on aikaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      where
        (oo.sisaltyy_opiskeluoikeuteen_oid = any ($masterOids))
      group by oo.opiskeluoikeus_oid
      """
  }
}
