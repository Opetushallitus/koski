import React from 'baret'
import {modelData, modelLookup} from '../editor/EditorModel.js'
import {Editor} from '../editor/Editor'
import {PropertiesEditor, shouldShowProperty} from '../editor/PropertiesEditor'
import {
  modelErrorMessages, modelItems, modelProperties, modelProperty, modelTitle, optionalPrototypeModel,
  pushRemoval
} from '../editor/EditorModel'
import * as R from 'ramda'
import {buildClassNames} from '../components/classnames'
import {accumulateExpandedState} from '../editor/ExpandableItems'
import {hasArvosana, suorituksenTyyppi, suoritusValmis, tilaText} from './Suoritus'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import {tutkinnonOsanRyhmät} from '../koodisto/koodistot'
import {fetchLaajuudet, YhteensäSuoritettu} from './YhteensaSuoritettu'
import UusiTutkinnonOsa  from '../ammatillinen/UusiTutkinnonOsa'
import {
  createTutkinnonOsanSuoritusPrototype, isYhteinenTutkinnonOsa, osanOsa,
  placeholderForNonGrouped
} from '../ammatillinen/TutkinnonOsa'
import {sortLanguages} from '../util/sorting'
import {isKieliaine} from './Koulutusmoduuli'
import {ArvosanaEditor} from './ArvosanaEditor'
import {flatMapArray} from '../util/util'


export class Suoritustaulukko extends React.Component {
  render() {
    let {suorituksetModel, parentSuoritus, nested} = this.props
    let context = suorituksetModel.context
    parentSuoritus = parentSuoritus || context.suoritus
    let suoritukset = modelItems(suorituksetModel) || []

    let suoritusProto = context.edit ? createTutkinnonOsanSuoritusPrototype(suorituksetModel) : suoritukset[0]
    let suoritustapa = modelData(parentSuoritus, 'suoritustapa')
    let isAmmatillinenTutkinto = parentSuoritus.value.classes.includes('ammatillisentutkinnonsuoritus')
    if (suoritukset.length === 0 && !context.edit) return null

    const {isExpandedP, allExpandedP, toggleExpandAll, setExpanded} = accumulateExpandedState({
      suoritukset,
      filter: s => suoritusProperties(s).length > 0,
      component: this
    })

    const groupsP = tutkinnonOsanRyhmät(modelData(parentSuoritus, 'koulutusmoduuli.perusteenDiaarinumero'), suoritustapa).map(ammatillisentutkinnonosanryhmaKoodisto => {
      let grouped, groupIds, groupTitles
      if (isAmmatillinenTutkinto && R.keys(ammatillisentutkinnonosanryhmaKoodisto).length > 1) {
        grouped = R.groupBy(s => modelData(s, 'tutkinnonOsanRyhmä.koodiarvo') || placeholderForNonGrouped)(suoritukset)
        groupTitles = R.merge(ammatillisentutkinnonosanryhmaKoodisto, { [placeholderForNonGrouped] : t('Muut suoritukset')})
        groupIds = R.keys(grouped).sort()
        if (context.edit) {
          // Show the empty groups too
          groupIds = R.uniq(R.keys(ammatillisentutkinnonosanryhmaKoodisto).concat(groupIds))
        }
      } else {
        grouped = { [placeholderForNonGrouped] : suoritukset }
        groupTitles = { [placeholderForNonGrouped] : t(modelProperty(suoritukset[0] || suoritusProto, 'koulutusmoduuli').title) }
        groupIds = [placeholderForNonGrouped]
      }

      return {
        grouped: grouped,
        groupTitles: groupTitles,
        groupIds: groupIds
      }
    })


    let samaLaajuusYksikkö = suoritukset.every((s, i, xs) => modelData(s, 'koulutusmoduuli.laajuus.yksikkö.koodiarvo') === modelData(xs[0], 'koulutusmoduuli.laajuus.yksikkö.koodiarvo'))
    let laajuusModel = modelLookup(suoritusProto, 'koulutusmoduuli.laajuus')
    if (laajuusModel && laajuusModel.optional && !modelData(laajuusModel)) laajuusModel = optionalPrototypeModel(laajuusModel)
    let laajuusYksikkö = t(modelData(laajuusModel, 'yksikkö.lyhytNimi'))
    let showTila = !näyttötutkintoonValmistava(parentSuoritus)
    let showExpandAll = suoritukset.some(s => suoritusProperties(s).length > 0)
    let columns = [TutkintokertaColumn, SuoritusColumn, LaajuusColumn, KoepisteetColumn, ArvosanaColumn].filter(column => column.shouldShow({parentSuoritus, suorituksetModel, suoritukset, suoritusProto, context}))

    return !suoritustapa && context.edit && isAmmatillinenTutkinto
        ? <Text name="Valitse ensin tutkinnon suoritustapa" />
        : (suoritukset.length > 0 || context.edit) && (
          <div className="suoritus-taulukko">
            <table>
              <thead>
              <tr>
                <th className="suoritus">
                  {showExpandAll &&
                  <div>
                    {allExpandedP.map(allExpanded => (
                      <button className={'expand-all koski-button' + (allExpanded ? ' expanded' : '')} onClick={toggleExpandAll}>
                        <Text name={allExpanded ? 'Sulje kaikki' : 'Avaa kaikki'}/>
                      </button>)
                    )}
                  </div>
                  }
                </th>
              </tr>
              </thead>
              {
                groupsP.map(groups => flatMapArray(groups.groupIds, (groupId, i) => suoritusGroup(groups, groupId, i)))
              }
            </table>
          </div>)

    function suoritusGroup(groups, groupId, i) {
      const items = (groups.grouped[groupId] || [])
      const groupTitles = groups.groupTitles

      return [
        <tbody key={'group-' + i} className={`group-header ${groupId}`}>
          <tr>
            { nested && items.length === 0 ? null : columns.map(column => column.renderHeader({suoritusProto, laajuusYksikkö, groupTitles, groupId})) }
          </tr>
        </tbody>,
        items.map((suoritus, j) => suoritusEditor(suoritus, i * 100 + j, groupId)),
        context.edit && <tbody key={'group-' + i + '-new'} className={'uusi-tutkinnon-osa ' + groupId}>
          <tr>
            <td colSpan="4">
              <UusiTutkinnonOsa suoritus={parentSuoritus}
                                suoritusPrototype={createTutkinnonOsanSuoritusPrototype(suorituksetModel, groupId)}
                                suorituksetModel={suorituksetModel}
                                groupId={groupId}
                                setExpanded={setExpanded}
                                groupTitles={groupTitles}
              />
            </td>
          </tr>
        </tbody>,
        !nested && !näyttötutkintoonValmistava(parentSuoritus) && !ylioppilastutkinto(parentSuoritus) && <tbody key={'group- '+ i + '-footer'} className="yhteensä">
          <tr><td>
            <YhteensäSuoritettu osasuoritukset={items} laajuusP={fetchLaajuudet(parentSuoritus, groups.groupIds).map(l => l[groupId])} laajuusYksikkö={laajuusYksikkö}/>
          </td></tr>
        </tbody>
      ]
    }

    function suoritusEditor(suoritus, key, groupId) {
      return (<TutkinnonOsanSuoritusEditor baret-lift
                                           model={suoritus} showScope={!samaLaajuusYksikkö} showTila={showTila}
                                           expanded={isExpandedP(suoritus)} onExpand={setExpanded(suoritus)} key={key}
                                           groupId={groupId} columns={columns}/>)
    }
  }
}

