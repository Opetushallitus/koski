import React from 'react'
import R from 'ramda'
import { modelData, modelTitle, modelEmpty, modelItems } from './EditorModel.js'
import { opiskeluoikeusChange } from './Oppija.jsx'
import { formatISODate, parseFinnishDate } from './date.js'
import Http from './http'
import Bacon from 'baconjs'

export const Editor = React.createClass({
  render() {
    let { model, context, editorMapping } = this.props

    if (!context) {
      if (!editorMapping) throw new Error('editorMapping required for root editor')
      context = {
        root: true,
        path: '',
        prototypes: model.prototypes,
        editorMapping: R.merge(defaultEditorMapping, editorMapping),
        expandMyPath (c, expanded) {
          // expand/collapse this node
          c.setState({expanded})
          if (this.parentComponent && this.parentContext) {
            let delta = expanded ? 1 : -1
            this.parentContext.expandChild(this.parentComponent, delta)
          }
        },
        expandChild (c, delta) {
          // notification of a child component expanded/collapsed
          let previousCount = ((c.state && c.state.expandCount) || 0)
          let expandCount = previousCount + delta
          c.setState({expandCount})
          if (this.parentComponent && this.parentContext) {
            // if there's a parent, we'll bubble up the change
            let selfCount = (c.state && c.state.expanded ? 1 : 0)
            // this component + children together count as 1 expanded child for the parent component.
            let countForParent = (expandCount + selfCount > 0 ? 1 : 0)
            let previousCountForParent = (previousCount + selfCount > 0 ? 1 : 0)
            let deltaForParent = countForParent - previousCountForParent
            if (deltaForParent !== 0) {
              // only notify parent if there's an actual change.
              this.parentContext.expandChild(this.parentComponent, deltaForParent)
            }
          }
        },
        isExpanded(c) {
          return !this.forceInline && c.state && c.state.expanded
        },
        isChildExpanded(c) {
          return !this.forceInline && c.state && c.state.expandCount
        }
      }
    }
    return getModelEditor(model, context)
  }
})

export const ObjectEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let className = model.value
      ? 'object ' + model.value.class
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
        ? representativeEditor() // just show the representative property, no need for ExpandableEditor
        : isArrayItem(context) // for array item, show representative property in expanded view too
          ? (<span className={objectWrapperClass}>
              <span className="representative">{representativeEditor({ forceInline: true })}</span>
              <ExpandableEditor editor = {this} expandedView={objectEditor} defaultExpanded={context.edit} context={context}/>
            </span>)
          : (<span className={objectWrapperClass}>
              <ExpandableEditor editor = {this} expandedView={objectEditor} collapsedView={() => representativeEditor({ forceInline: true })} defaultExpandeded={context.edit} context={context}/>
            </span>)
  }
})
ObjectEditor.canShowInline = (component) => {
  var canInline = !!findRepresentative(component.props.model) && !component.props.context.edit && !isArrayItem(component.props.context) && !component.props.context.isChildExpanded(component) && !component.props.context.isExpanded(component)
  //console.log("Object inline", component.props.context.path, canInline)
  return canInline
}

export const ExpandableEditor = React.createClass({
  render() {
    let {editor, collapsedView, expandedView, context} = this.props
    var expanded = context.isExpanded(editor)
    let toggleExpanded = () => {
      expanded = !expanded
      context.expandMyPath(editor, expanded)
    }
    let className = expanded ? 'foldable expanded' : 'foldable collapsed'
    return (<span ref="foldable" className={className}>
      <a className="toggle-expand" onClick={toggleExpanded}>{ expanded ? '-' : '+' }</a>
      { expanded ? expandedView() : (collapsedView ? collapsedView() : null) }
    </span>)
  }
})
ExpandableEditor.canShowInline = () => true

export const PropertiesEditor = React.createClass({
  render() {
    let {properties, context} = this.props
    let edit = context.edit || (this.state && this.state.edit)
    let toggleEdit = () => this.setState({edit: !edit})
    let shouldShow = shouldShowProperty(edit)
    return (<ul className="properties">
      {
        context.editable && !context.edit ? <a className="toggle-edit" onClick={toggleEdit}>{edit ? 'valmis' : 'muokkaa'}</a> : null
      }
      {
        properties.filter(shouldShow).map(property => {
          let propertyClassName = 'property ' + property.key
          return (<li className={propertyClassName} key={property.key}>
            <label>{property.title}</label>
            <span className="value">{ getModelEditor(property.model, childContext(this, R.merge(context, {edit: edit}), property.key)) }</span>
          </li>)
        })
      }
    </ul>)
  }
})
PropertiesEditor.canShowInline = () => false

const shouldShowProperty = (edit) => (property) => (edit || !modelEmpty(property.model)) && !property.hidden

export const ArrayEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let items = modelItems(model)
    let inline = ArrayEditor.canShowInline(this)
    let wasInline = this.state && this.state.wasInline
    let className = inline
      ? 'array inline'
      : wasInline
        ? 'array inline-when-collapsed'
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
  },
  componentWillMount() {
    let inline = ArrayEditor.canShowInline(this)
    if (inline) {
      this.setState({ wasInline: true})
    }
  }
})
ArrayEditor.canShowInline = (component) => {
  let {model, context} = component.props
  var items = modelItems(model)
  // consider inlineability of first item here. make a stateless "fake component" because the actual React component isn't available to us here.
  let fakeComponent = {props: { model: items[0], context: childContext({}, context, 0) }}
  return items.length <= 1 && canShowInline(fakeComponent) && !context.isChildExpanded(component)
}

export const OptionalEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let adding = this.state && this.state.adding
    let add = () => this.setState({adding: true})
    return adding
      ? getModelEditor(model.prototype, context, true)
      : <a className="add-value" onClick={add}>lisää</a>
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
    this.state.valueBus.throttle(1000).onValue((v) => {opiskeluoikeusChange.push(v)})
  }
})
StringEditor.canShowInline = () => true

export const BooleanEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let onChange = event => {
      opiskeluoikeusChange.push([context, {data: event.target.checked}])
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
    this.state.valueBus.throttle(1000).onValue((v) => {opiskeluoikeusChange.push(v)})
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
      opiskeluoikeusChange.push([context, selected])
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
        this.state.alternativesP = Http.cachedGet(model.alternativesPath).toProperty()
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
  model = resolveModel(model, context)
  if (!model) return NullEditor
  if (modelEmpty(model) && model.optional && model.prototype !== undefined) {
    return OptionalEditor
  }
  let editor = (model.value && context.editorMapping[model.value.class]) || context.editorMapping[model.type]
  if (!editor) {
    if (!model.type) {
      console.log('Typeless model', model)
    }
    console.log('Missing editor ' + model.type)
    return NullEditor
  }
  return editor
}

const getModelEditor = (model, context) => {
  model = resolveModel(model, context)
  var ModelEditor = getEditorFunction(model, context)
  return <ModelEditor model={model} context={context} />
}

const defaultEditorMapping = {
  'object': ObjectEditor,
  'array': ArrayEditor,
  'string': StringEditor,
  'number': StringEditor,
  'date': DateEditor,
  'boolean': BooleanEditor,
  'enum': EnumEditor
}