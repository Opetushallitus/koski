import React from 'react'
import {modelData, modelLookup, contextualizeModel, modelEmpty} from './EditorModel.js'
import {PropertiesEditor} from './PropertiesEditor.jsx'

// TODO: use wrapOptional for automatic creation/deletion

export const ExpandablePropertiesEditor = React.createClass({
  render() {
    let {model, propertyName} = this.props
    let {open} = this.state

    let propertiesModel = modelLookup(model, propertyName)

    return modelData(model, propertyName) || model.context.edit ?
      <div className={'expandable-container ' + propertyName}>
        <a className={open ? 'open expandable' : 'expandable'} onClick={this.toggleOpen}>{model.value.properties.find(p => p.key === propertyName).title}</a>
        { (open && propertiesModel.value) ?
          <div className="value">
            <PropertiesEditor model={propertiesModel} />
          </div> : null
        }
      </div> : null
  },
  toggleOpen() {
    let {model, propertyName} = this.props
    let propertiesModel = modelLookup(model, propertyName)

    if (model.context.edit && modelEmpty(propertiesModel)) {
      let addingModel = contextualizeModel(propertiesModel.optionalPrototype, propertiesModel.context)
      if (!modelData(addingModel)) {
        throw new Error('Prototype value data missing')
      }
      model.context.changeBus.push([addingModel.context, addingModel])
    }
    this.setState({open: !this.state.open})
  },
  componentWillReceiveProps(newProps) {
    if (newProps.model.context.edit && !this.props.model.context.edit && !modelEmpty(modelLookup(newProps.model, newProps.propertyName))) {
      this.setState({open: true})
    }
  },
  getInitialState() {
    return {open: false}
  }
})
