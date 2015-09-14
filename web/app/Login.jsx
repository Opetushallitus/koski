import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import http from "axios"

const loginE = new Bacon.Bus()

export const Login = React.createClass({
  render() {
    return <form className="login">
      <h3>TOR LOGIN</h3>
      <input ref="username" placeholder="Tunnus"></input>
      <input ref="password" placeholder="Salasana"></input>
      <button onClick={this.doLogin}>Kirjaudu sisään</button>
    </form>
  },
  doLogin(e) {
    e.preventDefault()
    loginE.push({ username: this.refs.username.value, password: this.refs.password.value })
  }
})

const loginResultE = loginE
  .flatMap((credentials) => Bacon.fromPromise(http.post("/login", credentials)))
  .map(".data")

export const userP = Bacon.fromPromise(http.get("/user"))
  .mapError(undefined)
  .merge(loginResultE)
  .toProperty()

