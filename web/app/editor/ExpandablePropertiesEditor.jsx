import React from 'react'
import {modelData, modelLookup, contextualizeModel, modelEmpty} from './EditorModel.js'
import {PropertiesEditor} from './PropertiesEditor.jsx'

export const ExpandablePropertiesEditor = React.createClass({
  render() {
    let {model, propertyName} = this.props
    let edit = model.context.edit
    let {open, addingModel} = this.state
    // TODO: state needs to reset on context switch (tab switch), how to detect?
    // TODO: similar logic for pushing the newly created value should be included in OptionalEditor, ArrayEditor
    //console.log({edit, open, addingModel})
    let propertiesModel = addingModel || modelLookup(model, propertyName)

    return modelData(model, propertyName) || edit ?
      <div className={'expandable-container ' + propertyName}>
        <a className={open ? 'open expandable' : 'expandable'} onClick={this.toggleOpen}>{model.value.properties.find(p => p.key === propertyName).title}</a>
        { (open && propertiesModel.value) ? // can be "open" without value, when 1. edit, 2. doneWithEdit : still open, but new value from server still missing
          <div className="value">
            <PropertiesEditor model={propertiesModel} />
          </div> : null
        }
      </div> : null
  },
  toggleOpen() {
    let {model, propertyName} = this.props
    let edit = model.context.edit
    let propertiesModel = modelLookup(model, propertyName)
    if (edit && modelEmpty(propertiesModel) && !this.state.addingModel) {
      let addingModel = contextualizeModel(propertiesModel.optionalPrototype, propertiesModel.context)
      if (!addingModel.value.data) {
        throw new Error('Prototype value data missing')
      }
      model.context.changeBus.push([addingModel.context, addingModel.value])
      this.setState({addingModel})
    }
    this.setState({open: !this.state.open})
  },
  componentWillReceiveProps(newProps) {
    if (!newProps.model.context.edit && this.props.model.context.edit) { // TODO: this is a bit dirty and seems that it's needed in many editors
      this.setState({ addingModel: undefined})
    }
  },
  getInitialState() {
    return {open: false, addingModel: undefined}
  }
})
