import React from 'baret'
import Bacon from 'baconjs'
import Http from './http'
import {
  applyChangesAndValidate,
  getModelFromChange,
  getPathFromChange,
  modelData,
  modelLookup,
  modelTitle,
  modelValid
} from './editor/EditorModel'
import {editorMapping} from './editor/Editors.jsx'
import {Editor} from './editor/Editor.jsx'
import R from 'ramda'
import {currentLocation} from './location.js'
import {OppijaHaku} from './OppijaHaku.jsx'
import Link from './Link.jsx'
import {decreaseLoading, increaseLoading} from './loadingFlag'
import delays from './delays'
import {locationP, navigateToOppija, navigateWithQueryParams, showError} from './location'
import {buildClassNames} from './classnames'
import {addExitHook, removeExitHook} from './exitHook'
import {listviewPath} from './Oppijataulukko.jsx'
import {ISO2FinnishDate} from './date'
import {doActionWhileMounted} from './util'
import Text from './Text.jsx'
import {t} from './i18n'
import {EditLocalizationsLink} from './EditLocalizationsLink.jsx'
import {setOpiskeluoikeusInvalidated} from './OpiskeluoikeusInvalidation.jsx'

Bacon.Observable.prototype.flatScan = function(seed, f) {
  let current = seed
  return this.flatMapConcat(next =>
    f(current, next).doAction(updated => current = updated)
  ).toProperty(seed)
}

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

const invalidateBus = Bacon.Bus()
export const invalidateOpiskeluoikeus = oid => {
  invalidateBus.push(oid)
}

const invalidateE = invalidateBus.flatMapLatest(oid =>
  Http.delete(`/koski/api/opiskeluoikeus/${oid}`, {
    invalidateCache: ['/koski/api/oppija', '/koski/api/opiskeluoikeus', '/koski/api/editor']
  })).map(() => oppija => Bacon.once(R.merge(oppija, {event: 'invalidated'})))

const createState = (oppijaOid) => {
  const changeBus = Bacon.Bus()
  const editBus = Bacon.Bus()
  const saveChangesBus = Bacon.Bus()
  const cancelChangesBus = Bacon.Bus()
  const editingP = locationP.skipErrors().filter(loc => loc.path.startsWith('/koski/oppija/')).map(loc => !!loc.params.edit).skipDuplicates()

  cancelChangesBus.onValue(() => navigateWithQueryParams({edit: false}))
  editBus.onValue((opiskeluoikeusOid) => navigateWithQueryParams({edit: opiskeluoikeusOid}))

  const queryString = currentLocation().filterQueryParams(key => ['opiskeluoikeus', 'versionumero'].includes(key)).queryString

  const oppijaEditorUri = `/koski/api/editor/${oppijaOid}${queryString}`

  const cancelE = editingP.changes().filter(R.complement(R.identity)) // Use location instead of cancelBus, because you can also use the back button to cancel changes
  const loadOppijaE = Bacon.once(!!currentLocation().params.edit)
    .merge(cancelE.map(false))
    .map((edit) => () =>
      Http.cachedGet(oppijaEditorUri, { willHandleErrors: true})
        .map(setupModelContext)
        .map( oppija => R.merge(oppija, { event: edit ? 'edit' : 'view' }))
    )

  let changeBuffer = null

  let setupModelContext = (oppijaModel) => Editor.setupContext(oppijaModel, {saveChangesBus, editBus, changeBus, editorMapping})

  const shouldThrottle = (batch) => {
    let model = getModelFromChange(batch[0])
    var willThrottle = model && (model.type == 'string' || model.type == 'date' || model.type == 'number' || model.oneOfClass == 'localizedstring')
    return willThrottle
  }

  const localModificationE = changeBus.flatMap(firstBatch => {
    if (changeBuffer) {
      changeBuffer = changeBuffer.concat(firstBatch)
      return Bacon.never()
    } else {
      //console.log('start batch', firstBatch)
      changeBuffer = firstBatch
      return Bacon.once(oppijaBeforeChange => {
        let batchEndE = shouldThrottle(firstBatch) ? Bacon.later(delays().stringInput).merge(saveChangesBus).take(1) : Bacon.once()
        return batchEndE.flatMap(() => {
          let opiskeluoikeusPath = getPathFromChange(firstBatch[0]).slice(0, 6).join('.')
          let opiskeluoikeusOid = modelData(oppijaBeforeChange, opiskeluoikeusPath).oid

          let batch = changeBuffer
          changeBuffer = null
          //console.log('Apply', batch.length, 'changes:', batch)
          let locallyModifiedOppija = applyChangesAndValidate(oppijaBeforeChange, batch)

          return R.merge(locallyModifiedOppija, {event: 'dirty', inProgress: false, opiskeluoikeusOid: opiskeluoikeusOid })
        })
      })
    }
  })

  const saveOppijaE = saveChangesBus.map(() => oppijaBeforeSave => {
    var oppijaData = modelData(oppijaBeforeSave)
    let opiskeluoikeusOid = oppijaBeforeSave.opiskeluoikeusOid
    let opiskeluoikeudet = oppijaData.opiskeluoikeudet.flatMap(x => x.opiskeluoikeudet).flatMap(x => x.opiskeluoikeudet)
    let opiskeluoikeus = opiskeluoikeudet.find(x => x.oid == opiskeluoikeusOid)

    let oppijaUpdate = {
      henkilö: {oid: oppijaOid},
      opiskeluoikeudet: [opiskeluoikeus]
    }

    let errorHandler = e => {
      e.topLevel = true
      if (e.httpStatus === 404) {
        e.opiskeluoikeusDeleted = true
      }
      showError(e)
    }

    return Bacon.once(R.merge(oppijaBeforeSave, { event: 'saving', inProgress: true})).concat(
      Http.put('/koski/api/oppija', oppijaUpdate, { willHandleErrors: true, invalidateCache: ['/koski/api/oppija', '/koski/api/opiskeluoikeus', '/koski/api/editor/' + oppijaOid]})
        .flatMap(() => Http.cachedGet(oppijaEditorUri, {errorHandler})) // loading after save fails -> rare, not easily recoverable error, show full screen
        .map(setupModelContext)
        .map( oppija => R.merge(oppija, { event: 'saved' }))
    )
  })

  const editE = editingP.changes().filter(R.identity).map(() => (oppija) => Bacon.once(R.merge(oppija, { event: 'edit' })))

  let allUpdatesE = Bacon.mergeAll(loadOppijaE, localModificationE, saveOppijaE, editE, invalidateE) // :: EventStream [Model -> EventStream[Model]]

  let oppijaP = allUpdatesE.flatScan({ loading: true }, (currentOppija, updateF) => {
    increaseLoading()
    return updateF(currentOppija).doAction((x) => { if (!x.inProgress) decreaseLoading() }).doError(decreaseLoading)
  })
  oppijaP.onValue()

  const stateP = oppijaP.map('.event').mapError(() => 'dirty').slidingWindow(2).flatMapLatest(events => {
    let prev = events[0]
    let next = events.last()
    if(prev === 'saved' && next === 'view') {
      return Bacon.later(2000, 'view')
    }
    return Bacon.once(next)
  }).toProperty().doAction((state) => {
    state === 'dirty' ? addExitHook(t('Haluatko varmasti poistua sivulta?')) : removeExitHook()
    if (state === 'saved') navigateWithQueryParams({edit: undefined})
  })
  return { oppijaP, changeBus, editBus, saveChangesBus, cancelChangesBus, stateP}
}

