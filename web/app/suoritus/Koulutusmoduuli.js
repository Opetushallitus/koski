import {modelData, modelLookup, oneOfPrototypes} from '../editor/EditorModel'

export const isPaikallinen = (m) => m && m.value.classes.includes('paikallinenkoulutusmoduuli')
export const isKieliaine = (m) => m && m.value.classes.includes('kieliaine')
export const isÄidinkieli = (m) => m && m.value.classes.includes('aidinkieli')
export const isUusi = (oppiaine) => {
  return !modelData(oppiaine, 'tunniste').koodiarvo
}
export const isLukionKurssi = (m) => m && m.value.classes.includes('lukionkurssi')
export const koulutusModuuliprototypes = (suoritus) => oneOfPrototypes(modelLookup(suoritus, 'koulutusmoduuli'))
