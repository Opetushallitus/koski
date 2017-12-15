import '../polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import '../style/main.less'
import HetuLogin from './HetuLogin.jsx'

ReactDOM.render((
  <div>
    <HetuLogin/>
  </div>
), document.getElementById('content'))

document.body.id = "fake-shibboleth-login"