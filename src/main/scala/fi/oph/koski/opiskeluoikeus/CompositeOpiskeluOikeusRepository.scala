package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.koski.QueryFilter
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.oppija.PossiblyUnverifiedOppijaOid
import fi.oph.koski.schema.{HenkilötiedotJaOid, KoskeenTallennettavaOpiskeluoikeus}

class CompositeOpiskeluOikeusRepository(main: OpiskeluOikeusRepository, aux: List[AuxiliaryOpiskeluOikeusRepository]) extends OpiskeluOikeusRepository {
  override def query(filters: List[QueryFilter])(implicit user: KoskiUser) = main.query(filters)

  override def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiUser) = {
    (main :: aux).foldLeft((oppijat, Nil: Seq[HenkilötiedotJaOid])) { case ((left, found), repo) =>
      val newlyFound = repo.filterOppijat(left)
      val stillLeft = left.diff(newlyFound)
      (stillLeft, found ++ newlyFound)
    }._2
  }

  override def findById(id: Int)(implicit user: KoskiUser) = main.findById(id)

  override def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiUser) = main.createOrUpdate(oppijaOid, opiskeluOikeus)

  override def findByOppijaOid(oid: String)(implicit user: KoskiUser) = (main :: aux).par.flatMap(_.findByOppijaOid(oid)).toList
}
