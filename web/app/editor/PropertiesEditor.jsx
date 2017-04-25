import React from 'react'
import { modelEmpty, modelItems, addContext } from './EditorModel.js'
import { Editor } from './Editor.jsx'
import { ArrayEditor } from './ArrayEditor.jsx'
import {modelProperties} from './EditorModel'

export const PropertiesEditor = React.createClass({
  render() {
    let defaultValueEditor = (prop, getDefault) => getDefault()
    let {properties, model, context, getValueEditor = defaultValueEditor, propertyFilter = () => true} = this.props
    if (!properties) {
      if (!model) throw new Error('model or properties required')
      properties = modelProperties(model)
    }
    if (!context) {
      if (!model) throw new Error('model or context required')
      context = model.context
    }
    let edit = context.edit
    let shouldShow = (property) => shouldShowProperty(context)(property) && propertyFilter(property)

    let munch = (prefix) => (property, i) => { // TODO: just index passing is enough, no context needed
      if (property.flatten && property.model.value && property.model.value.properties) {
        return modelProperties(property.model, shouldShow).flatMap(munch(prefix + property.key + '.'))
      } else if (!edit && property.flatten && (property.model.type === 'array')) {
        return modelItems(property.model).flatMap((item, j) => {
          return modelProperties(item, shouldShow).flatMap(munch(prefix + j + '.'))
        })
      } else {
        let key = prefix + property.key + i
        let propertyClassName = 'property ' + property.key
        let valueEditor = property.tabular
          ? <TabularArrayEditor model={property.model} />
          : getValueEditor(property, () => <Editor model={(property.editable || context.editAll ) ? property.model : addContext(property.model, { edit: false })}/> )

        return [(<tr className={propertyClassName} key={key}>
          {
            property.complexObject
              ? (<td className="complex" colSpan="2">
              <div className="label">{property.title}</div>
              <div className="value">{ valueEditor }</div>
            </td>)
              : [<td className="label" key="label">{property.title}</td>,
              <td className="value" key="value">{ valueEditor }</td>
            ]
          }
        </tr>)]
      }
    }

    return (<div className="properties">
      <table><tbody>
      { properties.filter(shouldShow).flatMap(munch('')) }
      </tbody></table>
    </div>)
  }
})
PropertiesEditor.canShowInline = () => false

export const shouldShowProperty = (context) => (property) => {
  if (!context.edit && modelEmpty(property.model)) return false
  if (property.hidden) return false
  return true
}

export const TabularArrayEditor = React.createClass({
  render() {
    let {model} = this.props
    let items = modelItems(model)
    if (!items.length) return null
    if (model.context.edit) return <ArrayEditor {...this.props}/>
    let properties = modelProperties(items[0])
    return (<table className="tabular-array">
      <thead>
      <tr>{ properties.map((p, i) => <th key={i}>{p.title}</th>) }</tr>
      </thead>
      <tbody>
      {
        items.map((item, i) => {
          return (<tr key={i}>
            {
              modelProperties(item).map((p, j) => {
                return (<td key={j}><Editor model = {p.model}/></td>)
              })
            }
          </tr>)
        })
      }
      </tbody>
    </table>)
  }
})