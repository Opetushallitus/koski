import React from 'react'
import {modelData, pushModelValue, wrapOptional, modelSetValue, modelValid} from './EditorModel'
import {numberToString} from '../util/format'

export class NumberEditor extends React.Component {
  render() {
    let {model, disabled} = this.props
    let wrappedModel = wrapOptional(model)
    let onChange = (event) => pushModelValue(wrappedModel, event.target.value ? { data: parseNumber(event.target.value) } : undefined)

    let data = modelData(wrappedModel)
    let value = numberToString(data, model.scale)
    let error = !modelValid(model)

    return wrapWithUnitOfMeasure(wrappedModel.unitOfMeasure, wrappedModel.context.edit && !disabled
      ? <input type="text"  defaultValue={value} onChange={ onChange } className={'editor-input inline number' + (error ? ' error' : '')}/>
      : <span className="inline number">{value}</span>)
  }
}

const wrapWithUnitOfMeasure = (unitOfMeasure, content) => unitOfMeasure ? <span>{content}<span className="unit-of-measure">{unitOfMeasure}</span></span> : content

NumberEditor.handlesOptional = () => true
NumberEditor.isEmpty = m => modelData(m) === undefined || modelData(m) === null
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

let parseNumber = (s) => {
  s = s.replace(',', '.')
  if (isNaN(s)) return s
  return parseFloat(s)
}
