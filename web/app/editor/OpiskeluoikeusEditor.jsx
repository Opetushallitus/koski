import React from 'baret'
import Atom from 'bacon.atom'
import R from 'ramda'
import {addContext, modelData, modelItems, modelLookup, modelTitle} from './EditorModel.js'
import {TogglableEditor} from './TogglableEditor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {onLopputilassa, OpiskeluoikeudenTilaEditor} from './OpiskeluoikeudenTilaEditor.jsx'
import Versiohistoria from '../Versiohistoria.jsx'
import Link from '../Link.jsx'
import {currentLocation} from '../location.js'
import {yearFromIsoDateString} from '../date'
import {ExpandablePropertiesEditor} from './ExpandablePropertiesEditor.jsx'
import UusiPerusopetuksenSuoritusPopup from './UusiPerusopetuksenSuoritusPopup.jsx'
import {Editor} from './Editor.jsx'
import {navigateTo} from '../location'
import {pushModel} from './EditorModel'
import {suorituksenTyyppi, suoritusValmis} from './Suoritus'
import Text from '../Text.jsx'
import {isPerusopetuksenOppimäärä, luokkaAste} from './Perusopetus'

export const OpiskeluoikeusEditor = ({model}) => {
  let id = modelData(model, 'id')
  model = addContext(model, {opiskeluoikeus: model})
  return (<TogglableEditor model={model} renderChild={ (mdl, editLink) => {
    let context = mdl.context
    let suoritukset = modelItems(mdl, 'suoritukset')
    assignTabNames(suoritukset)
    let excludedProperties = ['suoritukset', 'alkamispäivä', 'arvioituPäättymispäivä', 'päättymispäivä', 'oppilaitos', 'lisätiedot']
    var suoritusIndex = SuoritusTabs.suoritusIndex(mdl, suoritukset)
    if (suoritusIndex < 0 || suoritusIndex >= suoritukset.length) {
      navigateTo(SuoritusTabs.urlForTab(mdl, suoritukset[0].tabName))
      return null
    }
    let valittuSuoritus = suoritukset[suoritusIndex]

    return (
      <div className="opiskeluoikeus">
        <h3>
          <span className="oppilaitos inline-text">{modelTitle(mdl, 'oppilaitos')}{','}</span>
          <span className="koulutus inline-text">{(näytettävätPäätasonSuoritukset(model)[0] || {}).title}</span>
           { modelData(mdl, 'alkamispäivä')
              ? <span className="inline-text">{'('}
                    <span className="alku pvm">{yearFromIsoDateString(modelTitle(mdl, 'alkamispäivä'))}</span>{'-'}
                    <span className="loppu pvm">{yearFromIsoDateString(modelTitle(mdl, 'päättymispäivä'))}{','}</span>
                </span>
              : null
            }
          <span className="tila">{modelTitle(mdl, 'tila.opiskeluoikeusjaksot.-1.tila').toLowerCase()}{')'}</span>
          <Versiohistoria opiskeluoikeusId={id} oppijaOid={context.oppijaOid}/>
        </h3>
        <div className={mdl.context.edit ? 'opiskeluoikeus-content editing' : 'opiskeluoikeus-content'}>
          <div className="opiskeluoikeuden-tiedot">
            {editLink}
            <OpiskeluoikeudenOpintosuoritusoteLink opiskeluoikeus={mdl}/>
            <OpiskeluoikeudenVoimassaoloaika opiskeluoikeus={mdl}/>
            <PropertiesEditor
              model={mdl}
              propertyFilter={ p => !excludedProperties.includes(p.key) }
              getValueEditor={ (prop, getDefault) => prop.key === 'tila'
                ? <OpiskeluoikeudenTilaEditor model={mdl} />
                : getDefault() }
             />
            {
              modelLookup(mdl, 'lisätiedot') && <ExpandablePropertiesEditor model={mdl} propertyName="lisätiedot" />
            }
          </div>
          <div className="suoritukset">
            <h4><Text name="Suoritukset"/></h4>
            <SuoritusTabs model={mdl} suoritukset={suoritukset}/>
            <Editor key={valittuSuoritus.tabName} model={valittuSuoritus} alwaysUpdate="true" />
          </div>
        </div>
      </div>)
    }
  } />)
}

const OpiskeluoikeudenVoimassaoloaika = ({opiskeluoikeus}) => {
  let päättymispäiväProperty = (modelData(opiskeluoikeus, 'arvioituPäättymispäivä') && !modelData(opiskeluoikeus, 'päättymispäivä')) ? 'arvioituPäättymispäivä' : 'päättymispäivä'
  return (<div className="alku-loppu">
    <Text name="Opiskeluoikeuden voimassaoloaika"/>{': '}
    <span className="alkamispäivä"><Editor model={addContext(opiskeluoikeus, {edit: false})} path="alkamispäivä" /></span>
    {' — '}
    <span className="päättymispäivä"><Editor model={addContext(opiskeluoikeus, {edit: false})} path={päättymispäiväProperty} /></span>
    {' '}
    {päättymispäiväProperty == 'arvioituPäättymispäivä' && <Text name="(arvioitu)"/>}
  </div>)
}

const assignTabNames = (suoritukset) => {
  suoritukset = R.reverse(suoritukset) // they are in reverse chronological-ish order
  let tabNamesInUse = {}
  for (var i in suoritukset) {
    let suoritus = suoritukset[i]
    if (suoritus.tabName) {
      tabNamesInUse[suoritus.tabName] = true
    }
  }
  for (var i in suoritukset) {
    let suoritus = suoritukset[i]
    if (!suoritus.tabName) {
      let tabName = modelTitle(suoritus, 'koulutusmoduuli.tunniste')
      while (tabNamesInUse[tabName]) {
        tabName += '-2'
      }
      tabNamesInUse[tabName] = true
      suoritus.tabName = tabName
    }
  }
}

