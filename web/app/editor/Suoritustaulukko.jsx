import React from 'react'
import {modelData, modelTitle, modelLookup} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {shouldShowProperty, PropertiesEditor} from './PropertiesEditor.jsx'
import {modelProperties, modelProperty, modelItems} from './EditorModel'
import R from 'ramda'
import {buildClassNames} from '../classnames'

export const Suoritustaulukko = React.createClass({
  render() {
    let {suoritukset} = this.props

    let grouped = R.sortBy(([groupId]) => groupId, R.toPairs(R.groupBy(s => modelData(s, 'tutkinnonOsanRyhmä.koodiarvo') || '5' )(suoritukset)))
    let groupTitles = R.fromPairs(grouped.map(([groupId, [s]]) => [groupId, modelTitle(s, 'tutkinnonOsanRyhmä') || 'Muut suoritukset' /*i18n*/]))

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
            grouped.length > 1
              ? grouped.flatMap(([groupId, ryhmänSuoritukset], i) => [
                  <tbody key={'group-' + i} className="group-header"><tr><td colSpan="4">{groupTitles[groupId]}</td></tr></tbody>,
                  ryhmänSuoritukset.map((suoritus, j) => {
                    let key = i*100 + j
                    return <SuoritusEditor showPakollisuus={showPakollisuus} model={suoritus} showScope={!samaLaajuusYksikkö}
                                    expanded={this.state.expanded.includes(key)} onExpand={this.toggleExpand(key)} key={key} grouped={true}/>
                  })
                ])
              : grouped[0][1].map((suoritus, i) =>
                  <SuoritusEditor showPakollisuus={showPakollisuus} model={suoritus} showScope={!samaLaajuusYksikkö} expanded={this.state.expanded.includes(i)} onExpand={this.toggleExpand(i)} key={i}/>
                )
          }
        </table>
      </div>)
  },
  toggleExpand(key) {
    return (expand) => {
      this.setState(expandStateCalc(this.state, this.props.suoritukset).toggleExpand(key, expand))
    }
  },
  toggleExpandAll() {
    this.setState(expandStateCalc(this.state, this.props.suoritukset).toggleExpandAll())
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
    let {model, showPakollisuus, showScope, onExpand, expanded, grouped} = this.props
    let arviointi = modelLookup(model, 'arviointi.-1')
    let properties = suoritusProperties(model)
    let propertiesWithoutOsasuoritukset = properties.filter(p => p.key !== 'osasuoritukset')
    let hasProperties = properties.length > 0
    let nimi = modelTitle(model, 'koulutusmoduuli')
    let osasuoritukset = modelLookup(model, 'osasuoritukset')

    return (<tbody className={buildClassNames([(!grouped && 'alternating'), (expanded && 'expanded')])}>
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
  return modelProperties(suoritus, p => !(['koulutusmoduuli', 'arviointi', 'tila', 'tutkinnonOsanRyhmä'].includes(p.key)))
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

export const expandStateCalc = (currentState, suoritukset, filter = s => suoritusProperties(s).length > 0) => {
  return {
    toggleExpandAll() {
      let {allExpandedToggle} = currentState
      let newExpanded = !allExpandedToggle ? suoritukset.reduce((acc, s, i) => filter(s) ? acc.concat(i) : acc , []) : []
      return {expanded: newExpanded, allExpandedToggle: !allExpandedToggle}
    },
    toggleExpand(key, expand) {
      let {expanded, allExpandedToggle} = currentState
      let newExpanded = expand ? expanded.concat(key) : R.without([key], expanded)

      return {
        expanded: newExpanded,
        allExpandedToggle: suoritukset.filter(filter).length === newExpanded.length
            ? true
            : newExpanded.length === 0 ? false : allExpandedToggle
      }
    }
  }
}