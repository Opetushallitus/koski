import React from 'baret'
import {UserInfo} from './UserInfo.jsx'
import Link from './Link.jsx'
import Text from './Text.jsx'
import {edit, startEdit} from './i18n-edit'

export const TopBar = ({user, saved, titleKey}) => {
  let onClick = e => {
    if (!edit.get()) {
      startEdit()
      e.stopPropagation()
      e.preventDefault()
    }
  }

  console.log("render", edit.get())
  return (<header id='topbar' className={saved ? 'saved' : ''}>
      <div id='logo'><Text name="Opintopolku.fi"/></div>
      <h1><Link href="/koski/"><Text name="Koski"/></Link>{titleKey ?
          <span>{' - '}<Text name={titleKey}/></span> : ''}</h1>{edit.map((e) => !e && <a className="edit" onClick={onClick}>edit</a>)}
      <span className="save-info"><Text name="Kaikki muutokset tallennettu."/></span>
      <UserInfo user={user}/>
    </header>
  )
}