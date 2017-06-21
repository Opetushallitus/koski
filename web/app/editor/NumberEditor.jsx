import React from 'react'
import {modelData, pushModelValue} from './EditorModel.js'
import {wrapOptional} from './OptionalEditor.jsx'

export const NumberEditor = React.createClass({
  render() {
    let {model} = this.props
    let wrappedModel = wrapOptional({model})
    let onChange = (event) => pushModelValue(wrappedModel, event.target.value ? { data: parseFloat(event.target.value) } : undefined)

    let data = modelData(wrappedModel)
    let value = data
      ? Math.round(data * 100) / 100
      : data

    let minValue = model.minValue || (model.minValueExclusive && model.minValueExclusive + 1)
    let maxValue = model.maxValue || (model.maxValueExclusive && model.maxValueExclusive - 1)

    return wrapWithUnitOfMeasure(model.unitOfMeasure, wrappedModel.context.edit
      ? <input type="number" min={minValue} max={maxValue} defaultValue={modelData(wrappedModel)} onChange={ onChange } className="editor-input inline number"/>
      : <span className="inline number">{value}</span>)
  }
})

const wrapWithUnitOfMeasure = (unitOfMeasure, content) => unitOfMeasure ? <span>{content}<span className="unit-of-measure">{unitOfMeasure}</span></span> : content

NumberEditor.handlesOptional = () => true
NumberEditor.validateModel = model => {
  let value = modelData(model)
  return (value !== undefined && isNaN(value)) ? [{key: 'invalid.number'}] : []
}
