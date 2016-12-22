package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.henkilo.PossiblyUnverifiedHenkilöOid
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.{HenkilötiedotJaOid, KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}
import fi.oph.koski.util.PaginationSettings

class CompositeOpiskeluOikeusRepository(main: OpiskeluOikeusRepository, aux: List[AuxiliaryOpiskeluOikeusRepository]) extends OpiskeluOikeusRepository {
  override def streamingQuery(filters: List[OpiskeluoikeusQueryFilter], sorting: Option[OpiskeluoikeusSortOrder], pagination: Option[PaginationSettings])(implicit user: KoskiSession) = main.streamingQuery(filters, sorting, pagination)

  override def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiSession) = {
    (main :: aux).foldLeft((oppijat, Nil: Seq[HenkilötiedotJaOid])) { case ((left, found), repo) =>
      val newlyFound = repo.filterOppijat(left)
      val stillLeft = left.diff(newlyFound)
      (stillLeft, found ++ newlyFound)
    }._2
  }

  override def findById(id: Int)(implicit user: KoskiSession) = main.findById(id)

  override def createOrUpdate(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluOikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession) = main.createOrUpdate(oppijaOid, opiskeluOikeus)

  override def findByOppijaOid(oid: String)(implicit user: KoskiSession) = (main :: aux).par.flatMap(_.findByOppijaOid(oid)).toList

  override def findByUserOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus] = main.findByUserOid(user.oid)

  override def delete(id: Int)(implicit user: KoskiSession) = main.delete(id)
}
