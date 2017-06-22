import React from 'react'
import {modelData, pushModelValue} from './EditorModel.js'
import {formatISODate, parseISODate, formatFinnishDate} from '../date.js'
import DateInput from '../DateInput.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {modelSetData} from './EditorModel'

export const DateEditor = ({model, isAllowedDate}) => {
  let wrappedModel = wrapOptional({model})

  let validityCallback = (valid, stringInput) => !valid && pushModelValue(wrappedModel, { data: stringInput}) // push raw string value to model in case of invalid input. will cause model validation to fail
  let valueCallback = (date) => pushModelValue(wrappedModel, date && { data: formatISODate(date), title: formatFinnishDate(date)})

  let dateInISOFormat = modelData(wrappedModel)
  var dateValue = dateInISOFormat && parseISODate(dateInISOFormat) || dateInISOFormat
  return model.context.edit
    ? <DateInput {...{value: dateValue, optional: wrappedModel.optional, isAllowedDate, validityCallback, valueCallback}} />
    : <span className="inline date">{dateValue && formatFinnishDate(dateValue)}</span>
}

DateEditor.createEmpty = (d) => modelSetData(d, '')
DateEditor.canShowInline = () => true
DateEditor.handlesOptional = () => true
DateEditor.validateModel = (model) => {
  let data = modelData(model)
  let empty = !data
  if (empty && model.optional) return
  if (!data) return [{key: 'missing'}]
  var dateValue = data && parseISODate(data)
  if (!dateValue) return [{key: 'invalid.date'}]
}