import React from 'react'
import ReactDOM from 'react-dom'
import Autocomplete from './Autocomplete.jsx'
import Bacon from 'baconjs'
import Http from './http'

const oppilaitosE = new Bacon.Bus()
const oppilaitosP = oppilaitosE.toProperty(undefined).skipDuplicates()
const tutkintoE = new Bacon.Bus()
const tutkintoP = tutkintoE.merge(oppilaitosP.changes().map(undefined)).toProperty(undefined).skipDuplicates()

const Oppilaitos = React.createClass({
  render() {
    return <label className='oppilaitos'>Oppilaitos
        <Autocomplete
          resultBus={oppilaitosE}

          fetchItems={(value) => (value.length >= 3)
        ? Http.get('/tor/api/oppilaitos?query=' + value)
        : Bacon.once([])}

          selected={this.props.oppilaitos}
          />
      </label>
  }
})

const Tutkinto = React.createClass({
  render() {
    return <label className='tutkinto'>Tutkinto
      <Autocomplete
        resultBus={tutkintoE}

        fetchItems={(value) => (value.length >= 3)
        ? Http.get('/tor/api/tutkinto/oppilaitos/' + this.props.oppilaitos.organisaatioId + '?query=' + value)
        : Bacon.once([])}

        selected={this.props.tutkinto}
        disabled={!this.props.oppilaitos}
        />
    </label>
  }
})

export const OpintoOikeus = ({opintoOikeus}) => <div>
  <Oppilaitos oppilaitos= {opintoOikeus.oppilaitos}/>
  <Tutkinto tutkinto={opintoOikeus.tutkinto} oppilaitos={opintoOikeus.oppilaitos}/>
</div>

export const opintoOikeusP = Bacon.combineTemplate({
  oppilaitos: oppilaitosP,
  tutkinto: tutkintoP,
  valid: oppilaitosP.and(tutkintoP)
})