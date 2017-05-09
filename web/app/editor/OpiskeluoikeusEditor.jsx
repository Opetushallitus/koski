import React from 'baret'
import Atom from 'bacon.atom'
import R from 'ramda'
import {modelData, modelLookup, modelTitle, modelItems, addContext} from './EditorModel.js'
import {PropertyEditor} from './PropertyEditor.jsx'
import {TogglableEditor} from './TogglableEditor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {OpiskeluoikeudenTilaEditor, onLopputilassa} from './OpiskeluoikeudenTilaEditor.jsx'
import Versiohistoria from '../Versiohistoria.jsx'
import Link from '../Link.jsx'
import {currentLocation} from '../location.js'
import {yearFromIsoDateString} from '../date'
import {SuoritusEditor} from './SuoritusEditor.jsx'
import {ExpandablePropertiesEditor} from './ExpandablePropertiesEditor.jsx'
import UusiPerusopetuksenSuoritusPopup from './UusiPerusopetuksenSuoritusPopup.jsx'
import {Editor} from './Editor.jsx'
import {navigateTo} from '../location'
import {pushModel} from './EditorModel'

export const OpiskeluoikeusEditor = ({model}) => {
  let id = modelData(model, 'id')
  model = addContext(model, {opiskeluoikeusId: id})
  return (<TogglableEditor model={model} renderChild={ (mdl, editLink) => {
    mdl = addContext(mdl, {opiskeluoikeus: mdl})
    let context = mdl.context
    let suoritukset = modelItems(mdl, 'suoritukset')
    assignTabNames(suoritukset)
    let excludedProperties = ['suoritukset', 'alkamispäivä', 'arvioituPäättymispäivä', 'päättymispäivä', 'oppilaitos', 'lisätiedot']
    let päättymispäiväProperty = (modelData(mdl, 'arvioituPäättymispäivä') && !modelData(mdl, 'päättymispäivä')) ? 'arvioituPäättymispäivä' : 'päättymispäivä'
    var suoritusIndex = SuoritusTabs.suoritusIndex(mdl, suoritukset)
    if (suoritusIndex < 0 || suoritusIndex >= suoritukset.length) {
      navigateTo(SuoritusTabs.urlForTab(mdl, suoritukset[0].tabName))
      return null
    }
    let valittuSuoritus = suoritukset[suoritusIndex]

    return (
      <div className="opiskeluoikeus">
        <h3>
          <span className="oppilaitos inline-text">{modelTitle(mdl, 'oppilaitos')},</span>
          <span className="koulutus inline-text">{modelTitle(suoritukset.find(SuoritusEditor.näytettäväPäätasonSuoritus), 'koulutusmoduuli')}</span>
           { modelData(mdl, 'alkamispäivä')
              ? <span className="inline-text">(
                    <span className="alku pvm">{yearFromIsoDateString(modelTitle(mdl, 'alkamispäivä'))}</span>-
                    <span className="loppu pvm">{yearFromIsoDateString(modelTitle(mdl, 'päättymispäivä'))},</span>
                </span>
              : null
            }
          <span className="tila">{modelTitle(mdl, 'tila.opiskeluoikeusjaksot.-1.tila').toLowerCase()})</span>
          <Versiohistoria opiskeluoikeusId={id} oppijaOid={context.oppijaOid}/>
        </h3>
        <div className={mdl.context.edit ? 'opiskeluoikeus-content editing' : 'opiskeluoikeus-content'}>
          <div className="opiskeluoikeuden-tiedot">
            {editLink}
            <OpiskeluoikeudenOpintosuoritusoteLink opiskeluoikeus={mdl}/>
            <div className="alku-loppu">
              <PropertyEditor model={addContext(mdl, {edit: false})} propertyName="alkamispäivä" /> — <PropertyEditor model={addContext(mdl, {edit: false})} propertyName={päättymispäiväProperty} />
            </div>

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
            <h4>Suoritukset</h4>
            <SuoritusTabs model={mdl} suoritukset={suoritukset}/>
            <Editor key={valittuSuoritus.tabName} model={valittuSuoritus} alwaysUpdate="true" />
          </div>

        </div>
      </div>)
    }
  } />)
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
      var tabName = modelTitle(suoritus, 'koulutusmoduuli.tunniste')
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
  return (<ul className="suoritus-tabs">
    {
      suoritukset.map((suoritusModel, i) => {
        let selected = i === SuoritusTabs.suoritusIndex(model, suoritukset)
        let titleEditor = <Editor edit="false" model={suoritusModel} path="koulutusmoduuli.tunniste"/>
        return (<li className={selected ? 'selected': null} key={i}>
          { selected ? titleEditor : <Link href={ SuoritusTabs.urlForTab(model, suoritusModel.tabName) } exitHook={false}> {titleEditor} </Link>}
        </li>)
      })
    }
    {
      model.context.edit && !onLopputilassa(model) && UusiPerusopetuksenSuoritusPopup.canAddSuoritus(model) && (
        <li className="add-suoritus"><a onClick={() => { addingAtom.modify(x => !x) }}><span className="plus"></span>lisää vuosiluokan suoritus</a></li>
      )
    }
    {
      addingAtom.map(adding => adding && <UusiPerusopetuksenSuoritusPopup opiskeluoikeus={model} resultCallback={uusiSuoritusCallback}/>)
    }
  </ul>
)}

SuoritusTabs.urlForTab = (model, i) => currentLocation().addQueryParams({[SuoritusTabs.suoritusQueryParam(model.context)]: i}).toString()
SuoritusTabs.suoritusQueryParam = context => context.opiskeluoikeusId + '.suoritus'
SuoritusTabs.suoritusIndex = (model, suoritukset) => {
  var paramName = SuoritusTabs.suoritusQueryParam(model.context)
  let index = currentLocation().params[paramName] || 0
  if (!isNaN(index)) return index // numeric index
  return suoritukset.map(s => s.tabName).indexOf(index)
}

const OpiskeluoikeudenOpintosuoritusoteLink = React.createClass({
  render() {
    let {opiskeluoikeus} = this.props
    let oppijaOid = opiskeluoikeus.context.oppijaOid
    var opiskeluoikeusTyyppi = modelData(opiskeluoikeus, 'tyyppi').koodiarvo
    if (opiskeluoikeusTyyppi === 'lukiokoulutus' || opiskeluoikeusTyyppi === 'ibtutkinto') { // lukio/ib näytetään opiskeluoikeuskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?opiskeluoikeus=' + modelData(opiskeluoikeus, 'id')
      return <a className="opintosuoritusote" href={href}>näytä opintosuoritusote</a>
    } else if (opiskeluoikeusTyyppi === 'korkeakoulutus') { // korkeakoulutukselle näytetään oppilaitoskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?oppilaitos=' + modelData(opiskeluoikeus, 'oppilaitos').oid
      return <a className="opintosuoritusote" href={href}>näytä opintosuoritusote</a>
    } else {
      return null
    }
  }
})