import React from 'react'
import {modelData, pushModelValue} from './EditorModel.js'
import {wrapOptional} from './OptionalEditor.jsx'
import {modelSetValue, modelValid} from './EditorModel'

export class NumberEditor extends React.Component {
  render() {
    let {model} = this.props
    let wrappedModel = wrapOptional({model})
    let onChange = (event) => pushModelValue(wrappedModel, event.target.value ? { data: parseNumber(event.target.value) } : undefined)

    let data = modelData(wrappedModel)
    let value = formatNumber(data)
    let error = !modelValid(model)

    return wrapWithUnitOfMeasure(wrappedModel.unitOfMeasure, wrappedModel.context.edit
      ? <input type="text"  defaultValue={value} onChange={ onChange } className={'editor-input inline number' + (error ? ' error' : '')}/>
      : <span className="inline number">{value}</span>)
  }
}

const wrapWithUnitOfMeasure = (unitOfMeasure, content) => unitOfMeasure ? <span>{content}<span className="unit-of-measure">{unitOfMeasure}</span></span> : content

NumberEditor.handlesOptional = () => true
NumberEditor.isEmpty = m => !modelData(m)
NumberEditor.createEmpty = (m) => modelSetValue(m, undefined)
NumberEditor.validateModel = model => {
  let value = modelData(model)
  if (value == undefined) {
    if (!model.optional) return [{key: 'missing'}]
  } else {
    if (typeof value !== 'number'
      || (model.minValue !== undefined && value < model.minValue)
      || (model.minValueExclusive !== undefined && value <= model.minValueExclusive)
      || (model.maxValue !== undefined && value > model.maxValue)
      || (model.maxValueExclusive !== undefined && value >= model.maxValueExclusive)
    ) {
      return [{key: 'invalid.number'}]
    }
  }
}

let formatNumber = (s) => {
  if (s == undefined) return s
  return (Math.round(s * 100) / 100).toString().replace('.', ',')
}

let parseNumber = (s) => {
  s = s.replace(',', '.')
  if (isNaN(s)) return s
  return parseFloat(s)
}