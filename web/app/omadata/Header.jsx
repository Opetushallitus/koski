import React from 'baret'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import Logout from './fragments/Logout'

const menuOpened = Atom(false)


export default ({ userP, logoutURL }) => (
  <div className='header'>
    <button id='header-mobile-menu-button' onClick={() => menuOpened.modify(x => !x)}>
      <img src='/koski/images/baseline-menu-24px.svg' />
    </button>
    <div className='title'>
      <img src='/koski/images/opintopolku_logo.svg' alt='' />
      <h1><Text name='Oma Opintopolku'/></h1>
    </div>

    { userP.map(user => ( user &&
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
    ))}

  </div>
)
