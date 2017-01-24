import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import { modelTitle, modelLookup, modelSet, objectLookup } from './EditorModel'
import { editorMapping } from './OppijaEditor.jsx'
import { Editor } from './GenericEditor.jsx'
import * as L from 'partial.lenses'
import R from 'ramda'
import {modelData} from './EditorModel.js'

export const saveBus = Bacon.Bus()

export const oppijaContentP = (oppijaOid, queryString) => {
  const changeBus = Bacon.Bus()

  const loadOppijaE = Http.cachedGet(`/koski/api/editor/${oppijaOid}${queryString}`).toEventStream()

  const updateResultE = Bacon.Bus()

  const oppijaP = Bacon.update({ loading: true },
    loadOppijaE, (previous, oppija) => oppija,
    updateResultE.map('.opiskeluoikeudet').flatMap(Bacon.fromArray), (currentOppija, {id, versionumero}) => {
      let correctId = R.whereEq({id})
      let containsOpiskeluoikeus = (oppilaitos) => oppilaitos.opiskeluoikeudet.find(correctId)
      let lens = L.compose('value', 'data', 'opiskeluoikeudet', L.find(containsOpiskeluoikeus), 'opiskeluoikeudet', L.find(correctId), 'versionumero')
      return L.set(lens, versionumero, currentOppija)
    },
    changeBus, (currentOppija, [context, value]) => {
      var modifiedModel = modelSet(currentOppija, context.path, value)
      return modifiedModel
    }
  )

  updateResultE.plug(oppijaP
    .sampledBy(changeBus, (oppija, [context]) => ({oppija, context}))
    .flatMapLatest(({oppija, context: {path}}) => {
      let opiskeluoikeusPath = path.split('.').slice(0, 6)
      var oppijaData = oppija.value.data
      let opiskeluoikeus = objectLookup(oppijaData, opiskeluoikeusPath.join('.'))
      let oppijaUpdate = {
        henkilö: {oid: oppijaData.henkilö.oid},
        opiskeluoikeudet: [opiskeluoikeus]
      }
      return Http.put('/koski/api/oppija', oppijaUpdate)
    })
  )

  saveBus.plug(updateResultE.map(true))

  return oppijaP.map((oppija) => {
    return {
      content: (<div className='content-area'>
        {
          oppija.loading
            ? <div className='main-content oppija loading'></div>
            : <ExistingOppija oppija={oppija} changeBus={changeBus}/>
        }
      </div>),
      title: modelData(oppija, 'henkilö') ? 'Oppijan tiedot' : ''
    }
  })
}

export const ExistingOppija = React.createClass({
  render() {
    let {oppija, changeBus} = this.props
    let henkilö = modelLookup(oppija, 'henkilö')
    return (
      <div className='main-content oppija'>
        <h2>{modelTitle(henkilö, 'sukunimi')}, {modelTitle(henkilö, 'etunimet')} <span className='hetu'>{modelTitle(henkilö, 'hetu')}</span>
          <a className="json" href={`/koski/api/oppija/${modelData(henkilö, 'oid')}`}>JSON</a>
        </h2>
        <hr></hr>
        <h4>Opiskeluoikeudet</h4>
        {
          oppija
            ? <Editor model={oppija} editorMapping={editorMapping} changeBus={changeBus} />
            : null
        }
      </div>
    )
  }
})