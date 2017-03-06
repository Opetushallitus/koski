import React from 'react'
import R from 'ramda'
import { modelEmpty, modelLookup, contextualizeModel } from './EditorModel.js'

export const Editor = React.createClass({
  render() {
    let { model, editorMapping, changeBus, doneEditingBus, path } = this.props
    if (!model.context) {
      if (!editorMapping) throw new Error('editorMapping required for root editor')
      R.toPairs(editorMapping).forEach(([key, value]) => { if (!value) throw new Error('Editor missing for ' + key) })
      model = contextualizeModel(model, {
        changeBus, doneEditingBus,
        root: true,
        rootModel: model,
        path: '',
        prototypes: model.prototypes,
        editorMapping
      })
    }
    return getModelEditor(model, path)
  }
})
Editor.propTypes = {
  model: React.PropTypes.object.isRequired
}
Editor.canShowInline = (component) => (getEditorFunction(component.props.model).canShowInline || (() => false))(component)

export const NullEditor = React.createClass({
  render() {
    return null
  }
})

const OptionalEditor = React.createClass({
  render() {
    let {model} = this.props
    let adding = this.state && this.state.adding
    let removed = this.state && this.state.removed
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
      return getModelEditor(R.merge(prototype, { optional: false }))
    }
    let empty = (modelEmpty(model) || removed)
    let canRemove = model.context.edit && !empty && !adding
    return (<span className="optional-wrapper">
      {
        empty
          ? adding
            ? addingContent()
            : model.context.edit
              ? <a className="add-value" onClick={addValue}>lisää</a>
              : null
          : getModelEditor(R.merge(model, { optional: false }))
      }
      {
        canRemove && <a className="remove-value" onClick={removeValue}></a>
      }
    </span>)
  },
  componentWillReceiveProps(props) {
    if (!props.model.context.edit) {
      this.setState({ adding: false, removed: false })
    }
  }
})
OptionalEditor.canShowInline = () => true

const getEditorFunction = (model) => {
  let editorByClass = (classes) => {
    for (var i in classes) {
      var editor = model.context.editorMapping[classes[i]]
      if (editor && (!model.context.edit || !editor.readOnly)) { return editor }
    }
  }
  if (!model) return NullEditor
  if (model.optional) {
    return OptionalEditor
  }
  let editor = (model.value && editorByClass(model.value.classes)) || model.context.editorMapping[model.type]
  if (!editor) {
    if (!model.type) {
      console.error('Typeless model', model)
    } else {
      console.error('Missing editor for type "' + model.type + '"')
    }
    return NullEditor
  }
  return editor
}

const getModelEditor = (model, path) => {
  if (path) {
    return getModelEditor(modelLookup(model, path))
  }
  if (model && !model.context) {
    console.error('Context missing from model', model)
  }
  var ModelEditor = getEditorFunction(model)
  return <ModelEditor model={model}/>
}