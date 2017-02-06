import React from 'react'
import R from 'ramda'
import { modelData, modelTitle, modelEmpty, modelItems, modelLookup } from './EditorModel.js'
import { formatISODate, parseFinnishDate } from './date.js'
import Http from './http'
import Bacon from 'baconjs'
import { showInternalError } from './location.js'

export const Editor = React.createClass({
  render() {
    let { model, context, editorMapping, changeBus, path, parent } = this.props

    if (!context) {
      if (!editorMapping) throw new Error('editorMapping required for root editor')
      context = {
        changeBus: changeBus,
        root: true,
        path: '',
        prototypes: model.prototypes,
        editorMapping: R.merge(defaultEditorMapping, editorMapping)
      }
    }
    return getModelEditor(model, context, parent, path)
  }
})

export const ObjectEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let className = model.value
      ? 'object ' + model.value.classes.join(' ')
      : 'object empty'
    let representative = findRepresentative(model)
    let representativeEditor = (props) => getModelEditor(representative.model, R.merge(childContext(this, context, representative.key), props || {}))
    let objectEditor = () => <div className={className}><PropertiesEditor properties={model.value.properties}
                                                                          context={context}/></div>

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
  var canInline = !!findRepresentative(component.props.model) && !component.props.context.edit && !isArrayItem(component.props.context)
  //console.log("Object inline", component.props.context.path, canInline)
  return canInline
}

export const TogglableEditor = React.createClass({
  render() {
    let { context, renderChild } = this.props
    let edit = context.edit || (this.state && this.state.edit)
    let toggleEdit = () => this.setState({edit: !edit})
    let showToggleEdit = context.editable && !context.edit && !context.hasToggleEdit
    let childContext = R.merge(context, {
      edit: edit,
      hasToggleEdit: context.hasToggleEdit || showToggleEdit  // to prevent nested duplicate "edit" links
    })
    let editLink = showToggleEdit ? <a className="toggle-edit" onClick={toggleEdit}>{edit ? 'valmis' : 'muokkaa'}</a> : null

    return (renderChild(childContext, editLink))
  }
})

export const PropertiesEditor = React.createClass({
  render() {
    let defaultValueEditor = (prop, ctx, getDefault) => getDefault()
    let {properties, context, getValueEditor = defaultValueEditor} = this.props
    let edit = context.edit
    let shouldShow = shouldShowProperty(edit)

    let munch = (prefix) => (property, i) => {
      if (property.flatten) {
        return property.model.value.properties.filter(shouldShow).flatMap(munch(prefix + i + '.'))
      } else {
        let propertyClassName = 'property ' + property.key
        let propertyContext = childContext(this, context, property.key)
        let valueEditor = property.tabular
          ? <TabularArrayEditor model={property.model} context={propertyContext} />
          : getValueEditor(property, propertyContext, () => getModelEditor(property.model, propertyContext))

        return [(<tr className={propertyClassName} key={prefix + i}>
          {
            property.complexObject
              ? (<td className="complex" colSpan="2">
                  <div className="label">{property.title}</div>
                  <div className="value">{ valueEditor }</div>
                </td>)
              : [<td className="label" key="label">{property.title}</td>,
                  <td className="value" key="value">{ valueEditor }</td>
                ]
          }
        </tr>)]
      }
    }

    return (<div className="properties">
      <table><tbody>
      { properties.filter(shouldShow).flatMap(munch('')) }
      </tbody></table>
    </div>)
  }
})
PropertiesEditor.canShowInline = () => false

export const PropertyEditor = React.createClass({
  render() {
    let {propertyName, model, context} = this.props
    let property = model.value.properties.find(p => p.key == propertyName)
    let edit = context.edit || (this.state && this.state.edit)
    if (!property) return null
    return (<span className="single-property">
      {property.title}: { getModelEditor(property.model, childContext(this, R.merge(context, {edit: edit}), property.key)) }
    </span>)
  }
})

