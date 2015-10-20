import React from 'react'
import {UserInfo} from './UserInfo.jsx'

export const TopBar = ({user}) => (
  <header id='topbar'>
    <div id='logo'>Opintopolku.fi</div>
    <h1>Todennetun osaamisen rekisteri</h1>
    <UserInfo user={user} />
  </header>
)