import React from 'react'
import Bacon from 'baconjs'
import {modelData, modelTitle, modelItems} from './EditorModel.js'
import {formatISODate, parseFinnishDate} from '../date.js'
import {ObjectEditor} from './ObjectEditor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {Editor} from './GenericEditor.jsx'
import {ArrayEditor} from './ArrayEditor.jsx'
import {EnumEditor} from './EnumEditor.jsx'
import {StringEditor} from './StringEditor.jsx'
import {NumberEditor} from './NumberEditor.jsx'
import {LocalizedStringEditor} from './LocalizedStringEditor.jsx'

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


export const editorMapping = {
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
  'jakso': JaksoEditor
}