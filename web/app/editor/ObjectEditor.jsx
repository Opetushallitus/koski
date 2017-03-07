import React from 'react'
import { Editor } from './GenericEditor.jsx'
import { PropertiesEditor, shouldShowProperty } from './PropertiesEditor.jsx'

export const ObjectEditor = React.createClass({
  render() {
    let {model} = this.props
    let context = model.context
    let className = model.value
      ? 'object ' + model.value.classes.join(' ')
      : 'object empty'
    let representative = findRepresentative(model)
    let representativeEditor = () => <Editor model={representative.model}/>
    let objectEditor = () => <div className={className}><PropertiesEditor model={model}/></div>

    let exactlyOneVisibleProperty = model.value.properties.filter(shouldShowProperty(context.edit)).length == 1
    let isInline = ObjectEditor.canShowInline(this)
    let objectWrapperClass = 'foldable-wrapper with-representative' + (isInline ? ' inline' : '')

    return !representative
      ? objectEditor()
      : ((exactlyOneVisibleProperty || context.forceInline) && !context.edit)
        ? representativeEditor() // just show the representative property, as it is the only one
        : isArrayItem(context) // for array item always show representative property
          ? (<span className={objectWrapperClass}>
              <span className="representative">{representativeEditor({ forceInline: true })}</span>
              {objectEditor()}
             </span>)
          : (<span className={objectWrapperClass}>
              {objectEditor()}
             </span>)
  }
})
ObjectEditor.canShowInline = (component) => {
  var canInline = !!findRepresentative(component.props.model) && !component.props.model.context.edit && !isArrayItem(component.props.model.context)
  //console.log("Object inline", component.props.model.context.path, canInline)
  return canInline
}

const findRepresentative = (model) => model.value.properties.find(property => property.representative)
const isArrayItem = (context) => context.arrayItems && context.arrayItems.length > 1 // TODO: looks suspicious
