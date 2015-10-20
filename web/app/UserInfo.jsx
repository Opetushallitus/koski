import React from 'react'
import { logout } from './Login.jsx'

export const UserInfo = ({user}) => user ?
  <div className='user-info'>{user.name} <a className='button blue' id='logout' onClick={ logout }>Kirjaudu ulos</a></div> :
  <div/>