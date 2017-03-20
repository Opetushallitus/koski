import React from 'react'
import { modelEmpty, modelItems, addContext } from './EditorModel.js'
import { Editor } from './GenericEditor.jsx'
import { ArrayEditor } from './ArrayEditor.jsx'

export const PropertiesEditor = React.createClass({
  render() {
    let defaultValueEditor = (prop, getDefault) => getDefault()
    let {properties, model, context, getValueEditor = defaultValueEditor, propertyFilter = () => true} = this.props
    if (!properties) {
      if (!model) throw new Error('model or properties required')
      properties = model.value.properties
    }
    if (!context) {
      if (!model) throw new Error('model or context required')
      context = model.context
    }
    let edit = context.edit
    let shouldShow = (property) => shouldShowProperty(edit)(property) && propertyFilter(property)

    let munch = (prefix) => (property, i) => { // TODO: just index passing is enough, no context needed
      if (!edit && property.flatten && property.model.value && property.model.value.properties) {
        return property.model.value.properties.filter(shouldShow).flatMap(munch(prefix + property.key + '.'))
      } else if (!edit && property.flatten && (property.model.type == 'array')) {
        return modelItems(property.model).flatMap((item, j) => {
          return item.value.properties.filter(shouldShow).flatMap(munch(prefix + j + '.'))
        })
      } else {
        let key = prefix + property.key
        let propertyClassName = 'property ' + property.key
        let valueEditor = property.tabular
          ? <TabularArrayEditor model={property.model} />
          : getValueEditor(property, () => <Editor model={property.editable ? property.model : addContext(property.model, { edit: false })}/> )

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
export const shouldShowProperty = (edit) => (property) => (edit || !modelEmpty(property.model)) && !property.hidden

export const TabularArrayEditor = React.createClass({
  render() {
    let {model} = this.props
    let items = modelItems(model)
    if (!items.length) return null
    if (model.context.edit) return <ArrayEditor {...this.props}/>
    let properties = items[0].value.properties
    return (<table className="tabular-array">
      <thead>
      <tr>{ properties.map((p, i) => <th key={i}>{p.title}</th>) }</tr>
      </thead>
      <tbody>
      {
        items.map((item, i) => {
          return (<tr key={i}>
            {
              item.value.properties.map((p, j) => {
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