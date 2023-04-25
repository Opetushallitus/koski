package fi.oph.koski.db

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.json.JsonManipulation.removeFields
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.schema.KoskiSchema.skipSyntheticProperties
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.{FilterNonAnnotationableSensitiveData, _}
import fi.oph.scalaschema.extraction.ValidationError
import fi.oph.scalaschema.{Serializer, _}
import org.json4s._
import slick.lifted.ProvenShape

object KoskiTables {

  trait OpiskeluoikeusTable[T <: OpiskeluoikeusRow] extends Table[T] {
    def id: Rep[Int]
    def oid: Rep[String]
    def versionumero: Rep[Int]
    def aikaleima: Rep[Timestamp]
    def oppijaOid: Rep[String]
    def data: Rep[JValue]
    def oppilaitosOid: Rep[String]
    def koulutustoimijaOid: Rep[Option[String]]
    def sisältäväOpiskeluoikeusOid: Rep[Option[String]]
    def sisältäväOpiskeluoikeusOppilaitosOid: Rep[Option[String]]
    def luokka: Rep[Option[String]]
    def mitätöity: Rep[Boolean]
    def koulutusmuoto: Rep[String]
    def alkamispäivä: Rep[Date]
    def päättymispäivä: Rep[Option[Date]]
    def suoritusjakoTehty: Rep[Boolean]
    def suoritustyypit: Rep[List[String]]
    def poistettu: Rep[Boolean]

    def updateableFields:
      (Rep[JValue], Rep[Int], Rep[Option[String]], Rep[Option[String]], Rep[Option[String]], Rep[Option[String]], Rep[String], Rep[Boolean], Rep[Date], Rep[Option[Date]], Rep[List[String]])
  }

  trait OpiskeluoikeusTableCompanion[OOROW <: OpiskeluoikeusRow] {
    protected val serializationContext = SerializationContext(KoskiSchema.schemaFactory, skipSyntheticProperties)
    protected val fieldsToExcludeInJson = Set("oid", "versionumero", "aikaleima")
    protected implicit val deserializationContext = ExtractionContext(KoskiSchema.schemaFactory).copy(validate = false)

    protected def serialize(opiskeluoikeus: Opiskeluoikeus) = removeFields(Serializer.serialize(opiskeluoikeus, serializationContext), fieldsToExcludeInJson)

    def readAsJValue(data: JValue, oid: String, versionumero: Int, aikaleima: Timestamp): JValue = {
      // note: for historical reasons, Opiskeluoikeus.aikaleima is Option[LocalDateTime], instead of Option[DateTime].
      // this Timestamp->LocalDateTime conversion assumes JVM time zone is Europe/Helsinki
      data.merge(Serializer.serialize(OidVersionTimestamp(oid, versionumero, aikaleima.toLocalDateTime), serializationContext))
    }

    def readAsOpiskeluoikeus(data: JValue, oid: String, versionumero: Int, aikaleima: Timestamp): Either[List[ValidationError], KoskeenTallennettavaOpiskeluoikeus] = {
      SchemaValidatingExtractor.extract[KoskeenTallennettavaOpiskeluoikeus](readAsJValue(data, oid, versionumero, aikaleima))
    }

    def makeInsertableRow(oppijaOid: String, opiskeluoikeusOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): OOROW
    def updatedFieldValues(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, versionumero: Int):
      (JObject, Int, Option[String], Option[Oid], Option[String], Option[Oid], Oid, Boolean, Date, Option[Date], List[String])
  }


