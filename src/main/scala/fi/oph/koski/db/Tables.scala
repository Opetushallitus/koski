package fi.oph.koski.db

import java.sql.{Date, Timestamp}
import java.time.LocalDateTime

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.json.JsonManipulation.removeFields
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.schema._
import fi.oph.scalaschema.extraction.ValidationError
import fi.oph.scalaschema.{Serializer, _}
import org.json4s._

object Tables {
  class OpiskeluoikeusTable(tag: Tag) extends Table[OpiskeluoikeusRow](tag, "opiskeluoikeus") {
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val oid = column[String]("oid", O.Unique)
    val versionumero = column[Int]("versionumero")
    val aikaleima = column[Timestamp]("aikaleima")
    val oppijaOid = column[String]("oppija_oid")
    val data = column[JValue]("data")
    val oppilaitosOid = column[String]("oppilaitos_oid")
    val koulutustoimijaOid = column[Option[String]]("koulutustoimija_oid")
    val sisältäväOpiskeluoikeusOid = column[Option[String]]("sisaltava_opiskeluoikeus_oid")
    val sisältäväOpiskeluoikeusOppilaitosOid = column[Option[String]]("sisaltava_opiskeluoikeus_oppilaitos_oid")
    val luokka = column[Option[String]]("luokka")
    val mitätöity = column[Boolean]("mitatoity")
    val koulutusmuoto = column[String]("koulutusmuoto")
    val alkamispäivä = column[Date]("alkamispaiva")
    val päättymispäivä = column[Option[Date]]("paattymispaiva")

    def * = (id, oid, versionumero, aikaleima, oppijaOid, oppilaitosOid, koulutustoimijaOid, sisältäväOpiskeluoikeusOid, sisältäväOpiskeluoikeusOppilaitosOid, data, luokka, mitätöity, koulutusmuoto, alkamispäivä, päättymispäivä) <> (OpiskeluoikeusRow.tupled, OpiskeluoikeusRow.unapply)
    def updateableFields = (data, versionumero, sisältäväOpiskeluoikeusOid, sisältäväOpiskeluoikeusOppilaitosOid, luokka, koulutustoimijaOid, oppilaitosOid, mitätöity, alkamispäivä, päättymispäivä)
  }

  object OpiskeluoikeusTable {
    private def skipSyntheticProperties(s: ClassSchema, p: Property) = if (p.synthetic) Nil else List(p)

    private val serializationContext = SerializationContext(KoskiSchema.schemaFactory, skipSyntheticProperties)
    private val fieldsToExcludeInJson = Set("oid", "versionumero", "aikaleima")
    private implicit val deserializationContext = ExtractionContext(KoskiSchema.schemaFactory).copy(validate = false)

    private def serialize(opiskeluoikeus: Opiskeluoikeus) = removeFields(Serializer.serialize(opiskeluoikeus, serializationContext), fieldsToExcludeInJson)

    def makeInsertableRow(oppijaOid: String, opiskeluoikeusOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
      OpiskeluoikeusRow(
        0,
        opiskeluoikeusOid,
        Opiskeluoikeus.VERSIO_1,
        new Timestamp(0), // Will be replaced by db trigger (see V51__refresh_timestamp_on_insert_too.sql)
        oppijaOid,
        opiskeluoikeus.getOppilaitos.oid,
        opiskeluoikeus.koulutustoimija.map(_.oid),
        opiskeluoikeus.sisältyyOpiskeluoikeuteen.map(_.oid),
        opiskeluoikeus.sisältyyOpiskeluoikeuteen.map(_.oppilaitos.oid),
        serialize(opiskeluoikeus),
        opiskeluoikeus.luokka,
        opiskeluoikeus.mitätöity,
        opiskeluoikeus.tyyppi.koodiarvo,
        Date.valueOf(opiskeluoikeus.alkamispäivä.get),
        opiskeluoikeus.päättymispäivä.map(Date.valueOf)
      )
    }

    def readAsJValue(data: JValue, oid: String, versionumero: Int, aikaleima: Timestamp): JValue = {
      // note: for historical reasons, Opiskeluoikeus.aikaleima is Option[LocalDateTime], instead of Option[DateTime].
      // this Timestamp->LocalDateTime conversion assumes JVM time zone is Europe/Helsinki
      data.merge(Serializer.serialize(OidVersionTimestamp(oid, versionumero, aikaleima.toLocalDateTime), serializationContext))
    }

