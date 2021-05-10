package fi.oph.koski.valpas.valpasrepository

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.db.ValpasDatabase
import fi.oph.koski.valpas.db.ValpasSchema.{OpiskeluoikeusLisätiedot, OpiskeluoikeusLisätiedotKey, OpiskeluoikeusLisätiedotRow}

class OpiskeluoikeusLisätiedotRepository(valpasDatabase: ValpasDatabase) extends QueryMethods with Logging {
  protected val db: DB = valpasDatabase.db

  def read(keys: Set[OpiskeluoikeusLisätiedotKey]): Seq[OpiskeluoikeusLisätiedotRow] = {
    runDbSync(OpiskeluoikeusLisätiedot
      .filter { t =>
        keys.map(k =>
          t.oppijaOid === k.oppijaOid &&
            t.opiskeluoikeusOid === k.opiskeluoikeusOid &&
            t.oppilaitosOid === k.oppilaitosOid
        ).reduceLeft(_ || _)
      }.result
    )
  }

  def setMuuHaku(key: OpiskeluoikeusLisätiedotKey, value: Boolean): HttpStatus = {
    runDbSync(OpiskeluoikeusLisätiedot.insertOrUpdate(OpiskeluoikeusLisätiedotRow(
      oppijaOid = key.oppijaOid,
      opiskeluoikeusOid = key.opiskeluoikeusOid,
      oppilaitosOid = key.oppilaitosOid,
      muuHaku = value
    ))) match {
      case 1 => HttpStatus.ok
      case _ => ValpasErrorCategory.internalError()
    }
  }

  def truncate(): Unit = runDbSync(OpiskeluoikeusLisätiedot.delete) // TODO: Lisää tsekki ettei olla tuotannossa
}
