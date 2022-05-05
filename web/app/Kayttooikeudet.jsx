// eslint-disable-next-line no-undef
__webpack_nonce__ = window.nonce
import(/* webpackChunkName: "styles" */ './style/main.less')
import './polyfills/polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import {LuvanHallinta} from './omadata/luvanhallinta/LuvanHallinta'
import OmatTiedotTopBar from './topbar/OmatTiedotTopBar'

ReactDOM.render((
  <div>
    <OmatTiedotTopBar />
    <LuvanHallinta/>
  </div>
), document.getElementById('content'))
