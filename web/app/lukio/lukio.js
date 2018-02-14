import {wrapOptional, modelLookup, modelItems, contextualizeSubModel, oneOfPrototypes} from '../editor/EditorModel'

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

const createOppiaineenSuoritus = (model, suoritusClass) => {
  const oppiaineet = wrapOptional(modelLookup(model, 'osasuoritukset'))
  const newItemIndex = modelItems(oppiaineet).length
  const oppiaineenSuoritusProto = contextualizeSubModel(oppiaineet.arrayPrototype, oppiaineet, newItemIndex)
  const options = oneOfPrototypes(oppiaineenSuoritusProto)
  const proto = suoritusClass && options.find(p => p.value.classes.includes(suoritusClass)) || options[0]
  return contextualizeSubModel(proto, oppiaineet, newItemIndex)
}

export {
  perusteenDiaarinumeroToOppimäärä,
  createOppiaineenSuoritus
}