    def readAsOpiskeluoikeus(data: JValue, oid: String, versionumero: Int, aikaleima: Timestamp): Either[List[ValidationError], KoskeenTallennettavaOpiskeluoikeus] = {
      SchemaValidatingExtractor.extract[KoskeenTallennettavaOpiskeluoikeus](readAsJValue(data, oid, versionumero, aikaleima))
    }

    def updatedFieldValues(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, versionumero: Int) = {
      val data = serialize(opiskeluoikeus)

      (data,
       versionumero,
       opiskeluoikeus.sisältyyOpiskeluoikeuteen.map(_.oid),
       opiskeluoikeus.sisältyyOpiskeluoikeuteen.map(_.oppilaitos.oid),
       opiskeluoikeus.luokka,
       opiskeluoikeus.koulutustoimija.map(_.oid),
       opiskeluoikeus.getOppilaitos.oid,
       opiskeluoikeus.mitätöity,
       Date.valueOf(opiskeluoikeus.alkamispäivä.get),
       opiskeluoikeus.päättymispäivä.map(Date.valueOf))
    }
  }

  class HenkilöTable(tag: Tag) extends Table[HenkilöRow](tag, "henkilo") {
    val oid = column[String]("oid", O.PrimaryKey)
    val sukunimi = column[String]("sukunimi")
    val etunimet = column[String]("etunimet")
    val kutsumanimi = column[String]("kutsumanimi")
    val masterOid = column[Option[String]]("master_oid")

    def * = (oid, sukunimi, etunimet, kutsumanimi, masterOid) <> (HenkilöRow.tupled, HenkilöRow.unapply)
  }

  class OpiskeluoikeusHistoryTable(tag: Tag) extends Table[OpiskeluoikeusHistoryRow] (tag, "opiskeluoikeushistoria") {
    val opiskeluoikeusId = column[Int]("opiskeluoikeus_id")
    val versionumero = column[Int]("versionumero")
    val aikaleima = column[Timestamp]("aikaleima")
    val kayttajaOid = column[String]("kayttaja_oid")
    val muutos = column[JValue]("muutos")

    def * = (opiskeluoikeusId, versionumero, aikaleima, kayttajaOid, muutos) <> (OpiskeluoikeusHistoryRow.tupled, OpiskeluoikeusHistoryRow.unapply)
  }

  class CasServiceTicketSessionTable(tag: Tag) extends Table[SSOSessionRow] (tag, "casserviceticket") {
    val serviceTicket = column[String]("serviceticket")
    val username = column[String]("username")
    val userOid = column[String]("useroid")
    val name = column[String]("name")
    val started = column[Timestamp]("started")
    val updated = column[Timestamp]("updated")
    val huollettavatSearchResult = column[Option[JValue]]("huollettavat_search_result")

    def * = (serviceTicket, username, userOid, name, started, updated, huollettavatSearchResult) <> (SSOSessionRow.tupled, SSOSessionRow.unapply)
  }

  class PreferencesTable(tag: Tag) extends Table[PreferenceRow] (tag, "preferences") {
    val organisaatioOid = column[String]("organisaatio_oid", O.PrimaryKey)
    val koulutustoimijaOid = column[Option[String]]("koulutustoimija_oid")
    val `type` = column[String]("type", O.PrimaryKey)
    val key = column[String]("key", O.PrimaryKey)
    val value = column[JValue]("value")

    def * = (organisaatioOid, koulutustoimijaOid, `type`, key, value) <> (PreferenceRow.tupled, PreferenceRow.unapply)
  }

  class SuoritusjakoTable(tag: Tag) extends Table[SuoritusjakoRow] (tag, "suoritusjako") {
    val id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    val secret = column[String]("secret", O.Unique)
    val oppijaOid = column[String]("oppija_oid")
    val suoritusIds = column[JValue]("suoritus_ids")
    val voimassaAsti = column[Date]("voimassa_asti")
    val aikaleima = column[Timestamp]("aikaleima")

