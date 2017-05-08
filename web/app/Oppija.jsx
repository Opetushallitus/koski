import React from 'baret'
import Bacon from 'baconjs'
import Http from './http'
import {
  modelTitle,
  modelLookup,
  modelData,
  applyChanges,
  getModelFromChange,
  getPathFromChange,
  modelValid
} from './editor/EditorModel'
import {editorMapping} from './editor/Editors.jsx'
import {Editor} from './editor/Editor.jsx'
import R from 'ramda'
import {currentLocation} from './location.js'
import {OppijaHaku} from './OppijaHaku.jsx'
import Link from './Link.jsx'
import {increaseLoading, decreaseLoading} from './loadingFlag'
import delays from './delays'
import {previousLocation, navigateToOppija, navigateWithQueryParams, locationP} from './location'

Bacon.Observable.prototype.flatScan = function(seed, f) {
  let current = seed
  return this.flatMapConcat(next =>
    f(current, next).doAction(updated => current = updated)
  ).toProperty(seed)
}

export const saveBus = Bacon.Bus()

let currentState = null

export const reloadOppija = () => {
  let oppijaOid = currentState.oppijaOid
  currentState = null
  navigateToOppija({oid: oppijaOid})
}

export const oppijaContentP = (oppijaOid) => {
  let version = currentLocation().params['versionumero']
  if (!currentState || currentState.oppijaOid != oppijaOid || currentState.version != version) {
    currentState = {
      oppijaOid, version, state: createState(oppijaOid)
    }
  }
  return stateToContent(currentState.state)
}

const createState = (oppijaOid) => {
  const changeBus = Bacon.Bus()
  const editBus = Bacon.Bus()
  const saveChangesBus = Bacon.Bus()
  const cancelChangesBus = Bacon.Bus()
  const editingP = locationP.map(loc => !!loc.params.edit)
  const stateP = Bacon.update(currentLocation().params.edit ? 'editing' : 'viewing',
    editingP.changes(), (prev, edit) => prev == 'saved' && !edit ? 'saved' : (edit ? 'editing' : 'viewing'),
    saveChangesBus, (prev) => prev == 'dirty' ? 'saved' : 'viewing',
    saveChangesBus.delay(5000), (prev) => prev == 'saved' ? 'viewing' : prev,
    changeBus, () => 'dirty'
  ).skipDuplicates()

  cancelChangesBus.onValue(() => navigateWithQueryParams({edit: false}))
  editBus.onValue((opiskeluoikeusId) => navigateWithQueryParams({edit: opiskeluoikeusId}))

  const queryString = currentLocation().filterQueryParams(key => ['opiskeluoikeus', 'versionumero'].includes(key)).queryString

  const oppijaEditorUri = `/koski/api/editor/${oppijaOid}${queryString}`

  const loadOppijaE = Bacon.once().map(() => () => Http.cachedGet(oppijaEditorUri, { willHandleErrors: true}).map( oppija => R.merge(oppija, { event: 'load' })))

  let changeBuffer = null

  const shouldThrottle = (batch) => {
    let model = getModelFromChange(batch[0])
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
        let batchEndE = shouldThrottle(firstBatch) ? Bacon.later(delays().stringInput).merge(saveChangesBus).take(1) : Bacon.once()
        return batchEndE.flatMap(() => {
          let opiskeluoikeusPath = getPathFromChange(firstBatch[0]).slice(0, 6).join('.')
          let opiskeluoikeusId = modelData(oppijaBeforeChange, opiskeluoikeusPath).id

          let batch = changeBuffer
          changeBuffer = null
          //console.log("Apply", batch.length / 2, "changes:", batch)
          let locallyModifiedOppija = applyChanges(oppijaBeforeChange, batch)
          return R.merge(locallyModifiedOppija, {event: 'modify', opiskeluoikeusId})
        })
      })
    }
  })

  const saveOppijaE = saveChangesBus.map(() => oppijaBeforeSave => {
    if (oppijaBeforeSave.event != 'modify') {
      return Bacon.once(oppijaBeforeSave)
    }
    var oppijaData = modelData(oppijaBeforeSave)
    let opiskeluoikeusId = oppijaBeforeSave.opiskeluoikeusId
    let opiskeluoikeudet = oppijaData.opiskeluoikeudet.flatMap(x => x.opiskeluoikeudet).flatMap(x => x.opiskeluoikeudet)
    let opiskeluoikeus = opiskeluoikeudet.find(x => x.id == opiskeluoikeusId)

    let oppijaUpdate = {
      henkilö: {oid: oppijaOid},
      opiskeluoikeudet: [opiskeluoikeus]
    }

    return Http.put('/koski/api/oppija', oppijaUpdate, { willHandleErrors: true, invalidateCache: ['/koski/api/oppija', '/koski/api/opiskeluoikeus', '/koski/api/editor/' + oppijaOid]})
      .flatMap(() => Http.cachedGet(oppijaEditorUri, { willHandleErrors: true}))
      .map( oppija => R.merge(oppija, { event: 'save' }))
  })

  const cancelE = cancelChangesBus.map(() => () => {
    return Http.cachedGet(oppijaEditorUri, { willHandleErrors: true})
  })

  let allUpdatesE = Bacon.mergeAll(loadOppijaE, localModificationE, saveOppijaE, cancelE) // :: EventStream [Model -> EventStream[Model]]

  let oppijaP = allUpdatesE.flatScan({ loading: true }, (currentOppija, updateF) => {
    increaseLoading()
    return updateF(currentOppija).doAction(decreaseLoading)
  })
  oppijaP.onValue()
  oppijaP.map('.event').filter(event => event == 'save').onValue(() => saveBus.push(true))

  return { oppijaP, changeBus, editBus, saveChangesBus, cancelChangesBus, stateP}
}

