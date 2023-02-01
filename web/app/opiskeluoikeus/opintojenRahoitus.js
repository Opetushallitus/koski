import Http from '../util/http'
import Bacon from 'baconjs'
import {
  isJotpaRahoitteinenKoodiarvo,
  isJotpaRahoitteinen,
  jotpaSallitutRahoituskoodiarvot
} from '../jotpa/jotpa'

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
  opiskeluoikeudenTyyppiKoodiarvo,
  tila,
  suoritustyyppiKoodiarvo
) => {
  if (
    isJotpaRahoitteinenKoodiarvo(
      opiskeluoikeudenTyyppiKoodiarvo,
      suoritustyyppiKoodiarvo
    )
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
      'diatutkinto'
    ].includes(opiskeluoikeudenTyyppiKoodiarvo)
  ) {
    return ['lasna', 'valmistunut'].includes(tila)
  }
  if (
    ['ammatillinenkoulutus', 'tuva'].includes(opiskeluoikeudenTyyppiKoodiarvo)
  ) {
    return ['lasna', 'valmistunut', 'loma'].includes(tila)
  }
  return false
}

export const opiskeluoikeudenRahoitusmuotoEiVoiVaihdella = isJotpaRahoitteinen

export const defaultRahoituskoodi = (opiskeluoikeudenTyyppi, suoritustyyppi) =>
  isJotpaRahoitteinen(opiskeluoikeudenTyyppi, suoritustyyppi)
    ? jotpaSallitutRahoituskoodiarvot[0]
    : valtionosuusrahoitteinen
