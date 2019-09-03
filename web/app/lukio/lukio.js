import {
  modelData
} from '../editor/EditorModel'

const perusteenDiaarinumeroToOppimäärä = diaarinumero => {
  switch (diaarinumero) {
    case '60/011/2015':
    case '33/011/2003':
      return 'nuortenops'
    case '70/011/2015':
    case '4/011/2004':
      return 'aikuistenops'
  }
}

const sallitutRahoituskoodiarvot = ['1', '6']

const suoritetutKurssit = kurssit => kurssit.map(k => modelData(k)).filter(k => k.arviointi)
const hyväksytystiSuoritetutKurssit = kurssit => kurssit.filter(k => modelData(k, 'arviointi.-1.arvosana.koodiarvo') !== 'H')

const laajuudet = kurssit => kurssit.map(k => {
  const laajuus = modelData(k, 'koulutusmoduuli.laajuus.arvo')
  return laajuus ? laajuus : 1
}).reduce((x, y) => x + y, 0)

export {
  perusteenDiaarinumeroToOppimäärä,
  sallitutRahoituskoodiarvot,
  suoritetutKurssit,
  hyväksytystiSuoritetutKurssit,
  laajuudet
}
