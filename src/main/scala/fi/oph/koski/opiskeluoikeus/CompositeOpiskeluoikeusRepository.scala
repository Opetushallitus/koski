package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.henkilo.PossiblyUnverifiedHenkilöOid
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{HenkilötiedotJaOid, KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}
import fi.oph.koski.util.WithWarnings

import scala.collection.mutable.ListBuffer

class CompositeOpiskeluoikeusRepository(main: KoskiOpiskeluoikeusRepository, virta: AuxiliaryOpiskeluoikeusRepository, ytr: AuxiliaryOpiskeluoikeusRepository) {

  private val aux: List[AuxiliaryOpiskeluoikeusRepository] = List(virta, ytr)

  def filterOppijat(oppijat: List[HenkilötiedotJaOid])(implicit user: KoskiSession): List[HenkilötiedotJaOid] = {
    (main :: aux).foldLeft((oppijat, ListBuffer.empty[HenkilötiedotJaOid])) { case ((left, found), repo) =>
      val newlyFound = repo.filterOppijat(left)
      val stillLeft = left.diff(newlyFound)
      (stillLeft, found ++= newlyFound)
    }._2.toList
  }

  def findByOid(oid: String)(implicit user: KoskiSession): Either[HttpStatus, OpiskeluoikeusRow] =
    main.findByOid(oid)

  def createOrUpdate(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowUpdate: Boolean)(implicit user: KoskiSession): Either[HttpStatus, CreateOrUpdateResult] =
    main.createOrUpdate(oppijaOid, opiskeluoikeus, allowUpdate)

  def findByOppijaOid(oid: String)(implicit user: KoskiSession): WithWarnings[Seq[Opiskeluoikeus]] = {
    WithWarnings((main :: aux).par.flatMap(_.findByOppijaOid(oid)).toList, Nil)
  }

  def findByUserOid(oid: String)(implicit user: KoskiSession): WithWarnings[Seq[Opiskeluoikeus]] = {
    WithWarnings((main :: aux).par.flatMap(_.findByUserOid(oid)).toList, Nil)
  }

  def getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSession): Either[HttpStatus, List[Oid]] =
    main.getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid)
}
