package fi.oph.koski.valpas.db

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.log.Logging

import java.time.Instant

object ValpasSchema extends Logging {
  type Koodiarvo = String

  // A helper to help with creating migrations: dumps the SQL DDL to create the full schema
  def logCreateSchemaDdl(): Unit = {
    val schema = Ilmoitukset.schema ++ IlmoitusYhteystiedot.schema
    logger.info((schema.createStatements ++ "\n").mkString(";\n"))
  }


  class IlmoitusTable(tag: Tag) extends Table[IlmoitusRow](tag, "ilmoitus") {
    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val luotu = column[Instant]("luotu", O.SqlType("timestamp default now()"))
    val oppijaOid = column[String]("oppija_oid")
    val kuntaOid = column[String]("kunta_oid")
    val tekijäOrganisaatioOid = column[String]("tekijä_organisaatio_oid")
    val tekijäOid = column[Option[String]]("tekijä_oid")

    def * = (
      id,
      luotu,
      oppijaOid,
      kuntaOid,
      tekijäOrganisaatioOid,
      tekijäOid
    ) <> (IlmoitusRow.tupled, IlmoitusRow.unapply)
  }

  case class IlmoitusRow(
    id: Int,
    luotu: Instant,
    oppijaOid: String,
    kuntaOid: String,
    tekijäOrganisaatioOid: String,
    tekijäOid: Option[String]
  )

  val Ilmoitukset = TableQuery[IlmoitusTable]


  class IlmoitusYhteystiedotTable(tag: Tag) extends Table[IlmoitusYhteystiedotRow](tag, "ilmoitus_yhteystiedot") {
    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val ilmoitusId = column[Int]("ilmoitus_id")
    val ilmoitusFk = foreignKey("ilmoitus_fk", ilmoitusId, Ilmoitukset)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
    val yhteydenottokieli = column[Option[Koodiarvo]]("yhteydenottokieli")
    val puhelin = column[Option[String]]("puhelin")
    val sähköposti = column[Option[String]]("sähköposti")
    val lähiosoite = column[Option[String]]("lähiosoite")
    val postinumero = column[Option[String]]("postinumero")
    val postitoimipaikka = column[Option[String]]("postitoimipaikka")
    val maa = column[Option[String]]("maa")

    def * = (
      id,
      ilmoitusId,
      yhteydenottokieli,
      puhelin,
      sähköposti,
      lähiosoite,
      postinumero,
      postitoimipaikka,
      maa
    ) <> (IlmoitusYhteystiedotRow.tupled, IlmoitusYhteystiedotRow.unapply)
  }

  case class IlmoitusYhteystiedotRow(
    id: Int,
    ilmoitusId: Int,
    yhteydenottokieli: Option[Koodiarvo],
    puhelin: Option[String],
    sähköposti: Option[String],
    lähiosoite: Option[String],
    postinumero: Option[String],
    postitoimipaikka: Option[String],
    maa: Option[String],
  )

  val IlmoitusYhteystiedot = TableQuery[IlmoitusYhteystiedotTable]
}
