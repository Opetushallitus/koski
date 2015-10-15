import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import Http from './http'
import {navigateToOppija, routeP, showError} from './router'
import {isValidHetu} from './hetu'
import {OpintoOikeus} from './OpintoOikeus.jsx'

export const oppijaP = routeP.flatMap(route => {
  var match = route.match(new RegExp('oppija/(.*)'))
  return match ? Bacon.once(undefined).concat(Http.get(`/tor/api/oppija/${match[1]}`).mapError(undefined)) : Bacon.later(0, undefined)
}).toProperty()

export const uusiOppijaP = routeP.map(route => {
  var match = route.match(new RegExp('uusioppija'))
  return !!match
})

export const loadingOppijaP = routeP.awaiting(oppijaP.mapError())

export const Oppija = ({oppija, opintoOikeus}) =>
  oppija.loading
    ? <Loading/>
    : (oppija.valittuOppija
      ? <ExistingOppija oppija={oppija.valittuOppija}/>
      : (
      oppija.uusiOppija
        ? <CreateOppija opintoOikeus={opintoOikeus}/>
        : <div></div>
      ))

const Loading = () => <div className='main-content oppija loading'></div>

const ExistingOppija = React.createClass({
  render() {
    let {oppija} = this.props
    return (
      <div className='main-content oppija'>
        <h2>{oppija.sukunimi}, {oppija.etunimet} <span className='hetu'>{oppija.hetu}</span></h2>
        <hr></hr>
        <Opintooikeus opintooikeus={oppija.opintoOikeudet.length ? oppija.opintoOikeudet[0] : undefined} />
      </div>
    )
  }
})

const Opintooikeus = React.createClass({
  render() {
    let {opintooikeus} = this.props
    return opintooikeus ?
      <div className="opintooikeus">
        <h4>Opinto-oikeudet</h4>
        <span className="tutkinto">{opintooikeus.nimi}</span> <span className="oppilaitos">{opintooikeus.oppilaitos.nimi}</span>
      </div> : null
  }
})

const CreateOppija = React.createClass({
  render() {
    const opintoOikeus = this.props.opintoOikeus
    const {etunimet, sukunimi, kutsumanimi, hetu, inProgress} = this.state
    const validKutsumanimi = this.isKutsumanimiOneOfEtunimet(kutsumanimi, etunimet)
    const submitDisabled = !etunimet || !sukunimi || !kutsumanimi || !isValidHetu(hetu) || !validKutsumanimi || inProgress || !opintoOikeus.valid
    const buttonText = !inProgress ? 'Lisää henkilö' : 'Lisätään...'
    const hetuClassName = !hetu ? 'hetu' : isValidHetu(hetu) ? 'hetu' : 'hetu error'
    const kutsumanimiClassName = validKutsumanimi ? 'kutsumanimi' : 'kutsumanimi error'

    const errors = []

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
        <OpintoOikeus opintoOikeus={this.props.opintoOikeus}/>
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
      opintoOikeus: this.props.opintoOikeus
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
    const createOppijaS = Http.post('/tor/api/oppija',  this.toCreateOppija()).map(oid => ({oid: oid}))
    createOppijaS.onValue(navigateToOppija)
    createOppijaS.onError((e) => {
      this.setState({inProgress: false})
      showError(e)
    })
  },

  toCreateOppija() {
    const {etunimet, sukunimi, kutsumanimi, hetu, opintoOikeus: {tutkinto: {ePerusteDiaarinumero: peruste},oppilaitos: {organisaatioId: organisaatio}}} = this.formState()
    return {
      etunimet: etunimet,
      sukunimi: sukunimi,
      kutsumanimi: kutsumanimi,
      hetu: hetu,
      opintoOikeus: {
        organisaatioId: organisaatio,
        ePerusteDiaarinumero: peruste
      }
    }
  },

  isKutsumanimiOneOfEtunimet(kutsumanimi, etunimet) {
    return kutsumanimi && etunimet ? etunimet.split(' ').indexOf(kutsumanimi) > -1 || etunimet.split('-').indexOf(kutsumanimi) > -1: true
  }
})