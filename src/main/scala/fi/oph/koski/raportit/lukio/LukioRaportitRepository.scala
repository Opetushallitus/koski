package fi.oph.koski.raportit.lukio

import fi.oph.koski.db.QueryMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.DB
import fi.oph.koski.raportit.RaporttiUtils.arvioituAikavälillä
import fi.oph.koski.raportit.YleissivistäväRaporttiRows
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.util.DateOrdering.sqlDateOrdering
import slick.jdbc.GetResult

import java.sql.Date
import java.time.LocalDate
import scala.concurrent.duration.DurationInt

case class LukioRaportitRepository(db: DB) extends QueryMethods with RaportointikantaTableQueries {

  private val defaultTimeout = 5.minutes
  private type OpiskeluoikeusOid = String
  private type OppijaOid = String
  private type PäätasonSuoritusId = Long
  private type AikajaksoId = Long
  private type Koulutusmooduuli_koodiarvo = String
  private type OppiaineenNimi = String
  private type Koulutusmoduuli_Paikallinen = Boolean

  def suoritustiedot(
    oppilaitosOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    osasuoritustenAikarajaus: Boolean,
    kotikuntaPvm: Option[LocalDate],
  ): Seq[LukioRaporttiRows] = {
    val opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet = opiskeluoikeusAikajaksotPaatasonSuorituksetResult(oppilaitosOid, alku, loppu)

    val opiskeluoikeusOids = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.map(_._1)
    val paatasonSuoritusIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_._2)
    val aikajaksoIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_._3)

    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result, timeout = defaultTimeout)
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.id inSet aikajaksoIds).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)
    val paatasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)

    val osasuoritukset = runDbSync(ROsasuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = defaultTimeout)
      .filter(osasuoritus => !osasuoritustenAikarajaus || arvioituAikavälillä(alku, loppu)(osasuoritus))
      .groupBy(_.päätasonSuoritusId)

    val henkilot = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct).result, timeout = defaultTimeout).groupBy(_.oppijaOid).view.mapValues(_.head).toMap

    val kotikuntahistoriat = kotikuntaPvm.toSeq.flatMap { pvm =>
      val pvmDateBegin = Date.valueOf(LocalDate.of(1900, 1, 1))
      val pvmDateEnd = Date.valueOf(LocalDate.now())
      val pvmDate = Date.valueOf(pvm)
      runDbSync(RKotikuntahistoria
        .filter(_.masterOppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct)
        .filter(r => r.muuttoPvm.getOrElse(pvmDateBegin) <= pvmDate && r.poismuuttoPvm.getOrElse(pvmDateEnd) >= pvmDate)
        .result
      )
    }

    opiskeluoikeudet.foldLeft[Seq[LukioRaporttiRows]](Seq.empty) {
      combineOpiskeluoikeusWith(_, _, aikajaksot, paatasonSuoritukset, osasuoritukset, henkilot, kotikuntahistoriat)
    }
  }

  private def combineOpiskeluoikeusWith(
    acc: Seq[LukioRaporttiRows],
    opiskeluoikeus: ROpiskeluoikeusRow,
    aikajaksot: Map[OpiskeluoikeusOid, Seq[ROpiskeluoikeusAikajaksoRow]],
    paatasonSuoritukset: Map[OpiskeluoikeusOid, Seq[RPäätasonSuoritusRow]],
    osasuoritukset: Map[PäätasonSuoritusId, Seq[ROsasuoritusRow]],
    henkilot: Map[OppijaOid, RHenkilöRow],
    kotikuntahistoria: Seq[RKotikuntahistoriaRow],
  ) = {
    paatasonSuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).map(paatasonsuoritus =>
      LukioRaporttiRows(
        opiskeluoikeus,
        henkilot(opiskeluoikeus.oppijaOid),
        aikajaksot.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).sortBy(_.alku)(sqlDateOrdering),
        paatasonsuoritus,
        osasuoritukset.getOrElse(paatasonsuoritus.päätasonSuoritusId, Nil),
        kotikuntahistoria.find(_.masterOppijaOid == opiskeluoikeus.oppijaOid),
      )
    ) ++ acc
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetResult(oppilaitos: String, alku: LocalDate, loppu: LocalDate) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    implicit val getResult = GetResult[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])](pr => (pr.nextString(), pr.nextArray(), pr.nextArray()))
    runDbSync(opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos, alku, loppu).as[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])], timeout = defaultTimeout)
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos: String, alku: LocalDate, loppu: LocalDate) = {
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
      oo.oppilaitos_oid = $oppilaitos and
      oo.koulutusmuoto = 'lukiokoulutus' and
      aikaj.alku <= $loppu and aikaj.loppu >= $alku and
      (pts.data -> 'koulutusmoduuli' ->> 'perusteenDiaarinumero' is null or
      	pts.data -> 'koulutusmoduuli' ->> 'perusteenDiaarinumero' not in ('OPH-2267-2019', 'OPH-2263-2019'))
    group by oo.opiskeluoikeus_oid"""
  }
}

case class LukioRaporttiRows(
  opiskeluoikeus: ROpiskeluoikeusRow,
  henkilo: RHenkilöRow,
  aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow],
  päätasonSuoritus: RPäätasonSuoritusRow,
  osasuoritukset: Seq[ROsasuoritusRow],
  kotikuntaHistoriassa: Option[RKotikuntahistoriaRow],
) extends YleissivistäväRaporttiRows
