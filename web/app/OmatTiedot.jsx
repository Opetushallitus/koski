import Atom from 'bacon.atom'
import './polyfills/polyfills.js'
import './polyfills/omattiedot-polyfills.js'
import './style/main.less'
import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import {Error, errorP, handleError, isTopLevel, TopLevelError} from './util/Error'
import OmatTiedotTopBar from './topbar/OmatTiedotTopBar'
import {t} from './i18n/i18n'
import Http from './util/http'
import {Editor} from './editor/Editor'
import Text from './i18n/Text'
import editorMapping from './oppija/editors'
import {userP} from './util/user'
import {addContext, modelData, modelItems} from './editor/EditorModel'
import {locationP} from './util/location'
import {Header} from './omattiedot/header/Header'
import {EiSuorituksiaInfo} from './omattiedot/EiSuorituksiaInfo'
import {patchSaavutettavuusLeima} from './saavutettavuusLeima'
import delays from './util/delays'

const henkilöAtom = Atom()
const henkilöP = henkilöAtom.throttle(delays().delay(100))
const getOmatTiedotP = () => henkilöP.flatMap(oppija => {
  const url = oppija ? `/koski/api/omattiedot/editor/${modelData(oppija, 'oid')}` : '/koski/api/omattiedot/editor'
  return Http.cachedGet(url, { errorMapper: (e) => e.httpStatus === 404 ? null : new Bacon.Error(e)}).toProperty()
})

const omatTiedotP = () => Bacon.combineWith(
  getOmatTiedotP(),
  userP,
  (omattiedot, user) => omattiedot && user && addContext(omattiedot, {kansalainen: true})
)

const topBarP = userP.map(user => <OmatTiedotTopBar user={user}/>)

const contentP = locationP.flatMapLatest(() => omatTiedotP().map(oppija =>
    oppija
      ? <div className="main-content oppija"><Oppija oppija={Editor.setupContext(oppija, {editorMapping})} stateP={Bacon.constant('viewing')}/></div>
      : <div className="main-content"><EiSuorituksiaInfo/></div>
    )
).toProperty().startWith(<div className="main-content ajax-indicator-bg"><Text name="Ladataan..."/></div>)

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

export const hasOpintoja = oppija => modelItems(oppija, 'opiskeluoikeudet').length > 0

const Oppija = ({oppija}) => {
  return oppija.loading
    ? <div className="loading"/>
    : (<div>
        <div className="oppija-content">
          <Header oppijaP={omatTiedotP()} onOppijaChanged={o => henkilöAtom.set(o)}/>
          {hasOpintoja(oppija) ? <Editor key={document.location.toString()} model={oppija}/> : <EiSuorituksiaInfo/>}
        </div>
      </div>)

}


patchSaavutettavuusLeima()
