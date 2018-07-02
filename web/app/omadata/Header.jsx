import React from 'baret'
import Text from '../i18n/Text'
import Atom from 'bacon.atom'
const menuOpened = Atom(false)


export default ({ userP, onLogoutClicked }) => (
  <div className="header">
    <button id="header-mobile-menu-button" onClick={() => menuOpened.set(!menuOpened.get())}>
      <img src="/koski/images/baseline-menu-24px.svg" />
    </button>
    <div className="title">
      <img src="/koski/images/opintopolku_logo.svg" alt="" />
      <h1><Text name="Oma Opintopolku"/></h1>
    </div>

    { userP.map(user => ( user &&
      <div>

        <div className="user">
          <div className="username">
            <img src="/koski/images/profiili.svg" alt="user-icon" />
            {user.name}
          </div>
          <div className="logout" onClick={() => { onLogoutClicked() }}><Text name="Kirjaudu ulos"/></div>
        </div>

        <div id="header-mobile-menu" className={menuOpened.map(opened => opened ? 'menu-open' : 'menu-closed')}>
          <div className="top">
            <div className="username">{user.name}</div>
            <div className="logout" onClick={() => { onLogoutClicked() }}><Text name="Kirjaudu ulos"/></div>
          </div>
        </div>

      </div>
    ))}

  </div>
)
