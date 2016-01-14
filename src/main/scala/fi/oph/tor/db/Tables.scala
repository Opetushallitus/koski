package fi.oph.tor.db

import java.sql.Timestamp

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.json.Json
import fi.oph.tor.schema.OpiskeluOikeus
import fi.oph.tor.toruser.TorUser
import org.json4s._

object Tables {
  class OpiskeluOikeusTable(tag: Tag) extends Table[OpiskeluOikeusRow](tag, "opiskeluoikeus") {
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val oppijaOid: Rep[String] = column[String]("oppija_oid")
    val versionumero = column[Int]("versionumero")
    def data = column[JValue]("data")

    def * = (id, oppijaOid, versionumero, data) <> (OpiskeluOikeusRow.tupled, OpiskeluOikeusRow.unapply)
  }

  class OpiskeluOikeusHistoryTable(tag: Tag) extends Table[OpiskeluOikeusHistoryRow] (tag, "opiskeluoikeushistoria") {
    val opiskeluoikeusId = column[Int]("opiskeluoikeus_id")
    val versionumero = column[Int]("versionumero")
    val aikaleima = column[Timestamp]("aikaleima")
    val kayttajaOid = column[String]("kayttaja_oid")
    val muutos = column[JValue]("muutos")

    def * = (opiskeluoikeusId, versionumero, aikaleima, kayttajaOid, muutos) <> (OpiskeluOikeusHistoryRow.tupled, OpiskeluOikeusHistoryRow.unapply)
  }

  // OpiskeluOikeudet-taulu. Käytä kyselyissä aina OpiskeluOikeudetWithAccessCheck, niin tulee myös käyttöoikeudet tarkistettua samalla.
  val OpiskeluOikeudet = TableQuery[OpiskeluOikeusTable]

  val OpiskeluOikeusHistoria = TableQuery[OpiskeluOikeusHistoryTable]

  def OpiskeluOikeudetWithAccessCheck(implicit user: TorUser): Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq] = {
    val oids = user.userOrganisations.oids
    for {
      oo <- OpiskeluOikeudet
      if oo.data.#>>(List("oppilaitos", "oid")) inSetBind oids
    }
    yield {
      oo
    }
  }
}

case class OpiskeluOikeusRow(id: Int, oppijaOid: String, versionumero: Int, data: JValue) {
  lazy val toOpiskeluOikeus: OpiskeluOikeus = {
    Json.fromJValue[OpiskeluOikeus](data).copy ( id = Some(id), versionumero = Some(versionumero) )
  }

  def this(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus, versionumero: Int) = {
    this(0, oppijaOid, versionumero, Json.toJValue(opiskeluOikeus))
  }
}

case class OpiskeluOikeusHistoryRow(opiskeluoikeusId: Int, versionumero: Int, aikaleima: Timestamp, kayttajaOid: String, muutos: JValue)