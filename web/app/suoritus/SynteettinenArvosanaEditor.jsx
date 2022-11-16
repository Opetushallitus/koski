import React from 'baret'
import { Editor } from '../editor/Editor'
import { modelProperty, modelLookup } from '../editor/EditorModel'
import { SynteettinenKoodiviiteEditor } from '../editor/SynteettinenKoodiviiteEditor'
import { fixArviointi } from './Suoritus'

export const SynteettinenArvosanaEditor = ({ model, notFoundText }) => {
  const arvosanaModel = resolveArvosanaModel(model)
  if (!model.context.edit) {
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

  // Palautetaan ainut editori, joka on käytössä synteettiselle koodiviitteelle
  return <SynteettinenKoodiviiteEditor model={arvosanaModel} />
}

export const resolveArvosanaModel = (suoritus) => {
  // arviointi.-1 tarkoittaa listan viimeisintä arviointia
  const arviointi = modelLookup(suoritus, 'arviointi.-1')
  return arviointi ? modelLookup(arviointi, 'arvosana') : null
}
