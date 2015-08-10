package fi.oph.tor.db

import slick.lifted

// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.PostgresDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema = Arviointi.schema ++ Kurssisuoritus.schema ++ SchemaVersion.schema ++ Tutkinnonosasuoritus.schema ++ Tutkintosuoritus.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Arviointi
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param asteikko Database column asteikko SqlType(text)
   *  @param numero Database column numero SqlType(numeric)
   *  @param kuvaus Database column kuvaus SqlType(text), Default(None) */
  case class ArviointiRow(id: Int, asteikko: String, numero: scala.math.BigDecimal, kuvaus: Option[String] = None)
  /** GetResult implicit for fetching ArviointiRow objects using plain SQL queries */
  implicit def GetResultArviointiRow(implicit e0: GR[Int], e1: GR[String], e2: GR[scala.math.BigDecimal], e3: GR[Option[String]]): GR[ArviointiRow] = GR{
    prs => import prs._
    ArviointiRow.tupled((<<[Int], <<[String], <<[scala.math.BigDecimal], <<?[String]))
  }
  /** Table description of table arviointi. Objects of this class serve as prototypes for rows in queries. */
  class Arviointi(_tableTag: Tag) extends Table[ArviointiRow](_tableTag, Some("tor"), "arviointi") {
    def * = (id, asteikko, numero, kuvaus) <> (ArviointiRow.tupled, ArviointiRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(asteikko), Rep.Some(numero), kuvaus).shaped.<>({r=>import r._; _1.map(_=> ArviointiRow.tupled((_1.get, _2.get, _3.get, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column asteikko SqlType(text) */
    val asteikko: Rep[String] = column[String]("asteikko")
    /** Database column numero SqlType(numeric) */
    val numero: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("numero")
    /** Database column kuvaus SqlType(text), Default(None) */
    val kuvaus: Rep[Option[String]] = column[Option[String]]("kuvaus", O.Default(None))
  }
  /** Collection-like TableQuery object for table Arviointi */
  lazy val Arviointi: lifted.TableQuery[Arviointi] = new TableQuery(tag => new Arviointi(tag))

  /** Entity class storing rows of table Kurssisuoritus
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param tutkinnonosasuoritusId Database column tutkinnonosasuoritus_id SqlType(int4)
   *  @param komoOid Database column komo_oid SqlType(text)
   *  @param status Database column status SqlType(text)
   *  @param arviointiId Database column arviointi_id SqlType(int4), Default(None) */
  case class KurssisuoritusRow(id: Int, tutkinnonosasuoritusId: Int, komoOid: String, status: String, arviointiId: Option[Int] = None)
  /** GetResult implicit for fetching KurssisuoritusRow objects using plain SQL queries */
  implicit def GetResultKurssisuoritusRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[Int]]): GR[KurssisuoritusRow] = GR{
    prs => import prs._
    KurssisuoritusRow.tupled((<<[Int], <<[Int], <<[String], <<[String], <<?[Int]))
  }
  /** Table description of table kurssisuoritus. Objects of this class serve as prototypes for rows in queries. */
  class Kurssisuoritus(_tableTag: Tag) extends Table[KurssisuoritusRow](_tableTag, Some("tor"), "kurssisuoritus") {
    def * = (id, tutkinnonosasuoritusId, komoOid, status, arviointiId) <> (KurssisuoritusRow.tupled, KurssisuoritusRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(tutkinnonosasuoritusId), Rep.Some(komoOid), Rep.Some(status), arviointiId).shaped.<>({r=>import r._; _1.map(_=> KurssisuoritusRow.tupled((_1.get, _2.get, _3.get, _4.get, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column tutkinnonosasuoritus_id SqlType(int4) */
    val tutkinnonosasuoritusId: Rep[Int] = column[Int]("tutkinnonosasuoritus_id")
    /** Database column komo_oid SqlType(text) */
    val komoOid: Rep[String] = column[String]("komo_oid")
    /** Database column status SqlType(text) */
    val status: Rep[String] = column[String]("status")
    /** Database column arviointi_id SqlType(int4), Default(None) */
    val arviointiId: Rep[Option[Int]] = column[Option[Int]]("arviointi_id", O.Default(None))

    /** Foreign key referencing Arviointi (database name kurssisuoritus_arviointi_id_fkey) */
    lazy val arviointiFk = foreignKey("kurssisuoritus_arviointi_id_fkey", arviointiId, Arviointi)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing Tutkinnonosasuoritus (database name kurssisuoritus_tutkinnonosasuoritus_id_fkey) */
    lazy val tutkinnonosasuoritusFk = foreignKey("kurssisuoritus_tutkinnonosasuoritus_id_fkey", tutkinnonosasuoritusId, Tutkinnonosasuoritus)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Kurssisuoritus */
  lazy val Kurssisuoritus = new TableQuery(tag => new Kurssisuoritus(tag))

  /** Entity class storing rows of table SchemaVersion
   *  @param versionRank Database column version_rank SqlType(int4)
   *  @param installedRank Database column installed_rank SqlType(int4)
   *  @param version Database column version SqlType(varchar), PrimaryKey, Length(50,true)
   *  @param description Database column description SqlType(varchar), Length(200,true)
   *  @param `type` Database column type SqlType(varchar), Length(20,true)
   *  @param script Database column script SqlType(varchar), Length(1000,true)
   *  @param checksum Database column checksum SqlType(int4), Default(None)
   *  @param installedBy Database column installed_by SqlType(varchar), Length(100,true)
   *  @param installedOn Database column installed_on SqlType(timestamp)
   *  @param executionTime Database column execution_time SqlType(int4)
   *  @param success Database column success SqlType(bool) */
  case class SchemaVersionRow(versionRank: Int, installedRank: Int, version: String, description: String, `type`: String, script: String, checksum: Option[Int] = None, installedBy: String, installedOn: java.sql.Timestamp, executionTime: Int, success: Boolean)
  /** GetResult implicit for fetching SchemaVersionRow objects using plain SQL queries */
  implicit def GetResultSchemaVersionRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[Int]], e3: GR[java.sql.Timestamp], e4: GR[Boolean]): GR[SchemaVersionRow] = GR{
    prs => import prs._
    SchemaVersionRow.tupled((<<[Int], <<[Int], <<[String], <<[String], <<[String], <<[String], <<?[Int], <<[String], <<[java.sql.Timestamp], <<[Int], <<[Boolean]))
  }
  /** Table description of table schema_version. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class SchemaVersion(_tableTag: Tag) extends Table[SchemaVersionRow](_tableTag, Some("tor"), "schema_version") {
    def * = (versionRank, installedRank, version, description, `type`, script, checksum, installedBy, installedOn, executionTime, success) <> (SchemaVersionRow.tupled, SchemaVersionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(versionRank), Rep.Some(installedRank), Rep.Some(version), Rep.Some(description), Rep.Some(`type`), Rep.Some(script), checksum, Rep.Some(installedBy), Rep.Some(installedOn), Rep.Some(executionTime), Rep.Some(success)).shaped.<>({r=>import r._; _1.map(_=> SchemaVersionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8.get, _9.get, _10.get, _11.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column version_rank SqlType(int4) */
    val versionRank: Rep[Int] = column[Int]("version_rank")
    /** Database column installed_rank SqlType(int4) */
    val installedRank: Rep[Int] = column[Int]("installed_rank")
    /** Database column version SqlType(varchar), PrimaryKey, Length(50,true) */
    val version: Rep[String] = column[String]("version", O.PrimaryKey, O.Length(50,varying=true))
    /** Database column description SqlType(varchar), Length(200,true) */
    val description: Rep[String] = column[String]("description", O.Length(200,varying=true))
    /** Database column type SqlType(varchar), Length(20,true)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[String] = column[String]("type", O.Length(20,varying=true))
    /** Database column script SqlType(varchar), Length(1000,true) */
    val script: Rep[String] = column[String]("script", O.Length(1000,varying=true))
    /** Database column checksum SqlType(int4), Default(None) */
    val checksum: Rep[Option[Int]] = column[Option[Int]]("checksum", O.Default(None))
    /** Database column installed_by SqlType(varchar), Length(100,true) */
    val installedBy: Rep[String] = column[String]("installed_by", O.Length(100,varying=true))
    /** Database column installed_on SqlType(timestamp) */
    val installedOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("installed_on")
    /** Database column execution_time SqlType(int4) */
    val executionTime: Rep[Int] = column[Int]("execution_time")
    /** Database column success SqlType(bool) */
    val success: Rep[Boolean] = column[Boolean]("success")

    /** Index over (installedRank) (database name schema_version_ir_idx) */
    val index1 = index("schema_version_ir_idx", installedRank)
    /** Index over (success) (database name schema_version_s_idx) */
    val index2 = index("schema_version_s_idx", success)
    /** Index over (versionRank) (database name schema_version_vr_idx) */
    val index3 = index("schema_version_vr_idx", versionRank)
  }
  /** Collection-like TableQuery object for table SchemaVersion */
  lazy val SchemaVersion = new TableQuery(tag => new SchemaVersion(tag))

  /** Entity class storing rows of table Tutkinnonosasuoritus
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param tutkintosuoritusId Database column tutkintosuoritus_id SqlType(int4)
   *  @param komoOid Database column komo_oid SqlType(text)
   *  @param status Database column status SqlType(text)
   *  @param arviointiId Database column arviointi_id SqlType(int4), Default(None) */
  case class TutkinnonosasuoritusRow(id: Int, tutkintosuoritusId: Int, komoOid: String, status: String, arviointiId: Option[Int] = None)
  /** GetResult implicit for fetching TutkinnonosasuoritusRow objects using plain SQL queries */
  implicit def GetResultTutkinnonosasuoritusRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[Int]]): GR[TutkinnonosasuoritusRow] = GR{
    prs => import prs._
    TutkinnonosasuoritusRow.tupled((<<[Int], <<[Int], <<[String], <<[String], <<?[Int]))
  }
  /** Table description of table tutkinnonosasuoritus. Objects of this class serve as prototypes for rows in queries. */
  class Tutkinnonosasuoritus(_tableTag: Tag) extends Table[TutkinnonosasuoritusRow](_tableTag, Some("tor"), "tutkinnonosasuoritus") {
    def * = (id, tutkintosuoritusId, komoOid, status, arviointiId) <> (TutkinnonosasuoritusRow.tupled, TutkinnonosasuoritusRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(tutkintosuoritusId), Rep.Some(komoOid), Rep.Some(status), arviointiId).shaped.<>({r=>import r._; _1.map(_=> TutkinnonosasuoritusRow.tupled((_1.get, _2.get, _3.get, _4.get, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column tutkintosuoritus_id SqlType(int4) */
    val tutkintosuoritusId: Rep[Int] = column[Int]("tutkintosuoritus_id")
    /** Database column komo_oid SqlType(text) */
    val komoOid: Rep[String] = column[String]("komo_oid")
    /** Database column status SqlType(text) */
    val status: Rep[String] = column[String]("status")
    /** Database column arviointi_id SqlType(int4), Default(None) */
    val arviointiId: Rep[Option[Int]] = column[Option[Int]]("arviointi_id", O.Default(None))

    /** Foreign key referencing Arviointi (database name tutkinnonosasuoritus_arviointi_id_fkey) */
    lazy val arviointiFk = foreignKey("tutkinnonosasuoritus_arviointi_id_fkey", arviointiId, Arviointi)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing Tutkintosuoritus (database name tutkinnonosasuoritus_tutkintosuoritus_id_fkey) */
    lazy val tutkintosuoritusFk = foreignKey("tutkinnonosasuoritus_tutkintosuoritus_id_fkey", tutkintosuoritusId, Tutkintosuoritus)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Tutkinnonosasuoritus */
  lazy val Tutkinnonosasuoritus = new TableQuery(tag => new Tutkinnonosasuoritus(tag))

  /** Entity class storing rows of table Tutkintosuoritus
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param organisaatioOid Database column organisaatio_oid SqlType(text)
   *  @param personOid Database column person_oid SqlType(text)
   *  @param komoOid Database column komo_oid SqlType(text)
   *  @param status Database column status SqlType(text)
   *  @param arviointiId Database column arviointi_id SqlType(int4), Default(None) */
  case class TutkintosuoritusRow(id: Int, organisaatioOid: String, personOid: String, komoOid: String, status: String, arviointiId: Option[Int] = None)
  /** GetResult implicit for fetching TutkintosuoritusRow objects using plain SQL queries */
  implicit def GetResultTutkintosuoritusRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[Int]]): GR[TutkintosuoritusRow] = GR{
    prs => import prs._
    TutkintosuoritusRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<[String], <<?[Int]))
  }
  /** Table description of table tutkintosuoritus. Objects of this class serve as prototypes for rows in queries. */
  class Tutkintosuoritus(_tableTag: Tag) extends Table[TutkintosuoritusRow](_tableTag, Some("tor"), "tutkintosuoritus") {
    def * = (id, organisaatioOid, personOid, komoOid, status, arviointiId) <> (TutkintosuoritusRow.tupled, TutkintosuoritusRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(organisaatioOid), Rep.Some(personOid), Rep.Some(komoOid), Rep.Some(status), arviointiId).shaped.<>({r=>import r._; _1.map(_=> TutkintosuoritusRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column organisaatio_oid SqlType(text) */
    val organisaatioOid: Rep[String] = column[String]("organisaatio_oid")
    /** Database column person_oid SqlType(text) */
    val personOid: Rep[String] = column[String]("person_oid")
    /** Database column komo_oid SqlType(text) */
    val komoOid: Rep[String] = column[String]("komo_oid")
    /** Database column status SqlType(text) */
    val status: Rep[String] = column[String]("status")
    /** Database column arviointi_id SqlType(int4), Default(None) */
    val arviointiId: Rep[Option[Int]] = column[Option[Int]]("arviointi_id", O.Default(None))

    /** Foreign key referencing Arviointi (database name tutkintosuoritus_arviointi_id_fkey) */
    lazy val arviointiFk = foreignKey("tutkintosuoritus_arviointi_id_fkey", arviointiId, Arviointi)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Tutkintosuoritus */
  lazy val Tutkintosuoritus = new TableQuery(tag => new Tutkintosuoritus(tag))
}
