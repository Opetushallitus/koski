import React from 'react'
import { modelData, modelLookup, modelTitle, modelItems } from './EditorModel.js'
import * as GenericEditor from './GenericEditor.jsx'
import { LaajuusEditor, PäivämääräväliEditor } from './OppijaEditor.jsx'

export const TutkinnonOsatEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let suoritukset = modelItems(model, 'osasuoritukset')
    return suoritukset && (<div className="tutkinnonOsat">
      <table>
        <thead><tr>
          <th className="tutkinnonosa">Tutkinnon osa</th>
          <th className="pakollisuus">Pakollisuus</th>
          <th className="laajuus">Laajuus</th>
          <th className="arvosana">Arvosana</th>
        </tr></thead>
        {
          suoritukset.map((suoritus, i) =>
            <TutkinnonOsaEditor model={suoritus} context={GenericEditor.childContext(this, context, 'osasuoritukset', i)} key={i}/>
          )
        }
      </table>
     </div>)
  }
})

const TutkinnonOsaEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let {expanded} = this.state
    let properties = model.value.properties
      .filter(p => !['koulutusmoduuli', 'arviointi', 'tila'].includes(p.key))
      .filter(GenericEditor.shouldShowProperty(context.edit))
    let hasProperties = properties.length > 0
    let toggleExpand = () => { if (hasProperties) this.setState({expanded : !expanded}) }
    let nimi = modelTitle(model, 'koulutusmoduuli.tunniste')
    return (<tbody className={expanded ? 'alternating expanded' : 'alternating'}>
      <tr>
        <td className="tutkinnonosa">
          <a className={ hasProperties ? 'toggle-expand' : 'toggle-expand disabled'} onClick={toggleExpand}>{ expanded ? '' : ''}</a>
          <span className="tila" title={modelTitle(model, 'tila')}>{suorituksenTilaSymbol(modelData(model, 'tila.koodiarvo'))}</span>
          {
            hasProperties
              ? <a className="nimi" onClick={toggleExpand}>{nimi}</a>
              : <span className="nimi">{nimi}</span>
          }

        </td>
        <td className="pakollisuus">{ modelData(model, 'koulutusmoduuli.pakollinen') ? modelTitle(model, 'koulutusmoduuli.pakollinen') : 'ei' /* TODO: 18n*/}</td>
        <td className="laajuus"><LaajuusEditor model={modelLookup(model, 'koulutusmoduuli.laajuus')} context={GenericEditor.childContext(this, context, 'koulutusmoduuli', 'laajuus')} /></td>
        <td className="arvosana">{modelTitle(model, 'arviointi.-1.arvosana')}</td>
      </tr>
      {
        expanded && (<tr className="details">
          <td colSpan="4">
            <GenericEditor.PropertiesEditor properties={properties} context={context} />
          </td>
        </tr>)
      }
    </tbody>)
  },
  getInitialState() {
    return { expanded: false }
  }
})

export const NäytönSuorituspaikkaEditor = React.createClass({
  render() {
    let {model, context} = this.props
    if (context.edit) return <GenericEditor.ObjectEditor {...this.props}/>
    return <span>{modelTitle(model, 'kuvaus')}</span>
  }
})

export const NäytönArvioitsijaEditor = React.createClass({
  render() {
    let {model, context} = this.props
    if (context.edit) return <GenericEditor.ObjectEditor {...this.props}/>
    return <span>{modelTitle(model, 'nimi')} { modelData(model, 'ntm') ? ' (näyttötutkintomestari)' : ''}</span>
  }
})
NäytönArvioitsijaEditor.canShowInline = () => true

export const TyössäoppimisjaksoEditor = React.createClass({
  render() {
    let {model, context} = this.props
    if (context.edit) return <GenericEditor.ObjectEditor {...this.props}/>
    return (
      <div className="tyossaoppimisjakso">
        <PäivämääräväliEditor context={context} model={model}/> { modelTitle(model, 'paikkakunta')}, { modelTitle(model, 'maa')}
        <GenericEditor.PropertiesEditor
          properties={model.value.properties.filter(p => !['alku', 'loppu', 'paikkakunta', 'maa'].includes(p.key))}
          context={context}/>
      </div>
    )
  }
})

let suorituksenTilaSymbol = (tila) => {
  switch (tila) {
    case 'VALMIS': return ''
    case 'KESKEYTYNYT': return ''
    case 'KESKEN': return ''
    default: return ''
  }
}