import React from 'baret'
import Bacon from 'baconjs'
import Http from '../util/http'
import {
  applyChangesAndValidate,
  getModelFromChange,
  getPathFromChange,
  modelData,
  modelItems,
  modelLookup,
  modelTitle,
  modelValid
} from '../editor/EditorModel'
import editorMapping from '../oppija/editors'
import { Editor } from '../editor/Editor'
import * as R from 'ramda'
import {
  currentLocation,
  locationP,
  navigateToOppija,
  navigateWithQueryParams,
  showError
} from '../util/location.js'
import { OppijaHaku } from '../virkailija/OppijaHaku'
import Link from '../components/Link'
import { decreaseLoading, increaseLoading } from '../util/loadingFlag'
import delays from '../util/delays'
import { buildClassNames } from '../components/classnames'
import { addExitHook, removeExitHook } from '../util/exitHook'
import { listviewPath } from './Oppijataulukko'
import { ISO2FinnishDate } from '../date/date'
import { doActionWhileMounted, flatMapArray } from '../util/util'
import Text from '../i18n/Text'
import { t } from '../i18n/i18n'
import { EditLocalizationsLink } from '../i18n/EditLocalizationsLink'
import { setInvalidationNotification } from '../components/InvalidationNotification'
import { userP } from '../util/user'
import { Varoitukset } from '../util/Varoitukset'

Bacon.Observable.prototype.flatScan = function (seed, f) {
  let current = seed
  return this.flatMapConcat((next) =>
    f(current, next).doAction((updated) => (current = updated))
  ).toProperty(seed)
}

let currentState = null

export const reloadOppija = () => {
  const oppijaOid = currentState.oppijaOid
  currentState = null
  navigateToOppija({ oid: oppijaOid })
}

export const oppijaContentP = (oppijaOid) => {
  const version = currentLocation().params.versionumero
  if (
    !currentState ||
    currentState.oppijaOid !== oppijaOid ||
    currentState.version !== version
  ) {
    currentState = {
      oppijaOid,
      version,
      state: createState(oppijaOid)
    }
  }
  return stateToContent(currentState.state)
}

const invalidateBus = Bacon.Bus()
export const invalidateOpiskeluoikeus = (oid) => {
  invalidateBus.push(oid)
}

const invalidateE = invalidateBus
  .flatMapLatest((oid) =>
    Http.delete(`/koski/api/opiskeluoikeus/${oid}`, {
      invalidateCache: [
        '/koski/api/oppija',
        '/koski/api/opiskeluoikeus',
        '/koski/api/editor'
      ]
    })
  )
  .map(
    () => (oppija) => Bacon.once(R.mergeRight(oppija, { event: 'invalidated' }))
  )

const deletePäätasonSuoritusBus = Bacon.Bus()
export const deletePäätasonSuoritus = (opiskeluoikeus, päätasonSuoritus) =>
  deletePäätasonSuoritusBus.push({
    opiskeluoikeusOid: modelData(opiskeluoikeus, 'oid'),
    versionumero: modelData(opiskeluoikeus, 'versionumero'),
    päätasonSuoritus: modelData(päätasonSuoritus)
  })

const deletePäätasonSuoritusE = deletePäätasonSuoritusBus
  .flatMapLatest(({ opiskeluoikeusOid, versionumero, päätasonSuoritus }) =>
    Http.post(
      `/koski/api/opiskeluoikeus/${opiskeluoikeusOid}/${versionumero}/delete-paatason-suoritus`,
      päätasonSuoritus
    )
  )
  .map(
    () => (oppija) =>
      Bacon.once(R.mergeRight(oppija, { event: 'päätasonSuoritusDeleted' }))
  )

