import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import Http from "./http"
import {navigateToOppija, routeP} from "./router"
import {isValidHetu} from "./hetu"

export const oppijaP = routeP.flatMap(route => {
  var match = route.match(new RegExp("oppija/(.*)"))
  return match ? Http.get(`/tor/api/oppija?query=${match[1]}`).mapError([]).map(".0") : Bacon.once(undefined)
}).toProperty()

export const uusiOppijaP = routeP.map(route => {
  var match = route.match(new RegExp("uusioppija"))
  return !!match
})

export const Oppija = ({oppija, uusiOppija}) => oppija ?
  <div className="oppija">
    <h2>{oppija.sukunimi}, {oppija.etunimet} <span className="hetu">{oppija.hetu}</span></h2>
    <hr></hr>
  </div> : (
    uusiOppija
      ? <CreateOppija/>
      : <div></div>
    )


const CreateOppija = React.createClass({
  render() {
    const {etunimet, sukunimi, kutsumanimi, hetu, inProgress} = this.state

    const submitDisabled = !etunimet || !sukunimi || !kutsumanimi || !isValidHetu(hetu) || inProgress
    const buttonText = !inProgress ? "Lisää henkilö" : "Lisätään..."
    const hetuClassName = !hetu ? "" : isValidHetu(hetu) ? "" : "error"

    return (
      <form className="oppija stacked" onInput={this.onInput}>
        <label className="etunimet">
          Etunimet
          <input ref="etunimet"></input>
        </label>
        <label className="kutsumanimi">
          Kutsumanimi
          <input ref="kutsumanimi"></input>
        </label>
        <label className="sukunimi">
          Sukunimi
          <input ref="sukunimi"></input>
        </label>
        <label className="hetu">
          Henkilötunnus
          <input ref="hetu" className={hetuClassName}></input>
        </label>
        <button className="button blue" disabled={submitDisabled} onClick={this.submit}>{buttonText}</button>
      </form>
    )
  },

  getInitialState() {
    return {etunimet: '', sukunimi: '', kutsumanimi: '', hetu: ''}
  },

  formState() {
    return {
      etunimet: this.refs.etunimet.value,
      sukunimi: this.refs.sukunimi.value,
      kutsumanimi: this.refs.kutsumanimi.value,
      hetu: this.refs.hetu.value
    }
  },

  onInput() {
    this.setState(this.formState())
  },

  submit(e) {
    e.preventDefault()
    this.setState({inProgress: true})

    const createOppijaS = Http.post('/tor/api/oppija', this.formState()).map(oid => ({oid: oid}));
    createOppijaS.onValue(navigateToOppija)
    createOppijaS.onError(() => this.setState({inProgress: false}))
  }
})