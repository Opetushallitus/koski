import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import { modelTitle, modelLookup, modelSet, objectLookup } from './editor/EditorModel'
import { editorMapping } from './editor/OppijaEditor.jsx'
import { Editor } from './editor/GenericEditor.jsx'
import * as L from 'partial.lenses'
import R from 'ramda'
import {modelData} from './editor/EditorModel.js'
import {currentLocation} from './location.js'
import { oppijaHakuElementP } from './OppijaHaku.jsx'
import Link from './Link.jsx'

export const saveBus = Bacon.Bus()

export const oppijaContentP = (oppijaOid) => {
  const changeBus = Bacon.Bus()
  const doneEditingBus = Bacon.Bus()

  const queryString = currentLocation().filterQueryParams(key => ['opiskeluoikeus', 'versionumero'].includes(key)).queryString

  const oppijaEditorUri = `/koski/api/editor/${oppijaOid}${queryString}`

  const updateResultE = Bacon.Bus()

  const saveE = changeBus // might consider saving only when done editing
  const savingP = saveE.awaiting(updateResultE)

  const loadOppijaE = Http.cachedGet(oppijaEditorUri).toEventStream().concat(doneEditingBus.holdWhen(savingP).flatMap(() => Http.cachedGet(oppijaEditorUri, { force: true })))

  const oppijaP = Bacon.update({ loading: true },
    loadOppijaE, (previous, oppija) => oppija,
    updateResultE.map('.opiskeluoikeudet').flatMap(Bacon.fromArray), (currentOppija, {id, versionumero}) => {
      let correctId = R.whereEq({id})
      let containsOpiskeluoikeus = (oppilaitoksenOpiskeluoikeudet) => oppilaitoksenOpiskeluoikeudet.opiskeluoikeudet.find(correctId)
      let containsOppilaitos = (tyypinOpiskeluoikeudet) => tyypinOpiskeluoikeudet.opiskeluoikeudet.find(containsOpiskeluoikeus)

      let lens = L.compose('value', 'data', 'opiskeluoikeudet', L.find(containsOppilaitos), 'opiskeluoikeudet', L.find(containsOpiskeluoikeus), 'opiskeluoikeudet', L.find(correctId), 'versionumero')
      var modified = L.set(lens, versionumero, currentOppija)
      return modified
    },
    changeBus, (currentOppija, [context, value]) => {
      var modifiedModel = modelSet(currentOppija, context.path, value)
      return modifiedModel
    }
  )


  updateResultE.plug(oppijaP
    .sampledBy(saveE, (oppija, [context]) => ({oppija, context}))
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

  return Bacon.combineWith(oppijaP, oppijaHakuElementP, (oppija, haku) => {
    return {
      content: (<div className='content-area'><div className="main-content oppija">
        { haku }
        <Link className="back-link" href="/koski/">Opiskelijat</Link>
        <ExistingOppija {...{oppija, changeBus, doneEditingBus}}/>
        </div></div>),
      title: modelData(oppija, 'henkilö') ? 'Oppijan tiedot' : ''
    }
  })
}

export const ExistingOppija = React.createClass({
  render() {
    let {oppija, changeBus, doneEditingBus} = this.props
    let henkilö = modelLookup(oppija, 'henkilö')
    return oppija.loading
      ? <div className="loading"/>
      : (
        <div>
          <h2>{modelTitle(henkilö, 'sukunimi')}, {modelTitle(henkilö, 'etunimet')} <span
            className='hetu'>({modelTitle(henkilö, 'hetu')})</span>
            <a className="json" href={`/koski/api/oppija/${modelData(henkilö, 'oid')}`}>JSON</a>
          </h2>
          {
            oppija
              ? <Editor model={oppija} {... {doneEditingBus, changeBus, editorMapping}}/>
              : null
          }
        </div>
    )
  }
})