import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import Http from "./http"
const loginE = new Bacon.Bus()

const loginResultE = loginE
    .flatMap((credentials) => Http.post("/login", credentials))

export const userP = Http.get("/user").mapError()
    .merge(loginResultE)
    .toProperty()

export const Login = React.createClass({
  render() {
    const {username, password, inProgress} = this.state

    const usernameIsValid = username && username.length > 0
    const passwordIsValid = password && password.length > 0

    const buttonLabel = inProgress ? "Kirjaudutaan..." : "Kirjaudu Sisään"
    const buttonDisabled = !usernameIsValid || !passwordIsValid || inProgress

    return <form className={this.state.error ? "login error": "login"}>
      <h3>TOR LOGIN</h3>
      <input onInput={this.onInput} ref="username" placeholder="Tunnus" disabled={inProgress}></input>
      <input onInput={this.onInput} type="password" ref="password" placeholder="Salasana" disabled={inProgress}></input>
      <button onClick={this.doLogin} disabled={buttonDisabled}>{buttonLabel}</button>
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
    this.setState({inProgress: true})
    loginE.push(this.formState())
  },

  onInput() {
    this.setState(this.formState())
  },

  componentDidMount() {
    loginResultE.onError((e) => {this.setState({error: e, inProgress: false}); this.refs.username.focus()})
    this.refs.username.focus()
  }
})
