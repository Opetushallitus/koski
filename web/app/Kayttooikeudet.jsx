// eslint-disable-next-line no-undef
import './polyfills/polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import { LuvanHallinta } from './omadata/luvanhallinta/LuvanHallinta'
import OmatTiedotTopBar from './topbar/OmatTiedotTopBar'
import { loadStyles } from './util/loadStyles'

__webpack_nonce__ = window.nonce
loadStyles(() => import(/* webpackChunkName: "styles" */ './style/main.less'))

ReactDOM.render(
  <div>
    <OmatTiedotTopBar />
    <LuvanHallinta />
  </div>,
  document.getElementById('content')
)
