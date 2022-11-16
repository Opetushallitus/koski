import React from 'baret'
import Bacon from 'baconjs'
import {
  addContext,
  modelData,
  modelItems,
  modelLookup,
  modelSetValue,
  pushModel
} from '../editor/EditorModel'
import Text from '../i18n/Text'
import { isToimintaAlueittain, jääLuokalle, valmiitaSuorituksia } from './esh'
import { EuropeanSchoolOfHelsinkiSuoritustaulukko } from './EuropeanSchoolOfHelsinkiSuoritustaulukko'
import { equals, dissoc } from 'ramda'

export const EuropeanSchoolOfHelsinkiOsasuorituksetEditor = ({ model }) => {
  model = addContext(model, { suoritus: model })
  const osasuoritukset = modelItems(model, 'osasuoritukset')
  const osasuorituksetModel = modelLookup(model, 'osasuoritukset')

  if (model.context.edit) {
    if (!valmiitaSuorituksia(osasuoritukset)) {
      prefillOsasuorituksetIfNeeded(model, osasuoritukset)
    } else if (!jääLuokalle(model)) {
      // emptyOsasuoritukset(model)
    }
  }

  return (
    <div className="oppiaineet osasuoritukset">
      <div>
        <h5>
          <Text
            name={
              (isToimintaAlueittain(model)
                ? 'Toiminta-alueiden'
                : 'Oppiaineiden') + ' arvosanat'
            }
          />
        </h5>
        <p>{/* <Text name="(ESH arvosteluteksti TODO)" /> */}</p>
        {osasuorituksetModel && (
          <EuropeanSchoolOfHelsinkiSuoritustaulukko
            parentSuoritus={model}
            suorituksetModel={osasuorituksetModel}
          />
        )}
      </div>
    </div>
  )
}

const prefillOsasuorituksetIfNeeded = (model, currentSuoritukset) => {
  const wrongOsasuorituksetTemplateP = fetchOsasuorituksetTemplate(model, false)
  const hasWrongPrefillP = wrongOsasuorituksetTemplateP.map(
    (wrongOsasuorituksetTemplate) =>
      // esitäyttödatan tyyppi ei sisällä nimi ja versiotietoja, poistetaan tyyppi koska se ei ole relevanttia vertailussa
      currentSuoritukset.length > 0 &&
      equals(
        wrongOsasuorituksetTemplate.value.map(modelDataIlmanTyyppiä),
        currentSuoritukset.map(modelDataIlmanTyyppiä)
      )
  )
  const changeTemplateP = hasWrongPrefillP.or(
    Bacon.constant(jääLuokalle(model))
  )
  fetchOsasuorituksetTemplate(model, false)
    .filter(changeTemplateP)
    .onValue((osasuorituksetTemplate) =>
      pushModel(
        modelSetValue(model, osasuorituksetTemplate.value, 'osasuoritukset')
      )
    )
}

const fetchOsasuorituksetTemplate = (_model, _toimintaAlueittain) =>
  Bacon.constant({ value: [] })

const modelDataIlmanTyyppiä = (suoritus) =>
  dissoc('tyyppi', modelData(suoritus))
