import React from 'react'
import {modelLookup} from './EditorModel.ts'
import {PropertiesEditor, shouldShowProperty} from './PropertiesEditor'
import {modelErrorMessages, modelProperties, modelProperty, wrapOptional} from './EditorModel'
import {currentLocation, navigateWithQueryParams} from '../util/location'
import {parseBool} from '../util/util'
import Text from '../i18n/Text'
import {isEmpty} from 'ramda'
import * as Bacon from 'baconjs'

export const ExpandablePropertiesEditor = ({model, propertyName, propertyFilter = () => true}) => {
  const propertyModel = modelLookup(model, propertyName)
  const paramName = propertyName + '-expanded'
  const wrappedModel = model.context.edit ? wrapOptional(propertyModel) : propertyModel
  const zeroVisibleProperties = modelProperties(wrappedModel, shouldShowProperty(model.context)).filter(propertyFilter).length === 0

  return !zeroVisibleProperties || wrappedModel.context.edit
    ? <Expandable editor={<div className='value'><PropertiesEditor model={wrappedModel} propertyFilter={propertyFilter}/></div>}
                  expanded={parseBool(currentLocation().params[paramName])}
                  setExpanded={expand => setExpandedQueryParam(paramName, expand)}
                  propertyName={propertyName}
                  title={modelProperty(model, propertyName).title}
                  hasErrors={!isEmpty(modelErrorMessages(propertyModel, true))} />
    : null
}

export class Expandable extends React.Component {
  constructor() {
    super()
    this.expandBus = new Bacon.Bus()
  }

  render() {
    const {editor, expanded, setExpanded, propertyName, title} = this.props
    const toggleExpand = () => setExpanded(!expanded)
    return (<div className={'expandable-container ' + propertyName}>
      <a className={expanded ? 'open expandable' : 'expandable'} onClick={toggleExpand}><Text name={title}/></a>
      {expanded ? editor : null}
    </div>)
  }

  componentDidMount() {
    this.expandBus.onValue(() => this.props.setExpanded(true))
  }

  componentDidUpdate() {
    if (this.props.hasErrors) {
      this.expandBus.push()
    }
  }
}

const setExpandedQueryParam = (paramName, expand) =>
  navigateWithQueryParams({[paramName]: expand ? 'true' : undefined})
