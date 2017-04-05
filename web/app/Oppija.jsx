import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import {modelTitle, modelLookup, objectLookup, modelData, applyChanges} from './editor/EditorModel'
import {editorMapping} from './editor/Editors.jsx'
import {Editor} from './editor/Editor.jsx'
import R from 'ramda'
import {currentLocation} from './location.js'
import {OppijaHaku} from './OppijaHaku.jsx'
import Link from './Link.jsx'
import {increaseLoading, decreaseLoading} from './loadingFlag'
import {delays} from './delays'

Bacon.Observable.prototype.flatScan = function(seed, f) {
  let current = seed
  return this.flatMapConcat(next =>
    f(current, next).doAction(updated => current = updated)
  ).toProperty(seed)
}

export const saveBus = Bacon.Bus()

let currentState = null

export const oppijaContentP = (oppijaOid) => {
  if (!currentState || currentState.oppijaOid != oppijaOid ) {
    currentState = {
      oppijaOid, state: createState(oppijaOid)
    }
  }
  return stateToContent(currentState.state)
}

const createState = (oppijaOid) => {
  const changeBus = Bacon.Bus()
  const doneEditingBus = Bacon.Bus()

  const queryString = currentLocation().filterQueryParams(key => ['opiskeluoikeus', 'versionumero'].includes(key)).queryString

  const oppijaEditorUri = `/koski/api/editor/${oppijaOid}${queryString}`

  const loadOppijaE = Bacon.once().map(() => () => Http.cachedGet(oppijaEditorUri).map( oppija => R.merge(oppija, { event: 'load' })))

  let changeBuffer = null

  const shouldThrottle = (batch) => {
    let model = batch[1]
    var willThrottle = model && (model.type == 'string' || model.type == 'date' || model.oneOfClass == 'localizedstring')
    return willThrottle
  }

  const localModificationE = changeBus.flatMap(firstBatch => {
    if (changeBuffer) {
      changeBuffer = changeBuffer.concat(firstBatch)
      return Bacon.never()
    } else {
      //console.log('start batch', firstContext)
      changeBuffer = firstBatch
      return Bacon.once(oppijaBeforeChange => {
        let batchEndE = shouldThrottle(firstBatch) ? Bacon.later(delays().stringInput).merge(doneEditingBus).take(1) : Bacon.once()
        return batchEndE.flatMap(() => {
          let firstPath = firstBatch[0].path
          let opiskeluoikeusPath = firstPath.split('.').slice(0, 6).join('.')
          let batch = changeBuffer
          changeBuffer = null
          //console.log("Apply", batch.length / 2, "changes:", batch)
          let locallyModifiedOppija = applyChanges(oppijaBeforeChange, batch)
          return R.merge(locallyModifiedOppija, {event: 'modify', opiskeluoikeusPath})
        })
      })
    }
  })

  const saveOppijaE = doneEditingBus.map((onAfterLoad) => oppijaBeforeSave => {
    if (!oppijaBeforeSave.opiskeluoikeusPath) {
      return Bacon.once(oppijaBeforeSave)
    }
    var oppijaData = modelData(oppijaBeforeSave)
    let opiskeluoikeus = objectLookup(oppijaData, oppijaBeforeSave.opiskeluoikeusPath)
    let oppijaUpdate = {
      henkilö: {oid: oppijaData.henkilö.oid},
      opiskeluoikeudet: [opiskeluoikeus]
    }

    return Http.put('/koski/api/oppija', oppijaUpdate)
      .flatMap(() => Http.cachedGet(oppijaEditorUri, { force: true }))
      .map( oppija => R.merge(oppija, { event: 'save' }))
      .doAction(onAfterLoad)
  })

  let allUpdatesE = Bacon.mergeAll(loadOppijaE, localModificationE, saveOppijaE) // :: EventStream [Model -> EventStream[Model]]

  let oppijaP = allUpdatesE.flatScan({ loading: true }, (currentOppija, updateF) => {
    increaseLoading()
    return updateF(currentOppija).doAction(decreaseLoading)
  })
  oppijaP.onValue()
  oppijaP.map('.event').filter(event => event == 'save').onValue(() => saveBus.push(true))

  return { oppijaP, changeBus, doneEditingBus}
}

const stateToContent = ({ oppijaP, changeBus, doneEditingBus}) => oppijaP.map(oppija => ({
  content: (<div className='content-area'><div className="main-content oppija">
    <OppijaHaku/>
    <Link className="back-link" href="/koski/">Opiskelijat</Link>
    <ExistingOppija {...{oppija, changeBus, doneEditingBus}}/>
  </div></div>),
  title: modelData(oppija, 'henkilö') ? 'Oppijan tiedot' : ''
}))


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