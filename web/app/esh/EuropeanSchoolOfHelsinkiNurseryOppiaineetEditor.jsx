import React from 'baret'
import Bacon from 'baconjs'
import {
  addContext,
  modelData,
  modelItems,
  modelSetValue,
  pushModel
} from '../editor/EditorModel'
import { equals, dissoc } from 'ramda'
import Text from '../i18n/Text'
import { isToimintaAlueittain, jääLuokalle, valmiitaSuorituksia } from './esh'

export const EuropeanSchoolOfHelsinkiNurseryOppiaineetEditor = ({ model }) => {
  model = addContext(model, { suoritus: model })
  const oppiaineSuoritukset = modelItems(model, 'osasuoritukset')

  if (model.context.edit) {
    if (!valmiitaSuorituksia(oppiaineSuoritukset)) {
      prefillOsasuorituksetIfNeeded(model, oppiaineSuoritukset)
    } else if (!jääLuokalle(model)) {
      emptyOsasuoritukset(model)
    }
  }

  return (
    <div className="oppiaineet">
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
        <p>
          <Text name="(ESH arvosteluteksti TODO)" />
        </p>
        {'Nursery TODO'}
      </div>
    </div>
  )
}

const prefillOsasuorituksetIfNeeded = (model, currentSuoritukset) => {
  const wrongOsasuorituksetTemplateP = fetchOsasuorituksetTemplate(
    model,
    !isToimintaAlueittain(model)
  )
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
  fetchOsasuorituksetTemplate(model, isToimintaAlueittain(model))
    .filter(changeTemplateP)
    .onValue((osasuorituksetTemplate) =>
      pushModel(
        modelSetValue(model, osasuorituksetTemplate.value, 'osasuoritukset')
      )
    )
}

const emptyOsasuoritukset = (model) =>
  pushModel(modelSetValue(model, [], 'osasuoritukset'))

// TODO: TOR-1685 Osasuoritusten template
const fetchOsasuorituksetTemplate = (_model, _toimintaAlueittain) =>
  Bacon.constant({ value: [] })

const modelDataIlmanTyyppiä = (suoritus) =>
  dissoc('tyyppi', modelData(suoritus))

// TODO: TOR-1685
// eslint-disable-next-line no-unused-vars
const resolveSynteettinenArvosanaEditor = () => {}
