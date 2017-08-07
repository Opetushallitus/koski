import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {modelData, modelLookup, modelTitle} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {PropertiesEditor, shouldShowProperty} from './PropertiesEditor.jsx'
import {
  contextualizeSubModel,
  ensureArrayKey,
  modelItems,
  modelProperties,
  modelProperty,
  modelSet, modelSetTitle,
  modelSetValue,
  oneOfPrototypes,
  pushModel, pushRemoval
} from './EditorModel'
import R from 'ramda'
import {buildClassNames} from '../classnames'
import {accumulateExpandedState} from './ExpandableItems'
import {hasArvosana} from './Suoritus'
import {t} from '../i18n'
import Text from '../Text.jsx'
import {ammatillisentutkinnonosanryhmaKoodisto, toKoodistoEnumValue} from '../koodistot'
import Autocomplete from '../Autocomplete.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {isPaikallinen, koulutusModuuliprototypes} from './Koulutusmoduuli'
import {EnumEditor} from './EnumEditor.jsx'

const placeholderForNonGrouped = '999999'

export class Suoritustaulukko extends React.Component {
  render() {
    const {suorituksetModel} = this.props
    let context = suorituksetModel.context
    let suoritukset = modelItems(suorituksetModel) || []

    const {isExpandedP, allExpandedP, toggleExpandAll, setExpanded} = accumulateExpandedState({
      suoritukset,
      filter: s => suoritusProperties(s).length > 0,
      component: this
    })

    let grouped = R.groupBy(s => modelData(s, 'tutkinnonOsanRyhmä.koodiarvo') || placeholderForNonGrouped)(suoritukset)
    let groupIds = R.keys(grouped).sort()
    let groupTitles = R.fromPairs(groupIds.map(groupId => { let first = grouped[groupId][0]; return [groupId, modelTitle(first, 'tutkinnonOsanRyhmä') || <Text name='Muut suoritukset'/>] }))

    if (context.edit) {
      let suoritusProto = createTutkinnonOsanSuoritusPrototype(suorituksetModel)
      let ryhmäModel = modelLookup(suoritusProto, 'tutkinnonOsanRyhmä')
      if (ryhmäModel) {
        // Lisääminen mahdollista toistaiseksi vain ryhmitellyille suorituksille (== ammatilliset tutkinnon osat)
        groupIds = R.uniq(R.keys(ammatillisentutkinnonosanryhmaKoodisto).concat(groupIds))
        groupTitles = R.merge(groupTitles, ammatillisentutkinnonosanryhmaKoodisto)
      }
    }

    let showGrouped = groupIds.length > 1

    let showPakollisuus = suoritukset.find(s => modelData(s, 'koulutusmoduuli.pakollinen') !== undefined) !== undefined
    let showArvosana = suoritukset.find(hasArvosana) !== undefined
    let samaLaajuusYksikkö = suoritukset.every((s, i, xs) => modelData(s, 'koulutusmoduuli.laajuus.yksikkö.koodiarvo') === modelData(xs[0], 'koulutusmoduuli.laajuus.yksikkö.koodiarvo'))
    let laajuusYksikkö = t(modelData(suoritukset[0], 'koulutusmoduuli.laajuus.yksikkö.lyhytNimi'))
    let showLaajuus = suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus.arvo') !== undefined) !== undefined
    let showExpandAll = suoritukset.some(s => suoritusProperties(s).length > 0)

    return (suoritukset.length > 0 || context.edit) && (
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
              ? groupIds.flatMap((groupId, i) => suoritusGroup(groupId, i))
              : grouped[groupIds[0]].map((suoritus, i) => suoritusEditor(suoritus, i))
          }
        </table>
      </div>
    )

    function suoritusGroup(groupId, i) {
      let items = (grouped[groupId] || [])
      return [
        <tbody key={'group-' + i} className="group-header">
          <tr><td colSpan="4">{groupTitles[groupId]}</td></tr>
        </tbody>,
        items.map((suoritus, j) => {
          return suoritusEditor(suoritus, i * 100 + j, groupId)
        }),
        context.edit && <tbody key={'group-' + i + '-new'} className={'uusi-tutkinnon-osa ' + groupId}>
          <tr><td colSpan="4">
            <UusiTutkinnonOsa suoritusPrototype={createTutkinnonOsanSuoritusPrototype(suorituksetModel, groupId)} suoritukset={items} addTutkinnonOsa={addTutkinnonOsa} groupId={groupId != placeholderForNonGrouped && groupId}/>
          </td></tr>
        </tbody>
      ]
    }