const createState = (oppijaOid) => {
  const changeBus = Bacon.Bus()
  const editBus = Bacon.Bus()
  const saveChangesBus = Bacon.Bus()
  const cancelChangesBus = Bacon.Bus()
  const editingP = locationP
    .skipErrors()
    .filter((loc) => loc.path.startsWith('/koski/oppija/'))
    .map((loc) => !!loc.params.edit)
    .skipDuplicates()

  cancelChangesBus.onValue(() => navigateWithQueryParams({ edit: false }))
  editBus.onValue((opiskeluoikeusOid) =>
    navigateWithQueryParams({ edit: opiskeluoikeusOid })
  )

  const queryString = currentLocation().filterQueryParams((key) =>
    ['opiskeluoikeus', 'versionumero', 'newVSTUI'].includes(key)
  ).queryString

  const oppijaEditorUri = `/koski/api/editor/${oppijaOid}${queryString}`

  const cancelE = editingP.changes().filter(R.complement(R.identity)) // Use location instead of cancelBus, because you can also use the back button to cancel changes
  const loadOppijaE = Bacon.once(!!currentLocation().params.edit)
    .merge(cancelE.map(false))
    .map(
      (edit) => () =>
        Http.cachedGet(oppijaEditorUri, { willHandleErrors: true })
          .map(setupModelContext)
          .map((oppija) =>
            R.mergeRight(oppija, { event: edit ? 'edit' : 'view' })
          )
    )

  let changeBuffer = null

  const setupModelContext = (oppijaModel) => {
    return Editor.setupContext(oppijaModel, {
      saveChangesBus,
      editBus,
      changeBus,
      editorMapping
    })
  }

  const shouldThrottle = (batch) => {
    const model = getModelFromChange(batch[0])
    const willThrottle =
      model &&
      (model.type === 'string' ||
        model.type === 'date' ||
        model.type === 'number' ||
        model.oneOfClass === 'localizedstring')
    return willThrottle
  }

  const localModificationE = changeBus.flatMap((firstBatch) => {
    if (changeBuffer) {
      changeBuffer = changeBuffer.concat(firstBatch)
      return Bacon.never()
    } else {
      // console.log('start batch', firstBatch)
      changeBuffer = firstBatch
      return Bacon.once((oppijaBeforeChange) => {
        const batchEndE = shouldThrottle(firstBatch)
          ? Bacon.later(delays().stringInput).merge(saveChangesBus).take(1)
          : Bacon.once()
        return batchEndE.flatMap(() => {
          const opiskeluoikeusPath = getPathFromChange(firstBatch[0])
            .slice(0, 6)
            .join('.')
          const opiskeluoikeusOid = modelData(
            oppijaBeforeChange,
            opiskeluoikeusPath
          ).oid

          const batch = changeBuffer
          changeBuffer = null
          // console.log('Apply', batch.length, 'changes:', batch)
          const locallyModifiedOppija = applyChangesAndValidate(
            oppijaBeforeChange,
            batch
          )

          return R.mergeRight(locallyModifiedOppija, {
            event: 'dirty',
            inProgress: false,
            opiskeluoikeusOid
          })
        })
      })
    }
  })

  const saveOppijaE = saveChangesBus.map(() => (oppijaBeforeSave) => {
    const oppijaData = modelData(oppijaBeforeSave)
    const opiskeluoikeusOid = oppijaBeforeSave.opiskeluoikeusOid
    const opiskeluoikeudet = flatMapArray(
      flatMapArray(oppijaData.opiskeluoikeudet, (x) => x.opiskeluoikeudet),
      (x) => x.opiskeluoikeudet
    )
    const opiskeluoikeus = opiskeluoikeudet.find(
      (x) => x.oid === opiskeluoikeusOid
    )

    const oppijaUpdate = {
      henkilö: { oid: oppijaOid },
      opiskeluoikeudet: [opiskeluoikeus]
    }

    const errorHandler = (e) => {
      e.topLevel = true
      if (e.httpStatus === 404) {
        e.opiskeluoikeusDeleted = true
      }
      showError(e)
    }

    return Bacon.once(
      R.mergeRight(oppijaBeforeSave, { event: 'saving', inProgress: true })
    ).concat(
      Http.put('/koski/api/oppija', oppijaUpdate, {
        willHandleErrors: true,
        invalidateCache: [
          '/koski/api/oppija',
          '/koski/api/opiskeluoikeus',
          '/koski/api/editor/' + oppijaOid
        ]
      })
        .flatMap(() => Http.cachedGet(oppijaEditorUri, { errorHandler })) // loading after save fails -> rare, not easily recoverable error, show full screen
        .map(setupModelContext)
        .map((oppija) => R.mergeRight(oppija, { event: 'saved' }))
    )
  })

  const editE = editingP
    .changes()
    .filter(R.identity)
    .map(() => (oppija) => Bacon.once(R.mergeRight(oppija, { event: 'edit' })))

  const allUpdatesE = Bacon.mergeAll(
    loadOppijaE,
    localModificationE,
    saveOppijaE,
    editE,
    invalidateE,
    deletePäätasonSuoritusE
  ) // :: EventStream [Model -> EventStream[Model]]

  const oppijaP = allUpdatesE.flatScan(
    { loading: true },
    (currentOppija, updateF) => {
      increaseLoading()
      return updateF(currentOppija)
        .doAction((x) => {
          if (!x.inProgress) decreaseLoading()
        })
        .doError(decreaseLoading)
    }
  )
  oppijaP.onValue()

  const stateP = oppijaP
    .map('.event')
    .mapError(() => 'dirty')
    .slidingWindow(2)
    .flatMapLatest((events) => {
      const prev = events[0]
      const next = R.last(events)
      if (prev === 'saved' && next === 'view') {
        return Bacon.later(2000, 'view')
      }
      return Bacon.once(next)
    })
    .toProperty()
    .doAction((state) => {
      state === 'dirty'
        ? addExitHook(t('Haluatko varmasti poistua sivulta?'))
        : removeExitHook()
      if (state === 'saved') navigateWithQueryParams({ edit: undefined })
    })
  return {
    oppijaP,
    changeBus,
    editBus,
    saveChangesBus,
    cancelChangesBus,
    stateP
  }
}

