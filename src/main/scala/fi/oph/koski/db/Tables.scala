package fi.oph.koski.db

import java.sql.Timestamp

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.json.Json
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.koskiuser.{AccessType, KoskiUser}
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema.{Koodistokoodiviite, KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}
import org.json4s._

object Tables {
  class OpiskeluOikeusTable(tag: Tag) extends Table[OpiskeluOikeusRow](tag, "opiskeluoikeus") {
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val versionumero = column[Int]("versionumero")
    val oppijaOid: Rep[String] = column[String]("oppija_oid")
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

  def OpiskeluOikeudetWithAccessCheck(implicit user: KoskiUser): Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq] = {
    if (user.hasUniversalReadAccess) {
      OpiskeluOikeudet
    } else {
      val oids = user.organisationOids(AccessType.read).toList
      for {
        oo <- OpiskeluOikeudet
        if oo.data.#>>(List("oppilaitos", "oid")) inSetBind oids
      }
        yield {
          oo
        }
    }
  }
}

// Note: the data json must not contain [id, versionumero] fields. This is enforced by DB constraint.
case class OpiskeluOikeusRow(id: Int, oppijaOid: String, versionumero: Int, data: JValue) {
  lazy val toOpiskeluOikeus: Opiskeluoikeus = {
    OpiskeluOikeusStoredDataDeserializer.read(data, id, versionumero)
  }

  def this(oppijaOid: String, opiskeluOikeus: Opiskeluoikeus, versionumero: Int) = {
    this(0, oppijaOid, versionumero, Json.toJValue(opiskeluOikeus))
  }
}

object OpiskeluOikeusStoredDataDeserializer {
  def read(data: JValue, id: Int, versionumero: Int): Opiskeluoikeus = {
    def addDefaultTila(suoritus: JObject): JObject = {
      // Migrating data on the fly: if tila is missing, add default value. This migration should later be performed on db level or removed
      (if (!suoritus.values.contains("tila")) {
        suoritus.merge(JObject("tila" -> Json.toJValue(Koodistokoodiviite("KESKEN", Some(LocalizedString.finnish("Suoritus kesken")), "suorituksentila", Some(1))) ))
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
    Json.fromJValue[Opiskeluoikeus](migratedData).asInstanceOf[KoskeenTallennettavaOpiskeluoikeus].withIdAndVersion(id = Some(id), versionumero = Some(versionumero))
  }
}

case class OpiskeluOikeusHistoryRow(opiskeluoikeusId: Int, versionumero: Int, aikaleima: Timestamp, kayttajaOid: String, muutos: JValue)