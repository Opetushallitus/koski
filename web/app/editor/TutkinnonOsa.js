import {wrapOptional} from './EditorModel'
import {contextualizeSubModel, modelItems, oneOfPrototypes} from './EditorModel'
export const placeholderForNonGrouped = '999999'

export const createTutkinnonOsanSuoritusPrototype = (osasuoritukset, groupId) => {
  osasuoritukset = wrapOptional(osasuoritukset)
  let newItemIndex = modelItems(osasuoritukset).length
  let suoritusProto = contextualizeSubModel(osasuoritukset.arrayPrototype, osasuoritukset, newItemIndex)
  let preferredClass = groupId == '2' ? 'yhteisenammatillisentutkinnonosansuoritus' : 'muunammatillisentutkinnonosansuoritus'
  let sortValue = (oneOfProto) => oneOfProto.value.classes.includes(preferredClass) ? 0 : 1
  let alternatives = oneOfPrototypes(suoritusProto)
  suoritusProto = alternatives.sort((a, b) => sortValue(a) - sortValue(b))[0]
  return contextualizeSubModel(suoritusProto, osasuoritukset, newItemIndex)
}