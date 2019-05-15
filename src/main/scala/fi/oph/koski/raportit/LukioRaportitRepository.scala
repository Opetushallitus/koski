package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.raportointikanta._
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.util.DateOrdering.sqlDateOrdering

import scala.concurrent.duration._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.schema.Organisaatio
import slick.jdbc.GetResult

case class LukioRaportitRepository(db: DB) extends KoskiDatabaseMethods with RaportointikantaTableQueries {

  private type OpiskeluoikeusOid = String
  private type OppijaOid = String
  private type PäätasonSuoritusId = Long
  private type AikajaksoId = Long
  private type Koulutusmooduuli_koodiarvo = String
  private type OppiaineenNimi = String
  private type Koulutusmoduuli_Paikallinen = Boolean

  def oppilaitoksessaOpetettavatOppiaineet(oppilaitosOid: Organisaatio.Oid) = {
    implicit val getResult = GetResult[(OppiaineenNimi, Koulutusmooduuli_koodiarvo, Koulutusmoduuli_Paikallinen)](pr => (pr.nextString(), pr.nextString(), pr.nextBoolean()))
    runDbSync(oppilaitoksenOppiaineetQuery(oppilaitosOid).as[(OppiaineenNimi, Koulutusmooduuli_koodiarvo, Koulutusmoduuli_Paikallinen)])
  }

  private def oppilaitoksenOppiaineetQuery(oppilaitosOid: Organisaatio.Oid) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    sql"""select distinct
          case
            when (os.data->'koulutusmoduuli'->'kieli'->'nimi'->>'fi' notnull) then os.data->'koulutusmoduuli'->'kieli'->'nimi'->>'fi'
            when (os.data->'koulutusmoduuli'->'oppimäärä'->'nimi'->>'fi' notnull) then os.data->'koulutusmoduuli'->'oppimäärä'->'nimi'->>'fi'
            else os.data->'koulutusmoduuli'->'tunniste'->'nimi'->>'fi'
          end
          as nimi,
          os.koulutusmoduuli_koodiarvo,
          os.koulutusmoduuli_paikallinen
          from r_opiskeluoikeus oo
          join r_osasuoritus os
          on oo.opiskeluoikeus_oid = os.opiskeluoikeus_oid
          where
          oo.oppilaitos_oid = $oppilaitosOid
          and (os.suorituksen_tyyppi = 'lukionoppiaine' or os.suorituksen_tyyppi = 'lukionmuuopinto')
          order by nimi"""
  }

  def suoritustiedot(oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate) = {
    val opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet = opiskeluoikeusAikajaksotPaatasonSuorituksetResult(oppilaitosOid, alku, loppu)

    val opiskeluoikeusOids = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.map(_._1)
    val paatasonSuoritusIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_._2)
    val aikajaksoIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_._3)

    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result, timeout = 5.minutes)
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.id inSet aikajaksoIds).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)
    val paatasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)

    val osasuoritukset = runDbSync(ROsasuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result).groupBy(_.päätasonSuoritusId)
    val henkilot = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct).result).groupBy(_.oppijaOid).mapValues(_.head)

    opiskeluoikeudet.foldLeft[List[(ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROsasuoritusRow])]](List.empty) {
      combineOpiskeluoikeusWith(_, _, aikajaksot, paatasonSuoritukset, osasuoritukset, henkilot)
    }
  }

  private def combineOpiskeluoikeusWith
  (
    acc: List[(ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROsasuoritusRow])],
    opiskeluoikeus: ROpiskeluoikeusRow,
    aikajaksot: Map[OpiskeluoikeusOid, Seq[ROpiskeluoikeusAikajaksoRow]],
    paatasonSuoritukset: Map[OpiskeluoikeusOid, Seq[RPäätasonSuoritusRow]],
    osasuoritukset: Map[PäätasonSuoritusId, Seq[ROsasuoritusRow]],
    henkilot: Map[OppijaOid, RHenkilöRow]
  ) = {
    paatasonSuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).map(paatasonsuoritus =>
      (
        opiskeluoikeus,
        henkilot.get(opiskeluoikeus.oppijaOid),
        aikajaksot.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).sortBy(_.alku)(sqlDateOrdering),
        paatasonsuoritus,
        osasuoritukset.getOrElse(paatasonsuoritus.päätasonSuoritusId, Nil),
      )
    ).toList ::: acc
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetResult(oppilaitos: String, alku: LocalDate, loppu: LocalDate) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    implicit val getResult = GetResult[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])](pr => (pr.nextString(), pr.nextArray(), pr.nextArray()))
    runDbSync(opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos, Date.valueOf(alku), Date.valueOf(loppu)).as[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])])
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos: String, alku: Date, loppu: Date) = {
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
      aikaj.alku <= $loppu and (aikaj.loppu >= $alku or aikaj.loppu is null)
    group by oo.opiskeluoikeus_oid"""
  }
}
