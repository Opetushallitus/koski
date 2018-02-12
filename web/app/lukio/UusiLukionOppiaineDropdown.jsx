import React from 'baret'
import {t} from '../i18n/i18n'
import {UusiOppiaineDropdown} from '../oppiaine/UusiOppiaineDropdown'
import {
  contextualizeSubModel, ensureArrayKey, modelData, modelItems, modelLookup, modelSet, modelSetTitle, oneOfPrototypes,
  pushModel,
  wrapOptional
} from '../editor/EditorModel'

const createOppiaineenSuoritus = (model, suoritusClass) => {
  const oppiaineet = wrapOptional(modelLookup(model, 'osasuoritukset'))
  const newItemIndex = modelItems(oppiaineet).length
  const oppiaineenSuoritusProto = contextualizeSubModel(oppiaineet.arrayPrototype, oppiaineet, newItemIndex)
  const options = oneOfPrototypes(oppiaineenSuoritusProto)
  const proto = suoritusClass && options.find(p => p.value.classes.includes(suoritusClass)) || options[0]
  return contextualizeSubModel(proto, oppiaineet, newItemIndex)
}

export const UusiLukionOppiaineDropdown = ({model, oppiaineenSuoritusClass}) => {
  if (!model || !model.context.edit) return null

  const uusiOppiaineenSuoritus = createOppiaineenSuoritus(model, oppiaineenSuoritusClass)

  const addOppiaine = oppiaine => {
    const nimi = t(modelData(oppiaine, 'tunniste.nimi'))
    const oppiaineWithTitle = modelSetTitle(oppiaine, nimi)
    const suoritusUudellaOppiaineella = modelSet(uusiOppiaineenSuoritus, oppiaineWithTitle, 'koulutusmoduuli')
    pushModel(suoritusUudellaOppiaineella, model.context.changeBus)
    ensureArrayKey(suoritusUudellaOppiaineella)
  }

  return (
    <UusiOppiaineDropdown
      suoritukset={modelItems(model, 'osasuoritukset')}
      oppiaineenSuoritus={uusiOppiaineenSuoritus}
      resultCallback={addOppiaine}
      placeholder={t('Lisää oppiaine')}
      pakollinen={true}
      />
  )
}
