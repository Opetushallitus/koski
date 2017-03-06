import React from 'react'
import R from 'ramda'
import {modelEmpty, contextualizeModel} from './EditorModel.js'
import {Editor} from './GenericEditor.jsx'

export const OptionalEditor = React.createClass({
  render() {
    let {model} = this.props
    let {adding, removed} = this.state
    let addValue = () => {
      this.setState({adding: true})
    }
    let removeValue = () => {
      this.props.model.context.changeBus.push([model.context, {data: undefined}])
      this.setState({ removed: true, adding: false })
    }
    let addingContent = () => {
      // get value from prototype when adding item
      let prototype = contextualizeModel(model.prototype, model.context)
      if (prototype.oneOfPrototypes) {
        // This is a OneOfModel, just pick the first alternative for now. TODO: allow picking suitable prototype
        prototype = contextualizeModel(prototype.oneOfPrototypes[0], model.context)
      }
      return <Editor model={R.merge(prototype, { optional: false })}/>
    }
    let empty = (modelEmpty(model) || removed)
    let canRemove = model.context.edit && !empty && !adding
    return (<span className="optional-wrapper">
      {
        empty
          ? adding
            ? addingContent()
            : model.context.edit && model.prototype !== undefined
              ? <a className="add-value" onClick={addValue}>lisää</a>
              : null
          : <Editor model={R.merge(model, { optional: false })}/>
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
    return { adding: false, removed: false }
  }
})
OptionalEditor.canShowInline = () => true