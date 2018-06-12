import './polyfills/polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import './style/main.less'
import HetuLogin from './korhopankki/HetuLogin'
import { currentLocation } from './util/location'

const getParam = (parameter) => {
  return currentLocation().params ?
    currentLocation().params[parameter] :
    undefined
}

ReactDOM.render((
  <div>
    <HetuLogin LoginUrl={getParam('login')} RedirectUrl={getParam('redirect')} />
  </div>
), document.getElementById('content'))