const stateToContent = ({
  oppijaP,
  changeBus,
  editBus,
  saveChangesBus,
  cancelChangesBus,
  stateP
}) =>
  oppijaP.map((oppija) => ({
    content: (
      <div className="content-area">
        <div className="main-content oppija">
          <OppijaHaku />
          <EditLocalizationsLink />
          {userP.map((user) =>
            !user.isViranomainen ? (
              <Link className="back-link" href={listviewPath()}>
                <Text name="Opiskelijat" />
              </Link>
            ) : null
          )}
          <Oppija
            {...{
              oppija,
              changeBus,
              editBus,
              saveChangesBus,
              cancelChangesBus,
              stateP
            }}
          />
        </div>
      </div>
    ),
    title: modelData(oppija, 'henkilö') ? 'Oppijan tiedot' : ''
  }))

export class Oppija extends React.Component {
  render() {
    const { oppija, saveChangesBus, cancelChangesBus, stateP } = this.props

    const henkilö = modelLookup(oppija, 'henkilö')
    const hetu = modelTitle(henkilö, 'hetu')
    const syntymäaika = modelTitle(henkilö, 'syntymäaika')
    stateP.filter((e) => e === 'invalidated').onValue(opiskeluoikeusInvalidated)
    stateP
      .filter((e) => e === 'päätasonSuoritusDeleted')
      .onValue(päätasonSuoritusDeleted)
    const showHenkilöUiLink = userP.map('.hasHenkiloUiWriteAccess')
    const showVirtaXmlLink = userP.map('.hasGlobalReadAccess')
    const showSureLink = userP.map('.hasHenkiloUiWriteAccess')
    const varoitukset = modelItems(oppija, 'varoitukset').map(modelData)
    return oppija.loading ? (
      <div className="loading" />
    ) : (
      <div>
        <div className={stateP.map((state) => 'oppija-content ' + state)}>
          <Varoitukset varoitukset={varoitukset} />
          <h2 className="oppija-heading" data-testid="oppija-heading">
            {`${modelTitle(henkilö, 'sukunimi')}, ${modelTitle(
              henkilö,
              'etunimet'
            )} `}
            <span className="hetu" data-testid="oppija-henkilotunnus">
              {(hetu && '(' + hetu + ')') ||
                (syntymäaika && '(' + ISO2FinnishDate(syntymäaika) + ')')}
            </span>
            {modelData(henkilö, 'turvakielto') && (
              <span
                role="status"
                aria-label={t('Henkilöllä on turvakielto')}
                title={t('Henkilöllä on turvakielto')}
                className="turvakielto"
              />
            )}
            <a
              href={`/koski/api/oppija/${modelData(
                henkilö,
                'oid'
              )}/opintotiedot-json`}
            >
              {'JSON'}
            </a>
            {showHenkilöUiLink.map(
              (show) =>
                show && (
                  <HenkilöUiLink
                    henkilö={henkilö}
                    yksilöity={modelData(oppija, 'yksilöity')}
                  />
                )
            )}
            {showVirtaXmlLink.map(
              (show) =>
                show && (
                  <a
                    href={`/koski/api/oppija/${modelData(
                      henkilö,
                      'oid'
                    )}/virta-opintotiedot-xml`}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    {'Virta XML'}
                  </a>
                )
            )}
            {showSureLink.map(
              (show) =>
                show && (
                  <a
                    href={`/suoritusrekisteri/#/opiskelijat?henkilo=${modelData(
                      henkilö,
                      'oid'
                    )}`}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    {'Suoritusrekisteri'}
                  </a>
                )
            )}
          </h2>
          <div className="oppijanumero">
            {t('Oppijanumero')}
            {`: ${modelData(henkilö, 'oid')}`}
          </div>
          {
            // Set location as key to ensure full re-render when context changes
            oppija ? (
              <Editor key={document.location.toString()} model={oppija} />
            ) : null
          }
        </div>
        <EditBar {...{ saveChangesBus, cancelChangesBus, stateP, oppija }} />
        {doActionWhileMounted(
          globalSaveKeyEvent.filter(stateP.map((s) => s === 'dirty')),
          () => saveChangesBus.push()
        )}
      </div>
    )
  }
}

