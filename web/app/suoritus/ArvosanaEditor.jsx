import React from 'baret'
import * as L from 'partial.lenses'
import { Editor } from '../editor/Editor'
import {
  lensedModel,
  modelData,
  modelEmpty,
  modelLookup,
  modelProperty,
  modelSetValue,
  oneOfPrototypes,
  wrapOptional
} from '../editor/EditorModel'
import { fetchAlternativesBasedOnPrototypes } from '../editor/EnumEditor'
import { parasArviointi } from '../util/arviointi'
import { sortGrades } from '../util/sorting'
import { fixArviointi } from './Suoritus'

export const ArvosanaEditor = ({
  model,
  notFoundText,
  arviointiField,
  ...rest
}) => {
  arviointiField = arviointiField || 'arviointi'

  if (!model.context.edit) {
    const arvosanaModel = resolveArvosanaModel(model, arviointiField)
    return arvosanaModel ? (
      <Editor model={arvosanaModel} />
    ) : notFoundText ? (
      <span>{notFoundText}</span>
    ) : null
  }

  if (!modelProperty(model, arviointiField)) {
    return null
  }

  model = fixArviointi(model)
  const alternativesP = fetchAlternativesBasedOnPrototypes(
    oneOfPrototypes(wrapOptional(modelLookup(model, `${arviointiField}.-1`))),
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
        const arviointiModel = modelLookup(model, arviointiField)
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

export const resolveArvosanaModel = (
  suoritus,
  arviointiField = 'arviointi'
) => {
  const arviointi = parasArviointi(suoritus, arviointiField)
  const arvosana = arviointi ? modelLookup(arviointi, 'arvosana') : null

  const isPaikallinenArviointi =
    arviointi &&
    !modelEmpty(arviointi) &&
    arviointi.value.classes.includes('paikallinenarviointi')

  return isPaikallinenArviointi ? modelLookup(arvosana, 'nimi') : arvosana
}
