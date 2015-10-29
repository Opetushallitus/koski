package fi.oph.tor.opintooikeus

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.PostgresDriverWithJsonSupport.jsonMethods._

import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db._
import fi.oph.tor.http.{HttpStatus}
import fi.oph.tor.json.Json
import fi.oph.tor.oppija.Oppija
import fi.oph.tor.user.UserContext
import org.json4s._

class PostgresOpintoOikeusRepository(db: DB) extends OpintoOikeusRepository with Futures with GlobalExecutionContext {
  protected class OpintoOikeusTable(tag: Tag) extends Table[OpintoOikeusRow](tag, "opintooikeus") {
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val oppijaOid: Rep[String] = column[String]("oppija_oid")
    def data = column[JValue]("data")

    def * = (id, oppijaOid, data) <> (OpintoOikeusRow.tupled, OpintoOikeusRow.unapply)
  }
  protected val OpintoOikeudet = TableQuery[OpintoOikeusTable]

  // Note: this is a naive implementation. All filtering should be moved to query-level instead of in-memory-level


  override def filterOppijat(oppijat: Seq[Oppija])(implicit userContext: UserContext) = {
    val all = findAllRows
    oppijat.filter { oppija => all.exists(opintoOikeus => opintoOikeus.oppijaOid == oppija.oid.get)}
  }

  override def findByOppijaOid(oid: String)(implicit userContext: UserContext): Seq[OpintoOikeus] = {
    find(OpintoOikeudet.filter(_.oppijaOid === oid))
  }

  override def create(oppijaOid: String, opintoOikeus: OpintoOikeus) = {
    Right(await(db.run(OpintoOikeudet.returning(OpintoOikeudet.map(_.id)) += new OpintoOikeusRow(oppijaOid, opintoOikeus))))
  }

  private def findAll(implicit userContext: UserContext): Seq[OpintoOikeus] = {
    findAllRows.map(_.toOpintoOikeus)
  }

  override def find(identifier: OpintoOikeusIdentifier)(implicit userContext: UserContext) = identifier match{
    case PrimaryKey(id) => find(OpintoOikeudet.filter(_.id === id)).headOption
    case IdentifyingSetOfFields(oppijaOid, oppilaitosOrganisaatio, ePerusteetDiaarinumero) => {
      findByOppijaOid(oppijaOid).find({
        new IdentifyingSetOfFields(oppijaOid, _) == identifier
      })
    }
  }

  override def update(oppijaOid: String, opintoOikeus: OpintoOikeus) = {
    // TODO: always overriding existing data can not be the eventual update strategy
    val rowsUpdated: Int = await(db.run(OpintoOikeudet.filter(_.id === opintoOikeus.id.get).map(_.data).update(new OpintoOikeusRow(oppijaOid, opintoOikeus).data)))
    rowsUpdated match {
      case 1 => HttpStatus.ok
      case x => HttpStatus.internalError("Unexpected number of updated rows: " + x)
    }
  }

  private def findAllRows(implicit userContext: UserContext): Seq[OpintoOikeusRow] = {
    findRows(OpintoOikeudet)
  }

  private def find(filter: Query[OpintoOikeusTable, OpintoOikeusRow, Seq])(implicit userContext: UserContext): Seq[OpintoOikeus] = {
    findRows(filter).map(_.toOpintoOikeus)
  }

  private def findRows(filter: PostgresDriverWithJsonSupport.api.Query[OpintoOikeusTable, OpintoOikeusRow, Seq])(implicit userContext: UserContext): Seq[OpintoOikeusRow] = {
    await(db.run(filter.result)).filter(withAccess)
  }

  private def withAccess(oikeus: OpintoOikeusRow)(implicit userContext: UserContext) = {
    userContext.hasReadAccess(oikeus.toOpintoOikeus.oppilaitosOrganisaatio)
  }
}

case class OpintoOikeusRow(id: Int, oppijaOid: String, data: JValue) {
  lazy val toOpintoOikeus: OpintoOikeus = {
    Json.fromJValue[OpintoOikeus](data).copy ( id = Some(id) )
  }

  def this(oppijaOid: String, opintoOikeus: OpintoOikeus) = {
    this(0, oppijaOid, Json.toJValue(opintoOikeus))
  }
}