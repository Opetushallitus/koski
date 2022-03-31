import React from 'baret'
import Bacon from 'baconjs'
import Http from '../util/http'
import {
  applyChangesAndValidate,
  getModelFromChange,
  getPathFromChange,
  modelData, modelItems,
  modelLookup,
  modelTitle,
  modelValid
} from '../editor/EditorModel'
import editorMapping from '../oppija/editors'
import {Editor} from '../editor/Editor'
import * as R from 'ramda'
import {currentLocation} from '../util/location.js'
import {OppijaHaku} from '../virkailija/OppijaHaku'
import Link from '../components/Link'
import {decreaseLoading, increaseLoading} from '../util/loadingFlag'
import delays from '../util/delays'
import {locationP, navigateToOppija, navigateWithQueryParams, showError} from '../util/location'
import {buildClassNames} from '../components/classnames'
import {addExitHook, removeExitHook} from '../util/exitHook'
import {listviewPath} from './Oppijataulukko'
import {ISO2FinnishDate} from '../date/date'
import {doActionWhileMounted, flatMapArray} from '../util/util'
import Text from '../i18n/Text'
import {t} from '../i18n/i18n'
import {EditLocalizationsLink} from '../i18n/EditLocalizationsLink'
import {setInvalidationNotification} from '../components/InvalidationNotification'
import {userP} from '../util/user'
import {Varoitukset} from '../util/Varoitukset'

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
  })).map(() => oppija => Bacon.once(R.mergeRight(oppija, {event: 'invalidated'})))

const deletePäätasonSuoritusBus = Bacon.Bus()
export const deletePäätasonSuoritus = (opiskeluoikeus, päätasonSuoritus) =>
  deletePäätasonSuoritusBus.push({
    opiskeluoikeusOid: modelData(opiskeluoikeus, 'oid'),
    versionumero: modelData(opiskeluoikeus, 'versionumero'),
    päätasonSuoritus: modelData(päätasonSuoritus)
  })

const deletePäätasonSuoritusE = deletePäätasonSuoritusBus.flatMapLatest(({opiskeluoikeusOid, versionumero, päätasonSuoritus}) =>
  Http.post(`/koski/api/opiskeluoikeus/${opiskeluoikeusOid}/${versionumero}/delete-paatason-suoritus`, päätasonSuoritus)
).map(() => oppija => Bacon.once(R.mergeRight(oppija, {event: 'päätasonSuoritusDeleted'})))

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
        .map( oppija => R.mergeRight(oppija, { event: edit ? 'edit' : 'view' }))
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

          return R.mergeRight(locallyModifiedOppija, {event: 'dirty', inProgress: false, opiskeluoikeusOid: opiskeluoikeusOid })
        })
      })
    }
  })

  const saveOppijaE = saveChangesBus.map(() => oppijaBeforeSave => {
    var oppijaData = modelData(oppijaBeforeSave)
    let opiskeluoikeusOid = oppijaBeforeSave.opiskeluoikeusOid
    let opiskeluoikeudet = flatMapArray(flatMapArray(oppijaData.opiskeluoikeudet, x => x.opiskeluoikeudet), x => x.opiskeluoikeudet)
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

    return Bacon.once(R.mergeRight(oppijaBeforeSave, { event: 'saving', inProgress: true})).concat(
      Http.put('/koski/api/oppija', oppijaUpdate, { willHandleErrors: true, invalidateCache: ['/koski/api/oppija', '/koski/api/opiskeluoikeus', '/koski/api/editor/' + oppijaOid]})
        .flatMap(() => Http.cachedGet(oppijaEditorUri, {errorHandler})) // loading after save fails -> rare, not easily recoverable error, show full screen
        .map(setupModelContext)
        .map( oppija => R.mergeRight(oppija, { event: 'saved' }))
    )
  })

  const editE = editingP.changes().filter(R.identity).map(() => (oppija) => Bacon.once(R.mergeRight(oppija, { event: 'edit' })))

  let allUpdatesE = Bacon.mergeAll(loadOppijaE, localModificationE, saveOppijaE, editE, invalidateE, deletePäätasonSuoritusE) // :: EventStream [Model -> EventStream[Model]]

  let oppijaP = allUpdatesE.flatScan({ loading: true }, (currentOppija, updateF) => {
    increaseLoading()
    return updateF(currentOppija).doAction((x) => { if (!x.inProgress) decreaseLoading() }).doError(decreaseLoading)
  })
  oppijaP.onValue()

  const stateP = oppijaP.map('.event').mapError(() => 'dirty').slidingWindow(2).flatMapLatest(events => {
    let prev = events[0]
    let next = R.last(events)
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
    {userP.map((user) => !user.isViranomainen ? <Link className="back-link" href={listviewPath()}><Text name="Opiskelijat"/></Link> : null)}
    <Oppija {...{oppija, changeBus, editBus, saveChangesBus, cancelChangesBus, stateP}}/>
  </div></div>),
  title: modelData(oppija, 'henkilö') ? 'Oppijan tiedot' : ''
}))

