import './polyfills/polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import './style/main.less'
import {TopBar} from './topbar/TopBar'
import {LanderInfo} from './lander/LanderInfo'

ReactDOM.render((
  <div>
    <TopBar user={null} />
    <LanderInfo/>
  </div>
), document.getElementById('content'))

document.body.id = 'lander-page'
