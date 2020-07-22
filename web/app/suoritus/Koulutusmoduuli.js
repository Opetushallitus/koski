import {modelData, modelLookup, oneOfPrototypes} from '../editor/EditorModel'

export const isPaikallinen = (m) => m && m.value.classes.includes('paikallinenkoulutusmoduuli')
export const isKieliaine = (m) => m && m.value.classes.includes('kieliaine')
export const isÄidinkieli = (m) => m && m.value.classes.includes('aidinkieli')
export const isUusi = (oppiaine) => {
  return !modelData(oppiaine, 'tunniste').koodiarvo
}
export const isIBOppiaine = (m) => m && m.value.classes.includes('iboppiaine')
export const isLukionKurssi = (m) => m && m.value.classes.includes('lukionkurssi')
export const isPreIBKurssi = (m) => m && m.value.classes.includes('preibkurssi')
export const isDiaKurssi = (m) => m && m.value.classes.includes('diaoppiaineenosasuoritus')
export const isLukioonValmistavanKoulutuksenKurssi = (m) => m && m.value.classes.includes('lukioonvalmistavankoulutuksenkurssi')
export const isLukionKurssimainen = (m) => isLukionKurssi(m) || isPreIBKurssi(m) || isLukioonValmistavanKoulutuksenKurssi(m)
export const isLukionMatematiikka = (m) => m && m.value.classes.includes('lukionmatematiikka')
export const koulutusModuuliprototypes = (suoritus) => oneOfPrototypes(modelLookup(suoritus, 'koulutusmoduuli'))
export const isIBKurssi = (m) => m && m.value.classes.includes('ibkurssi')
export const isLukio2019ModuuliTaiOpintojakso = (m) => m && m.value.classes.includes('lukionmoduulitaipaikallinenopintojakso2019')
export const tutkinnonNimi = m => modelData(m, 'perusteenNimi') ? modelLookup(m, 'perusteenNimi') : modelLookup(m, 'tunniste')
