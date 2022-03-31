import React from 'baret'
import Text from '../i18n/Text'
import {lang, setLang} from '../i18n/i18n'


const ChangeLang = () =>
  (<span className='change-lang' onClick={() => lang === 'sv' ? setLang('fi') : setLang('sv')}>
    {lang === 'sv' ? 'Suomeksi' : 'På svenska'}
  </span>)

ChangeLang.displayName = 'ChangeLang'


const Header = () => (
  <div className='header'>
    <div className='title'>
      <img src='/koski/images/opintopolku_logo.svg' alt='' />
      <h1><Text name='Oma Opintopolku'/></h1>
    </div>

    <div className='lang'>
      <ChangeLang />
    </div>

  </div>
)

Header.displayName = 'Header'

export default Header
