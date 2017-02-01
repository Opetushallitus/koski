import React from 'react'
import { modelData, modelLookup, modelTitle, modelItems } from './EditorModel.js'
import * as GenericEditor from './GenericEditor.jsx'
import { LaajuusEditor } from './OppijaEditor.jsx'

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
    return (<tbody className="alternating" className={expanded ? 'expanded' : null}>
      <tr>
        <td className="tutkinnonosa">
          <a className="toggle-expand" onClick={() => this.setState({expanded : !expanded})}>+</a>
          {modelTitle(model, 'koulutusmoduuli.tunniste')}
        </td>
        <td className="pakollisuus">{ modelData(model, 'koulutusmoduuli.pakollinen') ? modelTitle(model, 'koulutusmoduuli.pakollinen') : 'ei' /* TODO: 18n*/}</td>
        <td className="laajuus"><LaajuusEditor model={modelLookup(model, 'koulutusmoduuli.laajuus')} context={GenericEditor.childContext(this, context, 'koulutusmoduuli', 'laajuus')} /></td>
        <td className="arvosana">{modelTitle(model, 'arviointi.-1.arvosana')}</td>
      </tr>
      {
        expanded && (<tr className="details">
          <td colSpan="4">
            <GenericEditor.PropertiesEditor properties={model.value.properties} context={context} />
          </td>
        </tr>)
      }
    </tbody>)
  },
  getInitialState() {
    return { expanded: false }
  }
})