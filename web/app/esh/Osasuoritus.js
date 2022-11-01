import {
  contextualizeSubModel,
  modelItems,
  oneOfPrototypes,
  wrapOptional
} from '../editor/EditorModel'
import dissoc from 'ramda/src/dissoc'

export const createOsasuoritusPrototype = (osasuoritukset, groupId) =>
  selectOsasuoritusPrototype(osasuoritusPrototypes(osasuoritukset), groupId)

export const selectOsasuoritusPrototype = (prototypes, preferredClass) => {
  const sortValue = (oneOfProto) =>
    oneOfProto.value.classes.includes(preferredClass) ? 0 : 1
  return prototypes.sort((a, b) => sortValue(a) - sortValue(b))[0]
}

export const osasuoritusPrototypes = (osasuorituksetModel) => {
  const osasuoritukset = wrapOptional(osasuorituksetModel)
  const newItemIndex = modelItems(osasuoritukset).length
  const suoritusProto = contextualizeSubModel(
    osasuoritukset.arrayPrototype,
    osasuoritukset,
    newItemIndex
  )
  const alts = oneOfPrototypes(dissoc('onlyWhen', suoritusProto))
  return alts.map((alt) =>
    contextualizeSubModel(alt, osasuoritukset, newItemIndex)
  )
}

export const osasuorituksenKoulutusmoduuli = (koulutusmoduulit, _oppiaine) => {
  console.log(koulutusmoduulit)
  return koulutusmoduulit[0]
}
