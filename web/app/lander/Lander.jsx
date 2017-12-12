import React from 'react'
import ReactDOM from 'react-dom'
import '../style/main.less'
import {TopBar} from '../TopBar.jsx'
import {LanderInfo} from './LanderInfo.jsx'

ReactDOM.render((
  <div>
    <TopBar user={null} saved={null} title={''}/>
    <LanderInfo/>
  </div>
), document.getElementById('content'))

document.body.id = 'lander-page'
