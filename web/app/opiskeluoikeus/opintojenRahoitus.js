import Http from '../util/http'
import Bacon from 'baconjs'
import { jotpaSallitutRahoituskoodiarvot } from '../jotpa/jotpa'

const valtionosuusrahoitteinen = '1'

export const getRahoitusmuoto = (rahoituskoodi) =>
  Http.cachedGet(
    `/koski/api/editor/koodit/opintojenrahoitus/${rahoituskoodi}`
  ).map((rahoitukset) =>
    rahoitukset.find((rahoitus) => rahoitus.data.koodiarvo === rahoituskoodi)
  )

export const defaultRahoitusmuotoP = Bacon.constant(valtionosuusrahoitteinen)
  .flatMap(getRahoitusmuoto)
  .toProperty()

export const autoFillRahoitusmuoto = ({
  vaatiiRahoituksen,
  rahoitusValittu,
  setDefaultRahoitus,
  setRahoitusNone
}) => {
  if (vaatiiRahoituksen && !rahoitusValittu) {
    setDefaultRahoitus()
  } else if (!vaatiiRahoituksen && rahoitusValittu) {
    setRahoitusNone()
  }
}

export const opiskeluoikeudenTilaVaatiiRahoitusmuodon = (
  opiskeluoikeudenTyyppi,
  tila,
  suoritustyyppi
) => {
  if (
    opiskeluoikeudenTyyppi === 'vapaansivistystyonkoulutus' &&
    suoritustyyppi === 'vstjotpakoulutus'
  ) {
    return ['lasna', 'hyvaksytystisuoritettu'].includes(tila)
  }
  if (
    [
      'aikuistenperusopetus',
      'lukiokoulutus',
      'luva',
      'ibtutkinto',
      'internationalschool',
      'diatutkinto',
      'tuva'
    ].includes(opiskeluoikeudenTyyppi)
  ) {
    return ['lasna', 'valmistunut'].includes(tila)
  }
  if (opiskeluoikeudenTyyppi === 'ammatillinenkoulutus') {
    return ['lasna', 'valmistunut', 'loma'].includes(tila)
  }
  return false
}

export const opiskeluoikeudenRahoitusmuotoEiVoiVaihdella = (
  opiskeluoikeudenTyyppi,
  suoritustyyppi
) =>
  opiskeluoikeudenTyyppi === 'vapaansivistystyonkoulutus' &&
  suoritustyyppi === 'vstjotpakoulutus'

export const defaultRahoituskoodi = (suoritustyyppi) => {
  if (suoritustyyppi?.koodiarvo === 'vstjotpakoulutus') {
    return jotpaSallitutRahoituskoodiarvot[0]
  }
  return valtionosuusrahoitteinen
}
