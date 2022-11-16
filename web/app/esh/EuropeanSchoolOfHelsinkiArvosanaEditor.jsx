import React from 'baret'
import { Editor } from '../editor/Editor'
import { modelLookup } from '../editor/EditorModel'
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

  const eshArvosanaModel = modelLookup(
    modelLookup(model, 'arviointi'),
    '-1.arvosana'
  )
  const eshArvosanaOsasuoritukset = modelLookup(model, 'osasuoritukset')

  if (
    eshArvosanaModel === undefined &&
    eshArvosanaOsasuoritukset !== undefined
  ) {
    return null
  }

  return <Editor model={eshArvosanaModel} showEmptyOption="true" />
}

export const resolveArvosanaModel = (model) =>
  modelLookup(model, 'arviointi.-1.arvosana')