export const shouldShowProperty = (edit) => (property) => (edit || !modelEmpty(property.model)) && !property.hidden

export const TabularArrayEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let items = modelItems(model)
    if (!items.length) return null
    if (context.edit) return <ArrayEditor {...this.props}/>
    let properties = items[0].value.properties
    return (<table className="tabular-array">
      <thead>
        <tr>{ properties.map((p, i) => <th key={i}>{p.title}</th>) }</tr>
      </thead>
      <tbody>
        {
          items.map((item, i) => {
            return (<tr key={i}>
              {
                item.value.properties.map((p, j) => {
                  return (<td key={j}>
                    {getModelEditor(p.model, childContext(this, context, p.key))}
                  </td>)
                })
              }
            </tr>)
          })
        }
      </tbody>
    </table>)
  }
})
export const ArrayEditor = React.createClass({
  render() {
    let {model, context, reverse} = this.props
    var items = modelItems(model)
    if (reverse && !context.edit) items = items.reverse()
    let inline = ArrayEditor.canShowInline(this)
    let className = inline
      ? 'array inline'
      : 'array'
    let adding = this.state && this.state.adding || []
    let add = () => this.setState({adding: adding.concat(model.prototype)})
    return (
      <ul ref="ul" className={className}>
        {
          items.concat(adding).map((item, i) =>
            <li key={i}>{getModelEditor(item, R.merge(childContext(this, context, i), { arrayItems: items }) )}</li>
          )
        }
        {
          context.edit && model.prototype !== undefined ? <li className="add-item"><a onClick={add}>lisää uusi</a></li> : null
        }
      </ul>
    )
  }
})
ArrayEditor.canShowInline = (component) => {
  let {model, context} = component.props
  var items = modelItems(model)
  // consider inlineability of first item here. make a stateless "fake component" because the actual React component isn't available to us here.
  let fakeComponent = {props: { model: items[0], context: childContext({}, context, 0) }}
  return canShowInline(fakeComponent)
}

export const OptionalEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let adding = this.state && this.state.adding
    let add = () => this.setState({adding: true})
    return adding
      ? getModelEditor(model.prototype, context)
      : context.edit
        ? <a className="add-value" onClick={add}>lisää</a>
        : null
  }
})
OptionalEditor.canShowInline = () => true

export const StringEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let {valueBus} = this.state

    let onChange = (event) => {
      valueBus.push([context, {data: event.target.value}])
    }

    return context.edit
      ? <input type="text" defaultValue={modelData(model)} onChange={ onChange }></input>
      : <span className="inline string">{modelData(model)}</span>
  },

  getInitialState() {
    return {valueBus: Bacon.Bus()}
  },

  componentDidMount() {
    this.state.valueBus.throttle(1000).onValue((v) => {this.props.context.changeBus.push(v)})
  }
})
StringEditor.canShowInline = () => true

export const NumberEditor = React.createClass({
  render() {
    let { model } = this.props
    let data = modelData(model)
    let value = data
      ? Math.round(data * 100) / 100
      : data
    return <span className="inline number">{value}</span>
  }
})

export const BooleanEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let onChange = event => {
      context.changeBus.push([context, {data: event.target.checked}])
    }

    return context.edit
      ? <input type="checkbox" defaultChecked={modelData(model)} onChange={ onChange }></input>
      : <span className="inline string">{modelTitle(model)}</span>
  }
})
BooleanEditor.canShowInline = () => true

export const DateEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let {invalidDate, valueBus} = this.state

    let onChange = (event) => {
      var date = parseFinnishDate(event.target.value)
      if (date) {
        valueBus.push([context, {data: formatISODate(date)}])
      }
      this.setState({invalidDate: date ? false : true})
    }

    return context.edit
      ? <input type="text" defaultValue={modelTitle(model)} onChange={ onChange } className={invalidDate ? 'error' : ''}></input>
      : <span className="inline date">{modelTitle(model)}</span>
  },

  getInitialState() {
    return {valueBus: Bacon.Bus()}
  },

  componentDidMount() {
    this.state.valueBus.throttle(1000).onValue((v) => {this.props.context.changeBus.push(v)})
  }
})
DateEditor.canShowInline = () => true

