import React from 'baret'
import * as R from 'ramda'
import Bacon from 'baconjs'
import {modelTitle} from './EditorModel.js'
import {modelLookup, wrapOptional} from './EditorModel'
import Http from '../util/http'
import DropDown from '../components/Dropdown'
import {modelSetValue, pushModel, modelValid} from './EditorModel'
import {t} from '../i18n/i18n.js'
import {parseBool} from '../util/util'
import {buildClassNames} from '../components/classnames'
import {hyphenate} from '../util/hyphenate'

export const EnumEditor = ({model, inline, asRadiogroup, disabledValue, sortBy, fetchAlternatives = EnumEditor.fetchAlternatives, displayValue = option => option.title, showEmptyOption, className}) => {
  if (!sortBy) sortBy = R.identity
  let wrappedModel = wrapOptional(model)
  showEmptyOption = parseBool(showEmptyOption, wrappedModel.optional)
  inline = parseBool(inline)

  let alternativesP = fetchAlternatives(wrappedModel, sortBy).map(sortBy)
  let valid = modelValid(model)
  let classNameP = alternativesP.startWith('loading').map(xs => buildClassNames([className, xs === 'loading' && 'loading', !valid && 'error']))

  let alternativesWithZeroValueP = alternativesP.map(xs => showEmptyOption ? R.prepend(zeroValue, xs) : xs)

  let defaultValue = wrappedModel.value || zeroValue

  let onChange = (option) => {
    pushModel(modelSetValue(wrappedModel, option))
  }

  let labelClass = alternative => {
    return 'alternative'
        + (disabledValue === alternative.value ? ' disabled' : '')
        + (wrappedModel.value && wrappedModel.value.value === alternative.value ? ' checked' : '')
  }

  // note: never used when editing
  const useHyphenate = (model.path.length > 0) && (model.path[model.path.length-1] === 'arvosana')

  return wrappedModel.context.edit
    ? asRadiogroup
      ? (
          <ul className={classNameP}>
            {
              alternativesP.map(alternatives =>
                alternatives.map(alternative =>
                  (<li key={ alternative.value }>
                    <label className={labelClass(alternative)}>
                      <input disabled={disabledValue === alternative.value} type="radio" name="alternative" value={ alternative.value } onChange={() => onChange(alternative)}/>
                      {alternative.title}
                    </label>
                  </li>)
                )
              )
            }
          </ul>
        )
      : (
           <span className={classNameP.map(n => 'dropdown-wrapper ' + n)}>
             <DropDown
               inline={inline}
               options={alternativesWithZeroValueP}
               keyValue={ option => option.value }
               displayValue={displayValue}
               onSelectionChanged={option => onChange(option)}
               selected={defaultValue}
               enableFilter={true}
             />
           </span>
        )
    : <span className="inline enum">{useHyphenate ? hyphenate(modelTitle(model)) : modelTitle(model)}</span>
}

let zeroValue = {title: t('Ei valintaa'), value: 'eivalintaa'}

EnumEditor.fetchAlternatives = (model) => {
  let alternativesPath = model.alternativesPath
  let edit = model.context.edit
  if (edit && alternativesPath) {
    return Http.cachedGet(alternativesPath)
  } else {
    return Bacon.constant([])
  }
}

// When selecting between more than 1 prototype, each of which has an enum field with the same same path,
// this fetches the alternatives for all the prototypes, returning an observable list of prototypes populated with
// each enum value.
export const fetchAlternativesBasedOnPrototypes = (models, pathToFieldWithAlternatives) => {
  const alternativesForField = (model) => EnumEditor.fetchAlternatives(modelLookup(model, pathToFieldWithAlternatives))
    .map(alternatives => alternatives.map(enumValue => modelSetValue(model, enumValue, pathToFieldWithAlternatives)))
  return Bacon.combineAsArray(models.map(alternativesForField)).last().map(R.unnest)
}

EnumEditor.knownAlternatives = (model) => model.alternativesPath && (model.alternativesPath.split('/')[6] || '').split(',').filter(R.identity)

EnumEditor.canShowInline = () => true
EnumEditor.handlesOptional = () => true
EnumEditor.createEmpty = (protomodel) => modelSetValue(protomodel, zeroValue)
EnumEditor.validateModel = (model) => {
  if (!model.value && !model.optional) {
    return [{key: 'missing'}]
  }
}
