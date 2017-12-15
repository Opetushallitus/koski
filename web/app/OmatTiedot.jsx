import './polyfills.js'
import './style/main.less'
import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import {Error, errorP, handleError, isTopLevel, TopLevelError} from './Error'
import OmatTiedotTopBar from './topbar/OmatTiedotTopBar'
import {t} from './i18n'
import Http from './http'
import {ExistingOppija} from './Oppija'
import {Editor} from './editor/Editor'
import Text from './Text'
import {editorMapping} from './editor/Editors'
import {userP} from './user'
import {addContext, modelData} from './editor/EditorModel'
import {locationP} from './location'

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
      ? <div className="main-content oppija"><ExistingOppija oppija={Editor.setupContext(oppija, {editorMapping})} stateP={Bacon.constant('viewing')}/></div>
      : <div className="main-content ei-opiskeluoikeuksia"><Text name="Tiedoillasi ei löydy opiskeluoikeuksia"/></div>
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