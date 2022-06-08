package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.KoskiTables.PäivitetytOpiskeluoikeudet
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{DB, PäivitettyOpiskeluoikeusRow, QueryMethods}
import fi.oph.koski.schema.Opiskeluoikeus
import fi.oph.koski.util.TimeConversions._

import java.time.ZonedDateTime

class PäivitetytOpiskeluoikeudetJonoService(application: KoskiApplication) {
  lazy val replica = new PäivitetytOpiskeluoikeudetReplicaDbService(application.replicaDatabase.db)
  lazy val master = new PäivitetytOpiskeluoikeudetMasterDbService(application.masterDatabase.db, Environment.isMockEnvironment(application.config))

  def päivitetytOpiskeluoikeudet(dueTime: ZonedDateTime): Seq[PäivitettyOpiskeluoikeusRow] = {
    // Filtteröidään replicasta haettu jono masterin tietojen perusteella, jotta saadaan sellainen osajoukko, joka
    //   1) löytyy replicasta
    //   2) ei ole vielä käsitelty.
    // Tämä siksi, että replica on masterista joskus jopa vartin jäljessä.

    val prosessoidutRivit = master.päivitetytOpiskeluoikeudet(dueTime)
      .filter(_.prosessoitu)
      .map(_.id)

    replica.päivitetytOpiskeluoikeudet(dueTime)
      .filterNot(r => prosessoidutRivit.contains(r.id))
  }

  def kaikki: Seq[PäivitettyOpiskeluoikeusRow] =
    replica.kaikki

  def lisää(opiskeluoikeusOid: Opiskeluoikeus.Oid): Unit =
    master.lisää(opiskeluoikeusOid, ZonedDateTime.now())

  def alustaKaikkiKäsiteltäviksi(): Unit =
    master.alustaKaikkiKäsiteltäviksi()

  def merkitseKäsitellyiksi(käsitellytRiviIdt: Seq[Int]): Unit =
    master.merkitseKäsitellyiksi(käsitellytRiviIdt)

  def poistaKäsitellyt(): Unit =
    master.poistaKäsitellyt()

  def poistaKaikki(): Unit =
    master.poistaKaikki()

  def poistaVanhat(aikaraja: ZonedDateTime): Unit =
    master.poistaVanhat(aikaraja)
}

trait PäivitetytOpiskeluoikeudetDbService extends QueryMethods {
  def päivitetytOpiskeluoikeudet(dueTime: ZonedDateTime): Seq[PäivitettyOpiskeluoikeusRow] = {
    val due = toTimestamp(dueTime)
    runDbSync {
      PäivitetytOpiskeluoikeudet
        .filter(_.prosessoitu === false)
        .filter(_.aikaleima < due)
        .sortBy(_.aikaleima)
        .result
    }
  }
}

class PäivitetytOpiskeluoikeudetReplicaDbService(val db: DB) extends PäivitetytOpiskeluoikeudetDbService {
  def kaikki: Seq[PäivitettyOpiskeluoikeusRow] = runDbSync(PäivitetytOpiskeluoikeudet.result)
}

class PäivitetytOpiskeluoikeudetMasterDbService(val db: DB, isMockEnv: Boolean) extends PäivitetytOpiskeluoikeudetDbService {
  def lisää(opiskeluoikeusOid: Opiskeluoikeus.Oid, aikaleima: ZonedDateTime): Unit = runDbSync {
    PäivitetytOpiskeluoikeudet += PäivitettyOpiskeluoikeusRow(
      opiskeluoikeusOid = opiskeluoikeusOid,
      aikaleima = toTimestamp(aikaleima),
    )
  }

  def alustaKaikkiKäsiteltäviksi(): Unit = {
    val query = for { r <- PäivitetytOpiskeluoikeudet } yield r.prosessoitu
    runDbSync(query.update(false))
  }

  def merkitseKäsitellyiksi(käsitellytRiviIdt: Seq[Int]): Unit = {
    val query = for { r <- PäivitetytOpiskeluoikeudet if r.id inSet käsitellytRiviIdt } yield r.prosessoitu
    runDbSync(query.update(true))
  }

  def poistaKäsitellyt(): Unit = runDbSync {
    PäivitetytOpiskeluoikeudet
      .filter(_.prosessoitu)
      .delete
  }

  def poistaKaikki(): Unit =
    if (isMockEnv) runDbSync(PäivitetytOpiskeluoikeudet.delete)

  def poistaVanhat(aikaraja: ZonedDateTime): Unit = runDbSync {
    PäivitetytOpiskeluoikeudet
      .filter(_.aikaleima < toTimestamp(aikaraja))
      .delete
  }
}
