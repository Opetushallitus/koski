// eslint-disable-next-line no-undef
__webpack_nonce__ = window.nonce
import(/* webpackChunkName: "styles" */ './style/main.less')
import './polyfills/polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import {Error, errorP, handleError, isTopLevel, TopLevelError} from './util/Error'
import {userP} from './util/user'
import {contentP, routeErrorP, titleKeyP} from './virkailija/virkailijaRouter'
import {TopBar} from './topbar/TopBar'
import {locationP} from './util/location.js'
import LocalizationEditBar from './i18n/LocalizationEditBar'
import {t} from './i18n/i18n'

const noAccessControlPaths = ['/koski/dokumentaatio']

const topBarP = Bacon.combineWith(userP, titleKeyP, locationP, (user, titleKey, location) => <TopBar user={user} titleKey={titleKey} inRaamit={inRaamit} location={location} />)
const allErrorsP = errorP(contentP, routeErrorP)

// Rendered Virtual DOM
const domP = Bacon.combineWith(topBarP, userP, contentP, allErrorsP, locationP, (topBar, user, content, error, location) =>
    (<div>
      <Error error={error}/>
      {topBar}
      {
        isTopLevel(error)
          ? <TopLevelError error={error} />
          : canAccess(user, location) ? content : null
      }
      { user && <LocalizationEditBar user={user}/> }
    </div>)
)

const canAccess = (user, location) => user || noAccessControlPaths.find(p => location.path.startsWith(p))

// Render to DOM
domP.onValue((component) => ReactDOM.render(component, document.getElementById('content')))

titleKeyP.onValue((titleKey) => {
  let defaultTitle = t('Koski') + ' - ' + t('Opintopolku.fi')
  document.querySelector('title').innerHTML = titleKey ? t(titleKey) + ' - ' + defaultTitle : defaultTitle
})

const inRaamit = !!document.getElementById('content').dataset.inraamit

// Handle errors
domP.onError(handleError)
