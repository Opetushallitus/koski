import {
  contextualizeSubModel,
  modelItems,
  oneOfPrototypes,
  wrapOptional
} from '../editor/EditorModel'
import * as R from 'ramda'

export const createTutkinnonOsanSuoritusPrototype = (osasuoritukset, groupId) =>
  selectTutkinnonOsanSuoritusPrototype(
    tutkinnonOsaPrototypes(osasuoritukset),
    groupId
  )

export const selectTutkinnonOsanSuoritusPrototype = (
  prototypes,
  preferredClass
) => {
  const sortValue = (oneOfProto) =>
    oneOfProto.value.classes.includes(preferredClass) ? 0 : 1
  return prototypes.sort((a, b) => sortValue(a) - sortValue(b))[0]
}

export const tutkinnonOsaPrototypes = (osasuorituksetModel) => {
  const osasuoritukset = wrapOptional(osasuorituksetModel)
  const newItemIndex = modelItems(osasuoritukset).length
  const suoritusProto = contextualizeSubModel(
    osasuoritukset.arrayPrototype,
    osasuoritukset,
    newItemIndex
  )
  const alts = oneOfPrototypes(R.dissoc('onlyWhen', suoritusProto))
  return alts.map((alt) =>
    contextualizeSubModel(alt, osasuoritukset, newItemIndex)
  )
}
