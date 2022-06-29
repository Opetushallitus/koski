import React from 'react'
import * as R from 'ramda'
import {wrapOptional} from './EditorModel'
import {StringEditor} from './StringEditor'
import {modelLookup} from './EditorModel.ts'
import {lang} from '../i18n/i18n'
import {lensedModel, modelData, modelSetValue, oneOfPrototypes} from './EditorModel'
import * as L from 'partial.lenses'

export const LocalizedStringEditor = ({model, ...rest}) => {
  let wrappedModel = wrapOptional(model)

  let usedLanguage = getUsedLanguage(wrappedModel)

  let stringModel = R.mergeRight(lensedModel(wrappedModel, localizedStringLens(wrappedModel)), { optional: model.optional, maxLines: wrappedModel.maxLines })

  return <span className={'localized-string ' + usedLanguage}><StringEditor {...{model: stringModel, ...rest}} /></span>
}

let languagePriority = [lang, 'fi', 'sv', 'en']

let getUsedLanguage = (m) => {
  let localizedData = modelData(m)
  return languagePriority.find(l => localizedData[l]) || lang
}

let localizedStringLens = (model) => {
  let localizedData = modelData(model)
  let usedLanguage = languagePriority.find(l => localizedData[l]) || lang
  return L.lens(
    (m) => modelLookup(m, usedLanguage),
    (v, m) => {
      let protoForUsedLanguage = oneOfPrototypes(m).find(proto => proto.value.properties[0].key == usedLanguage)
      let modelToPush = modelSetValue(protoForUsedLanguage, v.value, usedLanguage)
      return modelToPush
    }
  )
}

LocalizedStringEditor.handlesOptional = () => true
LocalizedStringEditor.isEmpty = (m) => StringEditor.isEmpty(lensedModel(m, localizedStringLens(m)))
LocalizedStringEditor.canShowInline = () => true
