import React from 'react'
import {modelSetValue, modelData} from './EditorModel.js'
import {formatISODate, parseISODate, formatFinnishDate} from '../date.js'
import DateInput from '../DateInput.jsx'

export const DateEditor = ({model, isAllowedDate}) => {
  let pushValue = (value) => model.context.changeBus.push([model.context, modelSetValue(model, value)])

  let validityCallback = (valid, stringInput) => {
    if (!valid) {
      pushValue({ data: stringInput})
    }
  }
  let valueCallback = (date) => {
    pushValue(date && { data: formatISODate(date), title: formatFinnishDate(date)})
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
