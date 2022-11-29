import React from 'baret'
import * as R from 'ramda'
import Bacon from 'baconjs'
import {
  modelData,
  modelTitle,
  modelLookup,
  wrapOptional,
  modelSetValue,
  pushModel,
  modelValid
} from './EditorModel'
import Http from '../util/http'
import DropDown from '../components/Dropdown'
import { t } from '../i18n/i18n.js'
import { parseBool } from '../util/util'
import { buildClassNames } from '../components/classnames'
import { hyphenate } from '../util/hyphenate'
import { sortGrades } from '../util/sorting'

export const EnumEditor = ({
  model,
  inline,
  asRadiogroup,
  disabledValue,
  sortBy,
  fetchAlternatives = EnumEditor.fetchAlternatives,
  displayValue = (option) => option.title,
  titleFormatter = undefined,
  asHyperlink = undefined,
  showEmptyOption,
  className
}) => {
  if (!sortBy) {
    sortBy =
      model.alternativesPath && model.alternativesPath.includes('arviointi')
        ? sortGrades
        : R.identity
  }
  const wrappedModel = wrapOptional(model)
  showEmptyOption = parseBool(showEmptyOption, wrappedModel.optional)
  inline = parseBool(inline)

  const alternativesP = fetchAlternatives(wrappedModel, sortBy).map(sortBy)
  const valid = modelValid(model)
  const classNameP = alternativesP
    .startWith('loading')
    .map((xs) =>
      buildClassNames([
        className,
        xs === 'loading' && 'loading',
        !valid && 'error'
      ])
    )

  const alternativesWithZeroValueP = alternativesP.map((xs) =>
    showEmptyOption ? R.prepend(zeroValue, xs) : xs
  )

  const defaultValue = wrappedModel.value || zeroValue

  const onChange = (option) => {
    pushModel(modelSetValue(wrappedModel, option))
  }

  const labelClass = (alternative) => {
    return (
      'alternative' +
      (disabledValue === alternative.value ? ' disabled' : '') +
      (wrappedModel.value && wrappedModel.value.value === alternative.value
        ? ' checked'
        : '')
    )
  }

  // note: never used when editing
  const useHyphenate =
    model.path.length > 0 && model.path[model.path.length - 1] === 'arvosana'
  const koodistoUri = wrappedModel?.value?.data?.koodistoUri
  const hasModelData = modelData(model) !== undefined

  return wrappedModel.context.edit ? (
    asRadiogroup ? (
      <ul className={classNameP}>
        {alternativesP.map((alternatives) =>
          alternatives.map((alternative) => (
            <li key={alternative.value}>
              <label className={labelClass(alternative)}>
                <input
                  disabled={disabledValue === alternative.value}
                  type="radio"
                  name="alternative"
                  value={alternative.value}
                  onChange={() => onChange(alternative)}
                />
                {alternative.title}
              </label>
            </li>
          ))
        )}
      </ul>
    ) : (
      <span className={classNameP.map((n) => 'dropdown-wrapper ' + n)}>
        <DropDown
          inline={inline}
          options={alternativesWithZeroValueP}
          keyValue={(option) => option.value}
          displayValue={displayValue}
          onSelectionChanged={(option) => onChange(option)}
          selected={defaultValue}
          enableFilter={true}
        />
      </span>
    )
  ) : asHyperlink && hasModelData ? (
    <a
      className="inline enum"
      data-testid={`hyperlink-for-${koodistoUri || 'unknown'}-enum-editor`}
      href={asHyperlink(model).url || '#'}
      target={asHyperlink(model).target || '_self'}
    >
      {useHyphenate
        ? hyphenate(modelTitle(model, undefined, titleFormatter))
        : modelTitle(model, undefined, titleFormatter)}
    </a>
  ) : (
    <span
      className="inline enum"
      data-testid={`span-for-${koodistoUri || 'unknown'}-enum-editor`}
    >
      {useHyphenate
        ? hyphenate(modelTitle(model, undefined, titleFormatter))
        : modelTitle(model, undefined, titleFormatter)}
    </span>
  )
}

export const zeroValue = { title: t('Ei valintaa'), value: 'eivalintaa' }

EnumEditor.fetchAlternatives = (model) => {
  const alternativesPath = model.alternativesPath
  const edit = model.context.edit
  if (edit && alternativesPath) {
    return Http.cachedGet(alternativesPath)
  } else {
    return Bacon.constant([])
  }
}

// When selecting between more than 1 prototype, each of which has an enum field with the same same path,
// this fetches the alternatives for all the prototypes, returning an observable list of prototypes populated with
// each enum value.
export const fetchAlternativesBasedOnPrototypes = (
  models,
  pathToFieldWithAlternatives
) => {
  const alternativesForField = (model) =>
    EnumEditor.fetchAlternatives(
      modelLookup(model, pathToFieldWithAlternatives)
    ).map((alternatives) =>
      alternatives.map((enumValue) =>
        modelSetValue(model, enumValue, pathToFieldWithAlternatives)
      )
    )
  return Bacon.combineAsArray(models.map(alternativesForField))
    .last()
    .map(R.unnest)
}

EnumEditor.knownAlternatives = (model) =>
  model.alternativesPath &&
  (model.alternativesPath.split('/')[6] || '').split(',').filter(R.identity)

EnumEditor.canShowInline = () => true
EnumEditor.handlesOptional = () => true
EnumEditor.createEmpty = (protomodel) => modelSetValue(protomodel, zeroValue)
EnumEditor.validateModel = (model) => {
  if (!model.value && !model.optional) {
    return [{ key: 'missing' }]
  }
}
