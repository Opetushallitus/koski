import React from 'react'
import {modelTitle, modelSetValue} from './EditorModel.js'
import {formatISODate, parseFinnishDate, formatFinnishDate} from '../date.js'
import DateInput from '../DateInput.jsx'

export const DateEditor = React.createClass({
  render() {
    let {model, isAllowedDate = () => true } = this.props
    let validityCallback = (valid) => model.context.errorBus.push([model.context, {error: !valid}])
    let valueCallback = (date) => model.context.changeBus.push([model.context, modelSetValue(model, { data : formatISODate(date) , title: formatFinnishDate(date) })])
    var value = parseFinnishDate(modelTitle(model))
    var optional = model.optional
    return model.context.edit
      ? <DateInput {...{value, optional, isAllowedDate, validityCallback, valueCallback}} />
      : <span className="inline date">{modelTitle(model)}</span>
  }
})
DateEditor.canShowInline = () => true
DateEditor.handlesOptional = true
