import React from 'baret'
import Atom from 'bacon.atom'
import {modelData, modelLookup, modelTitle, modelItems, addContext} from './EditorModel.js'
import {PropertyEditor} from './PropertyEditor.jsx'
import {TogglableEditor} from './TogglableEditor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {OpiskeluoikeudenTilaEditor, onLopputilassa} from './OpiskeluoikeudenTilaEditor.jsx'
import Versiohistoria from '../Versiohistoria.jsx'
import Link from '../Link.jsx'
import {currentLocation} from '../location.js'
import {yearFromFinnishDateString} from '../date'
import {SuoritusEditor} from './SuoritusEditor.jsx'
import {ExpandablePropertiesEditor} from './ExpandablePropertiesEditor.jsx'
import UusiPerusopetuksenSuoritusPopup from './UusiPerusopetuksenSuoritusPopup.jsx'
import {Editor} from './GenericEditor.jsx'
import {navigateTo} from '../location'

export const OpiskeluoikeusEditor = React.createClass({
  render() {
    return (<TogglableEditor model={this.props.model} renderChild={ (model, editLink) => {
      let context = model.context
      let id = modelData(model, 'id')
      let suoritukset = modelItems(model, 'suoritukset')
      let excludedProperties = ['suoritukset', 'alkamispäivä', 'arvioituPäättymispäivä', 'päättymispäivä', 'oppilaitos', 'lisätiedot']
      let päättymispäiväProperty = (modelData(model, 'arvioituPäättymispäivä') && !modelData(model, 'päättymispäivä')) ? 'arvioituPäättymispäivä' : 'päättymispäivä'
      var suoritusIndex = SuoritusTabs.suoritusIndex(model)
      if (suoritusIndex >= suoritukset.length) {
        navigateTo(SuoritusTabs.urlForTab(model, tabName(suoritukset[0])))
        return null
      }
      let valittuSuoritus = suoritukset[suoritusIndex]

      return(
        <div className="opiskeluoikeus">
          <h3>
            <span className="oppilaitos inline-text">{modelTitle(model, 'oppilaitos')},</span>
            <span className="koulutus inline-text">{modelTitle(modelLookup(model, 'suoritukset').value.find(SuoritusEditor.näytettäväPäätasonSuoritus), 'koulutusmoduuli')}</span>
             { modelData(model, 'alkamispäivä')
                ? <span className="inline-text">(
                      <span className="alku pvm">{yearFromFinnishDateString(modelTitle(model, 'alkamispäivä'))}</span>-
                      <span className="loppu pvm">{yearFromFinnishDateString(modelTitle(model, 'päättymispäivä'))},</span>
                  </span>
                : null
              }
            <span className="tila">{modelTitle(model, 'tila.opiskeluoikeusjaksot.-1.tila').toLowerCase()})</span>
            <Versiohistoria opiskeluoikeusId={id} oppijaOid={context.oppijaOid}/>
          </h3>
          <div className="opiskeluoikeus-content">
            <div className={model.context.edit ? 'opiskeluoikeuden-tiedot editing' : 'opiskeluoikeuden-tiedot'}>
              {editLink}
              <OpiskeluoikeudenOpintosuoritusoteLink opiskeluoikeus={model}/>
              <div className="alku-loppu">
                <PropertyEditor model={addContext(model, {edit: false})} propertyName="alkamispäivä" /> — <PropertyEditor model={addContext(model, {edit: false})} propertyName={päättymispäiväProperty} />
              </div>
              <PropertiesEditor
                model={model}
                propertyFilter={ p => !excludedProperties.includes(p.key) }
                getValueEditor={ (prop, getDefault) => prop.key == 'tila'
                  ? <OpiskeluoikeudenTilaEditor model={model} />
                  : getDefault() }
               />
              <ExpandablePropertiesEditor model={model} propertyName="lisätiedot" />
            </div>
            <div className="suoritukset">
              <h4>Suoritukset</h4>
              <SuoritusTabs model={model}/>
              <SuoritusEditor key={tabName(valittuSuoritus)} model={addContext(valittuSuoritus, {opiskeluoikeusId: id})} />
            </div>
          </div>
        </div>)
      }
    } />)
  }
})

const SuoritusTabs = ({ model }) => {
  let suoritukset = modelItems(model, 'suoritukset')
  let tyyppi = modelData(model, 'tyyppi.koodiarvo')
  let addingAtom = Atom(false)
  let uusiSuoritusCallback = (suoritus) => {
    addingAtom.set(false)
    if (suoritus) {
      model.context.changeBus.push([suoritus.context, suoritus])
      model.context.doneEditingBus.push((oppija) => {
        navigateTo(SuoritusTabs.urlForTab(model, tabName(suoritus)))
      })
    }
  }
  return (<ul className="suoritus-tabs">
    {
      suoritukset.map((suoritusModel, i) => {
        let selected = i == SuoritusTabs.suoritusIndex(model)
        let titleEditor = <Editor edit="false" model={suoritusModel} path="koulutusmoduuli.tunniste"/>
        return (<li className={selected ? 'selected': null} key={i}>
          { selected ? titleEditor : <Link href={ SuoritusTabs.urlForTab(model, tabName(suoritusModel)) }> {titleEditor} </Link>}
        </li>)
      })
    }
    {
      model.context.edit && !onLopputilassa(model) && UusiPerusopetuksenSuoritusPopup.canAddSuoritus(model) && (
        <li className="add-suoritus"><a onClick={() => { addingAtom.set(true) }}><span className="plus"></span>lisää suoritus</a></li>
      )
    }
    {
      addingAtom.map(adding => adding && <UusiPerusopetuksenSuoritusPopup opiskeluoikeus={model} resultCallback={uusiSuoritusCallback}/>)
    }
  </ul>
)}

SuoritusTabs.urlForTab = (model, i) => currentLocation().addQueryParams({[SuoritusTabs.suoritusQueryParam(model.context)]: i}).toString()
SuoritusTabs.suoritusQueryParam = context => context.path + '.suoritus'
SuoritusTabs.suoritusIndex = (model) => {
  var paramName = SuoritusTabs.suoritusQueryParam(model.context)
  let index = currentLocation().params[paramName] || 0
  if (!isNaN(index)) return index // numeric index
  return modelItems(model, 'suoritukset').map(tabName).indexOf(index)
}

const tabName = (suoritus) =>  modelTitle(suoritus, 'koulutusmoduuli.tunniste')

const OpiskeluoikeudenOpintosuoritusoteLink = React.createClass({
  render() {
    let {opiskeluoikeus} = this.props
    let oppijaOid = opiskeluoikeus.context.oppijaOid
    var opiskeluoikeusTyyppi = modelData(opiskeluoikeus, 'tyyppi').koodiarvo
    if (opiskeluoikeusTyyppi == 'lukiokoulutus' || opiskeluoikeusTyyppi == 'ibtutkinto') { // lukio/ib näytetään opiskeluoikeuskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?opiskeluoikeus=' + modelData(opiskeluoikeus, 'id')
      return <a className="opintosuoritusote" href={href}>näytä opintosuoritusote</a>
    } else if (opiskeluoikeusTyyppi == 'korkeakoulutus') { // korkeakoulutukselle näytetään oppilaitoskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?oppilaitos=' + modelData(opiskeluoikeus, 'oppilaitos').oid
      return <a className="opintosuoritusote" href={href}>näytä opintosuoritusote</a>
    } else {
      return null
    }
  }
})