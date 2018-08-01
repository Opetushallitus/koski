import React from 'react'
import ReactDOM from 'react-dom'
import './style/main.less'
import {Kayttooikeudet} from './kayttooikeudet/Kayttooikeudet'
import OmatTiedotTopBar from './topbar/OmatTiedotTopBar'

ReactDOM.render((
  <div>
    <OmatTiedotTopBar />
    <Kayttooikeudet/>
  </div>
), document.getElementById('content'))
