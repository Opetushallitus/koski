import React from 'react'
import { logout } from './user'
import Text from './Text.jsx'

export const UserInfo = ({user}) => user ?
  <div className='user-info'><span className="name">{user.name}</span> <a className='button' id='logout' onClick={ logout }><Text name="Kirjaudu ulos"/></a></div> :
  <div/>