const SuoritusTabs = ({ model, suoritukset }) => {
  let addingAtom = Atom(false)
  let uusiSuoritusCallback = (suoritus) => {
    if (suoritus) {
      pushModel(suoritus, model.context.changeBus)
      let suoritukset2 = [suoritus].concat(suoritukset)
      assignTabNames(suoritukset2) // to get the correct tab name for the new suoritus
      navigateTo(SuoritusTabs.urlForTab(model, suoritus.tabName))
    } else {
      addingAtom.set(false)
    }
  }
  let tabTitle = (suoritusModel) => isPerusopetuksenOppimäärä(suoritusModel) ? <Text name="Päättötodistus"/> : suoritusTitle(suoritusModel)

  return (<ul className="suoritus-tabs">
    {
      suoritukset.map((suoritusModel, i) => {
        let selected = i === SuoritusTabs.suoritusIndex(model, suoritukset)
        let titleEditor = tabTitle(suoritusModel)
        return (<li className={selected ? 'selected': null} key={i}>
          { selected ? titleEditor : <Link href={ SuoritusTabs.urlForTab(model, suoritusModel.tabName) } exitHook={false}> {titleEditor} </Link>}
        </li>)
      })
    }
    {
      model.context.edit && !onLopputilassa(model) && UusiPerusopetuksenSuoritusPopup.canAddSuoritus(model) && (
        <li className="add-suoritus"><a onClick={() => { addingAtom.modify(x => !x) }}><span className="plus">{''}</span>{UusiPerusopetuksenSuoritusPopup.addSuoritusTitle(model)}</a></li>
      )
    }
    {
      addingAtom.map(adding => adding && <UusiPerusopetuksenSuoritusPopup opiskeluoikeus={model} resultCallback={uusiSuoritusCallback}/>)
    }
  </ul>
)}

SuoritusTabs.urlForTab = (model, i) => currentLocation().addQueryParams({[SuoritusTabs.suoritusQueryParam(model.context)]: i}).toString()
SuoritusTabs.suoritusQueryParam = context => (modelData(context.opiskeluoikeus, 'id') || context.opiskeluoikeusIndex) + '.suoritus'
SuoritusTabs.suoritusIndex = (model, suoritukset) => {
  let paramName = SuoritusTabs.suoritusQueryParam(model.context)
  let selectedTabName = currentLocation().params[paramName]
  let index = suoritukset.map(s => s.tabName).indexOf(selectedTabName)
  if (index < 0) {
    index = suoritukset.findIndex(s => luokkaAste(s) || (isPerusopetuksenOppimäärä(s) && suoritusValmis(s) ))
    if (index < 0) index = 0
    selectedTabName = suoritukset[index].tabName
    let newLocation = currentLocation().addQueryParams({ [paramName]: selectedTabName }).toString()
    history.replaceState(null, null, newLocation)
  }
  return index
}
const OpiskeluoikeudenOpintosuoritusoteLink = React.createClass({
  render() {
    let {opiskeluoikeus} = this.props
    let oppijaOid = opiskeluoikeus.context.oppijaOid
    var opiskeluoikeusTyyppi = modelData(opiskeluoikeus, 'tyyppi').koodiarvo
    if (opiskeluoikeusTyyppi === 'lukiokoulutus' || opiskeluoikeusTyyppi === 'ibtutkinto') { // lukio/ib näytetään opiskeluoikeuskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?opiskeluoikeus=' + modelData(opiskeluoikeus, 'id')
      return <a className="opintosuoritusote" href={href}><Text name="näytä opintosuoritusote"/></a>
    } else if (opiskeluoikeusTyyppi === 'korkeakoulutus') { // korkeakoulutukselle näytetään oppilaitoskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?oppilaitos=' + modelData(opiskeluoikeus, 'oppilaitos').oid
      return <a className="opintosuoritusote" href={href}><Text name="näytä opintosuoritusote"/></a>
    } else {
      return null
    }
  }
})

let näytettäväPäätasonSuoritus = s => !['perusopetuksenvuosiluokka'].includes(modelData(s).tyyppi.koodiarvo)

export const näytettävätPäätasonSuoritukset = (opiskeluoikeus) => {
  let päätasonSuoritukset = modelItems(opiskeluoikeus, 'suoritukset').filter(näytettäväPäätasonSuoritus)
  let makeGroupTitle = (suoritus) => {
    switch (suorituksenTyyppi(suoritus)) {
      case 'perusopetuksenoppiaineenoppimaara': return 'oppiainetta'
      case 'korkeakoulunopintojakso': return 'opintojaksoa'
      default: return ''
    }
  }

  let grouped = R.toPairs(R.groupBy(makeGroupTitle, päätasonSuoritukset)).map(([groupTitle, suoritukset]) => {
    let title = groupTitle && (suoritukset.length > 1)
      ? <span>{suoritukset.length}{' '}<Text name={groupTitle}/></span>
      : suoritusTitle(suoritukset[0])
    return { title, suoritukset }
  })
  return grouped
}

const suoritusTitle = (suoritus) => {
  let title = modelTitle(suoritus, 'koulutusmoduuli.tunniste')
  return suorituksenTyyppi(suoritus) == 'ammatillinentutkintoosittainen'
    ? `${title}, osittainen`
    : title
}