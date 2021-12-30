package fi.oph.koski.validation

import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, OikeusmaksuttomaanAsuntolapaikkaanBooleanina, OikeusmaksuttomaanAsuntolapaikkaanAikajaksona}

object DeprekoitujenYhteistenKenttienFiltteröinti {
  def filterDeprekoidutKentät(oo: KoskeenTallennettavaOpiskeluoikeus) = {
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
}
