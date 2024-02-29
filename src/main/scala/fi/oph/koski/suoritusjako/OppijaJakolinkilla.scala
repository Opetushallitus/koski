package fi.oph.koski.suoritusjako

import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus, Oppija}
import fi.oph.koski.suoritusjako.common.Jakolinkki

case class OppijaJakolinkillä(
  jakolinkki: Option[Jakolinkki] = None,
  henkilö: Henkilö,
  opiskeluoikeudet: Seq[Opiskeluoikeus]
) {
  def toOppija(): Oppija =
    Oppija(henkilö, opiskeluoikeudet)
}
