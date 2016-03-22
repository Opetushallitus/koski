package fi.oph.tor.db

import java.sql.Timestamp

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.json.Json
import fi.oph.tor.schema.{KoodistoKoodiViite, OpiskeluOikeus}
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
    val oids = user.organisationOids.toList
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
    OpiskeluOikeusStoredDataDeserializer.read(data, id, versionumero)
  }

  def this(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus, versionumero: Int) = {
    this(0, oppijaOid, versionumero, Json.toJValue(opiskeluOikeus))
  }
}

object OpiskeluOikeusStoredDataDeserializer {
  def read(data: JValue, id: Int, versionumero: Int): OpiskeluOikeus = {
    def addDefaultTila(suoritus: JObject): JObject = {
      // Migrating data on the fly: if tila is missing, add default value. This migration should later be performed on db level or removed
      (if (!suoritus.values.contains("tila")) {
        suoritus.merge(JObject("tila" -> Json.toJValue(KoodistoKoodiViite("KESKEN", Some("Suoritus kesken"), "suorituksentila", Some(1))) ))
      } else {
        suoritus
      }).transformField {
        case JField("osasuoritukset", value: JArray) =>
          JField("osasuoritukset", JArray(value.arr.map(_.asInstanceOf[JObject]).map(addDefaultTila(_))))
      }.asInstanceOf[JObject]
    }
    val migratedData = data.transformField {
      case JField("suoritus", suoritus: JObject) => JField("suoritus", addDefaultTila(suoritus))
    }
    Json.fromJValue[OpiskeluOikeus](migratedData).copy ( id = Some(id), versionumero = Some(versionumero) )
  }
}

case class OpiskeluOikeusHistoryRow(opiskeluoikeusId: Int, versionumero: Int, aikaleima: Timestamp, kayttajaOid: String, muutos: JValue)