let näyttötutkintoonValmistava = suoritus => suoritus.value.classes.includes('nayttotutkintoonvalmistavankoulutuksensuoritus')
let ylioppilastutkinto = suoritus => suoritus.value.classes.includes('ylioppilastutkinnonsuoritus')

export class TutkinnonOsanSuoritusEditor extends React.Component {
  render() {
    let {model, showScope, showTila, onExpand, expanded, groupId, columns} = this.props
    let properties = suoritusProperties(model)
    let displayProperties = properties.filter(p => p.key !== 'osasuoritukset')
    let hasProperties = displayProperties.length > 0
    let osasuoritukset = modelLookup(model, 'osasuoritukset')
    let showOsasuoritukset = (osasuoritukset && osasuoritukset.value) || isYhteinenTutkinnonOsa(model)
    return (<tbody className={buildClassNames(['tutkinnon-osa', (expanded && 'expanded'), (groupId)])}>
    <tr>
      {columns.map(column => column.renderData({model, showScope, showTila, onExpand, hasProperties, expanded}))}
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
      expanded && showOsasuoritukset && (<tr className="osasuoritukset" key="osasuoritukset">
        <td colSpan="4">
          <Suoritustaulukko parentSuoritus={model} nested={true} suorituksetModel={ osasuoritukset }/>
        </td>
      </tr>)
    }
    </tbody>)
  }
}

