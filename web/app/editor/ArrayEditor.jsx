import React from 'react'
import {contextualizeSubModel, modelItems} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {pushRemoval, pushModel, modelLookup} from './EditorModel'

export const ArrayEditor = ({model, reverse}) => {
  let wrappedModel = wrapOptional({model})

  let items = modelItems(wrappedModel)

  if (reverse && !wrappedModel.context.edit) items = items.slice(0).reverse()

  let className = ArrayEditor.canShowInline(wrappedModel) ? 'array inline' : 'array'

  let addItem = () => {
    pushModel(contextualizeSubModel(wrappedModel.arrayPrototype, wrappedModel, items.length))
  }

  let itemEditorHandlesOptional = () => {
    let childModel = wrappedModel.arrayPrototype && contextualizeSubModel(wrappedModel.arrayPrototype, wrappedModel, modelItems(wrappedModel).length)
    return childModel && childModel.type !== 'prototype' ? Editor.handlesOptional(childModel) : false
  }
  let minItems = wrappedModel.minItems || 0
  let tooFewItems = items.length < minItems
  return (
    <ul className={className}>
      {
        items.map((item, i) => {
          return (<li key={item.arrayKey || i}>
            <Editor model = {item} />
            {item.context.edit && items.length > minItems && <a className="remove-item" onClick={() => pushRemoval(item)}></a>}
          </li>)
        })
      }
      {
        wrappedModel.context.edit && wrappedModel.arrayPrototype !== undefined
          ? itemEditorHandlesOptional() || tooFewItems
            ? <li className="add-item"><Editor model = {modelLookup(model, items.length)} /></li>
            : <li className="add-item"><a onClick={addItem}>lisää uusi</a></li>
          :null
      }
    </ul>
  )
}

ArrayEditor.validateModel = (model) => {
  if (model.optional && !model.value) return
  if (!model.value) return ['Arvo puuttuu']
  if (model.minItems && model.value.length < model.minItems) return [`Tarvitaan vähintään ${model.minItems} alkiota`]
  if (model.maxItems && model.value.length > model.maxItems) return [`Hyväksytään korkeintaan ${model.minItems} alkiota`]
}
ArrayEditor.canShowInline = (model) => {
  let items = modelItems(model)
  return items[0] && model.context.edit ? false : Editor.canShowInline(items[0])
}
ArrayEditor.handlesOptional = true