import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {modelData, modelLookup, modelTitle} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {PropertiesEditor, shouldShowProperty} from './PropertiesEditor.jsx'
import {
  contextualizeSubModel,
  ensureArrayKey,
  modelErrorMessages,
  modelItems,
  modelProperties,
  modelProperty,
  modelSet,
  modelSetTitle,
  modelSetValue,
  modelSetValues,
  oneOfPrototypes, optionalPrototypeModel,
  pushModel,
  pushRemoval
} from './EditorModel'
import R from 'ramda'
import {buildClassNames} from '../classnames'
import {accumulateExpandedState} from './ExpandableItems'
import {fixTila, hasArvosana} from './Suoritus'
import {t} from '../i18n'
import Text from '../Text.jsx'
import {ammatillisentutkinnonosanryhmaKoodisto, enumValueToKoodiviiteLens, toKoodistoEnumValue} from '../koodistot'
import Autocomplete from '../Autocomplete.jsx'
import KoodistoDropdown from '../KoodistoDropdown.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {isPaikallinen, koulutusModuuliprototypes} from './Koulutusmoduuli'
import {EnumEditor} from './EnumEditor.jsx'
import {YhteensäSuoritettu, fetchLaajuudet} from './YhteensaSuoritettu.jsx'
import Http from '../http'

const placeholderForNonGrouped = '999999'

