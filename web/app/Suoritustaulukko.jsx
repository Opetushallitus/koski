import React from 'react'
import { modelData, modelTitle } from './EditorModel.js'
import * as GenericEditor from './GenericEditor.jsx'

export const SuorituksetEditor = React.createClass({
  render() {
    let {suoritukset, context} = this.props
    let showPakollisuus = suoritukset.find(s => modelData(s, 'koulutusmoduuli.pakollinen') != undefined) != undefined
    return suoritukset.length > 0 && (<div className="suoritus-taulukko">
        <table>
          <thead><tr>
            <th className="suoritus">{suoritukset[0].value.properties.find(p => p.key == 'koulutusmoduuli').title}</th>
            {showPakollisuus && <th className="pakollisuus">Pakollisuus</th>}
            <th className="laajuus">Laajuus</th>
            <th className="arvosana">Arvosana</th>
          </tr></thead>
          {
            suoritukset.map((suoritus, i) =>
              <SuoritusEditor showPakollisuus={showPakollisuus} model={suoritus} context={GenericEditor.childContext(this, context, i)} key={i}/>
            )
          }
        </table>
      </div>)
  }
})

const SuoritusEditor = React.createClass({
  render() {
    let {model, context, showPakollisuus} = this.props
    let {expanded} = this.state
    let properties = model.value.properties
      .filter(p => !['koulutusmoduuli', 'arviointi', 'tila'].includes(p.key))
      .filter(GenericEditor.shouldShowProperty(context.edit))
    let hasProperties = properties.length > 0
    let toggleExpand = () => { if (hasProperties) this.setState({expanded : !expanded}) }
    let nimi = modelTitle(model, 'koulutusmoduuli.tunniste')
    return (<tbody className={expanded ? 'alternating expanded' : 'alternating'}>
    <tr>
      <td className="suoritus">
        <a className={ hasProperties ? 'toggle-expand' : 'toggle-expand disabled'} onClick={toggleExpand}>{ expanded ? '' : ''}</a>
        <span className="tila" title={modelTitle(model, 'tila')}>{suorituksenTilaSymbol(modelData(model, 'tila.koodiarvo'))}</span>
        {
          hasProperties
            ? <a className="nimi" onClick={toggleExpand}>{nimi}</a>
            : <span className="nimi">{nimi}</span>
        }

      </td>
      {showPakollisuus && <td className="pakollisuus">{ modelData(model, 'koulutusmoduuli.pakollinen') ? modelTitle(model, 'koulutusmoduuli.pakollinen') : 'ei' /* TODO: 18n*/}</td>}
      <td className="laajuus"><GenericEditor.Editor model={model} context={context} parent={this} path="koulutusmoduuli.laajuus" /></td>
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

let suorituksenTilaSymbol = (tila) => {
  switch (tila) {
    case 'VALMIS': return ''
    case 'KESKEYTYNYT': return ''
    case 'KESKEN': return ''
    default: return ''
  }
}