package fi.oph.koski.valpas.oppivelvollisuudestavapautus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.schema.Henkilö.Oid
import slick.jdbc.GetResult

import java.time.{LocalDate, LocalDateTime}

class ValpasOppivelvollisuudestaVapautusRepository(val db: DB) extends QueryMethods with Logging {
  // Vuotta 9999 käytetään ei-mitätöidyn vapautuksen indikaattorina, jotta uniikkius saadaan varmistettua tietokantatasolla
  // unique constraintilla. Taikalukua käytettävä NULLin sijaan, koska Postgresql käsittelee kaikki NULL-arvot keskenään uniikkeina.
  val VOIMASSA_OLEVAN_VAPAUTUKSEN_MITÄTÖINTIVUOSI = 9999

  def getOppivelvollisuudestaVapautetutOppijat(oppijaOids: Seq[String]): Seq[RawOppivelvollisuudestaVapautus] = {
    runDbSync(sql"""
      SELECT
        oppija_oid,
        vapautettu,
        kunta_koodiarvo,
        mitatoity,
        aikaleima
      FROM oppivelvollisuudesta_vapautetut
        WHERE oppija_oid = any($oppijaOids)
          AND (
            extract(year from mitatoity) = #$VOIMASSA_OLEVAN_VAPAUTUKSEN_MITÄTÖINTIVUOSI
            OR mitatoity + interval '1 day' > now()
          )
    """.as[RawOppivelvollisuudestaVapautus])
  }

  def lisääOppivelvollisuudestaVapautus(oppijaOid: Oid, virkailijaOid: Oid, vapautettu: LocalDate, kotipaikkaKoodiarvo: String): Unit = {
    runDbSync(sql"""
      INSERT INTO oppivelvollisuudesta_vapautetut
        (oppija_oid, virkailija_oid, vapautettu, kunta_koodiarvo)
        VALUES ($oppijaOid, $virkailijaOid, $vapautettu, $kotipaikkaKoodiarvo)
    """.asUpdate)
  }

  def mitätöiOppivelvollisuudestaVapautus(oppijaOid: Oid, kotipaikkaKoodiarvo: String): Boolean =
    runDbSync(sql"""
      UPDATE oppivelvollisuudesta_vapautetut
        SET mitatoity = now()
        WHERE oppija_oid = $oppijaOid
          AND kunta_koodiarvo = $kotipaikkaKoodiarvo
          AND extract(year from mitatoity) = #$VOIMASSA_OLEVAN_VAPAUTUKSEN_MITÄTÖINTIVUOSI
    """.asUpdate) > 0

  def readPage(offset: Int, pageSize: Int): Seq[RawOppivelvollisuudestaVapautus] =
    runDbSync(sql"""
      SELECT
        oppija_oid,
        vapautettu,
        kunta_koodiarvo,
        mitatoity,
        aikaleima
      FROM oppivelvollisuudesta_vapautetut
      ORDER BY aikaleima, oppija_oid
      OFFSET $offset
      LIMIT $pageSize
    """.as[RawOppivelvollisuudestaVapautus])

  def deleteAll(): Unit =
    runDbSync(sql"""TRUNCATE oppivelvollisuudesta_vapautetut""".asUpdate)

  private implicit def getOppivelvollisuudestaVapautus: GetResult[RawOppivelvollisuudestaVapautus] = GetResult(r =>
    RawOppivelvollisuudestaVapautus(
      oppijaOid = r.rs.getString("oppija_oid"),
      vapautettu = r.getLocalDate("vapautettu"),
      kunta = r.rs.getString("kunta_koodiarvo"),
      mitätöity = Option(r.rs.getTimestamp("mitatoity"))
        .map(_.toLocalDateTime)
        .flatMap(d => if (d.getYear == VOIMASSA_OLEVAN_VAPAUTUKSEN_MITÄTÖINTIVUOSI) None else Some(d)),
      aikaleima = r.rs.getTimestamp("aikaleima").toLocalDateTime,
    )
  )
}

case class RawOppivelvollisuudestaVapautus(
  oppijaOid: Henkilö.Oid,
  vapautettu: LocalDate,
  kunta: String,
  mitätöity: Option[LocalDateTime],
  aikaleima: LocalDateTime,
)
