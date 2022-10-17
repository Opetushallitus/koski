import React from 'baret'
import { t } from '../i18n/i18n'
import { UusiOppiaineDropdown } from './UusiOppiaineDropdown'
import {
  ensureArrayKey,
  modelData,
  modelItems,
  modelLookup,
  modelSet,
  modelSetData,
  modelSetTitle,
  pushModel
} from '../editor/EditorModel'
import { newOsasuoritusProto, suorituksenTyyppi } from '../suoritus/Suoritus'

const resolveRyhmäFieldName = (model) =>
  ['diavalmistavavaihe', 'diatutkintovaihe'].includes(suorituksenTyyppi(model))
    ? 'osaAlue'
    : 'ryhmä'

export const UusiRyhmiteltyOppiaineDropdown = ({
  model,
  aineryhmä,
  optionsFilter
}) => {
  if (!model || !model.context.edit) return null

  const addOppiaine = (oppiaine) => {
    const nimi = t(modelData(oppiaine, 'tunniste.nimi'))
    const ryhmäFieldName = resolveRyhmäFieldName(model)
    const oppiaineWithTitle = modelSetTitle(oppiaine, nimi)
    const oppiaineWithAineryhmä = modelLookup(oppiaineWithTitle, ryhmäFieldName)
      ? modelSetData(oppiaineWithTitle, aineryhmä, ryhmäFieldName)
      : oppiaineWithTitle
    const suoritusUudellaOppiaineella = modelSet(
      oppiaine.parent || newOsasuoritusProto(model),
      oppiaineWithAineryhmä,
      'koulutusmoduuli'
    )
    pushModel(suoritusUudellaOppiaineella, model.context.changeBus)
    ensureArrayKey(suoritusUudellaOppiaineella)
  }

  return (
    <UusiOppiaineDropdown
      suoritukset={modelItems(model, 'osasuoritukset')}
      oppiaineenSuoritukset={[newOsasuoritusProto(model)]}
      organisaatioOid={modelData(model, 'toimipiste.oid')}
      resultCallback={addOppiaine}
      placeholder={t('Lisää oppiaine')}
      pakollinen={true}
      optionsFilter={optionsFilter}
      allowSelectingDuplicates={suorituksenTyyppi(model) === 'ibtutkinto'}
    />
  )
}
