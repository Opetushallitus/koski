import React from 'react'
import {UserInfo} from './UserInfo.jsx'

export const TopBar = ({user, saved}) => (
  <header id='topbar' className={saved ? 'saved' : ''}>
    <div id='logo'>Opintopolku.fi</div>
    <h1><a href="/koski">Koski</a></h1>
    <span className="save-info">Kaikki muutokset tallennettu.</span>
    <UserInfo user={user} />
  </header>
)