import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import Http from "./http"
import {navigateToOppija, routeP, showError} from "./router"
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
    const {etunimet, sukunimi, kutsumanimi, hetu, inProgress, hetuConflict} = this.state
    console.log(hetuConflict)
    const submitDisabled = !etunimet || !sukunimi || !kutsumanimi || !isValidHetu(hetu) || !this.isKutsumanimiOneOfEtunimet(kutsumanimi, etunimet) || inProgress
    const buttonText = !inProgress ? "Lisää henkilö" : "Lisätään..."
    const hetuClassName = !hetu ? "hetu" : isValidHetu(hetu) ? (hetuConflict ? "hetu conflict": "hetu") : "hetu error"
    const kutsumanimiClassName = this.isKutsumanimiOneOfEtunimet(kutsumanimi, etunimet) ? "kutsumanimi" : "kutsumanimi error"

    return (
      <form className="oppija stacked" onInput={this.onInput}>
        <label className="etunimet">
          Etunimet
          <input ref="etunimet"></input>
        </label>
        <label className={kutsumanimiClassName}>
          Kutsumanimi
          <input ref="kutsumanimi"></input>
        </label>
        <label className="sukunimi">
          Sukunimi
          <input ref="sukunimi"></input>
        </label>
        <label className={hetuClassName}>
          Henkilötunnus
          <input ref="hetu"></input>
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
      hetu: this.refs.hetu.value.toUpperCase()
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
    createOppijaS.onError((e) => {
      this.setState({inProgress: false})
      if (e.httpStatus == 409) {
        this.setState({hetuConflict: true})
      } else {
        showError(e)
      }
    })
  },

  isKutsumanimiOneOfEtunimet(kutsumanimi, etunimet) {
    return kutsumanimi && etunimet ? etunimet.split(" ").indexOf(kutsumanimi) > -1 || etunimet.split("-").indexOf(kutsumanimi) > -1: true
  }
})