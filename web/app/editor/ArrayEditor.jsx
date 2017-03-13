import React from 'react'
import Bacon from 'baconjs'
import * as L from 'partial.lenses'
import {childContext, contextualizeModel, modelItems, addContext } from './EditorModel.js'
import { Editor } from './GenericEditor.jsx'

export const ArrayEditor = React.createClass({
  render() {
    let {model, reverse} = this.props
    var items = (this.state && this.state.items) || modelItems(model)
    if (reverse && !model.context.edit) items = items.slice(0).reverse()
    let inline = ArrayEditor.canShowInline(this)
    let className = inline
      ? 'array inline'
      : 'array'
    let addItem = () => {
      var newItemModel = contextualizeModel(model.arrayPrototype, childContext(model.context, items.length ))
      model.context.changeBus.push([newItemModel.context, newItemModel.value])
      this.setState({items: items.concat([newItemModel])})
    }

    // Assign unique key values to item models to be able to track them in the face of additions and removals
    items.forEach((itemModel) => {
      if (!itemModel.arrayKey) {
        itemModel.arrayKey = ++this.arrayKeyCounter || (this.arrayKeyCounter=1)
      }
    })

    return (
      <ul ref="ul" className={className}>
        {
          items.map((item, i) => {
              let removeItem = () => {
                let newItems = items
                newItems.splice(i, 1)
                item.context.changeBus.push([item.context, {data: undefined}])
                this.setState({ items: newItems })
              }
              return (<li key={item.arrayKey}>
                <Editor model = {item}/>
                {item.context.edit && <a className="remove-item" onClick={removeItem}></a>}
              </li>)
            }
          )
        }
        {
          model.context.edit && model.arrayPrototype !== undefined ? <li className="add-item"><a onClick={addItem}>lisää uusi</a></li> : null
        }
      </ul>
    )
  },
  componentWillReceiveProps(newProps) {
    if (!newProps.model.context.edit && this.props.model.context.edit) {
      this.setState(this.getInitialState())
    }
  },
  getInitialState() {
    return { items: undefined}
  }
})
ArrayEditor.canShowInline = (component) => {
  let {model} = component.props
  var items = modelItems(model)
  // consider inlineability of first item here. make a stateless "fake component" because the actual React component isn't available to us here.
  let fakeComponent = {props: { model: items[0] }}
  return Editor.canShowInline(fakeComponent)
}