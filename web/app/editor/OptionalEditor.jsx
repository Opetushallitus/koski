import React from 'react'
import R from 'ramda'
import {modelEmpty} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {lensedModel, pushModel, optionalModelLens, resetOptionalModel, optionalPrototypeModel} from './EditorModel'

export const OptionalEditor = React.createClass({
  render() {
    let {model} = this.props
    let prototype = optionalPrototypeModel(model)

    let removeValue = () => {
      resetOptionalModel(this.props.model)
    }

    let modelToBeShown = model
    let empty = modelEmpty(modelToBeShown)
    let canRemove = model.context.edit && !empty

    return (<span className="optional-wrapper">
      {
        empty
          ? model.context.edit && prototype
              ? <a className="add-value" onClick={() => pushModel(prototype)}>lisää</a>
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

export const wrapOptional = ({model, isEmpty, createEmpty}) => {
  if (!model.optional) return model
  if (!model.context) throw new Error('cannot wrap without context')

  return lensedModel(model, optionalModelLens({model, isEmpty, createEmpty}))
}
