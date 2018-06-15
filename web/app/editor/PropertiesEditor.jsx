import React from 'react'
import {addContext, modelEmpty, modelItems} from './EditorModel.js'
import {Editor} from './Editor'
import {ArrayEditor} from './ArrayEditor'
import {checkOnlyWhen, modelProperties} from './EditorModel'
import Text from '../i18n/Text'
import {buildClassNames} from '../components/classnames'
import {flatMapArray} from '../util/util'

export class PropertiesEditor extends React.Component {
  render() {
    let defaultValueEditor = (prop, getDefault) => getDefault()
    let {properties, model, context, getValueEditor = defaultValueEditor, propertyFilter = () => true, propertyEditable = p => p.editable || context.editAll, className} = this.props
    if (!properties) {
      if (!model) throw new Error('model or properties required')
      properties = modelProperties(model)
    }
    if (!context) {
      if (!model) throw new Error('model or context required')
      context = model.context
    }
    let edit = context.edit
    let contextForProperty = (property) => !propertyEditable(property) && context.edit ? { ...context, edit: false } : context
    let shouldShow = (property) => shouldShowProperty(contextForProperty(property))(property) && propertyFilter(property)

    let munch = (prefix) => (property, i) => {
      if (property.flatten && property.model.value && property.model.value.properties) {
        return flatMapArray(modelProperties(property.model, shouldShow), munch(prefix + property.key + '.'))
      } else if (!edit && property.flatten && (property.model.type === 'array')) {
        return flatMapArray(modelItems(property.model), (item, j) => {
          return flatMapArray(modelProperties(item, shouldShow), munch(prefix + j + '.'))
        })
      } else {
        let key = prefix + property.key + i
        let propertyClassName = 'property ' + property.key
        let valueClass = modelEmpty(property.model) ? 'value empty' : 'value'
        let valueEditor = property.tabular
          ? <TabularArrayEditor model={property.model} />
          : getValueEditor(property, () => <Editor model={propertyEditable(property) ? property.model : addContext(property.model, { edit: false })}/> )

        return [(<tr className={propertyClassName} key={key}>
          {
            property.complexObject
              ? (<td className="complex" colSpan="2">
              <div className="label"><PropertyTitle property={property}/></div>
              <div className={valueClass}>{ valueEditor }</div>
            </td>)
              : [<td className="label" key="label"><PropertyTitle property={property}/></td>,
              <td className={valueClass} key="value">{ valueEditor }</td>
            ]
          }
        </tr>)]
      }
    }

    const tableContents = flatMapArray(properties.filter(shouldShow), munch(''))
    return (tableContents && tableContents.length > 0 ? <div className={ buildClassNames(['properties', className]) }>
      <table><tbody>
      { tableContents }
      </tbody></table>
    </div> : null)
  }
}
PropertiesEditor.canShowInline = () => false

export const PropertyTitle = ({property}) => {
  let description = property.description && property.description.join(' ')
  if (description) {
    return <Text name={property.title} title={description} className="with-description"/>
  } else {
    return <Text name={property.title}/>
  }
}

export const shouldShowProperty = (context) => (property) => {
  if (!context.edit && modelEmpty(property.model)) return false
  if (property.hidden) return false
  if (property.onlyWhen && !checkOnlyWhen(property.owner, property.onlyWhen)) return false
  return true
}

export class TabularArrayEditor extends React.Component {
  render() {
    let {model} = this.props
    let items = modelItems(model)
    if (!items.length) return null
    if (model.context.edit) return <ArrayEditor {...this.props}/>
    let properties = modelProperties(items[0])
    return (<table className="tabular-array">
      <thead>
      <tr>{ properties.map((p, i) => <th key={i}><PropertyTitle property={p}/></th>) }</tr>
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
}
