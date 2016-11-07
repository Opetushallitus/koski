package fi.oph.koski.db

import java.sql.{Date, Timestamp}

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus, PäätasonSuoritus}
import fi.oph.koski.servlet.InvalidRequestException
import org.json4s._

object Tables {
  class OpiskeluOikeusTable(tag: Tag) extends Table[OpiskeluOikeusRow](tag, "opiskeluoikeus") {
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val versionumero = column[Int]("versionumero")
    val oppijaOid = column[String]("oppija_oid")
    val data = column[JValue]("data")
    val oppilaitosOid = column[String]("oppilaitos_oid")
    val koulutustoimijaOid = column[Option[String]]("koulutustoimija_oid")

    def * = (id, oppijaOid, oppilaitosOid, koulutustoimijaOid, versionumero, data) <> (OpiskeluOikeusRow.tupled, OpiskeluOikeusRow.unapply)
  }

  class OpiskeluOikeusHistoryTable(tag: Tag) extends Table[OpiskeluOikeusHistoryRow] (tag, "opiskeluoikeushistoria") {
    val opiskeluoikeusId = column[Int]("opiskeluoikeus_id")
    val versionumero = column[Int]("versionumero")
    val aikaleima = column[Timestamp]("aikaleima")
    val kayttajaOid = column[String]("kayttaja_oid")
    val muutos = column[JValue]("muutos")

    def * = (opiskeluoikeusId, versionumero, aikaleima, kayttajaOid, muutos) <> (OpiskeluOikeusHistoryRow.tupled, OpiskeluOikeusHistoryRow.unapply)
  }

  class CasServiceTicketSessionTable(tag: Tag) extends Table[CasServiceTicketSessionRow] (tag, "casserviceticket") {
    val serviceTicket = column[String]("serviceticket")
    val username = column[String]("username")
    val userOid = column[String]("useroid")
    val started = column[Timestamp]("started")
    val updated = column[Timestamp]("updated")

    def * = (serviceTicket, username, userOid, started, updated) <> (CasServiceTicketSessionRow.tupled, CasServiceTicketSessionRow.unapply)
  }

  class TiedonsiirtoTable(tag: Tag) extends Table[TiedonsiirtoRow] (tag, "tiedonsiirto") {
    val id = column[Int]("id")
    val kayttajaOid = column[String]("kayttaja_oid")
    val tallentajaOrganisaatioOid = column[String]("tallentaja_organisaatio_oid")
    val oppija = column[Option[JValue]]("oppija")
    val oppilaitos = column[Option[JValue]]("oppilaitos")
    val data = column[Option[JValue]]("data")
    val virheet = column[Option[JValue]]("virheet")
    val aikaleima = column[Timestamp]("aikaleima")
    val lahdejarjestelma = column[Option[String]]("lahdejarjestelma")

    def * = (id, kayttajaOid, tallentajaOrganisaatioOid, oppija, oppilaitos, data, virheet, aikaleima, lahdejarjestelma) <> (TiedonsiirtoRow.tupled, TiedonsiirtoRow.unapply)
  }

  class TiedonsiirtoYhteenvetoTable(tag: Tag) extends Table[TiedonsiirtoYhteenvetoRow] (tag, "tiedonsiirto_yhteenveto") {
    val tallentajaOrganisaatio = column[String]("tallentaja_organisaatio")
    val oppilaitos = column[String]("oppilaitos")
    val kayttaja = column[String]("kayttaja")
    val viimeisin = column[Timestamp]("viimeisin")
    val virheelliset = column[Int]("virheelliset")
    val siirretyt = column[Int]("siirretyt")
    val opiskeluoikeudet = column[Option[Int]]("opiskeluoikeudet")
    val lahdejarjestelma = column[Option[String]]("lahdejarjestelma")

    def * = (tallentajaOrganisaatio, oppilaitos, kayttaja, viimeisin, siirretyt, virheelliset, opiskeluoikeudet, lahdejarjestelma) <> (TiedonsiirtoYhteenvetoRow.tupled, TiedonsiirtoYhteenvetoRow.unapply)
  }

  val Tiedonsiirto = TableQuery[TiedonsiirtoTable]

  val TiedonsiirtoYhteenveto = TableQuery[TiedonsiirtoYhteenvetoTable]

