import React from 'baret'
import {modelData, modelTitle, modelLookup} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {shouldShowProperty, PropertiesEditor} from './PropertiesEditor.jsx'
import {modelProperties, modelProperty, modelItems} from './EditorModel'
import R from 'ramda'
import {buildClassNames} from '../classnames'
import {accumulateExpandedState} from './ExpandableItems'
import {hasArvosana} from './Suoritus'
import {t} from '../i18n'
import Text from '../Text.jsx'

export const Suoritustaulukko = React.createClass({
  getInitialState() {
    const suoritukset = this.props.suoritukset
    return {
      expanded: accumulateExpandedState({suoritukset, filter: s => suoritusProperties(s).length > 0})
    }
  },
  render() {
    const {suoritukset} = this.props
    const {isExpandedP, allExpandedP, toggleExpandAll, setExpanded} = this.state.expanded
    let grouped = R.sortBy(([groupId]) => groupId, R.toPairs(R.groupBy(s => modelData(s, 'tutkinnonOsanRyhmä.koodiarvo') || '5')(suoritukset)))
    let groupTitles = R.fromPairs(grouped.map(([groupId, [s]]) => [groupId, modelTitle(s, 'tutkinnonOsanRyhmä') ||
    <Text name='Muut suoritukset'/>]))

    let showPakollisuus = suoritukset.find(s => modelData(s, 'koulutusmoduuli.pakollinen') !== undefined) !== undefined
    let showArvosana = suoritukset.find(hasArvosana) !== undefined
    let samaLaajuusYksikkö = suoritukset.every((s, i, xs) => modelData(s, 'koulutusmoduuli.laajuus.yksikkö.koodiarvo') === modelData(xs[0], 'koulutusmoduuli.laajuus.yksikkö.koodiarvo'))
    let laajuusYksikkö = t(modelData(suoritukset[0], 'koulutusmoduuli.laajuus.yksikkö.lyhytNimi'))
    let showLaajuus = suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus.arvo') !== undefined) !== undefined
    let showExpandAll = suoritukset.some(s => suoritusProperties(s).length > 0)

    return suoritukset.length > 0 && (
      <div className="suoritus-taulukko">
        <table>
          <thead>
          <tr>
            <th className="suoritus">
              {modelProperty(suoritukset[0], 'koulutusmoduuli').title}
              {showExpandAll &&
              <div>
                {allExpandedP.map(allExpanded => <a className={'expand-all button' + (allExpanded ? ' expanded' : '')}
                                                    onClick={toggleExpandAll}>
                    <Text name={allExpanded ? 'Sulje kaikki' : 'Avaa kaikki'}/>
                  </a>
                )}
              </div>
              }
            </th>
            {showPakollisuus && <th className="pakollisuus"><Text name="Pakollisuus"/></th>}
            {showLaajuus && <th className="laajuus"><Text
              name="Laajuus"/>{((samaLaajuusYksikkö && laajuusYksikkö && ' (' + laajuusYksikkö + ')') || '')}</th>}
            {showArvosana && <th className="arvosana"><Text name="Arvosana"/></th>}
          </tr>
          </thead>
          {
            grouped.length > 1
              ? grouped.flatMap(([groupId, ryhmänSuoritukset], i) => [
                <tbody key={'group-' + i} className="group-header">
                <tr>
                  <td colSpan="4">{groupTitles[groupId]}</td>
                </tr>
                </tbody>,
                ryhmänSuoritukset.map((suoritus, j) => {
                  let key = i * 100 + j
                  return (<SuoritusEditor baret-lift showLaajuus={showLaajuus} showPakollisuus={showPakollisuus}
                                         showArvosana={showArvosana} model={suoritus} showScope={!samaLaajuusYksikkö}
                                         expanded={isExpandedP(suoritus)} onExpand={setExpanded(suoritus)} key={key}
                                         grouped={true}/>)
                })
              ])
              : grouped[0][1].map((suoritus, i) =>
                <SuoritusEditor baret-lift showLaajuus={showLaajuus} showPakollisuus={showPakollisuus}
                                showArvosana={showArvosana} model={suoritus} showScope={!samaLaajuusYksikkö}
                                expanded={isExpandedP(suoritus)} onExpand={setExpanded(suoritus)} key={i}/>
              )
          }
        </table>
      </div>
    )
  }
})

const SuoritusEditor = React.createClass({
  render() {
    let {model, showPakollisuus, showLaajuus, showArvosana, showScope, onExpand, expanded, grouped} = this.props
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
      {showLaajuus && <td className="laajuus"><Editor model={model} path="koulutusmoduuli.laajuus" compact="true" showReadonlyScope={showScope}/></td>}
      {showArvosana && <td className="arvosana">{modelTitle(arviointi, 'arvosana')}</td>}
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
  return modelProperties(modelLookup(suoritus, 'koulutusmoduuli'), p => p.key === 'kuvaus')
      .concat(modelProperties(suoritus, p => !(['koulutusmoduuli', 'arviointi', 'tila', 'tutkinnonOsanRyhmä'].includes(p.key))))
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