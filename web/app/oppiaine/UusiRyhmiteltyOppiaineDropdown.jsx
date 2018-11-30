import React from 'baret'
import {t} from '../i18n/i18n'
import {UusiOppiaineDropdown} from './UusiOppiaineDropdown'
import {
  ensureArrayKey, modelData, modelItems, modelLookup, modelSet, modelSetData, modelSetTitle,
  pushModel
} from '../editor/EditorModel'
import {createOppiaineenSuoritus} from '../lukio/lukio'

const resolveRyhmäFieldName = model => {
  const tyyppi = modelData(model, 'tyyppi').koodiarvo
  return ['diavalmistavavaihe', 'diatutkintovaihe'].includes(tyyppi) ? 'osaAlue' : 'ryhmä'
}

export const UusiRyhmiteltyOppiaineDropdown = ({model, aineryhmä}) => {
  if (!model || !model.context.edit) return null

  const addOppiaine = oppiaine => {
    const nimi = t(modelData(oppiaine, 'tunniste.nimi'))
    const ryhmäFieldName = resolveRyhmäFieldName(model)
    const oppiaineWithTitle = modelSetTitle(oppiaine, nimi)
    const oppiaineWithAineryhmä = modelLookup(oppiaineWithTitle, ryhmäFieldName) ? modelSetData(oppiaineWithTitle, aineryhmä, ryhmäFieldName) : oppiaineWithTitle
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
