import React from 'react'
import * as R from 'ramda'
import {
  contextualizeModel,
  addContext,
  contextualizeSubModel,
  modelLookup
} from './EditorModel'
import { parseBool } from '../util/util'
import PropTypes from 'prop-types'

/*
 model: required model object
 path: optional path to actually used model. It's ok if the model cannot be found by path: a NullEditor will be used
 */
export class Editor extends React.Component {
  render() {
    let { model, ...rest } = this.props
    if (!model) {
      throw new Error('model missing')
    }
    model = Editor.setupContext(model, this.props)
    return getModelEditor(model, rest)
  }

  shouldComponentUpdate(nextProps) {
    return Editor.shouldComponentUpdate.call(this, nextProps)
  }
}

Editor.setupContext = (
  model,
  { editorMapping, changeBus, editBus, saveChangesBus, edit, path }
) => {
  if (!model.context) {
    if (!editorMapping) {
      console.error('editorMapping required for root editor', model)
      throw new Error('editorMapping required for root editor')
    }
    R.toPairs(editorMapping).forEach(([key, value]) => {
      if (!value) throw new Error('Editor missing for ' + key)
    })
    model = contextualizeModel(model, {
      changeBus,
      saveChangesBus,
      editBus,
      path: '',
      prototypes: model.prototypes,
      editorMapping
    })
  } else {
    if (!model.context.prototypes)
      model = addContext(model, { prototypes: model.prototypes })
    if (editorMapping) model = addContext(model, { editorMapping })
    if (changeBus) model = addContext(model, { changeBus })
    if (saveChangesBus) model = addContext(model, { saveChangesBus })
    if (editBus) model = addContext(model, { editBus })
  }
  edit = !model.readOnly && parseBool(edit)
  if (edit !== model.context.edit) {
    model = addContext(model, { edit })
  }
  if (path) {
    model = modelLookup(model, path)
  }
  return model
}

Editor.shouldComponentUpdate = function (nextProps) {
  if (parseBool(nextProps.alwaysUpdate)) return true
  const next = nextProps.model
  const current = this.props.model
  let result = next.modelId != current.modelId
  if (pathHash(next) !== pathHash(current)) {
    result = true
  }
  if (result) {
    // console.log('update', next.path)
  } else {
    // console.log('skip', next.path)
  }
  return result
}
const pathHash = (m) => {
  let hash = 0
  for (const i in m.path || []) {
    if (typeof m.path[i] === 'number') {
      hash += m.path[i]
    }
  }
  if (m.context && m.context.changeBus) {
    hash += m.context.changeBus.id
  }
  return hash
}
Editor.propTypes = {
  model: PropTypes.object.isRequired
}
Editor.canShowInline = (model) =>
  (getEditorFunction(model).canShowInline || (() => false))(model)
Editor.handlesOptional = (model, modifier) =>
  editorFunctionHandlesOptional(getEditorFunction(model), modifier)
const editorFunctionHandlesOptional = (editor, modifier) =>
  editor && editor.handlesOptional && editor.handlesOptional(modifier)

const NullEditor = () => null

const getEditorFunction = (model) => {
  if (!model) return NullEditor

  if (model.optional) {
    const modelForFindingEditor = model.value
      ? model
      : contextualizeSubModel(model.optionalPrototype, model)
    return (
      editorForModel(modelForFindingEditor, (e) =>
        editorFunctionHandlesOptional(e)
      ) || model.context.editorMapping.optional
    )
  }

  const editor = editorForModel(model)
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

const editorForModel = (mdl, editorFilter = () => true) => {
  if (!mdl) return null
  if (mdl.value) {
    for (const i in mdl.value.classes) {
      const editor = mdl.context.editorMapping[mdl.value.classes[i]]
      if (
        editor &&
        editorFilter(editor, mdl) &&
        (!mdl.context.edit || !editor.readOnly) &&
        (mdl.context.edit || !editor.writeOnly)
      ) {
        return editor
      }
    }
  }
  const typeEditor = mdl.context.editorMapping[mdl.type]
  if (editorFilter(typeEditor, mdl)) return typeEditor
}

const getModelEditor = (model, props) => {
  if (model && !model.context) {
    console.error('Context missing from model', model)
  }
  const ModelEditor = getEditorFunction(model)
  return <ModelEditor model={model} {...props} />
}
