package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.koski.QueryFilter
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.oppija.PossiblyUnverifiedOppijaOid
import fi.oph.koski.schema.{Opiskeluoikeus, HenkilötiedotJaOid, KoskeenTallennettavaOpiskeluoikeus}

class CompositeOpiskeluOikeusRepository(main: OpiskeluOikeusRepository, aux: List[AuxiliaryOpiskeluOikeusRepository]) extends OpiskeluOikeusRepository {
  override def query(filters: List[QueryFilter])(implicit user: KoskiSession) = main.query(filters)

  override def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiSession) = {
    (main :: aux).foldLeft((oppijat, Nil: Seq[HenkilötiedotJaOid])) { case ((left, found), repo) =>
      val newlyFound = repo.filterOppijat(left)
      val stillLeft = left.diff(newlyFound)
      (stillLeft, found ++ newlyFound)
    }._2
  }

  override def findById(id: Int)(implicit user: KoskiSession) = main.findById(id)

  override def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession) = main.createOrUpdate(oppijaOid, opiskeluOikeus)

  override def findByOppijaOid(oid: String)(implicit user: KoskiSession) = (main :: aux).par.flatMap(_.findByOppijaOid(oid)).toList

  override def findByUserOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus] = main.findByUserOid(user.oid)

  override def delete(id: Int)(implicit user: KoskiSession) = main.delete(id)
}
