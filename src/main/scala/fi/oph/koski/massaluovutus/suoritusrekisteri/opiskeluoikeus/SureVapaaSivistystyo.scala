package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeus


object SureVapaanSivistystyönOpiskeluoikeus {
  def apply(o: VapaanSivistystyönOpiskeluoikeus): SureOpiskeluoikeus =
    SureOpiskeluoikeus(o)
}
