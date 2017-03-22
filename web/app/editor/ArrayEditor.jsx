import React from 'react'
import R from 'ramda'
import {addContext, childContext, contextualizeModel, modelItems} from './EditorModel.js'
import {Editor} from './GenericEditor.jsx'
import {resetOptionalModel} from './OptionalEditor.jsx'

export const ArrayEditor = React.createClass({
  render() {
    let reverse = this.props.reverse
    let model = this.getUsedModel()
    let items = modelItems(this.props.model)
    if (reverse && !model.context.edit) items = items.slice(0).reverse()
    let inline = ArrayEditor.canShowInline(this)
    let zeroValue = ArrayEditor.zeroValue(this)
    let className = inline
      ? 'array inline'
      : 'array'

    let itemModel = () => {
      return contextualizeModel(model.arrayPrototype, childContext(model.context, items.length))
    }

    let newItem = () => {
      let item = itemModel()
      return addContext(item.type === 'enum' ? R.dissoc('value', item) : item, {inArray: true})
    }

    let addItem = () => {
      if (this.props.model.optional && items.length == 0) {
        let prototype = this.props.model.optionalPrototype && contextualizeModel(this.props.model.optionalPrototype, this.props.model.context)
        prototype && model.context.changeBus.push([prototype.context, prototype])
      }
      let item = itemModel()
      model.context.changeBus.push([item.context, item])
    }

    return (
      <ul ref="ul" className={className}>
        {
          items.map((item, i) => {
              let removeItem = () => {
                let newItems = items
                newItems.splice(i, 1)
                item.context.changeBus.push([item.context, undefined])
                if (newItems.length === 0) {
                  resetOptionalModel(this.props.model)
                }
              }
              return (<li key={item.arrayKey}>
                <Editor model = {addContext(item, {zeroValue: zeroValue})} />
                {item.context.edit && <a className="remove-item" onClick={removeItem}></a>}
              </li>)
            }
          )
        }
        {
          model.context.edit && model.arrayPrototype !== undefined
            ? zeroValue
              ? <li className="add-item"><Editor model = {newItem()} /></li>
              : <li className="add-item"><a onClick={addItem}>lisää uusi</a></li>
            :null
        }
      </ul>
    )
  },
  getUsedModel() {
    let { model } = this.props
    let optionalModel = () => R.dissoc('value', contextualizeModel(model.optionalPrototype, model.context))
    return model.optional ? R.merge(model, optionalModel()) : model
  }
})

ArrayEditor.canShowInline = (component) => {
  let items = modelItems(component.props.model)
  // consider inlineability of first item here. make a stateless "fake component" because the actual React component isn't available to us here.
  let fakeComponent = {props: { model: items[0] }}
  return Editor.canShowInline(fakeComponent)
}
ArrayEditor.zeroValue = (component) => {
  let mdl = component.getUsedModel()
  let childModel = mdl.arrayPrototype && contextualizeModel(mdl.arrayPrototype, childContext(mdl.context, modelItems(mdl).length))
  let childComponent = {props: {model : R.dissoc('value', childModel)}}
  return childModel && childModel.type !== 'prototype' ? Editor.zeroValue(childComponent) : null
}
ArrayEditor.handlesOptional = true