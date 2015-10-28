import React from 'react'
import { logout } from './Login.jsx'

export const UserInfo = ({user}) => user ?
  <div className='user-info'><span className="name">{user.name}</span> <a className='button blue' id='logout' onClick={ logout }>Kirjaudu ulos</a></div> :
  <div/>