export class Oppija extends React.Component {
  render() {
    let {oppija, saveChangesBus, cancelChangesBus, stateP} = this.props

    let henkilö = modelLookup(oppija, 'henkilö')
    let hetu = modelTitle(henkilö, 'hetu')
    let syntymäaika = modelTitle(henkilö, 'syntymäaika')
    stateP.filter(e => e === 'invalidated').onValue(opiskeluoikeusInvalidated)
    stateP.filter(e => e === 'päätasonSuoritusDeleted').onValue(päätasonSuoritusDeleted)
    let showHenkilöUiLink = userP.map('.hasHenkiloUiWriteAccess')
    let showVirtaXmlLink = userP.map('.hasGlobalReadAccess')
    let showSureLink = userP.map('.hasHenkiloUiWriteAccess')
    let varoitukset = modelItems(oppija, 'varoitukset').map(modelData)
    return oppija.loading
      ? <div className="loading"/>
      : (
        <div>
          <div className={stateP.map(state => 'oppija-content ' + state)}>
            <Varoitukset varoitukset={varoitukset}/>
            <h2 className='oppija-heading'>{`${modelTitle(henkilö, 'sukunimi')}, ${modelTitle(henkilö, 'etunimet')} `}<span
              className='hetu'>{(hetu && '(' + hetu + ')') || (syntymäaika && '(' + ISO2FinnishDate(syntymäaika) + ')')}</span>
              {modelData(henkilö, 'turvakielto') && <span title={t('Henkilöllä on turvakielto')} className="turvakielto"/>}
              <a href={`/koski/api/oppija/${modelData(henkilö, 'oid')}/opintotiedot-json`}>{'JSON'}</a>
              {showHenkilöUiLink.map(show => show && <HenkilöUiLink henkilö={henkilö} yksilöity={modelData(oppija, 'yksilöity')} />)}
              {showVirtaXmlLink.map(show => show && <a href={`/koski/api/oppija/${modelData(henkilö, 'oid')}/virta-opintotiedot-xml`} target='_blank' rel="noopener noreferrer">{'Virta XML'}</a>)}
              {showSureLink.map(show => show && <a href={`/suoritusrekisteri/#/opiskelijat?henkilo=${modelData(henkilö, 'oid')}`} target='_blank' rel="noopener noreferrer">{'Suoritusrekisteri'}</a>)}
            </h2>
            <div className="oppijanumero">{t('Oppijanumero')}{`: ${modelData(henkilö, 'oid')}`}</div>
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

const HenkilöUiLink = ({henkilö, yksilöity}) => {
  return (<a href={`/henkilo-ui/oppija/${modelData(henkilö, 'oid')}?permissionCheckService=KOSKI`} target='_blank' title={t('OppijanumerorekisteriLinkTooltip')} className='oppijanumerorekisteri-link' rel="noopener noreferrer">
    <Text name='Oppijanumerorekisteri'/>
    {!yksilöity && <Text className='yksilöimätön' name='Oppijaa ei ole yksilöity. Tee yksilöinti oppijanumerorekisterissä'/>}
  </a>)
}

HenkilöUiLink.displayName = 'HenkilöUiLink'

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

  return (<div id="edit-bar-wrapper" className={classNameP}><div id="edit-bar">
    <a className={stateP.map(state => ['edit', 'dirty'].includes(state) ? 'cancel' : 'cancel disabled')} onClick={ () => cancelChangesBus.push() }><Text name="Peruuta"/></a>
    <button className='koski-button' disabled={canSaveP.not()} onClick={saveChanges}><Text name="Tallenna"/></button>
    <span className="state-indicator">{messageP.map(k => k ? <Text name={k}/> : null)}</span>
  </div></div>
  )
}

EditBar.displayName = 'EditBar'

const opiskeluoikeusInvalidated = () => {
  setInvalidationNotification('Opiskeluoikeus mitätöity')
  window.location = '/koski/virkailija'
}

const päätasonSuoritusDeleted = () => {
  setInvalidationNotification('Suoritus poistettu')
  window.location.reload(true)
}