export const EnumEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let alternatives = model.alternatives || (this.state.alternatives) || []
    let className = alternatives.length ? '' : 'loading'
    let onChange = (event) => {
      let selected = alternatives.find(alternative => alternative.value == event.target.value)
      context.changeBus.push([context, selected])
    }
    return context.edit
      ? (<select className={className} defaultValue={model.value && model.value.value} onChange={ onChange }>
      {
        alternatives.map( alternative =>
          <option value={ alternative.value } key={ alternative.value }>{alternative.title}</option>
        )
      }
    </select>)
      : <span className="inline enum">{modelTitle(model)}</span>
  },

  update(props) {
    let {model, context} = props
    if (context.edit && model.alternativesPath && !this.state.alternativesP) {
      this.state.alternativesP = EnumEditor.AlternativesCache[model.alternativesPath]
      if (!this.state.alternativesP) {
        this.state.alternativesP = Http.cachedGet(model.alternativesPath).doError(showInternalError)
        EnumEditor.AlternativesCache[model.alternativesPath] = this.state.alternativesP
      }
      this.state.alternativesP.onValue(alternatives => this.setState({alternatives}))
    }
  },

  componentWillMount() {
    this.update(this.props)
  },

  componentWillReceiveProps(props) {
    this.update(props)
  },

  getInitialState() {
    return {}
  }
})
EnumEditor.canShowInline = () => true
EnumEditor.AlternativesCache = {}

export const NullEditor = React.createClass({
  render() {
    return null
  }
})

export const childContext = (component, context, ...pathElems) => {
  let path = ((context.path && [context.path]) || []).concat(pathElems).join('.')
  return R.merge(context, { path, root: false, arrayItems: null, parentComponent: component, parentContext: context })
}

const findRepresentative = (model) => model.value.properties.find(property => property.representative)
const isArrayItem = (context) => context.arrayItems && context.arrayItems.length > 1
const canShowInline = (component) => (getEditorFunction(component.props.model, component.props.context).canShowInline || (() => false))(component)

const resolveModel = (model, context) => {
  if (model && model.type == 'prototype' && context.editable) {
    let prototypeModel = context.prototypes[model.key]
    model = model.optional
      ? R.merge(prototypeModel, { value: null, optional: true, prototype: model.prototype}) // Remove value from prototypal value of optional model, to show it as empty
      : prototypeModel
  }
  return model
}

const getEditorFunction = (model, context) => {
  let editorByClass = (classes) => {
    for (var i in classes) {
      if (context.editorMapping[classes[i]]) { return context.editorMapping[classes[i]] }
    }
  }
  model = resolveModel(model, context)
  if (!model) return NullEditor
  if (modelEmpty(model) && model.optional && model.prototype !== undefined) {
    return OptionalEditor
  }
  let editor = (model.value && editorByClass(model.value.classes)) || context.editorMapping[model.type]
  if (!editor) {
    if (!model.type) {
      console.log('Typeless model', model)
    } else {
      console.log('Missing editor ' + model.type)
    }
    return NullEditor
  }
  return editor
}

const getModelEditor = (model, context, parentComponent, path) => {
  model = resolveModel(model, context)
  if (parentComponent) {
    context = childContext(parentComponent, context, path)
    model = resolveModel(modelLookup(model, path), context)
  }
  var ModelEditor = getEditorFunction(model, context)
  return <ModelEditor model={model} context={context} />
}

const defaultEditorMapping = {
  'object': ObjectEditor,
  'array': ArrayEditor,
  'string': StringEditor,
  'number': NumberEditor,
  'date': DateEditor,
  'boolean': BooleanEditor,
  'enum': EnumEditor
}