import React from 'react'
import { logout } from './user'

export const UserInfo = ({user}) => user ?
  <div className='user-info'><span className="name">{user.name}</span> <a className='button' id='logout' onClick={ logout }>Kirjaudu ulos</a></div> :
  <div/>