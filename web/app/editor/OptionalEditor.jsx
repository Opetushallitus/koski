import React from 'react'
import R from 'ramda'
import {modelEmpty, modelData, contextualizeModel} from './EditorModel.js'
import {Editor} from './GenericEditor.jsx'

export const OptionalEditor = React.createClass({
  render() {
    let {model} = this.props
    let prototype = model.optionalPrototype && contextualizeModel(model.optionalPrototype, model.context)

    let addValue = () => {
      if (!prototype.value.data && prototype.oneOfPrototypes) {
        // This is a OneOfModel, just pick the first alternative for now. TODO: allow picking suitable prototype
        prototype = contextualizeModel(prototype.oneOfPrototypes[0], model.context)
      }

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
export const resetOptionalModel = (model) => model.context.changeBus.push([model.context, { optional: model.optional, optionalPrototype: model.optionalPrototype }])