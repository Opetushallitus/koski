import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import { modelTitle, modelLookup, modelSet, objectLookup } from './editor/EditorModel'
import { editorMapping } from './editor/Editors.jsx'
import { Editor } from './editor/GenericEditor.jsx'
import R from 'ramda'
import {modelData} from './editor/EditorModel.js'
import {currentLocation} from './location.js'
import { OppijaHaku } from './OppijaHaku.jsx'
import Link from './Link.jsx'
import { increaseLoading, decreaseLoading } from './loadingFlag'
import { delays } from './delays'

Bacon.Observable.prototype.flatScan = function(seed, f) {
  let current = seed
  return this.flatMapConcat(next =>
    f(current, next).doAction(updated => current = updated)
  ).toProperty(seed)
}

export const saveBus = Bacon.Bus()

export const oppijaContentP = (oppijaOid) => {
  const changeBus = Bacon.Bus()
  const errorBus = Bacon.Bus()
  const doneEditingBus = Bacon.Bus()

  const queryString = currentLocation().filterQueryParams(key => ['opiskeluoikeus', 'versionumero'].includes(key)).queryString

  const oppijaEditorUri = `/koski/api/editor/${oppijaOid}${queryString}`

  const loadOppijaE = Bacon.once().map(() => () => Http.cachedGet(oppijaEditorUri).map( oppija => R.merge(oppija, { event: 'load' })))

  let changeBuffer = null

  const shouldThrottle = (batch) => {
    let model = batch[1]
    return model && model.type == 'string'
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
          let locallyModifiedOppija = R.splitEvery(2, batch).reduce((acc, [context, model]) => modelSet(acc, model, context.path), oppijaBeforeChange)
          return R.merge(locallyModifiedOppija, {event: 'modify', opiskeluoikeusPath})
        })
      })
    }
  })

  const saveOppijaE = doneEditingBus.map(() => oppijaBeforeSave => {
    if (!oppijaBeforeSave.opiskeluoikeusPath) {
      return Bacon.once(oppijaBeforeSave)
    }
    var oppijaData = oppijaBeforeSave.value.data
    let opiskeluoikeus = objectLookup(oppijaData, oppijaBeforeSave.opiskeluoikeusPath)
    let oppijaUpdate = {
      henkilö: {oid: oppijaData.henkilö.oid},
      opiskeluoikeudet: [opiskeluoikeus]
    }

    return Http.put('/koski/api/oppija', oppijaUpdate)
      .flatMap(() => Http.cachedGet(oppijaEditorUri, { force: true }))
      .map( oppija => R.merge(oppija, { event: 'save' }))
  })

  let allUpdatesE = Bacon.mergeAll(loadOppijaE, localModificationE, saveOppijaE) // :: EventStream [Model -> EventStream[Model]]

  let oppijaP = allUpdatesE.flatScan({ loading: true }, (currentOppija, updateF) => {
    increaseLoading()
    return updateF(currentOppija).doAction(decreaseLoading)
  })
  oppijaP.onValue()
  oppijaP.map('.event').filter(event => event == 'save').onValue(() => saveBus.push(true))

  return Bacon.combineWith(oppijaP, (oppija) => {
    return {
      content: (<div className='content-area'><div className="main-content oppija">
        <OppijaHaku/>
        <Link className="back-link" href="/koski/">Opiskelijat</Link>
        <ExistingOppija {...{oppija, changeBus, errorBus, doneEditingBus}}/>
        </div></div>),
      title: modelData(oppija, 'henkilö') ? 'Oppijan tiedot' : ''
    }
  })
}

export const ExistingOppija = React.createClass({
  render() {
    let {oppija, changeBus, errorBus, doneEditingBus} = this.props
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
              ? <Editor model={oppija} {... {doneEditingBus, changeBus, errorBus, editorMapping}}/>
              : null
          }
        </div>
    )
  }
})