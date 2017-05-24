import React from 'baret'
import R from 'ramda'
import Bacon from 'baconjs'
import {modelTitle} from './EditorModel.js'
import {wrapOptional} from './OptionalEditor.jsx'
import Http from '../http'
import DropDown from '../Dropdown.jsx'
import {modelSetValue, pushModel, modelValid} from './EditorModel'
import {t} from '../i18n.js'

export const EnumEditor = ({model, asRadiogroup, disabledValue, sortBy, fetchAlternatives = EnumEditor.fetchAlternatives }) => {
  if (!sortBy) sortBy = R.identity
  let wrappedModel = wrapOptional({
    model,
    createEmpty: (protomodel) => modelSetValue(protomodel, zeroValue)
  })

  let alternativesP = fetchAlternatives(wrappedModel, sortBy).map(sortBy)
  let valid = modelValid(model)
  let classNameP = alternativesP.startWith([]).map(xs => (xs.length ? '' : 'loading') + (valid ? '' : ' error'))

  let alternativesWithZeroValueP = alternativesP.map(xs => wrappedModel.optional ? R.prepend(zeroValue, xs) : xs)

  let defaultValue = wrappedModel.value || zeroValue

  let onChange = (option) => {
    pushModel(modelSetValue(wrappedModel, option))
  }

  let labelClass = alternative => {
    return 'alternative'
        + (disabledValue === alternative.value ? ' disabled' : '')
        + (wrappedModel.value && wrappedModel.value.value === alternative.value ? ' checked' : '')
  }

  return wrappedModel.context.edit
    ? asRadiogroup
      ? (
          <ul className={classNameP}>
            {
              alternativesP.map(alternatives =>
                alternatives.map(alternative =>
                  <li key={ alternative.value }>
                    <label className={labelClass(alternative)}>
                      <input disabled={disabledValue === alternative.value} type="radio" name="alternative" value={ alternative.value } onChange={() => onChange(alternative)}/>
                      {alternative.title}
                    </label>
                  </li>
                )
              )
            }
          </ul>
        )
      : (
           <span className={classNameP.map(n => 'dropdown-wrapper ' + n)}>
             <DropDown
               options={alternativesWithZeroValueP}
               keyValue={option => option.value}
               displayValue={option => option.title}
               onSelectionChanged={option => onChange(option)}
               selected={defaultValue}
               enableFilter={true}
             />
           </span>
        )
    : <span className="inline enum">{modelTitle(model)}</span>
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

EnumEditor.knownAlternatives = (model) => model.alternativesPath && (model.alternativesPath.split('/')[6] || '').split(',').filter(R.identity)

EnumEditor.canShowInline = () => true
EnumEditor.handlesOptional = true
EnumEditor.validateModel = (model) => {
  if (!model.value && !model.optional) {
    return [{key: 'missing'}]
  }
}