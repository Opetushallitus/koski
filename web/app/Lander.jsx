import React from 'react'
import ReactDOM from 'react-dom'
import './style/main.less'
import {TopBar} from './TopBar.jsx'
import Text from './Text.jsx'

const Lander = () => (
  <div className={'lander'}>
    <h1><Text name="Koski-palvelu"/></h1>
    <p><Text name="Koulutustiedot luotettavasti yhdestä paikasta"/></p>
    <div className={'lander__caption'}>
      <p><Text name="Lander ingressi 1"/></p>
      <p><Text name="Lander ingressi 2"/></p>
    </div>
    <button className='button blue'>
      {'Kirjaudu sisään'}
    </button>
  </div>
)

ReactDOM.render((
  <div>
    <TopBar user={null} saved={null} title={''} />
    <Lander/>
  </div>
), document.getElementById('content'))

document.body.id = 'lander-page'
