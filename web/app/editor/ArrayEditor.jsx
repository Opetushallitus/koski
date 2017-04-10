import React from 'react'
import R from 'ramda'
import {contextualizeSubModel, modelItems} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {pushRemoval, pushModel} from './EditorModel'

let counter = 1

export const ArrayEditor = ({model, reverse}) => {
  let wrappedModel = wrapOptional({model})

  let items = modelItems(wrappedModel)

  if (reverse && !wrappedModel.context.edit) items = items.slice(0).reverse()

  let className = ArrayEditor.canShowInline(wrappedModel) ? 'array inline' : 'array'

  let newItemModel = () => {
    var m = contextualizeSubModel(wrappedModel.arrayPrototype, wrappedModel, items.length)
    m.arrayKey = 'new-' + (counter++)
    return m
  }

  let newItem = () => {
    let item = newItemModel()
    return item.type === 'enum' ? R.dissoc('value', item) : item // remove default value from enums TODO: should be done on the server
  }

  let addItem = () => {
    let item = newItemModel()
    pushModel(item)
  }

  let itemEditorHandlesOptional = () => {
    let childModel = wrappedModel.arrayPrototype && contextualizeSubModel(wrappedModel.arrayPrototype, wrappedModel, modelItems(wrappedModel).length)
    return childModel && childModel.type !== 'prototype' ? Editor.handlesOptional(childModel) : false
  }
  //console.log(model.context.path.slice(-1),items.map(item => item.arrayKey).join(','))
  return (
    <ul className={className}>
      {
        items.map((item, i) => {
          return (<li key={item.arrayKey || i}>
            <Editor model = {item} />
            {item.context.edit && <a className="remove-item" onClick={() => pushRemoval(item)}></a>}
          </li>)
        })
      }
      {
        wrappedModel.context.edit && wrappedModel.arrayPrototype !== undefined
          ? itemEditorHandlesOptional()
            ? <li className="add-item"><Editor model = {newItem()} /></li>
            : <li className="add-item"><a onClick={addItem}>lisää uusi</a></li>
          :null
      }
    </ul>
  )
}

ArrayEditor.canShowInline = (model) => {
  let items = modelItems(model)
  return items[0] && Editor.canShowInline(items[0])
}
ArrayEditor.handlesOptional = true