package fi.oph.koski.validation

import fi.oph.koski.schema.{AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot, KoskeenTallennettavaOpiskeluoikeus, OikeusmaksuttomaanAsuntolapaikkaanAikajaksona, OikeusmaksuttomaanAsuntolapaikkaanBooleanina}

object DeprekoitujenKenttienPudotus {
  def dropDeprekoidutKentät(oo: KoskeenTallennettavaOpiskeluoikeus) = {
    handleOpiskeluoikeudenLisätiedot(oo)
  }

  def handleOpiskeluoikeudenLisätiedot(oo: KoskeenTallennettavaOpiskeluoikeus) = {
    if (oo.lisätiedot.isDefined) {
      oo.withLisätiedot(oo.lisätiedot.map {
        case aikajaksona: OikeusmaksuttomaanAsuntolapaikkaanAikajaksona => aikajaksona.withOikeusMaksuttomaanAsuntolapaikkaan(None)
        case booleanina: OikeusmaksuttomaanAsuntolapaikkaanBooleanina => booleanina.withOikeusMaksuttomaanAsuntolapaikkaan(None)
        case any => any
      })
    } else {
      oo
    }
  }

  def handleAikuistenPerusopetus(oo: KoskeenTallennettavaOpiskeluoikeus) = {
    oo.lisätiedot match {
      case Some(lisätiedot: AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot) => oo.withLisätiedot(
        Some(lisätiedot.copy(
          tukimuodot = None,
          tehostetunTuenPäätökset = None,
          tehostetunTuenPäätös = None,
          vuosiluokkiinSitoutumatonOpetus = None,
          vammainen = None,
          vaikeastiVammainen = None
        ))
      )
      case _ => oo
    }
  }
}
