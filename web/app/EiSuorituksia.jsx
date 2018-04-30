import React from 'react'
import ReactDOM from 'react-dom'
import './style/main.less'
import {EiSuorituksiaInfo} from './omattiedot/EiSuorituksiaInfo'

ReactDOM.render((
  <div>
    <div className='koski-heading'><h1>{'KOSKI'}</h1></div>
    <EiSuorituksiaInfo/>
  </div>
), document.getElementById('content'))

