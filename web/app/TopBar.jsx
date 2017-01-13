import React from 'react'
import {UserInfo} from './UserInfo.jsx'
import Link from './Link.jsx'

export const TopBar = ({user, saved, title}) => (
  <header id='topbar' className={saved ? 'saved' : ''}>
    <div id='logo'>Opintopolku.fi</div>
    <h1><Link href="/koski/">Koski</Link>{title ? ' - ' + title : ''}</h1>
    <span className="save-info">Kaikki muutokset tallennettu.</span>
    <UserInfo user={user} />
  </header>
)