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
  oneOfPrototypes
} from '../editor/EditorModel'
import * as L from 'partial.lenses'
import { sortGrades } from '../util/sorting'
import { fetchAlternativesBasedOnPrototypes } from '../editor/EnumEditor'
import { fixArviointi } from './Suoritus'
import { last, sort } from 'fp-ts/lib/Array'
import { contramap, fromCompare } from 'fp-ts/lib/Ord'
import { Ord as StringOrd } from 'fp-ts/lib/string'

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

const parasArviointi = (suoritus, arviointiField = 'arviointi') => {
  const arvioinnit = sort(ArviointiDateOrd)(
    modelItems(suoritus, arviointiField)
  )
  let arviointi = arvioinnit[arvioinnit.length - 1]
  let arvosana = arviointi
    ? modelLookup(suoritus, `${arviointiField}.-1.arvosana`)
    : null

  arvioinnit.map((item) => {
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

const ArviointiDateOrd = contramap(
  (a) => modelData(a, 'päivä') || '0000-00-00'
)(StringOrd)
