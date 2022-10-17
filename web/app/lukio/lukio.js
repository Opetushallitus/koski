import { modelData } from '../editor/EditorModel'

const perusteenDiaarinumeroToOppimäärä = (diaarinumero) => {
  switch (diaarinumero) {
    case '60/011/2015':
    case '33/011/2003':
    case 'OPH-2263-2019':
    case '56/011/2015':
    case 'OPH-4958-2020':
      return 'nuortenops'
    case '70/011/2015':
    case '4/011/2004':
    case 'OPH-2267-2019':
      return 'aikuistenops'
  }
}

const sallitutRahoituskoodiarvot = ['1', '6']

const suoritetutKurssit = (kurssit) =>
  kurssit.map((k) => modelData(k)).filter((k) => k.arviointi)

const hylkäämättömätOsasuoritukset = (kurssit) =>
  kurssit.filter((k) => modelData(k, 'arviointi.-1.arvosana.koodiarvo') !== 'H')
const arvioidutOsasuoritukset = (kurssit) =>
  kurssit.filter((k) => modelData(k, 'arviointi.-1.arvosana.koodiarvo') !== 'O')
const hyväksytystiArvioidutOsasuoritukset = (osasuoritukset) =>
  osasuoritukset.filter(
    (k) =>
      !['H', 'O', '4'].includes(modelData(k, 'arviointi.-1.arvosana.koodiarvo'))
  )

const laajuudet = (osasuoritukset) =>
  osasuoritukset
    .map((k) => {
      const laajuus = modelData(k, 'koulutusmoduuli.laajuus.arvo')
      return laajuus || 1
    })
    .reduce((x, y) => x + y, 0)

const isLukioOps2019 = (suoritusModel) =>
  ['OPH-2263-2019', 'OPH-2267-2019'].includes(
    modelData(suoritusModel, 'koulutusmoduuli.perusteenDiaarinumero')
  )

const isPreIbLukioOps2019 = (suoritusModel) =>
  ['preiboppimaara2019'].includes(
    modelData(suoritusModel, 'koulutusmoduuli.tunniste.koodiarvo')
  )

const isLukionOppiaineidenOppimaarienSuoritus2019 = (suoritusModel) =>
  isLukioOps2019(suoritusModel) &&
  ['lukionaineopinnot'].includes(
    modelData(suoritusModel, 'koulutusmoduuli.tunniste.koodiarvo')
  )

const isLuvaOps2019 = (suoritusModel) =>
  ['OPH-4958-2020'].includes(
    modelData(suoritusModel, 'koulutusmoduuli.perusteenDiaarinumero')
  )

export {
  perusteenDiaarinumeroToOppimäärä,
  sallitutRahoituskoodiarvot,
  suoritetutKurssit,
  hylkäämättömätOsasuoritukset,
  laajuudet,
  isLukioOps2019,
  isPreIbLukioOps2019,
  isLuvaOps2019,
  isLukionOppiaineidenOppimaarienSuoritus2019,
  arvioidutOsasuoritukset,
  hyväksytystiArvioidutOsasuoritukset
}
