import React from 'react'
import R from 'ramda'
import {modelEmpty, modelData, contextualizeModel} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import * as L from 'partial.lenses'
import {modelSetValue, lensedModel} from './EditorModel'

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

const optionalModel = (model, context, pathElem) => {
  if (!context) {
    if (!model.context) throw new Error('Context missing')
    context = model.context
  }
  let prototype = model.optionalPrototype && contextualizeModel(model.optionalPrototype, context, pathElem)
  if (prototype && prototype.oneOfPrototypes && !modelData(prototype)) {
    // This is a OneOfModel, just pick the first alternative for now. TODO: allow picking suitable prototype
    prototype = contextualizeModel(prototype.oneOfPrototypes[0], context)
  }

  return makeOptional(prototype, model)
}
const resetOptionalModel = (model) => model.context.changeBus.push([model.context, createOptionalEmpty(model)])

const makeOptional = (model, optModel) => model && (model.optional ? model : R.merge(model, createOptionalEmpty(optModel)))
const createOptionalEmpty = (optModel) => ({ optional: optModel.optional, optionalPrototype: optModel.optionalPrototype })

const modelEmptyForOptional = (m) => {
  if (!m.value) return true
  if (m.type == 'object') {
    if (!m.value.properties) return true
    for (var i in m.value.properties) {
      if (!modelEmptyForOptional(m.value.properties[i].model)) return false
    }
    return true
  }
  if (m.type == 'array') {
    return m.value.length == 0
  }
  return !m.value.data
}

export const wrapOptional = ({model, isEmpty = modelEmptyForOptional, createEmpty = (x => x)}) => {
  if (!model.optional) return model
  if (!model.context) throw new Error('cannot wrap without context')

  let getUsedModel = (m) => {
    return m.value ? m : createEmpty(optionalModel(m, model.context, myLens))
  }

  let myLens = L.lens(
    m => {
      return getUsedModel(m)
    },
    (newModel, contextModel) => {
      if (isEmpty(newModel)) {
        //console.log('set empty', newModel)
        return createOptionalEmpty(contextModel)
      } else {
        //console.log('set non-empty', newModel)
        return modelSetValue(getUsedModel(contextModel), newModel.value)
      }
    }
  )

  return lensedModel(model, myLens)
}
