import React from 'baret'
import {t} from '../i18n/i18n'
import {UusiOppiaineDropdown} from '../oppiaine/UusiOppiaineDropdown'
import {ensureArrayKey, modelData, modelItems, modelSet, modelSetTitle, pushModel} from '../editor/EditorModel'
import {createOppiaineenSuoritus} from './lukio'

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
      organisaatioOid={modelData(model, 'toimipiste.oid')}
      resultCallback={addOppiaine}
      placeholder={t('Lisää oppiaine')}
      pakollinen={true}
      />
  )
}