const suoritusProperties = suoritus => {
  const filterProperties = filter => modelProperties(suoritus, filter)
  const includeProperties = (...properties) => filterProperties(p => properties.includes(p.key))
  const excludeProperties = (...properties) => filterProperties(p => !properties.includes(p.key))

  const propertiesForSuoritustyyppi = (tyyppi, isEdit) => {
    const simplifiedArviointi = modelProperties(modelLookup(suoritus, 'arviointi.-1'),
      p => !(['arvosana', 'päivä', 'arvioitsijat', 'pisteet']).includes(p.key)
    )

    const arviointipäivä = modelProperties(modelLookup(suoritus, 'arviointi.-1'), p => p.key === 'päivä')
    let showPakollinen = (tyyppi !== 'nayttotutkintoonvalmistavakoulutus') && modelData(suoritus, 'koulutusmoduuli.pakollinen') !== undefined
    const pakollinen = showPakollinen ? modelProperties(modelLookup(suoritus, 'koulutusmoduuli'), p => p.key === 'pakollinen') : []

    const defaultsForEdit = pakollinen
      .concat(arviointipäivä)
      .concat(includeProperties('näyttö', 'tunnustettu', 'lisätiedot'))

    const defaultsForView = pakollinen
      .concat(excludeProperties('koulutusmoduuli', 'arviointi', 'tutkinnonOsanRyhmä', 'tutkintokerta'))
      .concat(simplifiedArviointi)

    switch (tyyppi) {
      case 'valma':
      case 'telma':
        return isEdit
          ? pakollinen.concat(includeProperties('näyttö', 'tunnustettu', 'lisätiedot')).concat(arviointipäivä).concat(simplifiedArviointi)
          : defaultsForView

      default: return isEdit ? defaultsForEdit : defaultsForView
    }
  }

  const kuvaus = modelProperties(modelLookup(suoritus, 'koulutusmoduuli'), p => p.key === 'kuvaus')
  const parentSuorituksenTyyppi = suorituksenTyyppi(suoritus.context.suoritus)

  return kuvaus
    .concat(propertiesForSuoritustyyppi(parentSuorituksenTyyppi, suoritus.context.edit))
    .filter(shouldShowProperty(suoritus.context))
}

const TutkintokertaColumn = {
  shouldShow: ({parentSuoritus}) => ylioppilastutkinto(parentSuoritus),
  renderHeader: () => {
    return <td key="tutkintokerta" className="tutkintokerta"><Text name="Tutkintokerta"/></td>
  },
  renderData: ({model}) => (<td key="tutkintokerta" className="tutkintokerta">
    <Editor model={model} path="tutkintokerta.vuosi" compact="true"/>
    {' '}
    <Editor model={model} path="tutkintokerta.vuodenaika" compact="true"/>
  </td>)
}

const KoepisteetColumn = {
  shouldShow: ({parentSuoritus}) => ylioppilastutkinto(parentSuoritus),
  renderHeader: () => {
    return <td key="koepisteet" className="koepisteet"><Text name="Pisteet"/></td>
  },
  renderData: ({model}) => (<td key="koepisteet" className="koepisteet"><Editor model={modelLookup(model, 'arviointi.-1.pisteet')}/></td>)
}

const SuoritusColumn = {
  shouldShow : () => true,
  renderHeader: ({groupTitles, groupId}) => <td key="suoritus" className="tutkinnon-osan-ryhma">{groupTitles[groupId]}</td>,
  renderData: ({model, showTila, onExpand, hasProperties, expanded}) => {
    let koulutusmoduuli = modelLookup(model, 'koulutusmoduuli')
    let titleAsExpandLink = hasProperties && (!osanOsa(koulutusmoduuli) || !model.context.edit)
    let kieliaine = isKieliaine(koulutusmoduuli)

    return (<td key="suoritus" className="suoritus">
      <a className={ hasProperties ? 'toggle-expand' : 'toggle-expand disabled'}
         onClick={() => onExpand(!expanded)}>{ expanded ? '' : ''}</a>
      {showTila && <span className="tila" title={tilaText(model)}>{suorituksenTilaSymbol(model)}</span>}
      {
        titleAsExpandLink
          ? <button className='text-button small' onClick={() => onExpand(!expanded)}>{modelTitle(model, 'koulutusmoduuli')}</button>
          : <span className="nimi">
            {t(modelData(koulutusmoduuli, 'tunniste.nimi')) + (kieliaine ? ', ' : '')}
            {kieliaine && <span className="value kieli"><Editor model={koulutusmoduuli} inline={true} path="kieli" sortBy={sortLanguages}/></span>}
          </span>
      }
    </td>)
  }
}

const LaajuusColumn = {
  shouldShow: ({parentSuoritus, suoritukset, suorituksetModel, context}) => (!näyttötutkintoonValmistava(parentSuoritus) && (context.edit
    ? modelProperty(createTutkinnonOsanSuoritusPrototype(suorituksetModel), 'koulutusmoduuli.laajuus') !== null
    : suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus.arvo') !== undefined) !== undefined)),
  renderHeader: ({laajuusYksikkö}) => {
    return <td key="laajuus" className="laajuus"><Text name="Laajuus"/>{((laajuusYksikkö && ' (' + laajuusYksikkö + ')') || '')}</td>
  },
  renderData: ({model, showScope}) => <td key="laajuus" className="laajuus"><Editor model={model} path="koulutusmoduuli.laajuus" compact="true" showReadonlyScope={showScope}/></td>
}

const ArvosanaColumn = {
  shouldShow: ({parentSuoritus, suoritukset, context}) => !näyttötutkintoonValmistava(parentSuoritus) && (context.edit || suoritukset.find(hasArvosana) !== undefined),
  renderHeader: () => <td key="arvosana" className="arvosana"><Text name="Arvosana"/></td>,
  renderData: ({model}) => <td key="arvosana" className="arvosana"><ArvosanaEditor model={model} /></td>
}

export const suorituksenTilaSymbol = (suoritus) => suoritusValmis(suoritus) ? '' : ''
