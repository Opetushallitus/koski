import React from 'baret'
import { Editor } from '../editor/Editor'
import {
  wrapOptional,
  modelEmpty,
  modelProperty,
  modelItems,
  lensedModel,
  modelData,
  modelLookup,
  modelSetValue,
  contextualizeSubModel,
  oneOfPrototypes,
  resolveActualModel
} from '../editor/EditorModel'
import * as L from 'partial.lenses'
import { sortGrades } from '../util/sorting'
import { fetchAlternativesBasedOnPrototypes } from '../editor/EnumEditor'
import { fixArviointi } from './Suoritus'
import { isOneOfModel } from '../types/EditorModels'

export const EshArvosanaEditor = ({ model, notFoundText, ...rest }) => {
  if (!model.context.edit) {
    const arvosanaModel = resolveArvosanaModel(model)
    return arvosanaModel ? (
      <Editor model={arvosanaModel} />
    ) : notFoundText ? (
      <span>{notFoundText}</span>
    ) : null
  }

  if (!modelProperty(model, 'arviointi')) {
    return null
  }

  model = fixArviointi(model)
  const arviointi = wrapOptional(modelLookup(model, 'arviointi'))
  const uusiArviointiModel = contextualizeSubModel(
    arviointi.arrayPrototype,
    arviointi,
    modelItems(arviointi).length
  )
  const protoArviointi = isOneOfModel(uusiArviointiModel)
    ? resolveActualModel(uusiArviointiModel, uusiArviointiModel.parent)
    : uusiArviointiModel
  const alternativesP = fetchAlternativesBasedOnPrototypes(
    oneOfPrototypes(protoArviointi),
    'arvosana'
  ).startWith([])

  const arvosanatP = alternativesP.map((alternatives) =>
    alternatives.map((m) => modelLookup(m, 'arvosana').value)
  )

  return (
    <span>
      {alternativesP.map((alternatives) => {
        const arvosanaLens = L.lens(
          (m) => {
            return modelLookup(m, '-1.arvosana')
          },
          (v, m) => {
            const valittu = modelData(v)
            if (valittu) {
              // Arvosana valittu -> valitaan vastaava prototyyppi (eri prototyypit eri arvosanoille)
              const found = alternatives.find((alt) => {
                const altData = modelData(alt, 'arvosana')
                return (
                  altData.koodiarvo === valittu.koodiarvo &&
                  altData.koodistoUri === valittu.koodistoUri
                )
              })
              return modelSetValue(m, found.value, '-1')
            } else {
              // Ei arvosanaa -> poistetaan arviointi kokonaan
              return modelSetValue(m, undefined)
            }
          }
        )
        const arviointiModel = modelLookup(model, 'arviointi')
        const arvosanaModel = lensedModel(arviointiModel, arvosanaLens)
        // Use key to ensure re-render when alternatives are supplied
        return (
          <Editor
            key={alternatives.length}
            model={arvosanaModel}
            sortBy={sortGrades}
            fetchAlternatives={() => arvosanatP}
            showEmptyOption="true"
            {...rest}
          />
        )
      })}
    </span>
  )
}

export const resolveArvosanaModel = (suoritus) => {
  const arviointi = parasArviointi(suoritus)
  const arvosana = arviointi ? modelLookup(arviointi, 'arvosana') : null

  const isPaikallinenArviointi =
    arviointi &&
    !modelEmpty(arviointi) &&
    arviointi.value.classes.includes('paikallinenarviointi')

  return isPaikallinenArviointi ? modelLookup(arvosana, 'nimi') : arvosana
}

const parasArviointi = (suoritus) => {
  let arviointi = modelLookup(suoritus, 'arviointi.-1')
  let arvosana = arviointi
    ? modelLookup(suoritus, 'arviointi.-1.arvosana')
    : null

  modelItems(suoritus, 'arviointi').map((item) => {
    const nthArvosana = modelLookup(item, 'arvosana')
    if (
      nthArvosana.value.data !== undefined &&
      (nthArvosana.value.data.koodiarvo === 'S' ||
        parseInt(nthArvosana.value.data.koodiarvo) >
          parseInt(arvosana.value.data.koodiarvo))
    ) {
      arviointi = item
      arvosana = nthArvosana
    }
  })

  return arviointi
}
