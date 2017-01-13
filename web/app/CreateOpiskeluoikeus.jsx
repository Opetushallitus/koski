import React from 'react'
import Autocomplete from './Autocomplete.jsx'
import Bacon from 'baconjs'
import Http from './http'
import { showInternalError } from './location.js'

const Oppilaitos = React.createClass({
  render() {
    return (
        <label className='oppilaitos'>Oppilaitos
          <Autocomplete
              resultBus={this.props.oppilaitosBus}
              fetchItems={value => (value.length >= 1)
                ? this.state.oppilaitokset.map(oppilaitokset => oppilaitokset.filter(oppilaitos => oppilaitos.nimi.fi.toLowerCase().indexOf(value.toLowerCase()) >= 0))
                : Bacon.once([])
              }
              selected={this.state.selected}
              />
        </label>
    )
  },

  getInitialState() {
    return { oppilaitokset: Http.cachedGet('/koski/api/oppilaitos')}
  },

  componentDidMount() {
    this.props.oppilaitosP.onValue(o => {this.setState({selected:o})})
  }
})

const Tutkinto = React.createClass({
  render() {
    return (
        <label className='tutkinto'>Tutkinto
          <Autocomplete
              resultBus={this.props.tutkintoBus}
              fetchItems={(value) => (value.length >= 3)
                ? Http.cachedGet('/koski/api/tutkinnonperusteet/oppilaitos/' + this.state.oppilaitos.oid + '?query=' + value).doError(showInternalError)
                : Bacon.constant([])}
              disabled={!this.state.oppilaitos}
              selected={this.state.selected}
              />
        </label>
    )
  },

  getInitialState() {
    return {oppilaitos: undefined}
  },

  componentDidMount() {
    const {oppilaitosP, tutkintoP} = this.props
    oppilaitosP
      .map(o => ({oppilaitos: o, selected:undefined})).toEventStream()
      .merge(tutkintoP.map(t => ({selected:t})).toEventStream())
      .onValue(state => this.setState(state))
  }
})

export const Opiskeluoikeus = React.createClass({
  render() {
    const {oppilaitosBus, oppilaitosP, tutkintoBus, tutkintoP} = this.state
    return (
      <div>
        <Oppilaitos oppilaitosBus={oppilaitosBus} oppilaitosP={oppilaitosP} />
        <Tutkinto tutkintoBus={tutkintoBus} tutkintoP={tutkintoP} oppilaitosP={oppilaitosP}/>
      </div>
    )
  },

  componentDidMount() {
    const {oppilaitosP, tutkintoP} = this.state
    const opiskeluoikeus = Bacon.combineTemplate({
      oppilaitos: oppilaitosP,
      tutkinto: tutkintoP,
      valid: oppilaitosP.and(tutkintoP).map(v => !!(v))
    })
    this.props.opiskeluoikeusBus.plug(opiskeluoikeus)
  },

  getInitialState() {
    const oppilaitosBus = Bacon.Bus()
    const tutkintoBus = Bacon.Bus()
    const oppilaitosP = oppilaitosBus.toProperty(undefined).skipDuplicates()
    const tutkintoP = tutkintoBus.merge(oppilaitosP.changes().map(undefined)).toProperty(undefined).skipDuplicates()

    return {
      oppilaitosBus: oppilaitosBus,
      tutkintoBus: tutkintoBus,
      oppilaitosP: oppilaitosP,
      tutkintoP: tutkintoP
    }
  }
})