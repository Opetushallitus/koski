import React from 'baret'
import { Editor } from '../editor/Editor'
import { modelEmpty, modelItems, modelLookup } from '../editor/EditorModel'
import { fixArviointi } from './Suoritus'

export const EuropeanSchoolOfHelsinkiArvosanaEditor = ({
  model,
  notFoundText
}) => {
  if (!model.context.edit) {
    const arvosanaModel = resolveArvosanaModel(model)
    return arvosanaModel ? (
      <Editor model={arvosanaModel} />
    ) : notFoundText ? (
      <span>{notFoundText}</span>
    ) : null
  }

  model = fixArviointi(model)

  const eshArvosanaModel = modelLookup(model, 'arviointi')

  return (
    <span>
      <Editor model={eshArvosanaModel} showEmptyOption="true" />
    </span>
  )
}

// TODO: Siivoa
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
