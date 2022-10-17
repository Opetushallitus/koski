// eslint-disable-next-line no-undef
import './polyfills/polyfills.js'
import './polyfills/omattiedot-polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import {
  Error,
  errorP,
  handleError,
  isTopLevel,
  TopLevelError
} from './util/Error'
import OmatTiedotTopBar from './topbar/OmatTiedotTopBar'
import { t } from './i18n/i18n'
import Http from './util/http'
import { Editor } from './editor/Editor'
import Text from './i18n/Text'
import editorMapping from './oppija/editors'
import { userP } from './util/user'
import { addContext, modelData, modelItems } from './editor/EditorModel'
import { locationP } from './util/location'
import { Header } from './omattiedot/header/Header'
import { EiSuorituksiaInfo } from './omattiedot/EiSuorituksiaInfo'
import { patchSaavutettavuusLeima } from './saavutettavuusLeima'
__webpack_nonce__ = window.nonce
import(/* webpackChunkName: "styles" */ './style/main.less')

const omatTiedotP = (oid) => {
  const url = oid
    ? `/koski/api/omattiedot/editor/${oid}`
    : '/koski/api/omattiedot/editor'
  return Bacon.combineWith(
    Http.cachedGet(url, {
      errorMapper: (e) => (e.httpStatus === 404 ? null : new Bacon.Error(e))
    }).toProperty(),
    userP,
    (omattiedot, user) => {
      const kansalainen = user.oid === modelData(omattiedot, 'userHenkilö.oid')
      const huollettava =
        modelData(omattiedot, 'userHenkilö.oid') !==
        modelData(omattiedot, 'henkilö.oid')
      return (
        omattiedot &&
        user &&
        addContext(omattiedot, {
          kansalainen,
          huollettava
        })
      )
    }
  )
}

const topBarP = userP.map((user) => <OmatTiedotTopBar user={user} />)
const oppijaSelectionBus = new Bacon.Bus()
const loadingOppijaStream = Bacon.mergeAll(oppijaSelectionBus, locationP)
  .flatMapLatest((loc) =>
    Bacon.fromArray([
      Bacon.constant(
        <div className="main-content ajax-indicator-bg">
          <Text name="Ladataan..." />
        </div>
      ),
      omatTiedotP(loc.params.oid).map((oppija) =>
        oppija ? (
          <div className="main-content oppija">
            <Oppija
              oppija={Editor.setupContext(oppija, { editorMapping })}
              stateP={Bacon.constant('viewing')}
            />
          </div>
        ) : (
          <div className="main-content">
            <EiSuorituksiaInfo oppija={oppija} />
          </div>
        )
      )
    ])
  )
  .flatMapLatest((x) => x)

const contentP = loadingOppijaStream.toProperty().startWith(
  <div className="main-content ajax-indicator-bg">
    <Text name="Ladataan..." />
  </div>
)

const allErrorsP = errorP(contentP)

// Rendered Virtual DOM
const domP = Bacon.combineWith(
  topBarP,
  contentP,
  allErrorsP,
  (topBar, content, error) => (
    <div>
      <Error error={error} />
      {topBar}
      {isTopLevel(error) ? (
        <TopLevelError error={error} />
      ) : (
        <div className="content-area omattiedot">
          <nav className="sidebar omattiedot-navi"></nav>
          {content}
        </div>
      )}
    </div>
  )
)

document.querySelector('title').innerHTML =
  t('Omat tiedot') + ' - ' + t('Koski') + ' - ' + t('Opintopolku.fi')

// Render to DOM
domP.onValue((component) =>
  ReactDOM.render(component, document.getElementById('content'))
)

// Handle errors
domP.onError(handleError)

export const hasOpintoja = (oppija) =>
  modelItems(oppija, 'opiskeluoikeudet').length > 0
const Oppija = ({ oppija }) => (
  <div>
    <div className="oppija-content">
      <Header oppija={oppija} oppijaSelectionBus={oppijaSelectionBus} />
      {hasOpintoja(oppija) ? (
        <Editor key={document.location.toString()} model={oppija} />
      ) : (
        <EiSuorituksiaInfo oppija={oppija} />
      )}
    </div>
  </div>
)

patchSaavutettavuusLeima()
