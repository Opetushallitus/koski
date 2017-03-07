import React from 'react'
import R from 'ramda'
import {modelEmpty, contextualizeModel} from './EditorModel.js'
import {Editor} from './GenericEditor.jsx'

export const OptionalEditor = React.createClass({
  render() {
    let {model} = this.props
    let {addingModel, removed} = this.state
    let addValue = () => {
      let prototype = contextualizeModel(model.prototype, model.context)

      if (!prototype.value.data && prototype.oneOfPrototypes) {
        // This is a OneOfModel, just pick the first alternative for now. TODO: allow picking suitable prototype
        prototype = contextualizeModel(prototype.oneOfPrototypes[0], model.context)
      }

      if (!prototype.value.data) {
        throw new Error('Prototype value data missing')
      }
      model.context.changeBus.push([prototype.context, prototype.value])
      this.setState({addingModel: prototype, removed: false})

    }
    let removeValue = () => {
      this.props.model.context.changeBus.push([model.context, {data: undefined}])
      this.setState({ removed: true, addingModel: undefined })
    }

    let modelToBeShown = addingModel || model
    let empty = (modelEmpty(modelToBeShown) || removed)
    let canRemove = model.context.edit && !empty

    return (<span className="optional-wrapper">
      {
        empty
          ? model.context.edit && model.prototype !== undefined
              ? <a className="add-value" onClick={addValue}>lisää</a>
              : null
          : <Editor model={R.merge(modelToBeShown, { optional: false })}/>
      }
      {
        canRemove && <a className="remove-value" onClick={removeValue}></a>
      }
    </span>)
  },
  componentWillReceiveProps(props) {
    if (!props.model.context.edit && this.props.model.context.edit) {
      this.setState(this.getInitialState())
    }
  },
  getInitialState() {
    return { addingModel: undefined, removed: false }
  }
})
OptionalEditor.canShowInline = () => true