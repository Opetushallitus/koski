import React from 'react'
import { PäivämääräväliEditor } from './PaivamaaravaliEditor'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { wrapOptional, recursivelyEmpty } from '../editor/EditorModel'

export class JaksoEditor extends React.Component {
  render() {
    const { model, className } = this.props
    const wrappedModel = wrapOptional(model)
    return (
      <div className="jaksollinen">
        <PäivämääräväliEditor model={wrappedModel} />
        <PropertiesEditor
          model={wrappedModel}
          propertyFilter={(p) => !['alku', 'loppu'].includes(p.key)}
          className={className}
        />
      </div>
    )
  }
}

export const InlineJaksoEditor = ({ model }) => (
  <JaksoEditor model={model} className="inline" />
)
InlineJaksoEditor.validateModel = JaksoEditor.validateModel

JaksoEditor.validateModel = PäivämääräväliEditor.validateModel
JaksoEditor.isEmpty = recursivelyEmpty
