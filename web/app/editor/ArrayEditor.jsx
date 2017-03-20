import React from 'react'
import {childContext, contextualizeModel, modelItems} from './EditorModel.js'
import {Editor} from './GenericEditor.jsx'

export const ArrayEditor = React.createClass({
  render() {
    let {model, reverse} = this.props
    var items = modelItems(model)
    if (reverse && !model.context.edit) items = items.slice(0).reverse()
    let inline = ArrayEditor.canShowInline(this)
    let className = inline
      ? 'array inline'
      : 'array'
    let addItem = () => {
      var newItemModel = contextualizeModel(model.arrayPrototype, childContext(model.context, items.length ))
      model.context.changeBus.push([newItemModel.context, newItemModel])
    }

    return (
      <ul ref="ul" className={className}>
        {
          items.map((item, i) => {
              let removeItem = () => {
                let newItems = items
                newItems.splice(i, 1)
                item.context.changeBus.push([item.context, undefined])
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
  }
})
ArrayEditor.canShowInline = (component) => {
  let {model} = component.props
  var items = modelItems(model)
  // consider inlineability of first item here. make a stateless "fake component" because the actual React component isn't available to us here.
  let fakeComponent = {props: { model: items[0] }}
  return Editor.canShowInline(fakeComponent)
}