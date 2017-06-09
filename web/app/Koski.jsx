import './polyfills.js'
import './style/main.less'
import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import {Error, TopLevelError, isTopLevel, handleError, errorP} from './Error.jsx'
import {userP} from './user'
import {contentP, titleKeyP} from './router.jsx'
import {TopBar} from './TopBar.jsx'
import {locationP} from './location.js'
import {savedBus} from './Oppija.jsx'
import LocalizationEditBar from './LocalizationEditBar.jsx'
import { t } from './i18n'
import {currentLocation} from './location'

history.replaceState(null, null, currentLocation().filterQueryParams((k, v) => k !== 'ticket').toString())

// Stays at `true` for five seconds after latest saved change. Reset to `false` when another Oppija is selected.
const savedP = savedBus.flatMapLatest(() => Bacon.once(true).concat((locationP.changes().merge(Bacon.later(5000))).map(false))).toProperty(false).skipDuplicates()

const topBarP = Bacon.combineWith(userP, savedP, titleKeyP, (user, saved, titleKey) => <TopBar user={user} saved={saved} titleKey={titleKey} inRaamit={inRaamit}/>)
const allErrorsP = errorP(Bacon.combineAsArray(contentP, savedP))

// Renderered Virtual DOM
const domP = Bacon.combineWith(topBarP, userP, contentP, allErrorsP, (topBar, user, content, error) =>
    <div>
      <Error error={error}/>
      {topBar}
      {
        isTopLevel(error)
          ? <TopLevelError error={error} />
          : ( user
            ? content
            : null
          )
      }
      <LocalizationEditBar user={user}/>
    </div>
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