import React from 'react'
import {modelData} from './EditorModel.js'
import {formatISODate, parseISODate, formatFinnishDate} from '../date.js'
import DateInput from '../DateInput.jsx'
import {pushModelValue, wrapOptional} from './OptionalEditor.jsx'

export const DateEditor = ({model, isAllowedDate}) => {
  let wrappedModel = wrapOptional({model})

  let validityCallback = (valid, stringInput) => {
    if (!valid) {
      ((value) => pushModelValue(wrappedModel, value))({ data: stringInput}) // push raw string value to model in case of invalid input. will cause model validation to fail
    }
  }
  let valueCallback = (date) => {
    ((value) => pushModelValue(wrappedModel, value))(date && { data: formatISODate(date), title: formatFinnishDate(date)})
  }
  let dateInISOFormat = modelData(model)
  var dateValue = dateInISOFormat && parseISODate(dateInISOFormat) || dateInISOFormat
  var optional = model.optional
  return model.context.edit
    ? <DateInput {...{value: dateValue, optional, isAllowedDate, validityCallback, valueCallback}} />
    : <span className="inline date">{dateValue && formatFinnishDate(dateValue)}</span>
}
DateEditor.canShowInline = () => true
DateEditor.handlesOptional = true
DateEditor.validateModel = (model) => {
  var data = modelData(model)
  if (!model.optional && !data) return ['empty date']
  var dateValue = data && parseISODate(data)
  if (!dateValue) return ['invalid date']
}