const stateToContent = ({ oppijaP, changeBus, editBus, saveChangesBus, cancelChangesBus, stateP}) => oppijaP.map(oppija => ({
  content: (<div className='content-area'><div className="main-content oppija">
    <OppijaHaku/>
    <EditLocalizationsLink />
    <Link className="back-link" href={listviewPath()}><Text name="Opiskelijat"/></Link>
    <ExistingOppija {...{oppija, changeBus, editBus, saveChangesBus, cancelChangesBus, stateP}}/>
  </div></div>),
  title: modelData(oppija, 'henkilö') ? 'Oppijan tiedot' : ''
}))

export class ExistingOppija extends React.Component {
  render() {
    let {oppija, saveChangesBus, cancelChangesBus, stateP} = this.props

    let henkilö = modelLookup(oppija, 'henkilö')
    let hetu = modelTitle(henkilö, 'hetu')
    let syntymäaika = modelTitle(henkilö, 'syntymäaika')
    stateP.filter(e => e === 'invalidated').onValue(opiskeluoikeusInvalidated)
    return oppija.loading
      ? <div className="loading"/>
      : (
        <div>
          <div className={stateP.map(state => 'oppija-content ' + state)}>
            <h2>{`${modelTitle(henkilö, 'sukunimi')}, ${modelTitle(henkilö, 'etunimet')} `}<span
              className='hetu'>{(hetu && '(' + hetu + ')') || (syntymäaika && '(' + ISO2FinnishDate(syntymäaika) + ')')}</span>
              <a className="json" href={`/koski/api/oppija/${modelData(henkilö, 'oid')}`}>{'JSON'}</a>
            </h2>
            {
              // Set location as key to ensure full re-render when context changes
              oppija
                ? <Editor key={document.location.toString()} model={oppija}/>
                : null
            }
          </div>
          <EditBar {...{saveChangesBus, cancelChangesBus, stateP, oppija}}/>
          { doActionWhileMounted(globalSaveKeyEvent.filter(stateP.map(s => s == 'dirty')), () => saveChangesBus.push()) }
        </div>
    )
  }
}

const globalSaveKeyEvent = Bacon.fromEvent(window, 'keydown')
  .filter(e => (e.getModifierState('Meta') || e.getModifierState('Control')) && e.keyCode==83)
  .doAction('.preventDefault')

const EditBar = ({stateP, saveChangesBus, cancelChangesBus, oppija}) => {
  let saveChanges = (e) => {
    e.preventDefault()
    saveChangesBus.push()
  }

  let dirtyP = stateP.map(state => state == 'dirty')
  let validationErrorP = dirtyP.map(dirty => dirty && !modelValid(oppija))
  let canSaveP = dirtyP.and(validationErrorP.not())
  let messageMap = {
    saved: 'Kaikki muutokset tallennettu.',
    saving: 'Tallennetaan...',
    dirty: validationErrorP.map(error => error ? 'Korjaa virheelliset tiedot.': 'Tallentamattomia muutoksia'),
    edit: 'Ei tallentamattomia muutoksia'
  }
  let messageP = stateP.decode(messageMap)
  let classNameP = Bacon.combineAsArray(stateP, messageP.map(msg => msg ? 'visible' : '')).map(buildClassNames)

  return (<div id="edit-bar" className={classNameP}>
    <a className={stateP.map(state => ['edit', 'dirty'].includes(state) ? 'cancel' : 'cancel disabled')} onClick={ () => cancelChangesBus.push() }><Text name="Peruuta"/></a>
    <button disabled={canSaveP.not()} onClick={saveChanges}><Text name="Tallenna"/></button>
    <span className="state-indicator">{messageP.map(k => k ? <Text name={k}/> : null)}</span>
  </div>)
}

const opiskeluoikeusInvalidated = () => {
  setOpiskeluoikeusInvalidated()
  window.location = '/koski/virkailija'
}
