import React from 'react'
import { logout } from '../util/user'
import Text from '../i18n/Text'

export const UserInfo = ({ user }) =>
  user ? (
    <div className="user-info">
      <span className="name">{user.name}</span>
      <button
        className="koski-button"
        id="logout"
        onClick={logout}
        tabIndex="0"
      >
        <Text name="Kirjaudu ulos" />
      </button>
    </div>
  ) : (
    <div />
  )