const listviewPath = () => {
  let prev = previousLocation()
  return (prev && prev.path == '/koski/') ? prev : '/koski/'
}

const stateToContent = ({ oppijaP, changeBus, editBus, saveChangesBus, cancelChangesBus, stateP}) => oppijaP.map(oppija => ({
  content: (<div className='content-area'><div className="main-content oppija">
    <OppijaHaku/>
    <Link className="back-link" href={listviewPath()}>Opiskelijat</Link>
    <ExistingOppija {...{oppija, changeBus, editBus, saveChangesBus, cancelChangesBus, stateP}}/>
  </div></div>),
  title: modelData(oppija, 'henkilö') ? 'Oppijan tiedot' : ''
}))


export const ExistingOppija = React.createClass({
  render() {
    let {oppija, changeBus, editBus, saveChangesBus, cancelChangesBus, stateP} = this.props
    oppija = Editor.setupContext(oppija, {saveChangesBus, editBus, changeBus, editorMapping})
    let henkilö = modelLookup(oppija, 'henkilö')
    let hetu = modelTitle(henkilö, 'hetu')
    return oppija.loading
      ? <div className="loading"/>
      : (
        <div>
          <h2>{modelTitle(henkilö, 'sukunimi')}, {modelTitle(henkilö, 'etunimet')} <span
            className='hetu'>{hetu && '(' + hetu + ')'}</span>
            <a className="json" href={`/koski/api/oppija/${modelData(henkilö, 'oid')}`}>JSON</a>
          </h2>
          {
            // Set location as key to ensure full re-render when context changes
            oppija
              ? <Editor key={document.location.toString()} model={oppija}/>
              : null
          }
          <EditBar {...{saveChangesBus, cancelChangesBus, stateP, oppija}}/>
        </div>
    )
  }
})

const EditBar = ({stateP, saveChangesBus, cancelChangesBus, oppija}) => {
  let saveChanges = (e) => {
    e.preventDefault()
    saveChangesBus.push()
    navigateWithQueryParams({edit: undefined})
  }

  let dirtyP = stateP.map(state => state == 'dirty')
  let validationErrorP = dirtyP.map(dirty => dirty && !modelValid(oppija))
  let canSaveP = dirtyP.and(validationErrorP.not())

  let classNameP = stateP

  return (<div id="edit-bar" className={classNameP}>
    <a className={stateP.map(state => ['editing', 'dirty'].includes(state) ? 'cancel' : 'cancel 179disabled')} onClick={ () => cancelChangesBus.push() }>Peruuta</a>
    <button disabled={canSaveP.not()} onClick={saveChanges}>Tallenna</button>
    <span className="state-indicator">{stateP.map(state => state == 'dirty' ? 'Tallentamattomia muutoksia' : (state == 'saved' ? 'Kaikki muutokset tallennettu' : ''))}</span>
  </div>)
}