import React from 'react'
import { Editor } from './Editor'
import { findModelProperty } from './EditorModel'
import Text from '../i18n/Text'

export class PropertyEditor extends React.Component {
  render() {
    const { propertyName, model, ...rest } = this.props
    const property = findModelProperty(model, (p) => p.key === propertyName)
    if (!property) return null
    return (
      <span className={'single-property property ' + property.key}>
        <span className="label">
          <Text name={property.title} />
        </span>
        {': '}
        <span className="value">
          <Editor {...{ model: property.model, ...rest }} />
        </span>
      </span>
    )
  }
}
