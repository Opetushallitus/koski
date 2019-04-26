import React from 'baret'
import {ensureArrayKey, modelData, modelItems, modelSet, modelSetTitle, pushModel} from '../editor/EditorModel'
import {t} from '../i18n/i18n'
import {createOppiaineenSuoritus} from '../lukio/lukio'
import {UusiOppiaineDropdown} from '../oppiaine/UusiOppiaineDropdown'
import {suorituksenTyyppi} from '../suoritus/Suoritus'

export const UusiInternationalSchoolOppiaineDropdown = ({model}) => {
  if (!model || !model.context.edit) return null

  const addOppiaine = oppiaine => {
    const suoritusUudellaOppiaineella = modelSet(
      oppiaine.parent || createOppiaineenSuoritus(model),
      modelSetTitle(oppiaine, t(modelData(oppiaine, 'tunniste.nimi'))),
      'koulutusmoduuli'
    )
    pushModel(suoritusUudellaOppiaineella, model.context.changeBus)
    ensureArrayKey(suoritusUudellaOppiaineella)
  }

  const suoritusProtos = suorituksenTyyppi(model) === 'internationalschooldiplomavuosiluokka'
    ? ['diplomatoksuoritus', 'diplomaoppiaineensuoritus'].map(c => createOppiaineenSuoritus(model, c))
    : [createOppiaineenSuoritus(model)]

  return (
    <UusiOppiaineDropdown
      suoritukset={modelItems(model, 'osasuoritukset')}
      oppiaineenSuoritukset={suoritusProtos}
      organisaatioOid={modelData(model, 'toimipiste.oid')}
      resultCallback={addOppiaine}
      placeholder={t('Lisää oppiaine')}
      pakollinen={true}
    />
  )
}
