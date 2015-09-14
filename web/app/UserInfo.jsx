import React from "react"
import ReactDOM from "react-dom"

export const UserInfo = ({user}) => <div className="userInfo">{user.name}<a href="/logout">Logout</a></div>