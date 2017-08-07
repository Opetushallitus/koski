import {modelData, modelLookup, oneOfPrototypes} from './EditorModel'

export const isPaikallinen = (m) => m.value.classes.includes('paikallinenkoulutusmoduuli')
export const isUusi = (oppiaine) => {
  return !modelData(oppiaine, 'tunniste').koodiarvo
}
export const koulutusModuuliprototypes = (suoritus) => oneOfPrototypes(modelLookup(suoritus, 'koulutusmoduuli'))