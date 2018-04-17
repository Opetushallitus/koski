package fi.oph.koski.suoritusote

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.oppija.KoskiOppijaFacade
import fi.oph.koski.schema._
import fi.oph.koski.util.WithWarnings

case class OpiskeluoikeusFinder(koski: KoskiOppijaFacade) {
  def opiskeluoikeudet(oppijaOid: String, params: Map[String, String])(implicit user: KoskiSession): Either[HttpStatus, WithWarnings[Oppija]] with Product with Serializable = {
    val filters: List[(Opiskeluoikeus => Boolean)] = params.toList.flatMap {
      case ("oppilaitos", oppilaitosOid: String) => Some({ oo: Opiskeluoikeus => oo.oppilaitos.map(_.oid) == Some(oppilaitosOid) })
      case ("opiskeluoikeus", ooOid: String) => Some({ oo: Opiskeluoikeus => oo.oid.contains(ooOid) })
      case (_, _) => None
    }
    koski.findOppija(oppijaOid) match {
      case Right(oppijaWithWarnings) =>
        Right(oppijaWithWarnings.map(o => o.copy(
          opiskeluoikeudet = o.opiskeluoikeudet.filter{ oo: Opiskeluoikeus => filters.forall { f => f(oo)} }.toList
        )))
      case _ =>
        Left(KoskiErrorCategory.notFound.oppijaaEiLöydy())
    }
  }
}