export class Suoritustaulukko extends React.Component {
  render() {
    let {suorituksetModel, parentSuoritus, nested} = this.props
    let context = suorituksetModel.context
    parentSuoritus = parentSuoritus || context.suoritus
    let suoritukset = modelItems(suorituksetModel) || []

    let suoritusProto = context.edit ? createTutkinnonOsanSuoritusPrototype(suorituksetModel) : suoritukset[0]
    let koulutustyyppi = modelData(parentSuoritus, 'koulutusmoduuli.koulutustyyppi.koodiarvo')
    let suoritustapa = modelData(parentSuoritus, 'suoritustapa')
    let isAmmatillinenTutkinto = parentSuoritus.value.classes.includes('ammatillisentutkinnonsuoritus')
    if (suoritukset.length == 0 && !(context.edit && isAmmatillinenTutkinto)) return null
    let isAmmatillinenPerustutkinto = koulutustyyppi == '1'

    const {isExpandedP, allExpandedP, toggleExpandAll, setExpanded} = accumulateExpandedState({
      suoritukset,
      filter: s => suoritusProperties(s).length > 0,
      component: this
    })

    let grouped, groupIds, groupTitles

    if (isAmmatillinenPerustutkinto && suoritustapa && suoritustapa.koodiarvo == 'ops') {
      grouped = R.groupBy(s => modelData(s, 'tutkinnonOsanRyhmä.koodiarvo') || placeholderForNonGrouped)(suoritukset)
      groupTitles = R.merge(ammatillisentutkinnonosanryhmaKoodisto, { [placeholderForNonGrouped] : t('Muut suoritukset')})
      groupIds = R.keys(grouped).sort()
      if (context.edit) {
        // Show the empty groups too
        groupIds = R.uniq(R.keys(ammatillisentutkinnonosanryhmaKoodisto).concat(groupIds))
      }
    } else {
      grouped = { [placeholderForNonGrouped] : suoritukset }
      groupTitles = { [placeholderForNonGrouped] : modelProperty(suoritukset[0] || suoritusProto, 'koulutusmoduuli').title }
      groupIds = [placeholderForNonGrouped]
    }

    const laajuudetP = fetchLaajuudet(parentSuoritus, groupIds)

    let showPakollisuus = modelData(suoritusProto, 'koulutusmoduuli.pakollinen') !== undefined || suoritukset.find(s => modelData(s, 'koulutusmoduuli.pakollinen') !== undefined) !== undefined
    let showArvosana = context.edit || suoritukset.find(hasArvosana) !== undefined
    let samaLaajuusYksikkö = suoritukset.every((s, i, xs) => modelData(s, 'koulutusmoduuli.laajuus.yksikkö.koodiarvo') === modelData(xs[0], 'koulutusmoduuli.laajuus.yksikkö.koodiarvo'))
    let laajuusModel = modelLookup(suoritusProto, 'koulutusmoduuli.laajuus')
    if (laajuusModel && laajuusModel.optional && !modelData(laajuusModel)) laajuusModel = optionalPrototypeModel(laajuusModel)
    let laajuusYksikkö = t(modelData(laajuusModel, 'yksikkö.lyhytNimi'))
    let showLaajuus = context.edit
      ? modelProperty(createTutkinnonOsanSuoritusPrototype(suorituksetModel), 'koulutusmoduuli.laajuus') !== null
      : suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus.arvo') !== undefined) !== undefined
    let showExpandAll = suoritukset.some(s => suoritusProperties(s).length > 0)

    return !suoritustapa && context.edit && isAmmatillinenTutkinto
        ? <Text name="Valitse ensin tutkinnon suoritustapa" />
        : (suoritukset.length > 0 || (context.edit && isAmmatillinenTutkinto)) && (
          <div className="suoritus-taulukko">
            <table>
              <thead>
              <tr>
                <th className="suoritus">
                  {showExpandAll &&
                  <div>
                    {allExpandedP.map(allExpanded => (
                      <a className={'expand-all button' + (allExpanded ? ' expanded' : '')} onClick={toggleExpandAll}>
                        <Text name={allExpanded ? 'Sulje kaikki' : 'Avaa kaikki'}/>
                      </a>)
                    )}
                  </div>
                  }
                </th>
              </tr>
              </thead>
              {
                groupIds.flatMap((groupId, i) => suoritusGroup(groupId, i))
              }
            </table>
          </div>)

    function suoritusGroup(groupId, i) {
      const items = (grouped[groupId] || [])

      return [
        <tbody key={'group-' + i} className={`group-header ${groupId}`}>
          <tr>
            <td>{groupTitles[groupId]}</td>
            {showPakollisuus && <td className="pakollisuus"><Text name="Pakollisuus"/></td>}
            {showLaajuus && <td className="laajuus"><Text name="Laajuus"/>{((laajuusYksikkö && ' (' + laajuusYksikkö + ')') || '')}</td>}
            {showArvosana && <td className="arvosana"><Text name="Arvosana"/></td>}
          </tr>
        </tbody>,
        items.map((suoritus, j) => suoritusEditor(suoritus, i * 100 + j, groupId)),
        context.edit && uusiTutkinnonOsa(i, groupId, items),
          !nested && <tbody key={'group- '+ i + '-footer'} className="yhteensä">
          <tr><td>
            <YhteensäSuoritettu osasuoritukset={items} laajuusP={laajuudetP.map(l => l[groupId])} laajuusYksikkö={laajuusYksikkö}/>
          </td></tr>
        </tbody>
      ]
    }

    function uusiTutkinnonOsa(i, groupId, items) {
      return (<tbody key={'group-' + i + '-new'} className={'uusi-tutkinnon-osa ' + groupId}>
      <tr>
        <td colSpan="4">
          <UusiTutkinnonOsa suoritus={parentSuoritus}
                            suoritusPrototype={createTutkinnonOsanSuoritusPrototype(suorituksetModel, groupId)}
                            suoritukset={items} addTutkinnonOsa={addTutkinnonOsa} groupId={groupId}/>
        </td>
      </tr>
      </tbody>)
    }

    function suoritusEditor(suoritus, key, groupId) {
      return (<TutkinnonOsanSuoritusEditor baret-lift showLaajuus={showLaajuus} showPakollisuus={showPakollisuus}
                                           showArvosana={showArvosana} model={suoritus} showScope={!samaLaajuusYksikkö}
                                           expanded={isExpandedP(suoritus)} onExpand={setExpanded(suoritus)} key={key}
                                           groupId={groupId}/>)
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

const UusiTutkinnonOsa = ({ suoritus, groupId, suoritusPrototype, addTutkinnonOsa, suoritukset }) => {
  let displayValue = item => item.newItem ? 'Lisää uusi: ' + item.title : item.data.koodiarvo + ' ' + item.title
  let selectedAtom = Atom(undefined)
  let käytössäolevatKoodiarvot = suoritukset.map(s => modelData(s, 'koulutusmoduuli.tunniste').koodiarvo)

  let koulutusModuuliprotos = koulutusModuuliprototypes(suoritusPrototype)

  let [[paikallinenKoulutusmoduuli], [koulutusmoduuliProto]] = R.partition(isPaikallinen, koulutusModuuliprotos)

  let diaarinumero = modelData(suoritus, 'koulutusmoduuli.perusteenDiaarinumero')
  let suoritustapa = modelData(suoritus, 'suoritustapa.koodiarvo')

  if (!diaarinumero || !suoritustapa) return null

  let map404ToEmpty = { errorMapper: (e) => e.httpStatus == 404 ? [] : Bacon.Error(e) }
  let osatP = Http
    .cachedGet(`/koski/api/tutkinnonperusteet/tutkinnonosat/${encodeURIComponent(diaarinumero)}/${encodeURIComponent(suoritustapa)}` + (groupId == placeholderForNonGrouped ? '' : '/'  + encodeURIComponent(groupId)), map404ToEmpty)

  selectedAtom.filter(R.identity).onValue(newItem => {
    addTutkinnonOsa(modelSetTitle(newItem.newItem
      ? modelSetValues(paikallinenKoulutusmoduuli, { 'kuvaus.fi': { data: newItem.title}, 'tunniste.nimi.fi': { data: newItem.title}, 'tunniste.koodiarvo': { data: newItem.title } })
      : modelSetValues(koulutusmoduuliProto, { tunniste: newItem }), newItem.title), groupId == placeholderForNonGrouped ? undefined : groupId)
  })

  return (<span>
    {
      osatP.map(perusteistaLöytyvätOsat => perusteistaLöytyvätOsat.length
        ? <KoodistoDropdown
          options={perusteistaLöytyvätOsat.filter(osa => !käytössäolevatKoodiarvot.includes(osa.koodiarvo))}
          selected={ selectedAtom.view(enumValueToKoodiviiteLens) }
          enableFilter="true"
          selectionText={ t('Lisää tutkinnon osa')}
          showKoodiarvo="true"
          />
        : <Autocomplete
          fetchItems={ query => query.length < 3 ? Bacon.once([]) : EnumEditor.fetchAlternatives(modelLookup(koulutusmoduuliProto, 'tunniste')).map(osat => osat.filter(osa => (!käytössäolevatKoodiarvot.includes(osa.data.koodiarvo) && displayValue(osa).toLowerCase().includes(query.toLowerCase()))))}
          resultAtom={ selectedAtom }
          placeholder={t('Lisää tutkinnon osa')}
          displayValue={ displayValue }
          selected = { selectedAtom }
          createNewItem = { query => paikallinenKoulutusmoduuli && query.length ? { newItem: true, title: query} : null }
        />
      )
    }
  </span>)
}

export class TutkinnonOsanSuoritusEditor extends React.Component {
  render() {
    let {model, showPakollisuus, showLaajuus, showArvosana, showScope, onExpand, expanded, groupId} = this.props
    let properties = suoritusProperties(model)
    let displayProperties = properties.filter(p => p.key !== 'osasuoritukset')
    let hasProperties = displayProperties.length > 0
    let nimi = modelTitle(model, 'koulutusmoduuli')
    let osasuoritukset = modelLookup(model, 'osasuoritukset')
    let arvosanaModel = modelLookup(fixTila(model), 'arviointi.-1.arvosana')

    return (<tbody className={buildClassNames(['tutkinnon-osa', (expanded && 'expanded'), (groupId)])}>
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
      {showArvosana && arvosanaModel && <td className="arvosana"><Editor model={ arvosanaModel } showEmptyOption="true"/></td>}
      {
        model.context.edit && (
          <td className="remove">
            <a className="remove-value" onClick={() => pushRemoval(model)}/>
          </td>
        )
      }
    </tr>
    {
      modelErrorMessages(model).map((error, i) => <tr key={'error-' + i} className="error"><td colSpan="42" className="error">{error}</td></tr>)
    }
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
          <Suoritustaulukko parentSuoritus={model} nested={true} suorituksetModel={ osasuoritukset }/>
        </td>
      </tr>)
    }
    </tbody>)
  }
}

const suoritusProperties = suoritus => {
  let properties = modelProperties(modelLookup(suoritus, 'koulutusmoduuli'), p => p.key === 'kuvaus').concat(
    suoritus.context.edit
      ? modelProperties(suoritus, p => ['näyttö', 'tunnustettu'].includes(p.key))
      : modelProperties(suoritus, p => !(['koulutusmoduuli', 'arviointi', 'tila', 'tutkinnonOsanRyhmä'].includes(p.key)))
      .concat(modelProperties(modelLookup(suoritus, 'arviointi.-1'), p => !(['arvosana', 'päivä', 'arvioitsijat']).includes(p.key)))
  )
  return properties.filter(shouldShowProperty(suoritus.context))
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