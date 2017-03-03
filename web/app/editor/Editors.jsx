import React from 'react'
import R from 'ramda'
import Bacon from 'baconjs'
import BaconComponent from '../BaconComponent'
import Http from '../http'
import { modelData, modelTitle, modelItems, modelLookup, contextualizeModel } from './EditorModel.js'
import { formatISODate, parseFinnishDate } from '../date.js'
import { showInternalError } from '../location.js'
import { ObjectEditor } from './ObjectEditor.jsx'
import { PropertiesEditor } from './PropertiesEditor.jsx'
import { Editor } from './GenericEditor.jsx'
import { ArrayEditor } from './ArrayEditor.jsx'
import { OppijaEditor } from './OppijaEditor.jsx'
import * as Ammatillinen from './Ammatillinen.jsx'
import * as Perusopetus from './Perusopetus.jsx'

const OpiskeluoikeusjaksoEditor = React.createClass({
  render() {
    let { model } = this.props
    return (<div className="opiskeluoikeusjakso">
      <label className="date">{modelTitle(model, 'alku')}</label>
      <label className="tila">{modelTitle(model, 'tila')}</label>
    </div>)
  }
})
OpiskeluoikeusjaksoEditor.readOnly = true

export const LaajuusEditor = React.createClass({
  render() {
    let { model } = this.props
    var yksikköData = modelData(model, 'yksikkö')
    let yksikkö = yksikköData && (yksikköData.lyhytNimi || yksikköData.nimi).fi
    return (modelData(model, 'arvo'))
      ? <span className="property laajuus"><span className="value"><Editor model={model} path="arvo"/></span> <span className={'yksikko ' + yksikkö.toLowerCase()}>{yksikkö}</span></span>
      : <span>-</span>
  }
})
LaajuusEditor.readOnly = true

export const VahvistusEditor = React.createClass({
  render() {
    let { model } = this.props
    return (<span className="vahvistus inline">
      <span className="date">{modelTitle(model, 'päivä')}</span>&nbsp;
      <span className="allekirjoitus">{modelTitle(model, 'paikkakunta')}</span>&nbsp;
      {
        (modelItems(model, 'myöntäjäHenkilöt') || []).map( (henkilö,i) =>
          <span key={i} className="nimi">{modelData(henkilö, 'nimi')}</span>
        )
      }
    </span>)
  }
})

export const KoulutusmoduuliEditor = React.createClass({
  render() {
    let { model } = this.props
    return (<span className="koulutusmoduuli">
      <span className="tunniste">{modelTitle(model, 'tunniste')}</span>
      <span className="diaarinumero">{modelTitle(model, 'perusteenDiaarinumero')}</span>
      <PropertiesEditor model={model} propertyFilter={p => !['tunniste', 'perusteenDiaarinumero', 'pakollinen'].includes(p.key)} />
    </span>)
  }
})
KoulutusmoduuliEditor.readOnly = true

export const PäivämääräväliEditor = React.createClass({
  render() {
    let { model } = this.props
    return (<span>
      <Editor model={model} path="alku"/> — <Editor model={model} path="loppu"/>
    </span>)
  }
})
PäivämääräväliEditor.canShowInline = () => true
PäivämääräväliEditor.readOnly = true

export const JaksoEditor = React.createClass({
  render() {
    let {model} = this.props
    return (
      <div className="jaksollinen">
        <PäivämääräväliEditor model={model}/>
        <PropertiesEditor model={model} propertyFilter={p => !['alku', 'loppu'].includes(p.key)} />
      </div>
    )
  }
})

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


export const PropertyEditor = React.createClass({
  render() {
    let {propertyName, model} = this.props
    let property = model.value.properties.find(p => p.key == propertyName)
    if (!property) return null
    return (<span className={'single-property property ' + property.key}>
      <span className="label">{property.title}</span>: <span className="value"><Editor model = {property.model}/></span>
    </span>)
  }
})

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

export const LocalizedStringEditor = React.createClass({
  render() {
    let {model} = this.props
    if (!model.edit) {
      return <ObjectEditor model={model}/>
    }
    return <StringEditor model={ modelLookup(model, 'fi') } />
  }
})
LocalizedStringEditor.canShowInline = () => true

export const NumberEditor = React.createClass({
  render() {
    let {model} = this.props
    let onChange = (event) => model.context.changeBus.push([model.context, {data: parseFloat(event.target.value)}])

    let data = modelData(model)
    let value = data
      ? Math.round(data * 100) / 100
      : data

    return model.context.edit
      ? <input type="text" defaultValue={modelData(model)} onChange={ onChange } className="inline number"></input>
      : <span className="inline number">{value}</span>
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

export const editorMapping = R.mergeAll([{
  'object': ObjectEditor,
  'array': ArrayEditor,
  'string': StringEditor,
  'localizedstring': LocalizedStringEditor,
  'number': NumberEditor,
  'date': DateEditor,
  'boolean': BooleanEditor,
  'enum': EnumEditor,
  'vahvistus': VahvistusEditor,
  'laajuus' : LaajuusEditor,
  'koulutus' : KoulutusmoduuliEditor,
  'preibkoulutusmoduuli': KoulutusmoduuliEditor,
  'paatosjakso': PäivämääräväliEditor,
  'jakso': JaksoEditor,
  'oppijaeditorview': OppijaEditor,
  'opiskeluoikeusjakso': OpiskeluoikeusjaksoEditor
}, Ammatillinen.editorMapping, Perusopetus.editorMapping])