import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import Http from './http'
import {navigateToOppija, routeP, showError} from './router'
import {isValidHetu} from './hetu'
import {Koulutus} from './Koulutus.jsx'

export const oppijaP = routeP.flatMap(route => {
  var match = route.match(new RegExp('oppija/(.*)'))
  return match ? Http.get(`/tor/api/oppija/${match[1]}`).mapError(undefined) : Bacon.once(undefined)
}).toProperty()

export const uusiOppijaP = routeP.map(route => {
  var match = route.match(new RegExp('uusioppija'))
  return !!match
})

export const Oppija = ({oppija, koulutus}) => oppija.valittuOppija ?
    <ExistingOppija oppija={oppija.valittuOppija}/> : (
    oppija.uusiOppija
      ? <CreateOppija koulutus={koulutus}/>
      : <div></div>
    )

const ExistingOppija = React.createClass({
  render() {
    let {oppija} = this.props
    return (
      <div className='main-content oppija'>
        <h2>{oppija.sukunimi}, {oppija.etunimet} <span className='hetu'>{oppija.hetu}</span></h2>
        <hr></hr>
        <Tutkinto tutkinto={oppija.tutkinnot.length ? oppija.tutkinnot[0] : undefined} />
      </div>
    )
  }
})

const Tutkinto = React.createClass({
  render() {
    let {tutkinto} = this.props
    return tutkinto ?
      <div className="tutkinto">
        <h4>Opinto-oikeudet</h4>
        <span className="tutkinto-name">{tutkinto.nimi}</span> <span className="oppilaitos">{tutkinto.oppilaitos.nimi}</span>
      </div> : null
  }
})

const CreateOppija = React.createClass({
  render() {
    const koulutus = this.props.koulutus
    const {etunimet, sukunimi, kutsumanimi, hetu, inProgress, hetuConflict} = this.state
    const validKutsumanimi = this.isKutsumanimiOneOfEtunimet(kutsumanimi, etunimet)
    const submitDisabled = !etunimet || !sukunimi || !kutsumanimi || !isValidHetu(hetu) || !validKutsumanimi || inProgress || !koulutus.valid
    const buttonText = !inProgress ? 'Lisää henkilö' : 'Lisätään...'
    const hetuClassName = !hetu ? 'hetu' : isValidHetu(hetu) ? 'hetu' : 'hetu error'
    const kutsumanimiClassName = validKutsumanimi ? 'kutsumanimi' : 'kutsumanimi error'

    const errors = []
    if(hetuConflict) {
      errors.push(<li key='1' className='hetu'>Henkilötunnuksella löytyy jo henkilö.</li>)
    }
    if(!validKutsumanimi) {
      errors.push(<li key='2' className='kutsumanimi'>Kutsumanimen on oltava yksi etunimistä.</li>)
    }

    return (
      <form className='main-content oppija uusi-oppija' onInput={this.onInput}>
        <label className='etunimet'>
          Etunimet
          <input ref='etunimet'></input>
        </label>
        <label className={kutsumanimiClassName}>
          Kutsumanimi
          <input ref='kutsumanimi'></input>
        </label>
        <label className='sukunimi'>
          Sukunimi
          <input ref='sukunimi'></input>
        </label>
        <label className={hetuClassName}>
          Henkilötunnus
          <input ref='hetu'></input>
        </label>
        <hr/>
        <Koulutus koulutus={this.props.koulutus}/>
        <button className='button blue' disabled={submitDisabled} onClick={this.submit}>{buttonText}</button>
        <ul className='error-messages'>
          {errors}
        </ul>
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
      hetu: this.refs.hetu.value.toUpperCase(),
      koulutus: this.props.koulutus
    }
  },

  componentDidMount() {
    this.refs.etunimet.focus()
  },

  onInput() {
    this.setState(this.formState())
  },

  submit(e) {
    e.preventDefault()
    this.setState({inProgress: true})

    const createOppijaS = Http.post('/tor/api/oppija', this.formState()).map(oid => ({oid: oid}))
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
    return kutsumanimi && etunimet ? etunimet.split(' ').indexOf(kutsumanimi) > -1 || etunimet.split('-').indexOf(kutsumanimi) > -1: true
  }
})