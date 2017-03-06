import React from 'react'
import {ObjectEditor} from './ObjectEditor.jsx'
import {StringEditor} from './StringEditor.jsx'
import {modelLookup} from './EditorModel.js'

export const LocalizedStringEditor = React.createClass({
  render() {
    let {model} = this.props
    if (!model.context.edit) {
      return <ObjectEditor model={model}/>
    }
    return <StringEditor model={ modelLookup(model, 'fi') } />
  }
})
LocalizedStringEditor.canShowInline = () => true
