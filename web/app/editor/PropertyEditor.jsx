import React from 'react'
import { Editor } from './Editor.jsx'
import {findModelProperty} from './EditorModel'
import Text from '../Text.jsx'

export class PropertyEditor extends React.Component {
  render() {
    let {propertyName, model, ...rest} = this.props
    let property = findModelProperty(model, p => p.key === propertyName)
    if (!property) return null
    return (<span className={'single-property property ' + property.key}>
      <span className="label"><Text name={property.title}/></span>{': '}<span className="value"><Editor {...{model: property.model, ...rest}}/></span>
    </span>)
  }
}
