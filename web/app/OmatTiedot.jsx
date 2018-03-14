import './polyfills/polyfills.js'
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
import {addContext, modelData, modelLookup} from './editor/EditorModel'
import {locationP} from './util/location'
import {EiSuorituksia} from './EiSuorituksia'
import {Header} from './omattiedot/Header'

const omatTiedotP = () => Bacon.combineWith(
  Http.cachedGet('/koski/api/editor/omattiedot', { errorMapper: (e) => e.httpStatus === 404 ? null : new Bacon.Error}).toProperty(),
  userP,
  (omattiedot, user) => {
    let kansalainen = user.oid === modelData(omattiedot, 'henkilö.oid')
    return omattiedot && user && addContext(omattiedot, {kansalainen: kansalainen})
  }
)

const topBarP = userP.map(user => <OmatTiedotTopBar user={user}/>)
const contentP = locationP.flatMapLatest(() => omatTiedotP().map(oppija =>
    oppija
      ? <div className="main-content oppija"><Oppija oppija={Editor.setupContext(oppija, {editorMapping})} stateP={Bacon.constant('viewing')}/></div>
      : <div className="main-content"><EiSuorituksia/></div>
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

const Oppija = ({oppija}) => {
  return oppija.loading
    ? <div className="loading"/>
    : (
      <div>
        <div className="oppija-content">
          <Header henkilö={modelLookup(oppija, 'henkilö')}/>
          <Editor key={document.location.toString()} model={oppija}/>
        </div>
      </div>
    )
}
