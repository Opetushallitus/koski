package fi.oph.koski.valpas.valpasrepository

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.db.ValpasDatabase
import fi.oph.koski.valpas.db.ValpasSchema.{OpiskeluoikeusLisätiedot, OpiskeluoikeusLisätiedotKey, OpiskeluoikeusLisätiedotRow}
import fi.oph.koski.valpas.oppija.{OppijaHakutilanteillaLaajatTiedot, ValpasErrorCategory}

class OpiskeluoikeusLisätiedotRepository(valpasDatabase: ValpasDatabase, config: Config) extends QueryMethods with Logging {
  protected val db: DB = valpasDatabase.db

  private def keysForOppija(oppijanTiedot: OppijaHakutilanteillaLaajatTiedot): Seq[OpiskeluoikeusLisätiedotKey] = {
    oppijanTiedot.oppija.opiskeluoikeudet.filter(_.onHakeutumisValvottava).map(oo =>
      OpiskeluoikeusLisätiedotKey(
        oppijaOid = oppijanTiedot.oppija.henkilö.oid,
        opiskeluoikeusOid = oo.oid,
        oppilaitosOid = oo.oppilaitos.oid
      )
    )
  }

  def readForOppijat(oppijoidenTiedot: Seq[OppijaHakutilanteillaLaajatTiedot])
  : Seq[(OppijaHakutilanteillaLaajatTiedot, Seq[OpiskeluoikeusLisätiedotRow])] = {
    val keys = oppijoidenTiedot.flatMap(keysForOppija)
    val lisätiedotByOppijaOid = read(keys).groupBy(_.oppijaOid)
    oppijoidenTiedot.map(oppijanTiedot => (
      oppijanTiedot,
      lisätiedotByOppijaOid.getOrElse(oppijanTiedot.oppija.henkilö.oid, Seq())
    ))
  }

  private def read(keys: Seq[OpiskeluoikeusLisätiedotKey]): Seq[OpiskeluoikeusLisätiedotRow] = {
    if (keys.isEmpty) {
      Seq()
    } else {
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

  def truncate(): Unit = {
    if (Environment.isMockEnvironment(config)) {
      runDbSync(OpiskeluoikeusLisätiedot.delete)
    } else {
      throw new RuntimeException("Opiskeluoikeuden lisätietoja ei voi tyhjentää tuotantotilassa")
    }
  }
}
