import React from 'react'
import Text from '../i18n/Text'

export default ({ firstName, lastName }) => (
  <div className="header">
    <div className="title"><h1><Text name="Oma Opintopolku"/></h1></div>
    <div className="user">
      <div className="username">{firstName} {lastName}</div>
      <div className="logout"><Text name="Kirjaudu ulos"/></div>
    </div>
  </div>
)

