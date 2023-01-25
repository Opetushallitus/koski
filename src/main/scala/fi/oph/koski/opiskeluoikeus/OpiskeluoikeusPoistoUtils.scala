package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.KoskiTables.OpiskeluOikeudetWithAccessCheck
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{DatabaseExecutionContext, KoskiTables, PoistettuOpiskeluoikeusRow}
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}
import fi.oph.koski.schema.Opiskeluoikeus.Versionumero
import org.json4s.{JArray, JObject, JString}
import slick.dbio
import slick.dbio.Effect.Write

import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate}

object OpiskeluoikeusPoistoUtils extends DatabaseExecutionContext {

  def poistaOpiskeluOikeus(
    id: Int,
    oid: String,
    oo: Opiskeluoikeus,
    versionumero: Versionumero,
    oppijaOid: String,
    mitätöity: Boolean,
    aiemminPoistettuRivi: Option[PoistettuOpiskeluoikeusRow]
  ): dbio.DBIOAction[Unit, NoStream, Write with Effect.Transactional] = {
    DBIO.seq(
      OpiskeluoikeusPoistoUtils.opiskeluoikeudenPoistonQuery(oid, versionumero),
      poistettujenOpiskeluoikeuksienTauluunLisäämisenQuery(oid, oo, oppijaOid, mitätöity, None, aiemminPoistettuRivi),
      opiskeluoikeudenHistorianPoistonQuery(id)
    ).transactionally
  }

  def poistaPäätasonSuoritus(
    opiskeluoikeusOid: String,
    opiskeluoikeusId: Int,
    oo: Opiskeluoikeus,
    suorituksenTyyppi: String,
    versionumero: Versionumero,
    oppijaOid: String,
    mitätöity: Boolean,
    aiemminPoistettuRivi: Option[PoistettuOpiskeluoikeusRow],
    historyRepository: OpiskeluoikeusHistoryRepository
  ): dbio.DBIOAction[Unit, NoStream, Write with Effect.Transactional] = {
    val tallennettavaOpiskeluoikeus = oo.withSuoritukset(oo.suoritukset.filter(_.tyyppi.koodiarvo != suorituksenTyyppi)) match {
      case k: KoskeenTallennettavaOpiskeluoikeus => k
    }

    val updatedValues @ (newData, _, _, _, _, _, _, _, _, _, _) = KoskiTables.OpiskeluoikeusTable.updatedFieldValues(tallennettavaOpiskeluoikeus, versionumero)
    val diff = JArray(List(JObject("op" -> JString("add"), "path" -> JString(""), "value" -> newData)))

    DBIO.seq((
      List(
        OpiskeluOikeudetWithAccessCheck(KoskiSpecificSession.systemUser).filter(_.id === opiskeluoikeusId).map(_.updateableFields).update(updatedValues),
        poistettujenOpiskeluoikeuksienTauluunLisäämisenQuery(opiskeluoikeusOid, oo, oppijaOid, mitätöity, Some(suorituksenTyyppi), aiemminPoistettuRivi),
        opiskeluoikeudenHistorianPoistonQuery(opiskeluoikeusId),
      ) ++ Range.inclusive(1, versionumero).map(v => historyRepository.createAction(opiskeluoikeusId, v, oppijaOid, diff)).toList): _*
    ).transactionally
  }

  private def opiskeluoikeudenPoistonQuery(oid: String, versionumero: Versionumero): dbio.DBIOAction[Int, NoStream, Write] = {
    KoskiTables.OpiskeluOikeudet.filter(_.oid === oid)
      .map(_.updateableFieldsPoisto)
      .update(
        (JObject.apply(), versionumero, None, None, None, None, "", true, "", Date.valueOf(LocalDate.now()), None, List(), true)
      )
  }

  private def opiskeluoikeudenHistorianPoistonQuery(id: Int): dbio.DBIOAction[Int, NoStream, Write] = {
    KoskiTables.OpiskeluoikeusHistoria.filter(_.opiskeluoikeusId === id).delete
  }

  private def poistettujenOpiskeluoikeuksienTauluunLisäämisenQuery(
    opiskeluoikeusOid: String,
    opiskeluoikeus: Opiskeluoikeus,
    oppijaOid: String,
    mitätöity: Boolean,
    poistettuSuoritusTyyppi: Option[String],
    aiemminPoistettuRivi: Option[PoistettuOpiskeluoikeusRow]
  ): dbio.DBIOAction[Int, NoStream, Write] = {
    val timestamp = Timestamp.from(Instant.now())
    val aiemminPoistetutSuoritukset = aiemminPoistettuRivi.map(_.suoritustyypit).getOrElse(List.empty)

    KoskiTables.PoistetutOpiskeluoikeudet.insertOrUpdate(PoistettuOpiskeluoikeusRow(
      opiskeluoikeusOid,
      oppijaOid,
      opiskeluoikeus.oppilaitos.flatMap(_.nimi.map(_.get("fi"))),
      opiskeluoikeus.oppilaitos.map(_.oid),
      opiskeluoikeus.päättymispäivä.map(Date.valueOf),
      opiskeluoikeus.lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      opiskeluoikeus.lähdejärjestelmänId.flatMap(_.id),
      mitätöityAikaleima = if (mitätöity) Some(timestamp) else None,
      suostumusPeruttuAikaleima = if (mitätöity) None else Some(timestamp),
      koulutusmuoto = opiskeluoikeus.tyyppi.koodiarvo,
      suoritustyypit = poistettuSuoritusTyyppi
        .map(t => aiemminPoistetutSuoritukset :+ t)
        .getOrElse((aiemminPoistetutSuoritukset ++ opiskeluoikeus.suoritukset.map(_.tyyppi.koodiarvo)).distinct),
      versio = opiskeluoikeus.versionumero
    ))
  }

}
