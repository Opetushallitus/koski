package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.raportointikanta._
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.db.KoskiDatabaseMethods
import scala.concurrent.duration._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.util.DateOrdering.sqlDateOrdering
import fi.oph.koski.schema.Organisaatio

case class PerusopetuksenRaportitRepository(db: DB) extends KoskiDatabaseMethods with RaportointikantaTableQueries {

  def perusopetuksenvuosiluokka(oppilaitos: Organisaatio.Oid, paiva: LocalDate, vuosiluokka: String): List[(ROpiskeluoikeusRow, Option[RHenkilöRow], List[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROsasuoritusRow], Seq[String])] = {
    val paivaDate = Date.valueOf(paiva)

    val opiskeluoikeudetJaAikajaksotQuery = ROpiskeluoikeudet
      .filter(_.oppilaitosOid === oppilaitos)
      .filter(_.koulutusmuoto === "perusopetus")
      .join(ROpiskeluoikeusAikajaksot.filterNot(_.alku > paivaDate).filterNot(_.loppu < paivaDate))
      .on(_.opiskeluoikeusOid === _.opiskeluoikeusOid)
      .sortBy(_._1.opiskeluoikeusOid)
    val opiskeluoikeudetJaAikajaksot: Seq[(ROpiskeluoikeusRow, ROpiskeluoikeusAikajaksoRow)] = runDbSync(opiskeluoikeudetJaAikajaksotQuery.result, timeout = 5.minutes)

    val paatasonSuorituksetQuery = RPäätasonSuoritukset
      .filter(_.opiskeluoikeusOid inSet opiskeluoikeudetJaAikajaksot.map(_._1.opiskeluoikeusOid).distinct)
      .filter(_.suorituksenTyyppi === "perusopetuksenvuosiluokka")
      .filter(_.koulutusmoduuliKoodiarvo === vuosiluokka)
    val paatasonSuoritukset: Map[String, Seq[RPäätasonSuoritusRow]] = runDbSync(paatasonSuorituksetQuery.result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)

    val voimassaolevatVuosiluokkaSuorituksetQuery = RPäätasonSuoritukset
      .filter(_.opiskeluoikeusOid inSet opiskeluoikeudetJaAikajaksot.map(_._1.opiskeluoikeusOid).distinct)
      .filter(_.suorituksenTyyppi === "perusopetuksenvuosiluokka")
      .filter(_.vahvistusPäivä.isEmpty)
      .map(row => (row.opiskeluoikeusOid, row.koulutusmoduuliKoodiarvo))
    val voimassaolevatVuosiluokkaSuorituset: Map[String, Seq[String]] = runDbSync(
      voimassaolevatVuosiluokkaSuorituksetQuery.result,
      timeout = 5.minutes
    ).groupBy(_._1).mapValues(_.map(_._2).toSeq)

    val osasuorituksetQuery = ROsasuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritukset.flatMap(_._2.map(_.päätasonSuoritusId)))
    val osasuoritukset: Map[String, Seq[ROsasuoritusRow]] = runDbSync(osasuorituksetQuery.result).groupBy(_.opiskeluoikeusOid)

    val henkilotQuery = RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudetJaAikajaksot.map(_._1.oppijaOid))
    val henkilot: Map[String, RHenkilöRow] = runDbSync(henkilotQuery.result).groupBy(_.oppijaOid).mapValues(_.head)

    opiskeluoikeudetJaAikajaksot
      .filter(t => paatasonSuoritukset.contains(t._1.opiskeluoikeusOid))
      .foldRight[List[(ROpiskeluoikeusRow, List[ROpiskeluoikeusAikajaksoRow])]](List.empty) {
      case (t, head :: tail) if t._1.opiskeluoikeusOid == head._1.opiskeluoikeusOid => (head._1, t._2 :: head._2) :: tail
      case (t, acc) => (t._1, List(t._2)) :: acc
    }
      .foldRight[List[(ROpiskeluoikeusRow, Option[RHenkilöRow], List[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROsasuoritusRow], Seq[String])]](List.empty) {
      case ((opiskeluoikeus, aikajaksot), acc) => {
        paatasonSuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Seq.empty) match {
          case Seq(paatasonsuoritus) => {
            (
              opiskeluoikeus,
              henkilot.get(opiskeluoikeus.oppijaOid),
              aikajaksot.sortBy(_.alku)(sqlDateOrdering),
              paatasonsuoritus,
              osasuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Seq.empty),
              voimassaolevatVuosiluokkaSuorituset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Seq.empty)
            ) :: acc
          }
          case montaSamanVuosiluokanPaatasonsuoritusta@_ => {
            montaSamanVuosiluokanPaatasonsuoritusta.map { paatasonsuoritus =>
              (
                opiskeluoikeus,
                henkilot.get(opiskeluoikeus.oppijaOid),
                aikajaksot.sortBy(_.alku)(sqlDateOrdering),
                paatasonsuoritus,
                osasuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Seq.empty).filter(_.päätasonSuoritusId == paatasonsuoritus.päätasonSuoritusId),
                voimassaolevatVuosiluokkaSuorituset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Seq.empty)
              )
            }.toList ::: acc
          }
        }
      }
    }
  }
}
