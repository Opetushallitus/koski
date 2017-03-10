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
    let {model} = this.props
    let context = model.context
    let id = modelData(model, 'id')
    let suoritukset = modelItems(model, 'suoritukset')
    let excludedProperties = ['suoritukset', 'alkamispäivä', 'arvioituPäättymispäivä', 'päättymispäivä', 'oppilaitos', 'lisätiedot']
    let päättymispäiväProperty = (modelData(model, 'arvioituPäättymispäivä') && !modelData(model, 'päättymispäivä')) ? 'arvioituPäättymispäivä' : 'päättymispäivä'

    return (<TogglableEditor model={model} renderChild={ (mdl, editLink) => (<div className="opiskeluoikeus">
      <h3>
        <span className="oppilaitos inline-text">{modelTitle(mdl, 'oppilaitos')},</span>
        <span className="koulutus inline-text">{modelTitle(modelLookup(mdl, 'suoritukset').value.find(SuoritusEditor.näytettäväPäätasonSuoritus), 'koulutusmoduuli')}</span>
         { modelData(mdl, 'alkamispäivä')
            ? <span className="inline-text">(
                  <span className="alku pvm">{yearFromFinnishDateString(modelTitle(mdl, 'alkamispäivä'))}</span>-
                  <span className="loppu pvm">{yearFromFinnishDateString(modelTitle(mdl, 'päättymispäivä'))},</span>
              </span>
            : null
          }
        <span className="tila">{modelTitle(mdl, 'tila.opiskeluoikeusjaksot.-1.tila').toLowerCase()})</span>
        <Versiohistoria opiskeluoikeusId={id} oppijaOid={context.oppijaOid}/>
      </h3>
      <div className="opiskeluoikeus-content">
        <div className={mdl.context.edit ? 'opiskeluoikeuden-tiedot editing' : 'opiskeluoikeuden-tiedot'}>
          {editLink}
          <OpiskeluoikeudenOpintosuoritusoteLink opiskeluoikeus={mdl}/>
          <div className="alku-loppu">
            <PropertyEditor model={addContext(mdl, {edit: false})} propertyName="alkamispäivä" /> — <PropertyEditor model={addContext(mdl, {edit: false})} propertyName={päättymispäiväProperty} />
          </div>
          <PropertiesEditor
            model={mdl}
            propertyFilter={ p => !excludedProperties.includes(p.key) }
            getValueEditor={ (prop, getDefault) => prop.key == 'tila'
              ? <OpiskeluoikeudenTilaEditor model={modelLookup(prop.model, 'opiskeluoikeusjaksot')} opiskeluoikeusModel={mdl} />
              : getDefault() }
           />
          <ExpandablePropertiesEditor model={mdl} propertyName="lisätiedot" />
        </div>
        <div className="suoritukset">
          {
            suoritukset.length >= 2 ? (
              <div>
                <h4>Suoritukset</h4>
                <SuoritusTabs {...{ context, suoritukset}}/>
              </div>
            ) : <hr/>
          }
          {
            suoritukset.map((suoritusModel, i) =>
              i == SuoritusTabs.suoritusIndex(context)
                ? <SuoritusEditor model={addContext(suoritusModel, {opiskeluoikeusId: id})} key={i}/> : null
            )
          }
        </div>
      </div>
    </div>)
    } />)
  }
})

const SuoritusTabs = ({ suoritukset, context }) => (<ul className="suoritus-tabs">
    {
      suoritukset.map((suoritusModel, i) => {
        let selected = i == SuoritusTabs.suoritusIndex(context)
        let title = modelTitle(suoritusModel, 'koulutusmoduuli')
        return (<li className={selected ? 'selected': null} key={i}>
          { selected ? title : <Link href={currentLocation().addQueryParams({[SuoritusTabs.suoritusQueryParam(context)]: i}).toString()}> {title} </Link>}
        </li>)
      })
    }
  </ul>
)
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