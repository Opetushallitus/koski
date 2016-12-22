import './polyfills.js'
import './style/main.less'
import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import {Error, TopLevelError, isTopLevel, handleError, errorP} from './Error.jsx'
import {userP} from './user'
import {contentP, titleP} from './router'
import {selectOppijaE, updateResultE} from './Oppija.jsx'
import {TopBar} from './TopBar.jsx'


// Stays at `true` for five seconds after latest saved change. Reset to `false` when another Oppija is selected.
const savedP = updateResultE.flatMapLatest(() => Bacon.once(true).concat((selectOppijaE.merge(Bacon.later(5000))).map(false))).toProperty(false).skipDuplicates()

const topBarP = Bacon.combineWith(userP, savedP, titleP, (user, saved, title) => <TopBar user={user} saved={saved} title={title} />)
const allErrorsP = errorP(Bacon.combineAsArray(contentP, savedP))

// Renderered Virtual DOM
const domP = Bacon.combineWith(topBarP, userP, contentP, allErrorsP, (topBar, user, content, error) =>
    <div>
      <Error error={error}/>
      {topBar}
      {
        isTopLevel(error)
          ? <TopLevelError status={error.httpStatus} text={error.text} />
          : ( user
            ? content
            : null
          )
      }
    </div>
)

// Render to DOM
domP.onValue((component) => ReactDOM.render(component, document.getElementById('content')))

titleP.onValue((title) => {
  let defaultTitle = 'Koski - Opintopolku.fi'
  document.querySelector('title').innerHTML = title ? title + ' - ' + defaultTitle : defaultTitle
})

// Handle errors
domP.onError(handleError)