  class KoskiOpiskeluoikeusTable(tag: Tag)
    extends Table[KoskiOpiskeluoikeusRow](tag, "opiskeluoikeus")
      with OpiskeluoikeusTable[KoskiOpiskeluoikeusRow] {
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
    val suoritusjakoTehty = column[Boolean]("suoritusjako_tehty_rajapaivan_jalkeen") // Rajapäivä marraskuu 2021
    val suoritustyypit = column[List[String]]("suoritustyypit")
    val poistettu = column[Boolean]("poistettu")


    def * = (id, oid, versionumero, aikaleima, oppijaOid, oppilaitosOid, koulutustoimijaOid, sisältäväOpiskeluoikeusOid, sisältäväOpiskeluoikeusOppilaitosOid, data, luokka, mitätöity, koulutusmuoto, alkamispäivä, päättymispäivä, suoritusjakoTehty, suoritustyypit, poistettu) <> (KoskiOpiskeluoikeusRow.tupled, KoskiOpiskeluoikeusRow.unapply)
    def updateableFields = (data, versionumero, sisältäväOpiskeluoikeusOid, sisältäväOpiskeluoikeusOppilaitosOid, luokka, koulutustoimijaOid, oppilaitosOid, mitätöity, alkamispäivä, päättymispäivä, suoritustyypit)
    def updateableFieldsPoisto = (data, versionumero, sisältäväOpiskeluoikeusOid, sisältäväOpiskeluoikeusOppilaitosOid, luokka, koulutustoimijaOid, oppilaitosOid, mitätöity, koulutusmuoto, alkamispäivä, päättymispäivä, suoritustyypit, poistettu)
  }

  object KoskiOpiskeluoikeusTable extends OpiskeluoikeusTableCompanion[KoskiOpiskeluoikeusRow] {

    override def makeInsertableRow(oppijaOid: String, opiskeluoikeusOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
      KoskiOpiskeluoikeusRow(
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
        opiskeluoikeus.päättymispäivä.map(Date.valueOf),
        false,
        opiskeluoikeus.suoritukset.map(_.tyyppi.koodiarvo),
        false
      )
    }

