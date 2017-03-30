import React from 'react'
import {modelData, modelLookup, modelTitle, modelItems, addContext} from './EditorModel.js'
import {PropertyEditor} from './PropertyEditor.jsx'
import {TogglableEditor} from './TogglableEditor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {OpiskeluoikeudenTilaEditor} from './OpiskeluoikeudenTilaEditor.jsx'
import Versiohistoria from '../Versiohistoria.jsx'
import Link from '../Link.jsx'
import {currentLocation} from '../location.js'
import {yearFromFinnishDateString} from '../date'
import {SuoritusEditor} from './SuoritusEditor.jsx'
import {ExpandablePropertiesEditor} from './ExpandablePropertiesEditor.jsx'

export const OpiskeluoikeusEditor = React.createClass({
  render() {
    return (<TogglableEditor model={this.props.model} renderChild={ (model, editLink) => {
      let context = model.context
      let id = modelData(model, 'id')
      let suoritukset = modelItems(model, 'suoritukset')
      let excludedProperties = ['suoritukset', 'alkamispäivä', 'arvioituPäättymispäivä', 'päättymispäivä', 'oppilaitos', 'lisätiedot']
      let päättymispäiväProperty = (modelData(model, 'arvioituPäättymispäivä') && !modelData(model, 'päättymispäivä')) ? 'arvioituPäättymispäivä' : 'päättymispäivä'
      let valittuSuoritus = suoritukset[SuoritusTabs.suoritusIndex(context)]
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
                  ? <OpiskeluoikeudenTilaEditor model={modelLookup(prop.model, 'opiskeluoikeusjaksot')} opiskeluoikeusModel={model} />
                  : getDefault() }
               />
              <ExpandablePropertiesEditor model={model} propertyName="lisätiedot" />
            </div>
            <div className="suoritukset">
              <h4>Suoritukset</h4>
              <SuoritusTabs model={model}/>
              <SuoritusEditor model={addContext(valittuSuoritus, {opiskeluoikeusId: id})} />
            </div>
          </div>
        </div>)
      }
    } />)
  }
})

const SuoritusTabs = ({ model }) => {
  let suoritukset = modelItems(model, 'suoritukset')
  var tyyppi = modelData(model, 'tyyppi.koodiarvo')
  return (<ul className="suoritus-tabs">
    {
      suoritukset.map((suoritusModel, i) => {
        let selected = i == SuoritusTabs.suoritusIndex(model.context)
        let title = modelTitle(suoritusModel, 'koulutusmoduuli')
        return (<li className={selected ? 'selected': null} key={i}>
          { selected ? title : <Link href={currentLocation().addQueryParams({[SuoritusTabs.suoritusQueryParam(model.context)]: i}).toString()}> {title} </Link>}
        </li>)
      })
    }
    {
      model.context.edit && tyyppi == 'perusopetus' && (
        <li className="add-suoritus"><a><span className="plus"></span>lisää suoritus</a></li>
      )
    }
  </ul>
)}

SuoritusTabs.suoritusQueryParam = context => context.path + '.suoritus'
SuoritusTabs.suoritusIndex = (context) => currentLocation().params[SuoritusTabs.suoritusQueryParam(context)] || 0

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