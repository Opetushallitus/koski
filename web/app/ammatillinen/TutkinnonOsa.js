import {wrapOptional} from '../editor/EditorModel'
import {contextualizeSubModel, modelItems, oneOfPrototypes} from '../editor/EditorModel'
import * as R from 'ramda'
import {isKieliaine, isÄidinkieli} from '../suoritus/Koulutusmoduuli'

export const placeholderForNonGrouped = '999999'

export const createTutkinnonOsanSuoritusPrototype = (osasuoritukset, groupId) => {
  osasuoritukset = wrapOptional(osasuoritukset)
  let newItemIndex = modelItems(osasuoritukset).length
  let suoritusProto = contextualizeSubModel(osasuoritukset.arrayPrototype, osasuoritukset, newItemIndex)
  let preferredClass = groupId == '2' ? 'yhteisenammatillisentutkinnonosansuoritus' : 'muunammatillisentutkinnonosansuoritus'
  let sortValue = (oneOfProto) => oneOfProto.value.classes.includes(preferredClass) ? 0 : 1
  // TODO: onlyWhen is wrongly copied from implementing case class to traits prototype. This should really be fixed in the backend.
  let alternatives = oneOfPrototypes(R.dissoc('onlyWhen', suoritusProto))
  suoritusProto = alternatives.sort((a, b) => sortValue(a) - sortValue(b))[0]
  return contextualizeSubModel(suoritusProto, osasuoritukset, newItemIndex)
}

export const osanOsa = m => m && m.value.classes.includes('ammatillisentutkinnonosanosaalue')

export const isYhteinenTutkinnonOsa = suoritus => suoritus.value.classes.includes('yhteisenammatillisentutkinnonosansuoritus')

const muutKieliaineet = ['TK1', 'VK', 'VVAI', 'VVTK', 'VVVK']
const äidinkieli = 'AI'
const kieliAineet = [äidinkieli, ...muutKieliaineet]

export const isAmmatillisenKieliaine = koodiarvo => kieliAineet.includes(koodiarvo)

export const tutkinnonOsanOsaAlueenKoulutusmoduuli = (koulutusmoduulit, oppiaine) => {
  if (!isAmmatillisenKieliaine(oppiaine.koodiarvo)) {
    return koulutusmoduulit.find(R.complement(isKieliaine))
  }
  if (oppiaine.koodiarvo === äidinkieli) {
    return koulutusmoduulit.find(isÄidinkieli)
  }
  if (muutKieliaineet.includes(oppiaine.koodiarvo)) {
    return koulutusmoduulit.find(isKieliaine)
  }
}
