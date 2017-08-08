import React from 'react'
import R from 'ramda'
import {modelEmpty} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {lensedModel, optionalModelLens, optionalPrototypeModel, pushModel, resetOptionalModel} from './EditorModel'
import Text from '../Text.jsx'

export class OptionalEditor extends React.Component {
  render() {
    let {model} = this.props
    let prototype = () => optionalPrototypeModel(model)

    let removeValue = () => {
      resetOptionalModel(this.props.model)
    }
    let addValue = () => {
      pushModel(prototype())
    }

    let modelToBeShown = model
    let empty = modelEmpty(modelToBeShown)

    let canRemove = model.context.edit && !empty

    return (<span className="optional-wrapper">
      {
        empty
          ? model.context.edit && prototype()
              ? <a className="add-value" onClick={addValue}><Text name="lisää"/></a>
              : null
          : <Editor model={R.merge(modelToBeShown, { optional: false })}/>
      }
      {
        canRemove && <a className="remove-value" onClick={removeValue}>{''}</a>
      }
    </span>)
  }
}
OptionalEditor.canShowInline = () => true

// TODO: remove object wrapping
export const wrapOptional = ({model}) => {
  if (!model) throw new Error('model missing. remember to wrap model like { model }')
  if (!model.optional) return model
  if (!model.context) throw new Error('cannot wrap without context')

  return lensedModel(model, optionalModelLens({model}))
}
