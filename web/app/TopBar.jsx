import React from 'react'
import {UserInfo} from './UserInfo.jsx'
import Link from './Link.jsx'
import Text from './Text.jsx'
import {edit, startEdit} from './i18n-edit'

export const TopBar = ({user, saved, titleKey}) => {
  let onClick = e => {
    // Start editing localized text by alt-clicking on the H1 Heading
    if (e.getModifierState('Alt') && !edit.get()) {
      startEdit()
      e.stopPropagation()
      e.preventDefault()
    }
  }
  return (<header id='topbar' className={saved ? 'saved' : ''}>
      <div id='logo'><Text name="Opintopolku.fi"/></div>
      <h1 onClick={onClick}><Link href="/koski/"><Text name="Koski"/></Link>{titleKey ?
        <span>{' - '}<Text name={titleKey}/></span> : ''}</h1>
      <span className="save-info"><Text name="Kaikki muutokset tallennettu."/></span>
      <UserInfo user={user}/>
    </header>
  )
}