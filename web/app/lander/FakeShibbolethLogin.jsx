import '../polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import '../style/main.less'
import {IdLogin} from './IdLogin.jsx'

ReactDOM.render((
  <div>
    <IdLogin/>
  </div>
), document.getElementById('content'))

document.body.id = 'lander-page'
