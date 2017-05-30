import React from 'react'
import {UserInfo} from './UserInfo.jsx'
import Link from './Link.jsx'
import Text from './Text.jsx'

export const TopBar = ({user, saved, titleKey}) => (
  <header id='topbar' className={saved ? 'saved' : ''}>
    <div id='logo'><Text name="Opintopolku.fi"/></div>
    <h1><Link href="/koski/"><Text name="Koski"/></Link>{titleKey ? <span>{' - '}<Text name={titleKey}/></span> : ''}</h1>
    <span className="save-info"><Text name="Kaikki muutokset tallennettu."/></span>
    <UserInfo user={user} />
  </header>
)