import React from 'react'
import {modelLookup} from './EditorModel.js'
import {PropertiesEditor, shouldShowProperty} from './PropertiesEditor'
import {modelErrorMessages, modelProperties, modelProperty, wrapOptional} from './EditorModel'
import {currentLocation, navigateWithQueryParams} from '../util/location'
import {parseBool} from '../util/util'
import Text from '../i18n/Text'
import {isEmpty} from 'ramda'
import * as Bacon from 'baconjs'

export class ExpandablePropertiesEditor extends React.Component {
  constructor() {
    super()
    this.expandBus = new Bacon.Bus()
  }

  render() {
    const {model, propertyName, propertyFilter = () => true} = this.props
    let propertyModel = modelLookup(model, propertyName)
    let edit = model.context.edit
    let paramName = propertyName + '-expanded'
    let expanded = parseBool(currentLocation().params[paramName])
    let wrappedModel = edit ? wrapOptional(propertyModel) : propertyModel
    let zeroVisibleProperties = modelProperties(wrappedModel, shouldShowProperty(model.context)).filter(propertyFilter).length === 0

    return !zeroVisibleProperties || wrappedModel.context.edit ?
      <div className={'expandable-container ' + propertyName}>
        <a className={expanded ? 'open expandable' : 'expandable'} onClick={() => setExpanded(paramName, !expanded)}><Text name={modelProperty(model, propertyName).title}/></a>
        { expanded ?
          <div className="value">
            <PropertiesEditor model={wrappedModel} propertyFilter={propertyFilter}/>
          </div> : null
        }
      </div> : null
  }

  componentDidMount() {
    this.expandBus.onValue(() => setExpanded(this.props.propertyName + '-expanded', true))
  }

  componentDidUpdate() {
    const {model, propertyName} = this.props
    const hasErrors = !isEmpty(modelErrorMessages(modelLookup(model, propertyName), true))
    if (hasErrors) {
      this.expandBus.push()
    }
  }
}

const setExpanded = (paramName, expand) =>
  navigateWithQueryParams({[paramName]: expand ? 'true' : undefined})