    override def updatedFieldValues(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, versionumero: Int):
      (JObject, Int, Option[String], Option[Oid], Option[String], Option[Oid], Oid, Boolean, Date, Option[Date], List[String]) = {
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
       opiskeluoikeus.päättymispäivä.map(Date.valueOf),
       opiskeluoikeus.suoritukset.map(_.tyyppi.koodiarvo)
      )
    }
  }

  class YtrOpiskeluoikeusTable(tag: Tag)
    extends Table[YtrOpiskeluoikeusRow](tag, "ytr_opiskeluoikeus")
      with OpiskeluoikeusTable[YtrOpiskeluoikeusRow] {
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
    val suoritusjakoTehty = column[Boolean]("suoritusjako_tehty_rajapaivan_jalkeen") // Rajapäivä marraskuu 2021
    val suoritustyypit = column[List[String]]("suoritustyypit")
    val poistettu = column[Boolean]("poistettu")

    def * = (id, oid, versionumero, aikaleima, oppijaOid, oppilaitosOid, koulutustoimijaOid, sisältäväOpiskeluoikeusOid, sisältäväOpiskeluoikeusOppilaitosOid, data, luokka, mitätöity, koulutusmuoto, alkamispäivä, päättymispäivä, suoritusjakoTehty, suoritustyypit, poistettu) <> (YtrOpiskeluoikeusRow.tupled, YtrOpiskeluoikeusRow.unapply)
    def updateableFields = (data, versionumero, sisältäväOpiskeluoikeusOid, sisältäväOpiskeluoikeusOppilaitosOid, luokka, koulutustoimijaOid, oppilaitosOid, mitätöity, alkamispäivä, päättymispäivä, suoritustyypit)
  }

  object YtrOpiskeluoikeusTable extends OpiskeluoikeusTableCompanion[YtrOpiskeluoikeusRow] {

    override def makeInsertableRow(oppijaOid: String, opiskeluoikeusOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
      opiskeluoikeus match {
        case ytrOo: YlioppilastutkinnonOpiskeluoikeus =>
          YtrOpiskeluoikeusRow(
            0,
            opiskeluoikeusOid,
            Opiskeluoikeus.VERSIO_1,
            new Timestamp(0), // Will be replaced by db trigger (see V90__add_ytr_opiskeluoikeus_extra_triggers.sql)
            oppijaOid,
            opiskeluoikeus.getOppilaitos.oid,
            opiskeluoikeus.koulutustoimija.map(_.oid),
            opiskeluoikeus.sisältyyOpiskeluoikeuteen.map(_.oid),
            opiskeluoikeus.sisältyyOpiskeluoikeuteen.map(_.oppilaitos.oid),
            serialize(opiskeluoikeus),
            opiskeluoikeus.luokka,
            opiskeluoikeus.mitätöity,
            opiskeluoikeus.tyyppi.koodiarvo,
            Date.valueOf(ytrOo.keinotekoinenAlkamispäiväTutkintokerroista),
            opiskeluoikeus.päättymispäivä.map(Date.valueOf),
            false,
            opiskeluoikeus.suoritukset.map(_.tyyppi.koodiarvo),
            false
          )
        case _ =>
          throw new InternalError("Yritettiin tallentaa muu kuin YO-tutkinnon opiskeluoikeus")
      }
    }

    override def updatedFieldValues(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, versionumero: Int) = {
      opiskeluoikeus match {
        case ytrOo: YlioppilastutkinnonOpiskeluoikeus =>
          val data = serialize(opiskeluoikeus)

          (data,
            versionumero,
            opiskeluoikeus.sisältyyOpiskeluoikeuteen.map(_.oid),
            opiskeluoikeus.sisältyyOpiskeluoikeuteen.map(_.oppilaitos.oid),
            opiskeluoikeus.luokka,
            opiskeluoikeus.koulutustoimija.map(_.oid),
            opiskeluoikeus.getOppilaitos.oid,
            opiskeluoikeus.mitätöity,
            Date.valueOf(ytrOo.keinotekoinenAlkamispäiväTutkintokerroista),
            opiskeluoikeus.päättymispäivä.map(Date.valueOf),
            opiskeluoikeus.suoritukset.map(_.tyyppi.koodiarvo)
          )

        case _ =>
          throw new InternalError("Yritettiin tallentaa muu kuin YO-tutkinnon opiskeluoikeus")
      }
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

  trait OpiskeluoikeusHistoryTable extends Table[OpiskeluoikeusHistoryRow] {
    def opiskeluoikeusId: Rep[Int]
    def versionumero: Rep[Int]
    def aikaleima: Rep[Timestamp]
    def kayttajaOid: Rep[String]
    def muutos: Rep[JValue]
  }

  class KoskiOpiskeluoikeusHistoryTable(tag: Tag)
    extends Table[OpiskeluoikeusHistoryRow] (tag, "opiskeluoikeushistoria")
      with OpiskeluoikeusHistoryTable {
    val opiskeluoikeusId = column[Int]("opiskeluoikeus_id")
    val versionumero = column[Int]("versionumero")
    val aikaleima = column[Timestamp]("aikaleima")
    val kayttajaOid = column[String]("kayttaja_oid")
    val muutos = column[JValue]("muutos")

    def * = (opiskeluoikeusId, versionumero, aikaleima, kayttajaOid, muutos) <> (OpiskeluoikeusHistoryRow.tupled, OpiskeluoikeusHistoryRow.unapply)
  }

  class YtrOpiskeluoikeusHistoryTable(tag: Tag)
    extends Table[OpiskeluoikeusHistoryRow] (tag, "ytr_opiskeluoikeushistoria")
      with OpiskeluoikeusHistoryTable {
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

  class SuoritusjakoTableV2(tag: Tag) extends Table[SuoritusjakoRowV2] (tag, "suoritusjako_v2") {
    val secret = column[String]("secret", O.Unique)
    val oppijaOid = column[String]("oppija_oid")
    val data = column[JValue]("data")
    val voimassaAsti = column[Date]("voimassa_asti")
    val aikaleima = column[Timestamp]("aikaleima")

    def * = (secret, oppijaOid, data, voimassaAsti, aikaleima) <> (SuoritusjakoRowV2.tupled, SuoritusjakoRowV2.unapply)
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

  class YtrDownloadStatusTable(tag: Tag) extends Table[YtrDownloadStatusRow](tag, "ytr_download_status") {
    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val aikaleima = column[Timestamp]("aikaleima")
    val completed = column[Option[Timestamp]]("completed")
    val data = column[JValue]("data")

    def * = (id, aikaleima, data, completed) <> (YtrDownloadStatusRow.tupled, YtrDownloadStatusRow.unapply)
  }

  class YtrAlkuperäinenDataTable(tag: Tag) extends Table[YtrAlkuperäinenDataRow](tag, "ytr_alkuperainen_data") {
    val oppijaOid = column[String]("oppija_oid", O.PrimaryKey)
    val aikaleima = column[Timestamp]("aikaleima")
    val data = column[JValue]("data")

    def * = (oppijaOid, aikaleima, data) <> (YtrAlkuperäinenDataRow.tupled, YtrAlkuperäinenDataRow.unapply)
  }

  class PerustiedotSyncTable(tag: Tag) extends Table[PerustiedotSyncRow](tag, "perustiedot_sync") {
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val opiskeluoikeusId = column[Int]("opiskeluoikeus_id")
    val data = column[JValue]("data")
    val upsert = column[Boolean]("upsert")
    val aikaleima = column[Timestamp]("aikaleima")

    def * = (id, opiskeluoikeusId, data, upsert, aikaleima) <> (PerustiedotSyncRow.tupled, PerustiedotSyncRow.unapply)
  }

  class PerustiedotManualSyncTable(tag: Tag) extends Table[PerustiedotManualSyncRow](tag, "perustiedot_manual_sync") {
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid")
    val upsert = column[Boolean]("upsert")
    val aikaleima = column[Timestamp]("aikaleima")

    def * = (id, opiskeluoikeusOid, upsert, aikaleima) <> (PerustiedotManualSyncRow.tupled, PerustiedotManualSyncRow.unapply)
  }

  class OppilaitosIPOsoiteTable(tag: Tag) extends Table[OppilaitosIPOsoiteRow](tag, "oppilaitos_ip_osoite") {
    val username = column[String]("username", O.PrimaryKey)
    val ip = column[String]("ip")

    def * = (username, ip) <> (OppilaitosIPOsoiteRow.tupled, OppilaitosIPOsoiteRow.unapply)
  }

  class PoistettuOpiskeluoikeusTable(tag: Tag) extends Table[PoistettuOpiskeluoikeusRow] (tag, "poistettu_opiskeluoikeus") {
    val oid = column[String]("oid", O.PrimaryKey)
    val oppija_oid = column[String]("oppija_oid")
    val oppilaitos_nimi = column[Option[String]]("oppilaitos_nimi")
    val oppilaitos_oid = column[Option[String]]("oppilaitos_oid")
    val päättymispäivä = column[Option[Date]]("paattymispaiva")
    val lähdejärjestelmäKoodi = column[Option[String]]("lahdejarjestelma_koodi")
    val lähdejärjestelmäId = column[Option[String]]("lahdejarjestelma_id")
    val mitätöityAikaleima = column[Option[Timestamp]]("mitatoity_aikaleima")
    val suostumusPeruttuAikaleima = column[Option[Timestamp]]("suostumus_peruttu_aikaleima")
    val koulutusmuoto = column[String]("koulutusmuoto")
    val suoritustyypit = column[List[String]]("suoritustyypit")
    val versio = column[Option[Int]]("versio")

    def * = (oid, oppija_oid, oppilaitos_nimi, oppilaitos_oid, päättymispäivä, lähdejärjestelmäKoodi, lähdejärjestelmäId, mitätöityAikaleima, suostumusPeruttuAikaleima, koulutusmuoto, suoritustyypit, versio) <> (PoistettuOpiskeluoikeusRow.tupled, PoistettuOpiskeluoikeusRow.unapply)
  }

  class PäivitettyOpiskeluoikeusTable(tag: Tag) extends Table[PäivitettyOpiskeluoikeusRow] (tag, "paivitetty_opiskeluoikeus") {
    val id = column[Option[Int]]("id", O.AutoInc, O.PrimaryKey)
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid")
    val aikaleima = column[Timestamp]("aikaleima")
    val prosessoitu = column[Boolean]("prosessoitu")

    def * : ProvenShape[PäivitettyOpiskeluoikeusRow] = (
      id,
      opiskeluoikeusOid,
      aikaleima,
      prosessoitu,
    ) <> (PäivitettyOpiskeluoikeusRow.tupled, PäivitettyOpiskeluoikeusRow.unapply)
  }

  val Preferences = TableQuery[PreferencesTable]

  val SuoritusJako = TableQuery[SuoritusjakoTable]

  val SuoritusJakoV2 = TableQuery[SuoritusjakoTableV2]

  val MyDataJako = TableQuery[MyDataJakoTable]

  val CasServiceTicketSessions = TableQuery[CasServiceTicketSessionTable]

  // OpiskeluOikeudet-taulu. Käytä kyselyissä aina OpiskeluOikeudetWithAccessCheck,
  // niin tulee myös käyttöoikeudet tarkistettua samalla, ja mitätöidyt ja poistetut opiskeluoikeudet poistettua
  // listalta, jos on tarpeen, kuten yleensä on.
  val KoskiOpiskeluOikeudet = TableQuery[KoskiOpiskeluoikeusTable]

  // YTR OpiskeluOikeudet-taulu. Käytä kyselyissä aina YtrOpiskeluOikeudetWithAccessCheck,
  // niin tulee myös käyttöoikeudet tarkistettua samalla, ja mitätöidyt ja poistetut opiskeluoikeudet poistettua
  // listalta, jos on tarpeen, kuten yleensä on.
  val YtrOpiskeluOikeudet = TableQuery[YtrOpiskeluoikeusTable]

  val Henkilöt = TableQuery[HenkilöTable]
  val Scheduler = TableQuery[SchedulerTable]

  val YtrDownloadStatus = TableQuery[YtrDownloadStatusTable]

  val YtrAlkuperäinenData = TableQuery[YtrAlkuperäinenDataTable]

  val PerustiedotSync = TableQuery[PerustiedotSyncTable]
  val PerustiedotManualSync = TableQuery[PerustiedotManualSyncTable]

  val OppilaitosIPOsoite = TableQuery[OppilaitosIPOsoiteTable]

  val KoskiOpiskeluoikeusHistoria = TableQuery[KoskiOpiskeluoikeusHistoryTable]

  val YtrOpiskeluoikeusHistoria = TableQuery[YtrOpiskeluoikeusHistoryTable]

  val PoistetutOpiskeluoikeudet = TableQuery[PoistettuOpiskeluoikeusTable]

  val PäivitetytOpiskeluoikeudet = TableQuery[PäivitettyOpiskeluoikeusTable]

  def KoskiOpiskeluOikeudetWithAccessCheck(implicit user: KoskiSpecificSession): Query[KoskiOpiskeluoikeusTable, KoskiOpiskeluoikeusRow, Seq] = {
    val query = if (user.hasGlobalReadAccess || user.hasGlobalKoulutusmuotoReadAccess) {
      KoskiOpiskeluOikeudet
    } else {
      val oppilaitosOidit = user.organisationOids(AccessType.read).toList
      val varhaiskasvatusOikeudet = user.varhaiskasvatusKäyttöoikeudet.filter(_.organisaatioAccessType.contains(AccessType.read))

      for {
        oo <- KoskiOpiskeluOikeudet
        if (oo.oppilaitosOid inSet oppilaitosOidit) ||
           (oo.sisältäväOpiskeluoikeusOppilaitosOid inSet oppilaitosOidit) ||
           (oo.oppilaitosOid inSet varhaiskasvatusOikeudet.map(_.ulkopuolinenOrganisaatio.oid)) &&
             oo.koulutustoimijaOid.map(_ inSet varhaiskasvatusOikeudet.map(_.koulutustoimija.oid)).getOrElse(false) ||
          (oo.koulutusmuoto === OpiskeluoikeudenTyyppi.taiteenperusopetus.koodiarvo &&
            oo.koulutustoimijaOid.map(_ inSet user.orgKäyttöoikeudet
              .filter(_.organisaatioAccessType.contains(AccessType.read))
              .flatMap(_.organisaatio.toKoulutustoimija).map(_.oid))
            )
      } yield oo
    }

    query
      .filterIf(user.hasKoulutusmuotoRestrictions)(_.koulutusmuoto inSet user.allowedOpiskeluoikeusTyypit)
      .filterIf(!user.hasMitätöidytOpiskeluoikeudetAccess)(o => !o.mitätöity)
      .filterIf(!user.hasPoistetutOpiskeluoikeudetAccess)(o => !o.poistettu)
  }

  def YtrOpiskeluOikeudetWithAccessCheck(implicit user: KoskiSpecificSession): Query[YtrOpiskeluoikeusTable, YtrOpiskeluoikeusRow, Seq] = {
    val query = if (user.hasTallennetutYlioppilastutkinnonOpiskeluoikeudetAccess) {
      YtrOpiskeluOikeudet
    } else {
      YtrOpiskeluOikeudet.take(0)
    }

    query
      .filterIf(user.hasKoulutusmuotoRestrictions)(_.koulutusmuoto inSet user.allowedOpiskeluoikeusTyypit)
      .filterIf(!user.hasMitätöidytOpiskeluoikeudetAccess)(o => !o.mitätöity)
      .filterIf(!user.hasPoistetutOpiskeluoikeudetAccess)(o => !o.poistettu)
  }
}

case class SSOSessionRow(serviceTicket: String, username: String, userOid: String, name: String, started: Timestamp, updated: Timestamp, huollettavatSearchResult: Option[JValue])

trait OpiskeluoikeusRow {
  def id: Int
  def oid: String
  def versionumero: Int
  def aikaleima: Timestamp
  def oppijaOid: String
  def oppilaitosOid: String
  def koulutustoimijaOid: Option[String]
  def sisältäväOpiskeluoikeusOid: Option[String]
  def sisältäväOpiskeluoikeusOppilaitosOid: Option[String]
  def data: JValue
  def luokka: Option[String]
  def mitätöity: Boolean
  def koulutusmuoto: String
  def alkamispäivä: Date
  def päättymispäivä: Option[Date]
  def suoritusjakoTehty: Boolean
  def suoritustyypit: List[String]
  def poistettu: Boolean

  def toOpiskeluoikeus(implicit user: SensitiveDataAllowed): Either[List[ValidationError], KoskeenTallennettavaOpiskeluoikeus]
  def toOpiskeluoikeusUnsafe(implicit user: SensitiveDataAllowed): KoskeenTallennettavaOpiskeluoikeus
}

// Note: the data json must not contain [id, versionumero] fields. This is enforced by DB constraint.
case class KoskiOpiskeluoikeusRow(id: Int,
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
  päättymispäivä: Option[Date],
  suoritusjakoTehty: Boolean,
  suoritustyypit: List[String],
  poistettu: Boolean
) extends OpiskeluoikeusRow {

  def toOpiskeluoikeus(implicit user: SensitiveDataAllowed): Either[List[ValidationError], KoskeenTallennettavaOpiskeluoikeus] = {
    KoskiTables.KoskiOpiskeluoikeusTable.readAsOpiskeluoikeus(data, oid, versionumero, aikaleima) match {
      case Right(oo: KoskeenTallennettavaOpiskeluoikeus) =>
        Right(FilterNonAnnotationableSensitiveData.filter(oo))
      case Left(left) => Left(left)
    }
  }

  def toOpiskeluoikeusUnsafe(implicit user: SensitiveDataAllowed): KoskeenTallennettavaOpiskeluoikeus = {
    toOpiskeluoikeus(user) match {
      case Right(oo) => oo
      case Left(errors) =>
        throw new MappingException(s"Error deserializing opiskeluoikeus ${oid} for oppija ${oppijaOid}: ${errors}")
    }
  }
}

case class YtrOpiskeluoikeusRow(id: Int,
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
  päättymispäivä: Option[Date],
  suoritusjakoTehty: Boolean,
  suoritustyypit: List[String],
  poistettu: Boolean
) extends OpiskeluoikeusRow {
  def toOpiskeluoikeus(implicit user: SensitiveDataAllowed): Either[List[ValidationError], YlioppilastutkinnonOpiskeluoikeus] = {
    KoskiTables.YtrOpiskeluoikeusTable.readAsOpiskeluoikeus(data, oid, versionumero, aikaleima) match {
      case Right(oo: YlioppilastutkinnonOpiskeluoikeus) =>
        Right(FilterNonAnnotationableSensitiveData.filter(oo) match {
          case ytrOo: YlioppilastutkinnonOpiskeluoikeus => ytrOo
        })
      case Right(_: KoskeenTallennettavaOpiskeluoikeus) =>
        throw new InternalError("Tietoja väärässä taulussa")
      case Left(left) => Left(left)
    }
  }

  def toOpiskeluoikeusUnsafe(implicit user: SensitiveDataAllowed): YlioppilastutkinnonOpiskeluoikeus = {
    toOpiskeluoikeus(user) match {
      case Right(oo) => oo
      case Left(errors) =>
        throw new MappingException(s"Error deserializing opiskeluoikeus ${oid} for oppija ${oppijaOid}: ${errors}")
    }
  }
}

case class HenkilöRow(oid: String, sukunimi: String, etunimet: String, kutsumanimi: String, masterOid: Option[String])

case class HenkilöRowWithMasterInfo(henkilöRow: HenkilöRow, masterHenkilöRow: Option[HenkilöRow])

case class OpiskeluoikeusHistoryRow(opiskeluoikeusId: Int, versionumero: Int, aikaleima: Timestamp, kayttajaOid: String, muutos: JValue)

object ScheduledTaskStatus {
  type ScheduledTaskStatus = Int
  val scheduled: ScheduledTaskStatus = 0
  val running: ScheduledTaskStatus = 1
}

case class SchedulerRow(name: String, nextFireTime: Timestamp, context: Option[JValue], status: Int) {
  def running: Boolean = status == ScheduledTaskStatus.running
}

case class YtrDownloadStatusRow(id: Int, aikaleima: Timestamp, data: JValue, completed: Option[Timestamp])

case class YtrAlkuperäinenDataRow(oppijaOid: String, aikaleima: Timestamp, data: JValue)

case class PerustiedotSyncRow(id: Int = 0, opiskeluoikeusId: Int, data: JValue, upsert: Boolean, aikaleima: Timestamp = new Timestamp(System.currentTimeMillis))

case class PerustiedotManualSyncRow(id: Int = 0, opiskeluoikeusOid: String, upsert: Boolean, aikaleima: Timestamp = new Timestamp(System.currentTimeMillis))

case class OppilaitosIPOsoiteRow(username: String, ip: String)

case class PreferenceRow(organisaatioOid: String, koulutustoimijaOid: Option[String], `type`: String, key: String, value: JValue)

case class SuoritusjakoRow(id: Long, secret: String, oppijaOid: String, suoritusIds: JValue, voimassaAsti: Date, aikaleima: Timestamp)

case class SuoritusjakoRowV2(secret: String, oppijaOid: String, data: JValue, voimassaAsti: Date, aikaleima: Timestamp)

case class MyDataJakoRow(asiakas: String, oppijaOid: String, voimassaAsti: Date, aikaleima: Timestamp)

case class OidVersionTimestamp(oid: String, versionumero: Int, aikaleima: LocalDateTime)

case class PoistettuOpiskeluoikeusRow(
  oid: String,
  oppijaOid: String,
  oppilaitosNimi: Option[String],
  oppilaitosOid: Option[String],
  päättymispäivä: Option[Date],
  lähdejärjestelmäKoodi: Option[String],
  lähdejärjestelmäId: Option[String],
  mitätöityAikaleima: Option[Timestamp],
  suostumusPeruttuAikaleima: Option[Timestamp],
  koulutusmuoto: String,
  suoritustyypit: List[String],
  versio: Option[Int]
)

case class PäivitettyOpiskeluoikeusRow(
  id: Option[Int] = None,
  opiskeluoikeusOid: Opiskeluoikeus.Oid,
  aikaleima: Timestamp,
  prosessoitu: Boolean = false,
)
