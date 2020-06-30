import './polyfills/polyfills.js'
import './polyfills/omattiedot-polyfills.js'
import './style/main.less'
import React from 'baret'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import {Error, errorP, handleError, isTopLevel, TopLevelError} from './util/Error'
import OmatTiedotTopBar from './topbar/OmatTiedotTopBar'
import {OmatTiedotTabs} from './OmatTiedotTabs'
import {t} from './i18n/i18n'
import Http from './util/http'
import {Editor} from './editor/Editor'
import Text from './i18n/Text'
import editorMapping from './oppija/editors'
import {userP} from './util/user'
import {addContext, modelData, modelItems} from './editor/EditorModel'
import {locationP} from './util/location'
import {EiSuorituksiaInfo} from './omattiedot/EiSuorituksiaInfo'
import {patchSaavutettavuusLeima} from './saavutettavuusLeima'
import {HuollettavaDropdown} from './omattiedot/header/HuollettavaDropdown'
import {Varoitukset} from './util/Varoitukset'

const omatTiedotP = oid => {
  const url = oid ? `/koski/api/omattiedot/editor/${oid}` : '/koski/api/omattiedot/editor'
  return Bacon.combineWith(
    Http.cachedGet(url, { errorMapper: (e) => e.httpStatus === 404 ? null : new Bacon.Error(e)}).toProperty(),
    userP,
    (omattiedot, user) => {
      const kansalainen = user.oid === modelData(omattiedot, 'userHenkilö.oid')
      const huollettava = modelData(omattiedot, 'userHenkilö.oid') !== modelData(omattiedot, 'henkilö.oid')
      return omattiedot && user && addContext(omattiedot, {kansalainen: kansalainen, huollettava: huollettava})
    }
  )
}

const topBarP = userP.map(user => <OmatTiedotTopBar user={user}/>)
const oppijaSelectionBus = new Bacon.Bus()
const loadingOppijaStream = Bacon.mergeAll(oppijaSelectionBus, locationP).flatMapLatest(loc =>
  Bacon.fromArray([
    Bacon.constant(<div className="main-content ajax-indicator-bg"><Text name="Ladataan..."/></div>),
    omatTiedotP(loc.params.oid).map(oppija =>
      oppija
        ? <div className="main-content oppija"><Oppija oppija={Editor.setupContext(oppija, {editorMapping})} stateP={Bacon.constant('viewing')}/></div>
        : <div className="main-content"><EiSuorituksiaInfo oppija={oppija}/></div>
    )
  ])
).flatMapLatest(x => x)

const contentP = loadingOppijaStream.toProperty().startWith(<div className="main-content ajax-indicator-bg"><Text name="Ladataan..."/></div>)

const allErrorsP = errorP(contentP)

// Rendered Virtual DOM
const domP = Bacon.combineWith(topBarP, contentP, allErrorsP, (topBar, content, error) =>
  (<div>
    <Error error={error}/>
    {topBar}
    {
      isTopLevel(error)
        ? <TopLevelError error={error} />
        : (<div className="content-area omattiedot">
            <nav className="sidebar omattiedot-navi"></nav>
            {content}
          </div>)
    }
  </div>)
)

document.querySelector('title').innerHTML = t('Omat tiedot') + ' - ' + t('Koski') + ' - ' + t('Opintopolku.fi')

// Render to DOM
domP.onValue((component) => ReactDOM.render(component, document.getElementById('content')))

// Handle errors
domP.onError(handleError)

const Oppija = ({ oppija }) => {
  const varoitukset = modelItems(oppija, 'varoitukset').map(modelData)
  return (
    <div className="oppija-content">
      <HuollettavaDropdown oppija={oppija} oppijaSelectionBus={oppijaSelectionBus}/>
      <Varoitukset varoitukset={varoitukset}/> {/* TODO: Tsekkaa varoitusten tyylit (HeaderInfo komponentti poistettu välistä) */}
      <OmatTiedotTabs oppija={oppija}/>
    </div>
  )
}
patchSaavutettavuusLeima()
