package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.henkilo.PossiblyUnverifiedHenkilöOid
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.{HenkilötiedotJaOid, KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}
import fi.oph.koski.util.PaginationSettings

class CompositeOpiskeluoikeusRepository(main: OpiskeluoikeusRepository, aux: List[AuxiliaryOpiskeluoikeusRepository]) extends OpiskeluoikeusRepository {
  override def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiSession) = {
    (main :: aux).foldLeft((oppijat, Nil: Seq[HenkilötiedotJaOid])) { case ((left, found), repo) =>
      val newlyFound = repo.filterOppijat(left)
      val stillLeft = left.diff(newlyFound)
      (stillLeft, found ++ newlyFound)
    }._2
  }

  override def findById(id: Int)(implicit user: KoskiSession) = main.findById(id)

  override def createOrUpdate(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowUpdate: Boolean)(implicit user: KoskiSession) = main.createOrUpdate(oppijaOid, opiskeluoikeus, allowUpdate)

  override def findByOppijaOid(oid: String)(implicit user: KoskiSession) = (main :: aux).par.flatMap(_.findByOppijaOid(oid)).toList

  override def findByUserOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus] = main.findByUserOid(user.oid)

  override def delete(id: Int)(implicit user: KoskiSession) = main.delete(id)
}
