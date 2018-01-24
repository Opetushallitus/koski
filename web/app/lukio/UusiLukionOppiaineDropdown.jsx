import React from 'baret'
import {t} from '../i18n/i18n'
import DropDown from '../components/Dropdown'
import {
  contextualizeSubModel, ensureArrayKey, modelData, modelItems, modelLookup, modelSet, oneOfPrototypes, pushModel,
  wrapOptional
} from '../editor/EditorModel'
import {koulutusModuuliprototypes} from '../suoritus/Koulutusmoduuli'
import {fetchAlternativesBasedOnPrototypes} from '../editor/EnumEditor'

const createOppiaineenSuoritus = model => {
  const oppiaineet = wrapOptional(modelLookup(model, 'osasuoritukset'))
  const newItemIndex = modelItems(oppiaineet).length
  const oppiaineenSuoritusProto = contextualizeSubModel(oppiaineet.arrayPrototype, oppiaineet, newItemIndex)
  const options = oneOfPrototypes(oppiaineenSuoritusProto)
  return contextualizeSubModel(options[0], oppiaineet, newItemIndex)
}

const fetchOppiaineOptions = uusiOppiaineenSuoritus => {
  const oppiaineModels = koulutusModuuliprototypes(uusiOppiaineenSuoritus)
  return fetchAlternativesBasedOnPrototypes(oppiaineModels, 'tunniste')
}

export const UusiLukionOppiaineDropdown = ({model}) => {
  if (!model || !model.context.edit) return null

  const uusiOppiaineenSuoritus = createOppiaineenSuoritus(model)
  const options = fetchOppiaineOptions(uusiOppiaineenSuoritus)
  const placeholderText = t('Lisää oppiaine')

  const addOppiaine = oppiaine => {
    const suoritusUudellaOppiaineella = modelSet(uusiOppiaineenSuoritus, oppiaine, 'koulutusmoduuli')
    pushModel(suoritusUudellaOppiaineella, model.context.changeBus)
    ensureArrayKey(suoritusUudellaOppiaineella)
  }

  return (
    <DropDown
      options={options}
      keyValue={oppiaine => modelData(oppiaine, 'tunniste').koodiarvo}
      displayValue={oppiaine => modelLookup(oppiaine, 'tunniste').value.title}
      onSelectionChanged={addOppiaine}
      selectionText={placeholderText}
      isRemovable={() => false}
    />
  )
}
