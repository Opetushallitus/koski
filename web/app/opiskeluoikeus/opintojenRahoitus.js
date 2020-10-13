import Http from '../util/http'

const valtionosuusrahoitteinen = '1'
export const defaultRahoitusmuotoP = Http.cachedGet('/koski/api/editor/koodit/opintojenrahoitus/1').map(rahoitukset =>
  rahoitukset.find(rahoitus => rahoitus.data.koodiarvo === valtionosuusrahoitteinen)
)

export const autoFillRahoitusmuoto = ({vaatiiRahoituksen, rahoitusValittu, setDefaultRahoitus, setRahoitusNone}) => {
  if (vaatiiRahoituksen && !rahoitusValittu) {
    setDefaultRahoitus()
  } else if (!vaatiiRahoituksen && rahoitusValittu) {
    setRahoitusNone()
  }
}

export const opiskeluoikeudenTilaVaatiiRahoitusmuodon = (opiskeluoikeudenTyyppi, tila) => {
  if (['aikuistenperusopetus', 'lukiokoulutus', 'luva', 'ibtutkinto', 'internationalschool', 'diatutkinto'].includes(opiskeluoikeudenTyyppi)) {
    return ['lasna', 'valmistunut'].includes(tila)
  }
  if ('ammatillinenkoulutus' === opiskeluoikeudenTyyppi) {
    return ['lasna', 'valmistunut', 'loma'].includes(tila)
  }
  return false
}
