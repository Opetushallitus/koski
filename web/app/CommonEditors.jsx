import React from 'react'
import { modelData, modelTitle, modelItems } from './EditorModel.js'
import { Editor, PropertiesEditor } from './GenericEditor.jsx'

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

export const editorMapping = {
  'vahvistus': VahvistusEditor,
  'laajuus' : LaajuusEditor,
  'koulutus' : KoulutusmoduuliEditor,
  'preibkoulutusmoduuli': KoulutusmoduuliEditor,
  'paatosjakso': PäivämääräväliEditor,
  'jakso': JaksoEditor
}