const HenkilöUiLink = ({ henkilö, yksilöity }) => {
  return (
    <a
      href={`/henkilo-ui/oppija/${modelData(
        henkilö,
        'oid'
      )}?permissionCheckService=KOSKI`}
      target="_blank"
      title={t('OppijanumerorekisteriLinkTooltip')}
      className="oppijanumerorekisteri-link"
      rel="noopener noreferrer"
    >
      <Text name="Oppijanumerorekisteri" />
      {!yksilöity && (
        <Text
          className="yksilöimätön"
          name="Oppijaa ei ole yksilöity. Tee yksilöinti oppijanumerorekisterissä"
        />
      )}
    </a>
  )
}

const globalSaveKeyEvent = Bacon.fromEvent(window, 'keydown')
  .filter(
    (e) =>
      (e.getModifierState('Meta') || e.getModifierState('Control')) &&
      e.keyCode === 83
  )
  .doAction('.preventDefault')

const EditBar = ({ stateP, saveChangesBus, cancelChangesBus, oppija }) => {
  const saveChanges = (e) => {
    e.preventDefault()
    saveChangesBus.push()
  }

  const dirtyP = stateP.map((state) => state === 'dirty')
  const validationErrorP = dirtyP.map((dirty) => dirty && !modelValid(oppija))
  const canSaveP = dirtyP.and(validationErrorP.not())
  const messageMap = {
    saved: 'Kaikki muutokset tallennettu.',
    saving: 'Tallennetaan...',
    dirty: validationErrorP.map((error) =>
      error ? 'Korjaa virheelliset tiedot.' : 'Tallentamattomia muutoksia'
    ),
    edit: 'Ei tallentamattomia muutoksia'
  }
  const messageP = stateP.decode(messageMap)
  const classNameP = Bacon.combineAsArray(
    stateP,
    messageP.map((msg) => (msg ? 'visible' : ''))
  ).map(buildClassNames)

  return (
    <div id="edit-bar-wrapper" className={classNameP}>
      <div id="edit-bar">
        <a
          className={stateP.map((state) =>
            ['edit', 'dirty'].includes(state) ? 'cancel' : 'cancel disabled'
          )}
          onClick={() => cancelChangesBus.push()}
          role="link"
          aria-label="Peruuta muutokset"
        >
          <Text name="Peruuta" />
        </a>
        <button
          className="koski-button"
          disabled={canSaveP.not()}
          onClick={saveChanges}
          aria-label="Tallenna muutokset"
        >
          <Text name="Tallenna" />
        </button>
        <span className="state-indicator">
          {messageP.map((k) => (k ? <Text name={k} /> : null))}
        </span>
      </div>
    </div>
  )
}

const opiskeluoikeusInvalidated = () => {
  setInvalidationNotification('Opiskeluoikeus mitätöity')
  window.location = '/koski/virkailija'
}

const päätasonSuoritusDeleted = () => {
  setInvalidationNotification('Suoritus poistettu')
  window.location.reload(true)
}
