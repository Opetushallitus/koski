package fi.oph.koski.valpas.db

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Koodistokoodiviite, OrganisaatioWithOid}
import org.json4s.JValue
import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

object ValpasSchema extends Logging {
  // A helper to help with creating migrations: dumps the SQL DDL to create the full schema
  def logCreateSchemaDdl(): Unit = {
    val schema = Ilmoitukset.schema ++
      IlmoitusLisätiedot.schema ++
      OpiskeluoikeusLisätiedot.schema ++
      OppivelvollisuudenKeskeytys.schema ++
      IlmoitusOpiskeluoikeusKonteksti.schema ++
      OppivelvollisuudenKeskeytyshistoria.schema
    logger.info((schema.createStatements ++ "\n").mkString(";\n"))
  }


  class IlmoitusTable(tag: Tag) extends Table[IlmoitusRow](tag, "ilmoitus") {
    val uuid = column[UUID]("uuid", O.SqlType("uuid"), O.PrimaryKey)
    val luotu = column[LocalDateTime]("luotu", O.SqlType("timestamp"))
    val oppijaOid = column[String]("oppija_oid")
    val kuntaOid = column[String]("kunta_oid")
    val tekijäOrganisaatioOid = column[String]("tekijä_organisaatio_oid")
    val tekijäOid = column[String]("tekijä_oid")

    def * = (
      uuid,
      luotu,
      oppijaOid,
      kuntaOid,
      tekijäOrganisaatioOid,
      tekijäOid
    ) <> (IlmoitusRow.tupled, IlmoitusRow.unapply)
  }

  case class IlmoitusRow(
    uuid: UUID = UUID.randomUUID(),
    luotu: LocalDateTime,
    oppijaOid: String,
    kuntaOid: String,
    tekijäOrganisaatioOid: String,
    tekijäOid: String
  )

  val Ilmoitukset = TableQuery[IlmoitusTable]


  class IlmoitusLisätiedotTable(tag: Tag) extends Table[IlmoitusLisätiedotRow](tag, "ilmoitus_lisätiedot") {
    val ilmoitusUuid = column[UUID]("ilmoitus_uuid", O.SqlType("uuid"), O.PrimaryKey)
    val ilmoitusFk = foreignKey("ilmoitus_fk", ilmoitusUuid, Ilmoitukset)(_.uuid, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
    val data = column[JValue]("data")

    def * = (
      ilmoitusUuid,
      data
    ) <> (IlmoitusLisätiedotRow.tupled, IlmoitusLisätiedotRow.unapply)
  }

  case class IlmoitusLisätiedotRow(
    ilmoitusUuid: UUID,
    data: JValue
  )

  val IlmoitusLisätiedot = TableQuery[IlmoitusLisätiedotTable]


  class OpiskeluoikeusLisätiedotTable(tag: Tag) extends Table[OpiskeluoikeusLisätiedotRow](tag, "opiskeluoikeus_lisätiedot") {
    val oppijaOid = column[String]("oppija_oid")
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid")
    val oppilaitosOid = column[String]("oppilaitos_oid")
    val muuHaku = column[Boolean]("muu_haku")

    def pk = primaryKey("opiskeluoikeus_lisätiedot_pk", (oppijaOid, opiskeluoikeusOid, oppilaitosOid))

    def * = (
      oppijaOid,
      opiskeluoikeusOid,
      oppilaitosOid,
      muuHaku
    ) <> (OpiskeluoikeusLisätiedotRow.tupled, OpiskeluoikeusLisätiedotRow.unapply)
  }

  case class OpiskeluoikeusLisätiedotKey(
    oppijaOid: String,
    opiskeluoikeusOid: String,
    oppilaitosOid: String
  )

  case class OpiskeluoikeusLisätiedotRow(
    oppijaOid: String,
    opiskeluoikeusOid: String,
    oppilaitosOid: String,
    muuHaku: Boolean
  )

  val OpiskeluoikeusLisätiedot = TableQuery[OpiskeluoikeusLisätiedotTable]


  class OppivelvollisuudenKeskeytysTable(tag: Tag) extends Table[OppivelvollisuudenKeskeytysRow](tag, "oppivelvollisuuden_keskeytys") {
    val uuid = column[UUID]("uuid", O.SqlType("uuid"), O.PrimaryKey)
    val oppijaOid = column[String]("oppija_oid")
    val alku = column[LocalDate]("alku")
    val loppu = column[Option[LocalDate]]("loppu")
    val luotu = column[LocalDateTime]("luotu")
    val tekijäOid = column[String]("tekijä_oid")
    val tekijäOrganisaatioOid = column[String]("tekijä_organisaatio_oid")
    val peruttu = column[Boolean]("peruttu")

    val * = (
      uuid,
      oppijaOid,
      alku,
      loppu,
      luotu,
      tekijäOid,
      tekijäOrganisaatioOid,
      peruttu,
    ) <> (OppivelvollisuudenKeskeytysRow.tupled, OppivelvollisuudenKeskeytysRow.unapply)
  }

