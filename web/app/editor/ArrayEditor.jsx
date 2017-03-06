import React from 'react'
import * as L from 'partial.lenses'
import {childContext, contextualizeModel, modelItems } from './EditorModel.js'
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
    let adding = this.state.adding
    let addItem = () => {
      this.setState({adding: adding.concat(contextualizeModel(model.prototype, childContext(model.context, items.length + adding.length)))})
    }
    return (
      <ul ref="ul" className={className}>
        {
          items.concat(adding).map((item, i) => {
              let removeItem = () => {
                let newItems = L.set(L.index(i), undefined, items)
                item.context.changeBus.push([item.context, {data: undefined}])
                this.setState({ adding: [], items: newItems })
              }

              return (<li key={i}>
                <Editor model = {item}/>
                {item.context.edit && <a className="remove-item" onClick={removeItem}></a>}
              </li>)
            }
          )
        }
        {
          model.context.edit && model.prototype !== undefined ? <li className="add-item"><a onClick={addItem}>lisää uusi</a></li> : null
        }
      </ul>
    )
  },
  componentWillReceiveProps(newProps) {
    if (!newProps.model.context.edit && this.props.model.context.edit) { // TODO: this is a bit dirty and seems that it's needed in many editors
      this.setState(this.getInitialState())
    }
  },
  getInitialState() {
    return { adding: [], items: undefined}
  }
})
ArrayEditor.canShowInline = (component) => {
  let {model} = component.props
  var items = modelItems(model)
  // consider inlineability of first item here. make a stateless "fake component" because the actual React component isn't available to us here.
  let fakeComponent = {props: { model: items[0] }}
  return Editor.canShowInline(fakeComponent)
}