  val CasServiceTicketSessions = TableQuery[CasServiceTicketSessionTable]

  // OpiskeluOikeudet-taulu. Käytä kyselyissä aina OpiskeluOikeudetWithAccessCheck, niin tulee myös käyttöoikeudet tarkistettua samalla.
  val OpiskeluOikeudet = TableQuery[OpiskeluOikeusTable]

  val OpiskeluOikeusHistoria = TableQuery[OpiskeluOikeusHistoryTable]

  def OpiskeluOikeudetWithAccessCheck(implicit user: KoskiSession): Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq] = {
    if (user.hasGlobalReadAccess) {
      OpiskeluOikeudet
    } else {
      val oids = user.organisationOids(AccessType.read).toList
      for {
        oo <- OpiskeluOikeudet
        if oo.data.#>>(List("oppilaitos", "oid")) inSetBind oids
      } yield { oo}
    }
  }

  def TiedonsiirtoWithAccessCheck(implicit user: KoskiSession): Query[TiedonsiirtoTable, TiedonsiirtoRow, Seq] = {
    if (user.hasGlobalReadAccess) {
      Tiedonsiirto
    } else {
      val oids = user.organisationOids(AccessType.read).toList
      for {
        t <- Tiedonsiirto
        if t.tallentajaOrganisaatioOid inSetBind oids
      } yield { t}
    }
  }

  def TiedonsiirtoYhteenvetoWithAccessCheck(implicit user: KoskiSession): Query[TiedonsiirtoYhteenvetoTable, TiedonsiirtoYhteenvetoRow, Seq] = {
    if (user.hasGlobalReadAccess) {
      TiedonsiirtoYhteenveto
    } else {
      val oids = user.organisationOids(AccessType.read).toList
      TiedonsiirtoYhteenveto
        .filter(_.tallentajaOrganisaatio inSetBind oids)
        .filter(_.oppilaitos inSetBind oids)
    }
  }

}

case class CasServiceTicketSessionRow(serviceTicket: String, username: String, userOid: String, started: Timestamp, updated: Timestamp)

// Note: the data json must not contain [id, versionumero] fields. This is enforced by DB constraint.
case class OpiskeluOikeusRow(id: Int, oppijaOid: String, oppilaitosOid: String, koulutustoimijaOid: Option[String], versionumero: Int, data: JValue) {
  lazy val toOpiskeluOikeus: KoskeenTallennettavaOpiskeluoikeus = {
    try {
      OpiskeluOikeusStoredDataDeserializer.read(data, id, versionumero)
    } catch {
      case e: Exception => throw new MappingException(s"Error deserializing opiskeluoikeus ${id} for oppija ${oppijaOid}", e)
    }
  }

  def this(oppijaOid: String, opiskeluOikeus: Opiskeluoikeus, versionumero: Int) = {
    this(0, oppijaOid, opiskeluOikeus.oppilaitos.oid, opiskeluOikeus.koulutustoimija.map(_.oid), versionumero, Json.toJValue(opiskeluOikeus))
  }
}

object OpiskeluOikeusStoredDataDeserializer {
  def read(data: JValue, id: Int, versionumero: Int): KoskeenTallennettavaOpiskeluoikeus = {
    Json.fromJValue[Opiskeluoikeus](data).asInstanceOf[KoskeenTallennettavaOpiskeluoikeus].withIdAndVersion(id = Some(id), versionumero = Some(versionumero))
  }
}

case class OpiskeluOikeusHistoryRow(opiskeluoikeusId: Int, versionumero: Int, aikaleima: Timestamp, kayttajaOid: String, muutos: JValue)

case class TiedonsiirtoRow(id: Int, kayttajaOid: String, tallentajaOrganisaatioOid: String, oppija: Option[JValue], oppilaitos: Option[JValue], data: Option[JValue], virheet: Option[JValue], aikaleima: Timestamp, lahdejarjestelma: Option[String])

case class TiedonsiirtoYhteenvetoRow(tallentajaOrganisaatio: String, oppilaitos: String, kayttaja: String, viimeisin: Timestamp, siirretyt: Int, virheet: Int, opiskeluoikeudet: Option[Int], lahdejarjestelma: Option[String])
