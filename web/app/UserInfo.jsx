import React from "react"
import ReactDOM from "react-dom"

export const UserInfo = ({user}) => user ?
  <div className="user-info">{user.name} <a className="button blue" href="/tor/logout">Kirjaudu ulos</a></div> :
  <div/>