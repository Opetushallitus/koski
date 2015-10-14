package fi.oph.tor.opintooikeus

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db._
import fi.oph.tor.json.Json
import fi.oph.tor.oppija.Oppija
import fi.oph.tor.user.UserContext
import org.json4s._

class PostgresOpintoOikeusRepository(db: DB) extends OpintoOikeusRepository with Futures with GlobalExecutionContext {
  protected class OpintoOikeusTable(tag: Tag) extends Table[OpintoOikeusRow](tag, "opintooikeus") {
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def data = column[JValue]("data")

    def * = (id, data) <> (OpintoOikeusRow.tupled, OpintoOikeusRow.unapply)
  }
  protected val OpintoOikeudet = TableQuery[OpintoOikeusTable]

  override def filterOppijat(oppijat: List[Oppija])(implicit userContext: UserContext) = {
    val all = findAll
    oppijat.filter { oppija => all.exists(opintoOikeus => opintoOikeus.oppijaOid == oppija.oid) }
  }

  override def findBy(oppija: Oppija)(implicit userContext: UserContext) = {
    findAll.filter(_.oppijaOid == oppija.oid).toList
  }

  private def findAll(implicit userContext: UserContext): Seq[OpintoOikeus] = {
    await(db.run(OpintoOikeudet.result)).map(_.toOpintoOikeus).filter(opintoOikeus => userContext.hasReadAccess(opintoOikeus.oppilaitosOrganisaatio))
  }

  override def create(opintoOikeus: OpintoOikeus) = {
    await(db.run(OpintoOikeudet += new OpintoOikeusRow(opintoOikeus)))
  }
}

case class OpintoOikeusRow(id: Int, data: JValue) {
  def toOpintoOikeus = {
    Json.fromJValue[OpintoOikeus](data)
  }

  def this(opintoOikeus: OpintoOikeus) = {
    this(0, Json.toJValue(opintoOikeus))
  }
}