import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import { modelTitle, modelLookup, modelSet, objectLookup } from './editor/EditorModel'
import { editorMapping } from './editor/Editors.jsx'
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

  const loadOppijaE = Bacon.once().map(() => () => Http.cachedGet(oppijaEditorUri))

  const refreshFromServerE = doneEditingBus.map(() => () => Http.cachedGet(oppijaEditorUri, { force: true }))

  const savedOppijaE = changeBus.map((contextValuePairs) => ((oppijaBeforeChange) => {
    let locallyModifiedOppija = R.splitEvery(2, contextValuePairs).reduce((acc, [context, value]) => modelSet(acc, context.path, value), oppijaBeforeChange)

    let firstPath = contextValuePairs[0]
    let opiskeluoikeusPath = firstPath.split('.').slice(0, 6)
    var oppijaData = locallyModifiedOppija.value.data
    let opiskeluoikeus = objectLookup(oppijaData, opiskeluoikeusPath.join('.'))
    let oppijaUpdate = {
      henkilö: {oid: oppijaData.henkilö.oid},
      opiskeluoikeudet: [opiskeluoikeus]
    }
    return Http.put('/koski/api/oppija', oppijaUpdate).map('.opiskeluoikeudet').flatMap(Bacon.fromArray).map(({id, versionumero}) => {
      // This is done to update versionumero based on server response
      let correctId = R.whereEq({id})
      let containsOpiskeluoikeus = (oppilaitoksenOpiskeluoikeudet) => oppilaitoksenOpiskeluoikeudet.opiskeluoikeudet.find(correctId)
      let containsOppilaitos = (tyypinOpiskeluoikeudet) => tyypinOpiskeluoikeudet.opiskeluoikeudet.find(containsOpiskeluoikeus)
      let lens = L.compose('value', 'data', 'opiskeluoikeudet', L.find(containsOppilaitos), 'opiskeluoikeudet', L.find(containsOpiskeluoikeus), 'opiskeluoikeudet', L.find(correctId), 'versionumero')
      return L.set(lens, versionumero, locallyModifiedOppija)
    })
  }))

  let allUpdatesE = Bacon.mergeAll(loadOppijaE, savedOppijaE, refreshFromServerE) // :: EventStream [Model -> EventStream[Model]]

  let current = { loading: true}
  let oppijaP = allUpdatesE.flatMapConcat(updateFunc => {
    let updateE = updateFunc(current)
    return updateE.doAction(newValue => current = newValue)
  }).toProperty(current)
  oppijaP.onValue()

  saveBus.plug(savedOppijaE.map(true))

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