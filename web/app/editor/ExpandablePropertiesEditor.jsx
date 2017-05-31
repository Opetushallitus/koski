import React from 'react'
import {modelData, modelLookup} from './EditorModel.js'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {modelProperty} from './EditorModel'
import {navigateWithQueryParams, currentLocation} from '../location'
import {parseBool} from '../util'
import Text from '../Text.jsx'

export const ExpandablePropertiesEditor = ({model, propertyName}) => {
  let propertyModel = modelLookup(model, propertyName)
  let edit = model.context.edit
  let paramName = propertyName + '-expanded'
  let expanded = parseBool(currentLocation().params[paramName])
  let wrappedModel = edit ? wrapOptional({model: propertyModel}) : propertyModel
  let toggleOpen = () => {
    navigateWithQueryParams({[paramName]: !expanded ? 'true' : undefined})
  }

  return modelData(model, propertyName) || wrappedModel.context.edit ?
    <div className={'expandable-container ' + propertyName}>
      <a className={expanded ? 'open expandable' : 'expandable'} onClick={toggleOpen}><Text name={modelProperty(model, propertyName).title}/></a>
      { expanded ?
        <div className="value">
          <PropertiesEditor model={wrappedModel} />
        </div> : null
      }
    </div> : null
}