    function suoritusEditor(suoritus, key, groupId) {
      return (<SuoritusEditor baret-lift showLaajuus={showLaajuus} showPakollisuus={showPakollisuus}
                             showArvosana={showArvosana} model={suoritus} showScope={!samaLaajuusYksikkö}
                             expanded={isExpandedP(suoritus)} onExpand={setExpanded(suoritus)} key={key}
                             grouped={showGrouped} groupId={groupId}/>)
    }

    function addTutkinnonOsa(koulutusmoduuli, groupId) {
      let suoritus = modelSet(createTutkinnonOsanSuoritusPrototype(suorituksetModel, groupId), koulutusmoduuli, 'koulutusmoduuli')
      if (groupId) {
        suoritus = modelSetValue(suoritus, toKoodistoEnumValue('ammatillisentutkinnonosanryhma', groupId, groupTitles[groupId]), 'tutkinnonOsanRyhmä')
      }
      pushModel(suoritus, context.changeBus)
      ensureArrayKey(suoritus)
      setExpanded(suoritus)(true)
    }
  }
}

const UusiTutkinnonOsa = ({ groupId, suoritusPrototype, addTutkinnonOsa, suoritukset }) => {
  let displayValue = item => item.data.koodiarvo + ' ' + item.title
  let selectedAtom = Atom(undefined)
  let käytössäolevatKoodiarvot = suoritukset.map(s => modelData(s, 'koulutusmoduuli.tunniste').koodiarvo)

  // TODO: rajaa ePerusteiden mukaisesti?

  // TODO: paikallisen tutkinnon osan lisäys

  let koulutusmoduuliProto = koulutusModuuliprototypes(suoritusPrototype).filter(R.complement(isPaikallinen))[0]
  const tutkinnonOsatP = EnumEditor.fetchAlternatives(modelLookup(koulutusmoduuliProto, 'tunniste'))

  selectedAtom.filter(R.identity).onValue(koodi => {
    addTutkinnonOsa(modelSetValue(modelSetTitle(koulutusmoduuliProto, koodi.title), koodi, 'tunniste'), groupId)
  })

  return (<span>
    <Autocomplete
      fetchItems={ query => query.length < 3 ? Bacon.once([]) : tutkinnonOsatP.map(osat => osat.filter(osa => (!käytössäolevatKoodiarvot.includes(osa.data.koodiarvo) && displayValue(osa).toLowerCase().includes(query.toLowerCase()))))}
      resultAtom={ selectedAtom }
      placeholder="Lisää tutkinnonosa"
      displayValue={ displayValue }
      selected = { selectedAtom }
    />
  </span>)
}

class SuoritusEditor extends React.Component {
  render() {
    let {model, showPakollisuus, showLaajuus, showArvosana, showScope, onExpand, expanded, grouped, groupId} = this.props
    let arviointi = modelLookup(model, 'arviointi.-1')
    let properties = suoritusProperties(model)
    let propertiesWithoutOsasuoritukset = properties.filter(p => p.key !== 'osasuoritukset')
    let displayProperties = model.context.edit ? propertiesWithoutOsasuoritukset.filter(p => ['näyttö', 'tunnustettu'].includes(p.key)) : propertiesWithoutOsasuoritukset
    let hasProperties = displayProperties.length > 0
    let nimi = modelTitle(model, 'koulutusmoduuli')
    let osasuoritukset = modelLookup(model, 'osasuoritukset')

    return (<tbody className={buildClassNames(['tutkinnon-osa', (!grouped && 'alternating'), (expanded && 'expanded'), (groupId)])}>
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
      {
        model.context.edit && (
          <td>
            <a className="remove-value" onClick={() => pushRemoval(model)}>{''}</a>
          </td>
        )
      }
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
          <Suoritustaulukko suorituksetModel={ osasuoritukset }/>
        </td>
      </tr>)
    }
    </tbody>)
  }
}

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

let createTutkinnonOsanSuoritusPrototype = (osasuoritukset, groupId) => {
  osasuoritukset = wrapOptional({model: osasuoritukset})
  let newItemIndex = modelItems(osasuoritukset).length
  let suoritusProto = contextualizeSubModel(osasuoritukset.arrayPrototype, osasuoritukset, newItemIndex)
  let preferredClass = groupId == '2' ? 'yhteisenammatillisentutkinnonosansuoritus' : 'muunammatillisentutkinnonosansuoritus'
  let sortValue = (oneOfProto) => oneOfProto.value.classes.includes(preferredClass) ? 0 : 1
  let alternatives = oneOfPrototypes(suoritusProto)
  suoritusProto = alternatives.sort((a, b) => sortValue(a) - sortValue(b))[0]
  return contextualizeSubModel(suoritusProto, osasuoritukset, newItemIndex)
}