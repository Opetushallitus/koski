import React from 'baret'
import {t} from '../i18n/i18n'
import {UusiOppiaineDropdown} from '../oppiaine/UusiOppiaineDropdown'
import {
  ensureArrayKey, modelData, modelItems, modelSet, modelSetData, modelSetTitle,
  pushModel
} from '../editor/EditorModel'
import {createOppiaineenSuoritus} from '../lukio/lukio'

export const UusiIBOppiaineDropdown = ({model, aineryhmä}) => {
  if (!model || !model.context.edit) return null

  const addOppiaine = oppiaine => {
    const nimi = t(modelData(oppiaine, 'tunniste.nimi'))
    const oppiaineWithTitle = modelSetTitle(oppiaine, nimi)
    const oppiaineWithAineryhmä = modelSetData(oppiaineWithTitle, aineryhmä, 'ryhmä')
    const suoritusUudellaOppiaineella = modelSet(
      oppiaine.parent || createOppiaineenSuoritus(model),
      oppiaineWithAineryhmä,
      'koulutusmoduuli'
    )
    pushModel(suoritusUudellaOppiaineella, model.context.changeBus)
    ensureArrayKey(suoritusUudellaOppiaineella)
  }

  return (
    <UusiOppiaineDropdown
      suoritukset={modelItems(model, 'osasuoritukset')}
      oppiaineenSuoritukset={[createOppiaineenSuoritus(model)]}
      organisaatioOid={modelData(model, 'toimipiste.oid')}
      resultCallback={addOppiaine}
      placeholder={t('Lisää oppiaine')}
      pakollinen={true}
    />
  )
}
