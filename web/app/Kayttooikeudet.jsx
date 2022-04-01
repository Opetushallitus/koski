import './polyfills/polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import './style/main.less'
import { LuvanHallinta } from './omadata/luvanhallinta/LuvanHallinta'
import OmatTiedotTopBar from './topbar/OmatTiedotTopBar'

function KayttooikeudetContainer() {
  return (
    <div>
      <OmatTiedotTopBar />
      <LuvanHallinta />
    </div>
  )
}

ReactDOM.render(
  <KayttooikeudetContainer />,
  document.getElementById('content')
)
