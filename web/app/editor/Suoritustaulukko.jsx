import React from 'react'
import {modelData, modelTitle, modelLookup} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {shouldShowProperty, PropertiesEditor} from './PropertiesEditor.jsx'
import {modelProperties, modelProperty, modelItems} from './EditorModel'
import R from 'ramda'

export const Suoritustaulukko = React.createClass({
  render() {
    let {suoritukset} = this.props
    let {allExpandedToggle} = this.state
    let showPakollisuus = suoritukset.find(s => modelData(s, 'koulutusmoduuli.pakollinen') !== undefined) !== undefined
    let samaLaajuusYksikkö = suoritukset.every( (s, i, xs) => modelData(s, 'koulutusmoduuli.laajuus.yksikkö.koodiarvo') === modelData(xs[0], 'koulutusmoduuli.laajuus.yksikkö.koodiarvo') )
    let laajuusYksikkö = modelData(suoritukset[0], 'koulutusmoduuli.laajuus.yksikkö.lyhytNimi.fi')
    let showExpandAll = suoritukset.some(s => suoritusProperties(s).length > 0)
    return suoritukset.length > 0 && (<div className="suoritus-taulukko">
        <table>
          <thead><tr>
            <th className="suoritus">
              {modelProperty(suoritukset[0], 'koulutusmoduuli').title}
              { showExpandAll &&
                <div>
                  <a className={'expand-all button' + (allExpandedToggle ? ' expanded' : '')} onClick={this.toggleExpandAll}>
                    { allExpandedToggle ? 'Sulje kaikki' : 'Avaa kaikki' }
                  </a>
                </div>
              }
            </th>
            {showPakollisuus && <th className="pakollisuus">Pakollisuus</th>}
            <th className="laajuus">Laajuus {samaLaajuusYksikkö && laajuusYksikkö && '(' + laajuusYksikkö + ')'}</th>
            <th className="arvosana">Arvosana</th>
          </tr></thead>
          {
            suoritukset.map((suoritus, i) =>
              <SuoritusEditor showPakollisuus={showPakollisuus} model={suoritus} showScope={!samaLaajuusYksikkö} expanded={this.state.expanded[i]} onExpand={this.toggleExpand(i)} key={i}/>
            )
          }
        </table>
      </div>)
  },
  toggleExpand(key) {
    return (expanded) => {
      let {suoritukset} = this.props
      let newExpanded = R.clone(this.state.expanded)
      newExpanded[key] = expanded

      this.setState(
          {
            expanded: newExpanded,
            allExpandedToggle: newExpanded.length === suoritukset.length && newExpanded.every(e => e === true)
                ? true
                : newExpanded.every(e => e === false)
                    ? false
                    : this.state.allExpandedToggle
          }
      )
    }
  },
  toggleExpandAll() {
    let {suoritukset} = this.props
    let {allExpandedToggle} = this.state
    this.setState({expanded: suoritukset.map(() => !allExpandedToggle), allExpandedToggle: !allExpandedToggle})
  },
  getInitialState() {
    return {
      expanded: [],
      allExpandedToggle: false
    }
  }
})

const SuoritusEditor = React.createClass({
  render() {
    let {model, showPakollisuus, showScope, onExpand, expanded} = this.props
    let arviointi = modelLookup(model, 'arviointi.-1')
    let properties = suoritusProperties(model)
    let propertiesWithoutOsasuoritukset = properties.filter(p => p.key !== 'osasuoritukset')
    let hasProperties = properties.length > 0
    let nimi = modelTitle(model, 'koulutusmoduuli')
    let osasuoritukset = modelLookup(model, 'osasuoritukset')

    return (<tbody className={expanded ? 'alternating expanded' : 'alternating'}>
    <tr>
      <td className="suoritus">
        <a className={ hasProperties ? 'toggle-expand' : 'toggle-expand disabled'} onClick={() => onExpand(!expanded)}>{ expanded ? '' : ''}</a>
        <span className="tila" title={modelTitle(model, 'tila')}>{suorituksenTilaSymbol(modelData(model, 'tila.koodiarvo'))}</span>
        {
          hasProperties
            ? <a className="nimi" onClick={() => onExpand(!expanded)}>{nimi}</a>
            : <span className="nimi">{nimi}</span>
        }

      </td>
      {showPakollisuus && <td className="pakollisuus"><Editor model={model} path="koulutusmoduuli.pakollinen"/></td>}
      <td className="laajuus"><Editor model={model} path="koulutusmoduuli.laajuus" compact="true" showReadonlyScope={showScope}/></td>
      <td className="arvosana">{modelTitle(arviointi, 'arvosana')}</td>
    </tr>
    {
      expanded && hasProperties && (<tr className="details" key="details">
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
  }
})

const suoritusProperties = suoritus => {
  return modelProperties(suoritus, p => !(['koulutusmoduuli', 'arviointi', 'tila'].includes(p.key)))
      .concat(modelProperties(modelLookup(suoritus, 'arviointi.-1'), p => !(['arvosana', 'päivä', 'arvioitsijat']).includes(p.key)))
      .filter(shouldShowProperty(suoritus.context))
}

export const suorituksenTilaSymbol = (tila) => {
  switch (tila) {
    case 'VALMIS': return ''
    case 'KESKEYTYNYT': return ''
    case 'KESKEN': return ''
    default: return ''
  }
}