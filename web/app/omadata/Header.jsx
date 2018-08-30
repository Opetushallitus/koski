import React from 'baret'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import Logout from './fragments/Logout'
import {lang, setLang} from '../i18n/i18n'

const menuOpened = Atom(false)

const ChangeLang = () =>
  (<span className='change-lang' onClick={() => lang === 'sv' ? setLang('fi') : setLang('sv')}>
    {lang === 'sv' ? 'Suomeksi' : 'På svenska'}
  </span>)


export default ({ logoutURL }) => (
  <div className='header'>
    <button id='header-mobile-menu-button' onClick={() => menuOpened.modify(x => !x)}>
      <img src='/koski/images/baseline-menu-24px.svg' />
    </button>
    <div className='title'>
      <img src='/koski/images/opintopolku_logo.svg' alt='' />
      <h1><Text name='Oma Opintopolku'/></h1>
    </div>

    <div className='lang'>
      <ChangeLang />
    </div>

    <div>
      <div className='user'>
        <Logout logoutURL={logoutURL} />
      </div>

      <div id='header-mobile-menu' className={menuOpened.map(opened => opened ? 'menu-open' : 'menu-closed')}>
        <div className='top'>
          <Logout logoutURL={logoutURL} />
        </div>
      </div>
    </div>
  </div>
)