    def * = (id, secret, oppijaOid, suoritusIds, voimassaAsti, aikaleima) <> (SuoritusjakoRow.tupled, SuoritusjakoRow.unapply)
  }

  class MyDataJakoTable(tag: Tag) extends Table[MyDataJakoRow] (tag, "mydata_jako") {
    val asiakas = column[String]("asiakas")
    val oppijaOid = column[String]("oppija_oid")
    val voimassaAsti = column[Date]("voimassa_asti")
    val aikaleima = column[Timestamp]("aikaleima")
    val pk = primaryKey("mydata_jako_oppijaOid_asiakas_key", (oppijaOid, asiakas))

    def * = (asiakas, oppijaOid, voimassaAsti, aikaleima) <> (MyDataJakoRow.tupled, MyDataJakoRow.unapply)
  }

  class SchedulerTable(tag: Tag) extends Table[SchedulerRow](tag, "scheduler") {
    val name = column[String]("name", O.PrimaryKey)
    val nextFireTime = column[Timestamp]("nextfiretime")
    val context = column[Option[JValue]]("context")
    val status = column[Int]("status")

    def * = (name, nextFireTime, context, status) <> (SchedulerRow.tupled, SchedulerRow.unapply)
  }

  class PerustiedotSyncTable(tag: Tag) extends Table[PerustiedotSyncRow](tag, "perustiedot_sync") {
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val opiskeluoikeusId = column[Int]("opiskeluoikeus_id")
    val data = column[JValue]("data")
    val upsert = column[Boolean]("upsert")
    val aikaleima = column[Timestamp]("aikaleima")

    def * = (id, opiskeluoikeusId, data, upsert, aikaleima) <> (PerustiedotSyncRow.tupled, PerustiedotSyncRow.unapply)
  }

  class OppilaitosIPOsoiteTable(tag: Tag) extends Table[OppilaitosIPOsoiteRow](tag, "oppilaitos_ip_osoite") {
    val username = column[String]("username", O.PrimaryKey)
    val ip = column[String]("ip")

    def * = (username, ip) <> (OppilaitosIPOsoiteRow.tupled, OppilaitosIPOsoiteRow.unapply)
  }

  class ValtuudetSessionTable(tag: Tag) extends Table[ValtuudetSessionRow](tag, "valtuudet_session") {
    val oppijaOid = column[String]("oppija_oid", O.PrimaryKey)
    val sessionId = column[String]("session_id")
    val userId = column[String]("user_id")
    val code = column[Option[String]]("code")
    val accessToken = column[Option[String]]("access_token")
    val aikaleima = column[Timestamp]("aikaleima")

    def * = (oppijaOid, sessionId, userId, code, accessToken, aikaleima) <> (ValtuudetSessionRow.tupled, ValtuudetSessionRow.unapply)
  }

  val Preferences = TableQuery[PreferencesTable]

  val SuoritusJako = TableQuery[SuoritusjakoTable]

  val MyDataJako = TableQuery[MyDataJakoTable]

  val CasServiceTicketSessions = TableQuery[CasServiceTicketSessionTable]

  // OpiskeluOikeudet-taulu. Käytä kyselyissä aina OpiskeluOikeudetWithAccessCheck, niin tulee myös käyttöoikeudet tarkistettua samalla.
  val OpiskeluOikeudet = TableQuery[OpiskeluoikeusTable]

  val Henkilöt = TableQuery[HenkilöTable]
  val Scheduler = TableQuery[SchedulerTable]
  val PerustiedotSync = TableQuery[PerustiedotSyncTable]
  val OppilaitosIPOsoite = TableQuery[OppilaitosIPOsoiteTable]
  val ValtuudetSession = TableQuery[ValtuudetSessionTable]

  val OpiskeluoikeusHistoria = TableQuery[OpiskeluoikeusHistoryTable]

