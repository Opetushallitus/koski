package fi.oph.tor.db

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.json.Json
import fi.oph.tor.opintooikeus.OpintoOikeus
import org.json4s._

object Tables {
  class OpintoOikeusTable(tag: Tag) extends Table[OpintoOikeusRow](tag, "opintooikeus") {
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val oppijaOid: Rep[String] = column[String]("oppija_oid")
    def data = column[JValue]("data")

    def * = (id, oppijaOid, data) <> (OpintoOikeusRow.tupled, OpintoOikeusRow.unapply)
  }
  val OpintoOikeudet = TableQuery[OpintoOikeusTable]
}

case class OpintoOikeusRow(id: Int, oppijaOid: String, data: JValue) {
  lazy val toOpintoOikeus: OpintoOikeus = {
    Json.fromJValue[OpintoOikeus](data).copy ( id = Some(id) )
  }

  def this(oppijaOid: String, opintoOikeus: OpintoOikeus) = {
    this(0, oppijaOid, Json.toJValue(opintoOikeus))
  }
}