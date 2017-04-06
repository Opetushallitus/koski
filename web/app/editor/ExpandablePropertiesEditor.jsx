import React from 'react'
import {modelData, modelLookup} from './EditorModel.js'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {wrapOptional} from './OptionalEditor.jsx'

export const ExpandablePropertiesEditor = React.createClass({
  render() {
    let {model, propertyName} = this.props
    let {open} = this.state
    let propertyModel = modelLookup(model, propertyName)
    let edit = model.context.edit
    let wrappedModel = edit ? wrapOptional({model: propertyModel}) : propertyModel

    return modelData(model, propertyName) || wrappedModel.context.edit ?
      <div className={'expandable-container ' + propertyName}>
        <a className={open ? 'open expandable' : 'expandable'} onClick={this.toggleOpen}>{model.value.properties.find(p => p.key === propertyName).title}</a>
        { open || edit ?
          <div className="value">
            <PropertiesEditor model={wrappedModel} />
          </div> : null
        }
      </div> : null
  },
  toggleOpen() {
    this.setState({open: !this.state.open})
  },
  getInitialState() {
    return {open: false}
  }
})
