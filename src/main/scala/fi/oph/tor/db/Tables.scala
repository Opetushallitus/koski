package fi.oph.tor.db

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
  val OpiskeluOikeudet = TableQuery[OpiskeluOikeusTable]
}

case class OpiskeluOikeusRow(id: Int, oppijaOid: String, data: JValue) {
  lazy val toOpiskeluOikeus: OpiskeluOikeus = {
    Json.fromJValue[OpiskeluOikeus](data).copy ( id = Some(id) )
  }

  def this(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus) = {
    this(0, oppijaOid, Json.toJValue(opiskeluOikeus))
  }
}