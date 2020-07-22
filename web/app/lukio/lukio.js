import {
  modelData
} from '../editor/EditorModel'
import {suorituksenTyyppi} from '../suoritus/Suoritus'

const perusteenDiaarinumeroToOppimäärä = diaarinumero => {
  switch (diaarinumero) {
    case '60/011/2015':
    case '33/011/2003':
    case 'OPH-2263-2019':
      return 'nuortenops'
    case '70/011/2015':
    case '4/011/2004':
    case 'OPH-2267-2019':
      return 'aikuistenops'
  }
}

const sallitutRahoituskoodiarvot = ['1', '6']

const suoritetutKurssit = kurssit => kurssit.map(k => modelData(k)).filter(k => k.arviointi)
const hyväksytystiSuoritetutOsasuoritukset = kurssit => kurssit.filter(k => modelData(k, 'arviointi.-1.arvosana.koodiarvo') !== 'H')

const laajuudet = osasuoritukset => osasuoritukset.map(k => {
  const laajuus = modelData(k, 'koulutusmoduuli.laajuus.arvo')
  return laajuus ? laajuus : 1
}).reduce((x, y) => x + y, 0)


const isLukioOps2019 = suoritusModel =>
  [ 'lukionoppimaara2019', 'lukionoppiaineidenoppimaarat2019' ].includes(suorituksenTyyppi(suoritusModel))

export {
  perusteenDiaarinumeroToOppimäärä,
  sallitutRahoituskoodiarvot,
  suoritetutKurssit,
  hyväksytystiSuoritetutOsasuoritukset,
  laajuudet,
  isLukioOps2019
}
