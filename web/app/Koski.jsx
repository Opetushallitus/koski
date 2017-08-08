import './polyfills.js'
import './style/main.less'
import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import R from 'ramda'
import {Error, errorP, handleError, isTopLevel, TopLevelError} from './Error.jsx'
import {userP} from './user'
import {contentP, titleKeyP} from './router.jsx'
import {TopBar} from './TopBar.jsx'
import {locationP} from './location.js'
import LocalizationEditBar from './LocalizationEditBar.jsx'
import {t} from './i18n'

const noAccessControlPaths = ['/koski/documentation']

const topBarP = Bacon.combineWith(userP, titleKeyP, locationP, (user, titleKey, location) => <TopBar user={user} titleKey={titleKey} inRaamit={inRaamit} location={location} />)
const allErrorsP = errorP(contentP)

// Renderered Virtual DOM
const domP = Bacon.combineWith(topBarP, userP, contentP, allErrorsP, locationP, (topBar, user, content, error, location) =>
    (<div>
      <Error error={error}/>
      {topBar}
      {
        isTopLevel(error)
          ? <TopLevelError error={error} />
          : (R.any(R.map(p => location.path.endsWith(p), noAccessControlPaths)) || user
            ? content
            : null
          )
      }
      { user && <LocalizationEditBar user={user}/> }
    </div>)
)

// Render to DOM
domP.onValue((component) => ReactDOM.render(component, document.getElementById('content')))

titleKeyP.onValue((titleKey) => {
  let defaultTitle = t('Koski') + ' - ' + t('Opintopolku.fi')
  document.querySelector('title').innerHTML = titleKey ? t(titleKey) + ' - ' + defaultTitle : defaultTitle
})

const inRaamit = !!document.getElementById('content').dataset.inraamit

// Handle errors
domP.onError(handleError)