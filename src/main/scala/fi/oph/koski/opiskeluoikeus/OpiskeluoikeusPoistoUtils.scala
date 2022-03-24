package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{KoskiTables, PoistettuOpiskeluoikeusRow}
import fi.oph.koski.schema.Opiskeluoikeus
import fi.oph.koski.schema.Opiskeluoikeus.Versionumero
import org.json4s.JObject
import slick.dbio
import slick.dbio.Effect.Write

import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate}

object OpiskeluoikeusPoistoUtils {


  def poistaOpiskeluOikeus(
    id: Int,
    oid: String,
    oo: Opiskeluoikeus,
    versionumero: Versionumero,
    oppijaOid: String,
    mitätöity: Boolean
  ): dbio.DBIOAction[Unit, NoStream, Write with Effect.Transactional] = {
    DBIO.seq(
      OpiskeluoikeusPoistoUtils.opiskeluoikeudenPoistonQuery(oid, versionumero),
      poistettujenOpiskeluoikeuksienTauluunLisäämisenQuery(oo, oppijaOid, mitätöity),
      opiskeluoikeudenHistorianPoistonQuery(id)
    ).transactionally
  }

  private def opiskeluoikeudenPoistonQuery(oid: String, versionumero: Versionumero): dbio.DBIOAction[Int, NoStream, Write] = {
    KoskiTables.OpiskeluOikeudet.filter(_.oid === oid).map(_.updateableFieldsPoisto).update((JObject.apply(), versionumero, None, None, None, None, "", true, "", Date.valueOf(LocalDate.now()), None, List(), true))
  }

  private def opiskeluoikeudenHistorianPoistonQuery(id: Int): dbio.DBIOAction[Int, NoStream, Write] = {
    KoskiTables.OpiskeluoikeusHistoria.filter(_.opiskeluoikeusId === id).delete
  }

  private def poistettujenOpiskeluoikeuksienTauluunLisäämisenQuery(
    opiskeluoikeus: Opiskeluoikeus,
    oppijaOid: String,
    mitätöity: Boolean
  ): dbio.DBIOAction[Int, NoStream, Write] = {
    val timestamp = Timestamp.from(Instant.now())

    KoskiTables.PoistetutOpiskeluoikeudet.insertOrUpdate(PoistettuOpiskeluoikeusRow(
      opiskeluoikeus.oid.get,
      oppijaOid,
      opiskeluoikeus.oppilaitos.flatMap(_.nimi.map(_.get("fi"))),
      opiskeluoikeus.oppilaitos.map(_.oid),
      opiskeluoikeus.päättymispäivä.map(Date.valueOf),
      opiskeluoikeus.lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      opiskeluoikeus.lähdejärjestelmänId.flatMap(_.id),
      mitätöityAikaleima = if(mitätöity) Some(timestamp) else None,
      suostumusPeruttuAikaleima = if(mitätöity) None else Some(timestamp)
    ))
  }

}
