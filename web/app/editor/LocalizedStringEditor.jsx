import React from 'react'
import R from 'ramda'
import {wrapOptional} from './OptionalEditor.jsx'
import {ObjectEditor} from './ObjectEditor.jsx'
import {StringEditor} from './StringEditor.jsx'
import {modelLookup} from './EditorModel.js'
import {lang} from '../i18n'

export const LocalizedStringEditor = ({model, ...rest}) => {
  if (!model.context.edit) {
    return <ObjectEditor model={model}/>
  }
  let wrappedModel = wrapOptional({model})
  let stringModel = R.merge(modelLookup(wrappedModel, lang), { optional: model.optional })

  return <StringEditor {...{model: stringModel, ...rest}} />
}
LocalizedStringEditor.handlesOptional = () => true
LocalizedStringEditor.canShowInline = () => true
