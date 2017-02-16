import React from 'react'
import { modelData, modelLookup, modelTitle, modelItems } from './EditorModel.js'
import * as GenericEditor from './GenericEditor.jsx'

export const LaajuusEditor = React.createClass({
  render() {
    let { model, context } = this.props
    var yksikköData = modelData(model, 'yksikkö')
    let yksikkö = yksikköData && (yksikköData.lyhytNimi || yksikköData.nimi).fi
    return (modelData(model, 'arvo'))
      ? <span><GenericEditor.Editor model={model} context={context} path="arvo" parent={this}/> <span className={'yksikko ' + yksikkö.toLowerCase()}>{yksikkö}</span></span>
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
VahvistusEditor.readOnly = true

export const KoulutusmoduuliEditor = React.createClass({
  render() {
    let { model, context } = this.props
    return (<span className="koulutusmoduuli">
      <span className="tunniste">{modelTitle(model, 'tunniste')}</span>
      <span className="diaarinumero">{modelTitle(model, 'perusteenDiaarinumero')}</span>
      <GenericEditor.PropertiesEditor properties={model.value.properties} propertyFilter={p => !['tunniste', 'perusteenDiaarinumero', 'pakollinen'].includes(p.key)} context={context}/>
    </span>)
  }
})
KoulutusmoduuliEditor.readOnly = true

export const PäivämääräväliEditor = React.createClass({
  render() {
    let { model, context } = this.props
    return (<span>
      <GenericEditor.Editor context={context} model={model} parent={this} path="alku"/> — <GenericEditor.Editor context={context} model={modelLookup(model, 'loppu')}/>
    </span>)
  }
})
PäivämääräväliEditor.canShowInline = () => true
PäivämääräväliEditor.readOnly = true

export const JaksoEditor = React.createClass({
  render() {
    let {model, context} = this.props
    return (
      <div className="jaksollinen">
        <PäivämääräväliEditor context={context} model={model}/>
        <GenericEditor.PropertiesEditor properties={model.value.properties} propertyFilter={p => !['alku', 'loppu'].includes(p.key)} context={context}/>
      </div>
    )
  }
})

export const editorMapping = {
  'henkilovahvistuspaikkakunnalla': VahvistusEditor,
  'henkilovahvistusilmanpaikkakuntaa': VahvistusEditor,
  'organisaatiovahvistus': VahvistusEditor,
  'laajuus' : LaajuusEditor,
  'koulutus' : KoulutusmoduuliEditor,
  'preibkoulutusmoduuli': KoulutusmoduuliEditor,
  'paatosjakso': PäivämääräväliEditor,
  'jakso': JaksoEditor
}