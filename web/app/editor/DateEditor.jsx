import React from 'react'
import * as R from 'ramda'
import {modelData, pushModelValue} from './EditorModel.ts'
import {formatISODate, parseISODate, formatFinnishDate} from '../date/date.js'
import DateInput from '../date/DateInput'
import {wrapOptional} from './EditorModel'
import {modelSetData} from './EditorModel'

export const DateEditor = ({model, isAllowedDate}) => {
  let wrappedModel = wrapOptional(model)

  let validityCallback = (valid, stringInput) => !valid && pushModelValue(wrappedModel, { data: stringInput}) // push raw string value to model in case of invalid input. will cause model validation to fail
  let valueCallback = (date) => pushModelValue(wrappedModel, date && { data: formatISODate(date), title: formatFinnishDate(date)})

  let dateInISOFormat = modelData(wrappedModel)
  var dateValue = dateInISOFormat && parseISODate(dateInISOFormat) || dateInISOFormat
  return model.context.edit
    ? <DateInput {...{value: dateValue, optional: isOptional(model), isAllowedDate, validityCallback, valueCallback}} />
    : <span className="inline date">{dateValue && formatFinnishDate(dateValue)}</span>
}

DateEditor.createEmpty = (d) => modelSetData(d, '')
DateEditor.canShowInline = () => true
DateEditor.handlesOptional = () => true
DateEditor.validateModel = (model) => {
  let data = modelData(model)
  let empty = !data
  if (empty && isOptional(model)) return
  if (!data) return [{key: 'missing'}]
  var dateValue = data && parseISODate(data)
  if (!dateValue) return [{key: 'invalid.date'}]
}

const isOptional = model => model.optional && !isPerusopetuksenVuosiluokanSuorituksenAlkamispäivä(model)

const isPerusopetuksenVuosiluokanSuorituksenAlkamispäivä = model => R.and(
  R.includes('perusopetuksenvuosiluokansuoritus', model.parent.value.classes),
  R.last(model.path) === 'alkamispäivä'
)
