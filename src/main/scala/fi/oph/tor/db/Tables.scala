package fi.oph.tor.db

import java.sql.Timestamp

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.json.Json
import fi.oph.tor.schema.OpiskeluOikeus
import org.json4s._

object Tables {
  class OpiskeluOikeusTable(tag: Tag) extends Table[OpiskeluOikeusRow](tag, "opiskeluoikeus") {
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val oppijaOid: Rep[String] = column[String]("oppija_oid")
    def data = column[JValue]("data")

    def * = (id, oppijaOid, data) <> (OpiskeluOikeusRow.tupled, OpiskeluOikeusRow.unapply)
  }

  class OpiskeluOikeusHistoryTable(tag: Tag) extends Table[OpiskeluOikeusHistoryRow] (tag, "opiskeluoikeushistoria") {
    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val opiskeluoikeusId = column[Int]("opiskeluoikeus_id")
    val aikaleima = column[Timestamp]("aikaleima")
    val kayttajaOid = column[String]("kayttaja_oid")
    val muutos = column[JValue]("muutos")

    def * = (id, opiskeluoikeusId, aikaleima, kayttajaOid, muutos) <> (OpiskeluOikeusHistoryRow.tupled, OpiskeluOikeusHistoryRow.unapply)
  }

  val OpiskeluOikeudet = TableQuery[OpiskeluOikeusTable]
  val OpiskeluOikeusHistoria = TableQuery[OpiskeluOikeusHistoryTable]
}

case class OpiskeluOikeusRow(id: Int, oppijaOid: String, data: JValue) {
  lazy val toOpiskeluOikeus: OpiskeluOikeus = {
    Json.fromJValue[OpiskeluOikeus](data).copy ( id = Some(id) )
  }

  def this(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus) = {
    this(0, oppijaOid, Json.toJValue(opiskeluOikeus))
  }
}

case class OpiskeluOikeusHistoryRow(id: Int, opiskeluoikeusId: Int, aikaleima: Timestamp, kayttajaOid: String, muutos: JValue)