import React from 'react'
import R from 'ramda'
import Bacon from 'baconjs'
import * as L from 'partial.lenses'
import BaconComponent from './BaconComponent'
import Http from './http'
import { modelData, modelTitle, modelEmpty, modelItems, modelLookup, contextualizeModel, addContext } from './EditorModel.js'
import { formatISODate, parseFinnishDate } from './date.js'
import { showInternalError } from './location.js'

export const Editor = React.createClass({
  render() {
    let { model, editorMapping, changeBus, doneEditingBus, path } = this.props
    if (!model.context) {
      if (!editorMapping) throw new Error('editorMapping required for root editor')
      model = contextualizeModel(model, {
        changeBus, doneEditingBus,
        root: true,
        rootModel: model,
        path: '',
        prototypes: model.prototypes,
        editorMapping: R.merge(defaultEditorMapping, editorMapping)
      })
    }
    return getModelEditor(model, path)
  }
})
Editor.propTypes = {
  model: React.PropTypes.object.isRequired
}

export const ObjectEditor = React.createClass({
  render() {
    let {model} = this.props
    let context = model.context
    let className = model.value
      ? 'object ' + model.value.classes.join(' ')
      : 'object empty'
    let representative = findRepresentative(model)
    let representativeEditor = () => getModelEditor(representative.model)
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

export const TogglableEditor = React.createClass({
  render() {
    let { model, renderChild } = this.props
    let context = model.context
    let edit = context.edit || (this.state && this.state.edit)
    let toggleEdit = () => {
      if (edit) {
        context.doneEditingBus.push()
      }
      this.setState({edit: !edit})
    }
    let showToggleEdit = model.editable && !context.edit && !context.hasToggleEdit
    let modifiedContext = R.merge(context, {
      edit: edit,
      hasToggleEdit: context.hasToggleEdit || showToggleEdit  // to prevent nested duplicate "edit" links
    })
    let editLink = showToggleEdit ? <a className={edit ? 'toggle-edit editing' : 'toggle-edit'} onClick={toggleEdit}>{edit ? 'valmis' : 'muokkaa'}</a> : null

    return (renderChild(contextualizeModel(model, modifiedContext), editLink))
  }
})

export const PropertiesEditor = React.createClass({
  render() {
    let defaultValueEditor = (prop, getDefault) => getDefault()
    let {properties, model, context, getValueEditor = defaultValueEditor, propertyFilter = () => true} = this.props
    if (!properties) {
      if (!model) throw new Error('model or properties required')
      properties = model.value.properties
    }
    if (!context) {
      if (!model) throw new Error('model or context required')
      context = model.context
    }
    let edit = context.edit
    let shouldShow = (property) => shouldShowProperty(edit)(property) && propertyFilter(property)

    let munch = (prefix) => (property, i) => { // TODO: just index passing is enough, no context needed
      if (!edit && property.flatten && property.model.value && property.model.value.properties) {
        return property.model.value.properties.filter(shouldShow).flatMap(munch(prefix + property.key + '.'))
      } else if (!edit && property.flatten && (property.model.type == 'array')) {
        return modelItems(property.model).flatMap((item, j) => {
          return item.value.properties.filter(shouldShow).flatMap(munch(prefix + j + '.'))
        })
      } else {
        let propertyClassName = 'property ' + property.key
        let valueEditor = property.tabular
          ? <TabularArrayEditor model={property.model} />
          : getValueEditor(property, () => getModelEditor(property.editable ? property.model : addContext(property.model, { edit: false })))


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
    let {propertyName, model} = this.props
    let property = model.value.properties.find(p => p.key == propertyName)
    if (!property) return null
    return (<span className={'single-property property ' + property.key}>
      <span className="label">{property.title}</span>: <span className="value">{ getModelEditor(property.model) }</span>
    </span>)
  }
})

export const shouldShowProperty = (edit) => (property) => (edit || !modelEmpty(property.model)) && !property.hidden

export const TabularArrayEditor = React.createClass({
  render() {
    let {model} = this.props
    let items = modelItems(model)
    if (!items.length) return null
    if (model.context.edit) return <ArrayEditor {...this.props}/>
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
                    {getModelEditor(p.model)}
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
    let {model, reverse} = this.props
    var items = (this.state && this.state.items) || modelItems(model)
    if (reverse && !model.context.edit) items = items.slice(0).reverse()
    let inline = ArrayEditor.canShowInline(this)
    let className = inline
      ? 'array inline'
      : 'array'
    let adding = this.state && this.state.adding || []
    let addItem = () => {
      this.setState({adding: adding.concat(model.prototype)})
    }
    return (
      <ul ref="ul" className={className}>
        {
          items.concat(adding).map((item, i) => {
            let removeItem = () => {
              let newItems = L.set(L.index(i), undefined, items)
              item.context.changeBus.push([item.context, {data: undefined}])
              this.setState({ adding: false, items: newItems })
            }

            return (<li key={i}>
              {getModelEditor(item)}
              {item.context.edit && <a className="remove-item" onClick={removeItem}></a>}
            </li>)
          }
          )
        }
        {
          model.context.edit && model.prototype !== undefined ? <li className="add-item"><a onClick={addItem}>lisää uusi</a></li> : null
        }
      </ul>
    )
  }
})
ArrayEditor.canShowInline = (component) => {
  let {model} = component.props
  var items = modelItems(model)
  // consider inlineability of first item here. make a stateless "fake component" because the actual React component isn't available to us here.
  let fakeComponent = {props: { model: items[0] }}
  return canShowInline(fakeComponent)
}

const OptionalEditor = React.createClass({
  render() {
    let {model} = this.props
    let adding = this.state && this.state.adding
    let removed = this.state && this.state.removed
    let addValue = () => {
      this.setState({adding: true})
    }
    let removeValue = () => {
      this.props.model.context.changeBus.push([model.context, {data: undefined}])
      this.setState({ removed: true, adding: false })
    }
    let empty = (modelEmpty(model) || removed)
    let canRemove = model.context.edit && !empty && !adding
    return (<span className="optional-wrapper">
      {
        empty
          ? adding
            ? getModelEditor(R.merge(contextualizeModel(model.prototype, model.context), { optional: false })) // get value from prototype when adding item
            : model.context.edit && model.prototype !== undefined
              ? <a className="add-value" onClick={addValue}>lisää</a>
              : null
          : getModelEditor(R.merge(model, { optional: false }))
      }
      {
        canRemove && <a className="remove-value" onClick={removeValue}></a>
      }
    </span>)
  }
})
OptionalEditor.canShowInline = () => true

export const StringEditor = React.createClass({
  render() {
    let {model} = this.props
    let {valueBus} = this.state

    let onChange = (event) => {
      valueBus.push([model.context, {data: event.target.value}])
    }

    return model.context.edit
      ? <input type="text" defaultValue={modelData(model)} onChange={ onChange }></input>
      : <span className="inline string">{modelData(model).split('\n').map((line, k) => <span key={k}>{line}<br/></span>)}</span>
  },

  getInitialState() {
    return {valueBus: Bacon.Bus()}
  },

  componentDidMount() {
    this.state.valueBus.throttle(1000).onValue((v) => {this.props.model.context.changeBus.push(v)})
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
    let {model} = this.props
    let onChange = event => {
      model.context.changeBus.push([model.context, {data: event.target.checked}])
    }

    return model.context.edit
      ? <input type="checkbox" defaultChecked={modelData(model)} onChange={ onChange }></input>
      : <span className="inline string">{modelTitle(model)}</span>
  }
})
BooleanEditor.canShowInline = () => true

export const DateEditor = React.createClass({
  render() {
    let {model} = this.props
    let {invalidDate, valueBus} = this.state

    let onChange = (event) => {
      var date = parseFinnishDate(event.target.value)
      if (date) {
        valueBus.push([model.context, {data: formatISODate(date)}])
      }
      this.setState({invalidDate: date ? false : true})
    }

    return model.context.edit
      ? <input type="text" defaultValue={modelTitle(model)} onChange={ onChange } className={invalidDate ? 'error' : ''}></input>
      : <span className="inline date">{modelTitle(model)}</span>
  },

  getInitialState() {
    return {valueBus: Bacon.Bus()}
  },

  componentDidMount() {
    this.state.valueBus.throttle(1000).onValue((v) => {this.props.model.context.changeBus.push(v)})
  }
})
DateEditor.canShowInline = () => true

export const EnumEditor = BaconComponent({
  render() {
    let {model} = this.props
    let alternatives = model.alternatives || (this.state.alternatives) || []
    let className = alternatives.length ? '' : 'loading'
    let onChange = (event) => {
      let selected = alternatives.find(alternative => alternative.value == event.target.value)
      model.context.changeBus.push([model.context, selected])
    }
    return model.context.edit
      ? (<select className={className} defaultValue={model.value && model.value.value} onChange={ onChange }>
      {
        alternatives.map( alternative =>
          <option value={ alternative.value } key={ alternative.value }>{alternative.title}</option>
        )
      }
    </select>)
      : <span className="inline enum">{modelTitle(model)}</span>
  },

  componentWillMount() {
    this.propsE.map((props) => [props.model.alternativesPath, props.model.context.edit]).skipDuplicates(R.equals).onValues((alternativesPath, edit) => {
      if (edit && alternativesPath) {
        let alternativesP = EnumEditor.AlternativesCache[alternativesPath]
        if (!alternativesP) {
          alternativesP = Http.cachedGet(alternativesPath).doError(showInternalError)
          EnumEditor.AlternativesCache[alternativesPath] = alternativesP
        }
        alternativesP.takeUntil(this.unmountE).onValue(alternatives => this.setState({alternatives}))
      }
    })
  },

  getInitialState() {
    return { alternatives: [] }
  }
})
EnumEditor.canShowInline = () => true
EnumEditor.AlternativesCache = {}

export const NullEditor = React.createClass({
  render() {
    return null
  }
})

const findRepresentative = (model) => model.value.properties.find(property => property.representative)
const isArrayItem = (context) => context.arrayItems && context.arrayItems.length > 1 // TODO: looks suspicious
const canShowInline = (component) => (getEditorFunction(component.props.model).canShowInline || (() => false))(component)

const getEditorFunction = (model) => {
  let editorByClass = (classes) => {
    for (var i in classes) {
      var editor = model.context.editorMapping[classes[i]]
      if (editor && (!model.context.edit || !editor.readOnly)) { return editor }
    }
  }
  if (!model) return NullEditor
  if (model.optional) {
    return OptionalEditor
  }
  let editor = (model.value && editorByClass(model.value.classes)) || model.context.editorMapping[model.type]
  if (!editor) {
    if (!model.type) {
      console.error('Typeless model', model)
    } else {
      console.error('Missing editor for type "' + model.type + '"')
    }
    return NullEditor
  }
  return editor
}

const getModelEditor = (model, path) => {
  if (path) {
    return getModelEditor(modelLookup(model, path))
  }
  if (model && !model.context) {
    console.error('Context missing from model', model)
  }
  var ModelEditor = getEditorFunction(model)
  return <ModelEditor model={model}/>
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