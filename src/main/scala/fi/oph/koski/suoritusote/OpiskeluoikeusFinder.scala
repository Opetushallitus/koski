package fi.oph.koski.suoritusote

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koski.KoskiFacade
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.schema._

case class OpiskeluoikeusFinder(koski: KoskiFacade) {
  def opiskeluoikeudet(oppijaOid: String, params: Map[String, String])(implicit user: KoskiUser): Either[HttpStatus, Oppija] with Product with Serializable = {
    val filters: List[(Opiskeluoikeus => Boolean)] = params.toList.flatMap {
      case ("oppilaitos", oppilaitosOid: String) => Some({ oo: Opiskeluoikeus => oo.oppilaitos.oid == oppilaitosOid })
      case ("opiskeluoikeus", ooId: String) => Some({ oo: Opiskeluoikeus => oo.id.exists(_.toString == ooId) })
      case (_, _) => None
    }
    koski.findOppija(oppijaOid) match {
      case Right(Oppija(henkilö: TäydellisetHenkilötiedot, opiskeluoikeudet)) =>
        Right(Oppija(henkilö, opiskeluoikeudet.filter{ oo: Opiskeluoikeus => filters.forall { f => f(oo)} }.toList))
      case _ =>
        Left(KoskiErrorCategory.notFound.oppijaaEiLöydy())
    }
  }
}
