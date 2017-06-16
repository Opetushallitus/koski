import React from 'react'
import R from 'ramda'
import {wrapOptional} from './OptionalEditor.jsx'
import {StringEditor} from './StringEditor.jsx'
import {modelLookup} from './EditorModel.js'
import {lang} from '../i18n'
import {lensedModel, modelData, modelSetValue, oneOfPrototypes} from './EditorModel'
import * as L from 'partial.lenses'

export const LocalizedStringEditor = ({model, ...rest}) => {
  let wrappedModel = wrapOptional({model})

  let languagePriority = [lang, 'fi', 'sv', 'en']
  let localizedData = modelData(wrappedModel)
  let usedLanguage = languagePriority.find(l => localizedData[l]) || lang

  let localizedStringLens = L.lens(
    (m) => modelLookup(m, usedLanguage),
    (v, m) => {
      let protoForUsedLanguage = oneOfPrototypes(m).find(proto => proto.value.properties[0].key == usedLanguage)
      return modelSetValue(protoForUsedLanguage, v.value, usedLanguage)
    }
  )

  let stringModel = R.merge(lensedModel(wrappedModel, localizedStringLens), { optional: model.optional, maxLines: wrappedModel.maxLines })

  return <span className={'localized-string ' + usedLanguage}><StringEditor {...{model: stringModel, ...rest}} /></span>
}
LocalizedStringEditor.handlesOptional = () => true
LocalizedStringEditor.canShowInline = () => true
