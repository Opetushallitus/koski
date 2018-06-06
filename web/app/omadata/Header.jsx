import React from 'baret'
import Text from '../i18n/Text'
import Atom from 'bacon.atom'
const menuOpened = Atom(false)


export default ({ userP }) => (
  <div className="header">
    <button id="header-mobile-menu-button" onClick={() => menuOpened.set(!menuOpened.get())}><img src="/koski/images/baseline-menu-24px.svg" /></button>
    <div className="title"><h1><Text name="Oma Opintopolku"/></h1></div>
    <div className="user">
      <div className="username">{ userP.map(user => user && user.name ) }</div>
      <div className="logout"><Text name="Kirjaudu ulos"/></div>
    </div>

    {menuOpened.map((opened) => {
      return opened ?
        <div id="header-mobile-menu">
          <div className="top">
            <div className="username">{ userP.map(user => user && user.name ) }</div>
            <div className="logout"><Text name="Kirjaudu ulos"/></div>
          </div>
        </div>
        :
        null
    })}
  </div>
)
