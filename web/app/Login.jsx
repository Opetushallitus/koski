import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import Http from "./http"
const loginE = new Bacon.Bus()

export const Login = React.createClass({
  render() {
    const usernameIsValid = this.state.username && this.state.username.length > 0
    const passwordIsValid = this.state.password && this.state.password.length > 0

    return <form className="login">
      <h3>TOR LOGIN</h3>
      <input onInput={this.onInput} ref="username" placeholder="Tunnus"></input>
      <input onInput={this.onInput} ref="password" placeholder="Salasana"></input>
      <button onClick={this.doLogin} disabled={!usernameIsValid || !passwordIsValid}>Kirjaudu sisään</button>
    </form>
  },

  formState() {
    return { username: this.refs.username.value, password: this.refs.password.value }
  },

  getInitialState() {
    return {username: '', password: ''}
  },

  doLogin(e) {
    e.preventDefault()
    loginE.push(this.formState())
  },

  onInput() {
    this.setState(this.formState())
  },

  componentDidMount() {
    this.refs.username.focus()
  }
})

const loginResultE = loginE
  .flatMap((credentials) => Http.post("/login", credentials))

export const userP = Http.get("/user")
  .merge(loginResultE)
  .toProperty()

