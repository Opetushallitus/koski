import React from 'react'
import {modelData, modelTitle, modelLookup} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {shouldShowProperty, PropertiesEditor} from './PropertiesEditor.jsx'
import {modelProperties, modelProperty, modelItems} from './EditorModel'

export const Suoritustaulukko = React.createClass({
  render() {
    let {suoritukset} = this.props
    let showPakollisuus = suoritukset.find(s => modelData(s, 'koulutusmoduuli.pakollinen') != undefined) != undefined
    return suoritukset.length > 0 && (<div className="suoritus-taulukko">
        <table>
          <thead><tr>
            <th className="suoritus">{modelProperty(suoritukset[0], 'koulutusmoduuli').title}</th>
            {showPakollisuus && <th className="pakollisuus">Pakollisuus</th>}
            <th className="laajuus">Laajuus</th>
            <th className="arvosana">Arvosana</th>
          </tr></thead>
          {
            suoritukset.map((suoritus, i) =>
              <SuoritusEditor showPakollisuus={showPakollisuus} model={suoritus} key={i}/>
            )
          }
        </table>
      </div>)
  }
})

const SuoritusEditor = React.createClass({
  render() {
    let {model, showPakollisuus} = this.props
    let context = model.context
    let {expanded} = this.state
    let arviointi = modelLookup(model, 'arviointi.-1')
    let properties = modelProperties(model, p => !(['koulutusmoduuli', 'arviointi', 'tila'].includes(p.key)))
      .concat(modelProperties(arviointi, p => !(['arvosana', 'päivä', 'arvioitsijat']).includes(p.key)))
      .filter(shouldShowProperty(context))
    let propertiesWithoutOsasuoritukset = properties.filter(p => p.key != 'osasuoritukset')
    let hasProperties = properties.length > 0
    let toggleExpand = () => { if (hasProperties) this.setState({expanded : !expanded}) }
    let nimi = modelTitle(model, 'koulutusmoduuli')
    let osasuoritukset = modelLookup(model, 'osasuoritukset')

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
      {showPakollisuus && <td className="pakollisuus"><Editor model={model} path="koulutusmoduuli.pakollinen"/></td>}
      <td className="laajuus"><Editor model={model} path="koulutusmoduuli.laajuus" compact="true" /></td>
      <td className="arvosana">{modelTitle(arviointi, 'arvosana')}</td>
    </tr>
    {
      expanded && (<tr className="details" key="details">
        <td colSpan="4">
          <PropertiesEditor model={model} properties={propertiesWithoutOsasuoritukset} />
        </td>
      </tr>)
    }
    {
      expanded && osasuoritukset && osasuoritukset.value && (<tr className="osasuoritukset" key="osasuoritukset">
        <td colSpan="4">
          <Suoritustaulukko suoritukset={ modelItems(osasuoritukset) }/>
        </td>
      </tr>)
    }
    </tbody>)
  },
  getInitialState() {
    return { expanded: false }
  }
})

export const suorituksenTilaSymbol = (tila) => {
  switch (tila) {
    case 'VALMIS': return ''
    case 'KESKEYTYNYT': return ''
    case 'KESKEN': return ''
    default: return ''
  }
}