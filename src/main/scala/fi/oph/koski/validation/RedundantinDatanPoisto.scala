package fi.oph.koski.validation

import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, OikeusmaksuttomaanAsuntolapaikkaanAikajaksona, OikeusmaksuttomaanAsuntolapaikkaanBooleanina}

object RedundantinDatanPoisto {
  def dropRedundantData(oo: KoskeenTallennettavaOpiskeluoikeus) = {
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
