import React from 'baret'
import Text from '../i18n/Text'

export default ({ userP }) => (
  <div className="header">
    <div className="title"><h1><Text name="Oma Opintopolku"/></h1></div>
    <div className="user">
      <div className="username">{ userP.map(user => user.name) }</div>
      <div className="logout"><Text name="Kirjaudu ulos"/></div>
    </div>
  </div>
)

