import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import Http from './http'
import {navigateToOppija, routeP, showError} from './router'
import {isValidHetu} from './hetu'
import {OpintoOikeus} from './CreateOpintoOikeus.jsx'

export const oppijaP = routeP.map(".oppijaId").flatMap(oppijaId => {
  return oppijaId ? Bacon.once(undefined).concat(Http.get(`/tor/api/oppija/${oppijaId}`)) : Bacon.once(undefined)
}).toProperty()

export const uusiOppijaP = routeP.map(route => { return !!route.uusiOppija })

export const loadingOppijaP = routeP.awaiting(oppijaP.mapError())

export const Oppija = ({oppija}) =>
  oppija.loading
    ? <Loading/>
    : (oppija.valittuOppija
      ? <ExistingOppija oppija={oppija.valittuOppija}/>
      : (
      oppija.uusiOppija
        ? <CreateOppija/>
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
        { oppija.opintoOikeudet.map( opintoOikeus =>
          <Opintooikeus opintooikeus={ opintoOikeus } />
        ) }
      </div>
    )
  }
})

const Opintooikeus = React.createClass({
  render() {
    let {opintooikeus} = this.props
    return <div className="opintooikeus">
      <h4>Opinto-oikeudet</h4>
      <span className="tutkinto">{opintooikeus.nimi}</span> <span className="oppilaitos">{opintooikeus.oppilaitos.nimi}</span>
    </div>
  }
})

const CreateOppija = React.createClass({
  render() {
    const opintoOikeus = this.state.opintoOikeus
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
        <OpintoOikeus opintoOikeusBus={this.state.opintoOikeusBus}/>
        <button className='button blue' disabled={submitDisabled} onClick={this.submit}>{buttonText}</button>
        <ul className='error-messages'>
          {errors}
        </ul>
      </form>
    )
  },

  getInitialState() {
    return {etunimet: '', sukunimi: '', kutsumanimi: '', hetu: '', opintoOikeus: {valid: false}, opintoOikeusBus: Bacon.Bus()}
  },

  oppijaFromDom() {
    return {
      etunimet: this.refs.etunimet.value,
      sukunimi: this.refs.sukunimi.value,
      kutsumanimi: this.refs.kutsumanimi.value,
      hetu: this.refs.hetu.value.toUpperCase(),
    }
  },

  componentDidMount() {
    this.state.opintoOikeusBus.onValue(o => {this.setState({opintoOikeus: o})})
    this.refs.etunimet.focus()
  },

  onInput() {
    this.setState(this.oppijaFromDom())
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
    const {etunimet, sukunimi, kutsumanimi, hetu} = this.oppijaFromDom()
    const {tutkinto: {ePerusteDiaarinumero: peruste},oppilaitos: {organisaatioId: organisaatio}} = this.state.opintoOikeus
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