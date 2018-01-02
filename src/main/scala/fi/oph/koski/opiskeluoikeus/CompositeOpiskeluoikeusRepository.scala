package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.henkilo.PossiblyUnverifiedHenkilöOid
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{HenkilötiedotJaOid, KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}

import scala.collection.mutable.ListBuffer

class CompositeOpiskeluoikeusRepository(main: OpiskeluoikeusRepository, aux: List[AuxiliaryOpiskeluoikeusRepository]) extends OpiskeluoikeusRepository {
  override def filterOppijat(oppijat: List[HenkilötiedotJaOid])(implicit user: KoskiSession): List[HenkilötiedotJaOid] = {
    (main :: aux).foldLeft((oppijat, ListBuffer.empty[HenkilötiedotJaOid])) { case ((left, found), repo) =>
      val newlyFound = repo.filterOppijat(left)
      val stillLeft = left.diff(newlyFound)
      (stillLeft, found ++= newlyFound)
    }._2.toList
  }

  override def findByOid(oid: String)(implicit user: KoskiSession): Either[HttpStatus, OpiskeluoikeusRow] = main.findByOid(oid)

  override def createOrUpdate(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowUpdate: Boolean)(implicit user: KoskiSession) = main.createOrUpdate(oppijaOid, opiskeluoikeus, allowUpdate)

  override def findByOppijaOid(oid: String)(implicit user: KoskiSession) = (main :: aux).par.flatMap(_.findByOppijaOid(oid)).toList

  override def findByUserOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus] = (main :: aux).par.flatMap(_.findByUserOid(oid)).toList

  override def getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSession): Either[HttpStatus, List[Oid]] = main.getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid)
}
