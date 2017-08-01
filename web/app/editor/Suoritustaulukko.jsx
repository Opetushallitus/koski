import React from 'baret'
import {modelData, modelTitle, modelLookup} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {shouldShowProperty, PropertiesEditor} from './PropertiesEditor.jsx'
import {modelProperties, modelProperty, modelItems} from './EditorModel'
import R from 'ramda'
import {buildClassNames} from '../classnames'
import {accumulateExpandedState} from './ExpandableItems'
import {hasArvosana} from './Suoritus'
import {t, lang} from '../i18n'
import Text from '../Text.jsx'
import ryhmäKoodisto from '../../../src/main/resources/mockdata/koodisto/koodit/ammatillisentutkinnonosanryhma.json'

const koodiMetadata = rawKoodi => rawKoodi.metadata.find(m => m.kieli.toLowerCase() == lang) || rawKoodi.metadata.find(m => m.kieli == 'FI') || rawKoodi.metadata[0]
const readKoodisto = json => R.fromPairs(json.map(rawKoodi => ([ rawKoodi.koodiArvo, koodiMetadata(rawKoodi).nimi ])))

export const Suoritustaulukko = React.createClass({
  render() {
    const {suoritukset, context: { edit }} = this.props
    const {isExpandedP, allExpandedP, toggleExpandAll, setExpanded} = accumulateExpandedState({
      suoritukset,
      filter: s => suoritusProperties(s).length > 0,
      component: this
    })

    let grouped = R.groupBy(s => modelData(s, 'tutkinnonOsanRyhmä.koodiarvo') || '5')(suoritukset)
    let groupIds = R.keys(grouped).sort()
    let groupTitles = R.fromPairs(groupIds.map(groupId => { let first = grouped[groupId][0]; return [groupId, modelTitle(first, 'tutkinnonOsanRyhmä') || <Text name='Muut suoritukset'/>] }))

    if (edit) {
      let groupsFromKoodisto = readKoodisto(ryhmäKoodisto)
      groupIds = R.uniq(R.keys(groupsFromKoodisto).concat(groupIds))
      groupTitles = R.merge(groupTitles, groupsFromKoodisto)
    }

    let showGrouped = groupIds.length > 1

    let showPakollisuus = suoritukset.find(s => modelData(s, 'koulutusmoduuli.pakollinen') !== undefined) !== undefined
    let showArvosana = suoritukset.find(hasArvosana) !== undefined
    let samaLaajuusYksikkö = suoritukset.every((s, i, xs) => modelData(s, 'koulutusmoduuli.laajuus.yksikkö.koodiarvo') === modelData(xs[0], 'koulutusmoduuli.laajuus.yksikkö.koodiarvo'))
    let laajuusYksikkö = t(modelData(suoritukset[0], 'koulutusmoduuli.laajuus.yksikkö.lyhytNimi'))
    let showLaajuus = suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus.arvo') !== undefined) !== undefined
    let showExpandAll = suoritukset.some(s => suoritusProperties(s).length > 0)

    return (suoritukset.length > 0 || edit) && (
      <div className="suoritus-taulukko">
        <table>
          <thead>
          <tr>
            <th className="suoritus">
              {suoritukset[0] && modelProperty(suoritukset[0], 'koulutusmoduuli').title}
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
            showGrouped
              ? groupIds.flatMap((groupId, i) => [
                <tbody key={'group-' + i} className="group-header">
                <tr>
                  <td colSpan="4">{groupTitles[groupId]}</td>
                </tr>
                </tbody>,
                (grouped[groupId] || []).map((suoritus, j) => {
                  let key = i * 100 + j
                  return (<SuoritusEditor baret-lift showLaajuus={showLaajuus} showPakollisuus={showPakollisuus}
                                         showArvosana={showArvosana} model={suoritus} showScope={!samaLaajuusYksikkö}
                                         expanded={isExpandedP(suoritus)} onExpand={setExpanded(suoritus)} key={key}
                                         grouped={true}/>)
                })
              ])
              : grouped[groupIds[0]].map((suoritus, i) =>
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
    let displayProperties = propertiesWithoutOsasuoritukset.filter(p => ['näyttö', 'tunnustettu'].includes(p.key))
    let hasProperties = displayProperties.length > 0
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
          <PropertiesEditor model={model} properties={displayProperties}/>
        </td>
      </tr>)
    }
    {
      expanded && osasuoritukset && osasuoritukset.value && (<tr className="osasuoritukset" key="osasuoritukset">
        <td colSpan="4">
          <Suoritustaulukko suoritukset={ modelItems(osasuoritukset) }  context={model.context}/>
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