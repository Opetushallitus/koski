package fi.oph.koski.tiedonsiirto

import fi.oph.koski.db.{Futures, GlobalExecutionContext}
import fi.oph.koski.db.Tables.Tiedonsiirto
import org.json4s.JsonAST.JValue
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

class TiedonsiirtoRepository(db: DB) extends  GlobalExecutionContext with Futures {
  def create(kayttajaOid: String, tallentajaOrganisaatioOid:  String, data: Option[JValue]) = {
    db.run {
      Tiedonsiirto.map { row => (row.kayttajaOid, row.tallentajaOrganisaatioOid, row.data) } += (kayttajaOid, tallentajaOrganisaatioOid, data)
    }
  }
}

