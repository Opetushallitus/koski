import React from 'react'
import {modelSetValue, modelData} from './EditorModel.js'
import {formatISODate, parseISODate, formatFinnishDate} from '../date.js'
import DateInput from '../DateInput.jsx'

export const DateEditor = React.createClass({
  render() {
    let {model, isAllowedDate = () => true } = this.props
    let validityCallback = (valid) => model.context.errorBus.push([model.context, {error: !valid}])
    let valueCallback = (date) => model.context.changeBus.push([model.context, modelSetValue(model, date && { data : formatISODate(date) , title: formatFinnishDate(date) })])
    let dateInISOFormat = modelData(model)
    var dateValue = dateInISOFormat && parseISODate(dateInISOFormat)
    var optional = model.optional
    return model.context.edit
      ? <DateInput {...{value: dateValue, optional, isAllowedDate, validityCallback, valueCallback}} />
      : <span className="inline date">{dateValue && formatFinnishDate(dateValue)}</span>
  }
})
DateEditor.canShowInline = () => true
DateEditor.handlesOptional = true
