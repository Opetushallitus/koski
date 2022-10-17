import React from 'react'
import * as R from 'ramda'
import {
  wrapOptional,
  lensedModel,
  modelData,
  modelSetValue,
  oneOfPrototypes
} from './EditorModel'
import { StringEditor } from './StringEditor'
import { modelLookup } from './EditorModel.ts'
import { lang } from '../i18n/i18n'
import * as L from 'partial.lenses'

export const LocalizedStringEditor = ({ model, ...rest }) => {
  const wrappedModel = wrapOptional(model)

  const usedLanguage = getUsedLanguage(wrappedModel)

  const stringModel = R.mergeRight(
    lensedModel(wrappedModel, localizedStringLens(wrappedModel)),
    { optional: model.optional, maxLines: wrappedModel.maxLines }
  )

  return (
    <span className={'localized-string ' + usedLanguage}>
      <StringEditor {...{ model: stringModel, ...rest }} />
    </span>
  )
}

const languagePriority = [lang, 'fi', 'sv', 'en']

const getUsedLanguage = (m) => {
  const localizedData = modelData(m)
  return languagePriority.find((l) => localizedData[l]) || lang
}

const localizedStringLens = (model) => {
  const localizedData = modelData(model)
  const usedLanguage = languagePriority.find((l) => localizedData[l]) || lang
  return L.lens(
    (m) => modelLookup(m, usedLanguage),
    (v, m) => {
      const protoForUsedLanguage = oneOfPrototypes(m).find(
        (proto) => proto.value.properties[0].key == usedLanguage
      )
      const modelToPush = modelSetValue(
        protoForUsedLanguage,
        v.value,
        usedLanguage
      )
      return modelToPush
    }
  )
}

LocalizedStringEditor.handlesOptional = () => true
LocalizedStringEditor.isEmpty = (m) =>
  StringEditor.isEmpty(lensedModel(m, localizedStringLens(m)))
LocalizedStringEditor.canShowInline = () => true