  case class OppivelvollisuudenKeskeytysRow(
    uuid: UUID = UUID.randomUUID(),
    oppijaOid: String,
    alku: LocalDate,
    loppu: Option[LocalDate],
    luotu: LocalDateTime,
    tekijäOid: String,
    tekijäOrganisaatioOid: String,
    peruttu: Boolean = false,
  ) {
    def asOppivelvollisuudenKeskeytyshistoriaRow(tekijäOid: String): OppivelvollisuudenKeskeytyshistoriaRow =
      OppivelvollisuudenKeskeytyshistoriaRow(
        ovKeskeytysUuid = this.uuid,
        muutosTehty = LocalDateTime.now(),
        muutoksenTekijä = tekijäOid,
        oppijaOid = this.oppijaOid,
        alku = this.alku,
        loppu = this.loppu,
        luotu = this.luotu,
        tekijäOid = this.tekijäOid,
        tekijäOrganisaatioOid = this.tekijäOrganisaatioOid,
        peruttu = this.peruttu,
      )
  }

  val OppivelvollisuudenKeskeytys = TableQuery[OppivelvollisuudenKeskeytysTable]

  class OppivelvollisuudenKeskeytyshistoriaTable(tag: Tag) extends Table[OppivelvollisuudenKeskeytyshistoriaRow](tag, "oppivelvollisuuden_keskeytyshistoria") {
    val id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    val ovKeskeytysUuid = column[UUID]("ov_keskeytys_uuid", O.SqlType("uuid"))
    val muutosTehty = column[LocalDateTime]("muutos_tehty")
    val muutoksenTekijä = column[String]("muutoksen_tekija")
    val oppijaOid = column[String]("oppija_oid")
    val alku = column[LocalDate]("alku")
    val loppu = column[Option[LocalDate]]("loppu")
    val luotu = column[LocalDateTime]("luotu")
    val tekijäOid = column[String]("tekijä_oid")
    val tekijäOrganisaatioOid = column[String]("tekijä_organisaatio_oid")
    val peruttu = column[Boolean]("peruttu")

    val * = (
      ovKeskeytysUuid,
      muutosTehty,
      muutoksenTekijä,
      oppijaOid,
      alku,
      loppu,
      luotu,
      tekijäOid,
      tekijäOrganisaatioOid,
      peruttu,
    ) <> (OppivelvollisuudenKeskeytyshistoriaRow.tupled, OppivelvollisuudenKeskeytyshistoriaRow.unapply)
  }

  case class OppivelvollisuudenKeskeytyshistoriaRow(
    ovKeskeytysUuid: UUID,
    muutosTehty: LocalDateTime,
    muutoksenTekijä: String,
    oppijaOid: String,
    alku: LocalDate,
    loppu: Option[LocalDate],
    luotu: LocalDateTime,
    tekijäOid: String,
    tekijäOrganisaatioOid: String,
    peruttu: Boolean,
  )

  val OppivelvollisuudenKeskeytyshistoria = TableQuery[OppivelvollisuudenKeskeytyshistoriaTable]

  class IlmoitusOpiskeluoikeusKontekstiTable(tag: Tag) extends Table[IlmoitusOpiskeluoikeusKontekstiRow](tag, "ilmoitus_opiskeluoikeus_konteksti") {
    val ilmoitusUuid = column[UUID]("ilmoitus_uuid", O.SqlType("uuid"))
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid")

    def pk = primaryKey("ilmoitus_opiskeluoikeus_konteksti_pk", (ilmoitusUuid, opiskeluoikeusOid))

    def * = (
      ilmoitusUuid,
      opiskeluoikeusOid
    ) <> (IlmoitusOpiskeluoikeusKontekstiRow.tupled, IlmoitusOpiskeluoikeusKontekstiRow.unapply)
  }

  case class IlmoitusOpiskeluoikeusKontekstiKey(
    ilmoitusUuid: UUID,
    opiskeluoikeusOid: String,
  )

  case class IlmoitusOpiskeluoikeusKontekstiRow(
    ilmoitusUuid: UUID,
    opiskeluoikeusOid: String,
  )

  val IlmoitusOpiskeluoikeusKonteksti = TableQuery[IlmoitusOpiskeluoikeusKontekstiTable]
}


// JSON-kenttien tietomallit:

object ValpasJsonSchema {
  type Koodiarvo = String
}

case class IlmoitusLisätiedotData(
  yhteydenottokieli: Option[ValpasJsonSchema.Koodiarvo],
  oppijaYhteystiedot: OppijaYhteystiedotData,
  tekijäYhteystiedot: TekijäYhteystiedotData,
  tekijäOrganisaatio: OrganisaatioWithOid,
  kunta: OrganisaatioWithOid,
  hakenutMuualle: Boolean
)

case class OppijaYhteystiedotData(
  puhelin: Option[String],
  sähköposti: Option[String],
  lähiosoite: Option[String],
  postinumero: Option[String],
  postitoimipaikka: Option[String],
  maa: Option[Koodistokoodiviite]
)

case class TekijäYhteystiedotData(
  etunimet: Option[String],
  sukunimi: Option[String],
  kutsumanimi: Option[String],
  puhelin: Option[String],
  sähköposti: Option[String]
)
