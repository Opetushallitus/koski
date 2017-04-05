import React from 'react'
import R from 'ramda'
import {modelEmpty, modelData, contextualizeModel} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import * as L from 'partial.lenses'
import {modelSetValue} from './EditorModel'

export const OptionalEditor = React.createClass({
  render() {
    let {model} = this.props
    let prototype = optionalModel(model)

    let addValue = () => {
      if (!modelData(prototype)) {
        throw new Error('Prototype value data missing')
      }

      model.context.changeBus.push([prototype.context, prototype])
    }
    let removeValue = () => {
      resetOptionalModel(this.props.model)
    }

    let modelToBeShown = model
    let empty = modelEmpty(modelToBeShown)
    let canRemove = model.context.edit && !empty && prototype.type != 'array'

    return (<span className="optional-wrapper">
      {
        empty
          ? model.context.edit && model.optionalPrototype !== undefined
              ? <a className="add-value" onClick={addValue}>lisää</a>
              : null
          : <Editor model={R.merge(modelToBeShown, { optional: false })}/>
      }
      {
        canRemove && <a className="remove-value" onClick={removeValue}></a>
      }
    </span>)
  }
})
OptionalEditor.canShowInline = () => true
export const optionalModel = (model) => {
  let prototype = model.optionalPrototype && contextualizeModel(model.optionalPrototype, model.context)
  if (prototype && prototype.oneOfPrototypes && !modelData(prototype)) {
    // This is a OneOfModel, just pick the first alternative for now. TODO: allow picking suitable prototype
    prototype = contextualizeModel(prototype.oneOfPrototypes[0], model.context)
  }

  return makeOptional(prototype, model)
}
export const resetOptionalModel = (model) => model.context.changeBus.push([model.context, createOptional(model)])

export const pushOptionalModelValue = (model, value, path) => {
  if (!model.optional) {
    model.context.changeBus.push([model.context, modelSetValue(model, value, path)])
  } else {
    let usedModel = model.value ? model : optionalModel(model)
    if (value) {
      model.context.changeBus.push([model.context, modelSetValue(usedModel, value, path)])
    } else {
      resetOptionalModel(model)
    }
  }
}
const makeOptional = (model, optModel) => model && (model.optional ? model : R.merge(model, createOptional(optModel)))
const createOptional = (optModel) => ({ optional: optModel.optional, optionalPrototype: optModel.optionalPrototype })