  def OpiskeluOikeudetWithAccessCheck(implicit user: KoskiSession): Query[OpiskeluoikeusTable, OpiskeluoikeusRow, Seq] = {
    val query = if (user.hasGlobalReadAccess || user.hasGlobalKoulutusmuotoReadAccess) {
      OpiskeluOikeudet
    } else {
      val oppilaitosOidit = user.organisationOids(AccessType.read).toList
      val varhaiskasvatusOikeudet = user.varhaiskasvatusKäyttöoikeudet.filter(_.organisaatioAccessType.contains(AccessType.read))

      for {
        oo <- OpiskeluOikeudet
        if (oo.oppilaitosOid inSet oppilaitosOidit) ||
           (oo.sisältäväOpiskeluoikeusOppilaitosOid inSet oppilaitosOidit) ||
           (oo.oppilaitosOid inSet varhaiskasvatusOikeudet.map(_.ulkopuolinenOrganisaatio.oid)) && oo.koulutustoimijaOid.map(_ inSet varhaiskasvatusOikeudet.map(_.koulutustoimija.oid)).getOrElse(false)
      } yield oo
    }

    query
      .filterIf(user.hasKoulutusmuotoRestrictions)(_.koulutusmuoto inSet user.allowedOpiskeluoikeusTyypit)
      .filterIf(!user.hasMitätöidytOpiskeluoikeudetAccess)(o => !o.mitätöity)
  }
}

case class SSOSessionRow(serviceTicket: String, username: String, userOid: String, name: String, started: Timestamp, updated: Timestamp, huollettavatSearchResult: Option[JValue])

// Note: the data json must not contain [id, versionumero] fields. This is enforced by DB constraint.
case class OpiskeluoikeusRow(id: Int,
  oid: String,
  versionumero: Int,
  aikaleima: Timestamp,
  oppijaOid: String,
  oppilaitosOid: String,
  koulutustoimijaOid: Option[String],
  sisältäväOpiskeluoikeusOid: Option[String],
  sisältäväOpiskeluoikeusOppilaitosOid: Option[String],
  data: JValue,
  luokka: Option[String],
  mitätöity: Boolean,
  koulutusmuoto: String,
  alkamispäivä: Date,
  päättymispäivä: Option[Date]) {
  import fi.oph.koski.db.Tables.OpiskeluoikeusTable
  lazy val toOpiskeluoikeusData: JValue = {
    OpiskeluoikeusTable.readAsJValue(data, oid, versionumero, aikaleima)
  }
  lazy val toOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus = {
    OpiskeluoikeusTable.readAsOpiskeluoikeus(data, oid, versionumero, aikaleima) match {
      case Right(oo) =>
        oo.asInstanceOf[KoskeenTallennettavaOpiskeluoikeus]
      case Left(errors) =>
        throw new MappingException(s"Error deserializing opiskeluoikeus ${oid} for oppija ${oppijaOid}: ${errors}")
    }
  }
}

case class HenkilöRow(oid: String, sukunimi: String, etunimet: String, kutsumanimi: String, masterOid: Option[String])

case class HenkilöRowWithMasterInfo(henkilöRow: HenkilöRow, masterHenkilöRow: Option[HenkilöRow])

case class OpiskeluoikeusHistoryRow(opiskeluoikeusId: Int, versionumero: Int, aikaleima: Timestamp, kayttajaOid: String, muutos: JValue)

case class SchedulerRow(name: String, nextFireTime: Timestamp, context: Option[JValue], status: Int) {
  def running: Boolean = status == 1
}

case class PerustiedotSyncRow(id: Int = 0, opiskeluoikeusId: Int, data: JValue, upsert: Boolean, aikaleima: Timestamp = new Timestamp(System.currentTimeMillis))

case class OppilaitosIPOsoiteRow(username: String, ip: String)

case class ValtuudetSessionRow(oppijaOid: String, sessionId: String, userId: String, code: Option[String] = None, accessToken: Option[String] = None, aikaleima: Timestamp = new Timestamp(System.currentTimeMillis))

case class PreferenceRow(organisaatioOid: String, koulutustoimijaOid: Option[String], `type`: String, key: String, value: JValue)

case class SuoritusjakoRow(id: Long, secret: String, oppijaOid: String, suoritusIds: JValue, voimassaAsti: Date, aikaleima: Timestamp)

case class MyDataJakoRow(asiakas: String, oppijaOid: String, voimassaAsti: Date, aikaleima: Timestamp)

case class OidVersionTimestamp(oid: String, versionumero: Int, aikaleima: LocalDateTime)
