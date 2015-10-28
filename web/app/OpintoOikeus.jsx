import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'

export const opintoOikeusChange = Bacon.Bus()

const withEmptyValue = (xs) => [{ koodi: '', nimi: 'Valitse...'}].concat(xs)

export const OpintoOikeus = React.createClass({
  render() {
    let {opintoOikeus} = this.props
    return (
      <div className="opintooikeus">
        <h4>Opinto-oikeudet</h4>
        <span className="tutkinto">{opintoOikeus.tutkinto.nimi}</span> <span className="oppilaitos">{opintoOikeus.oppilaitosOrganisaatio.nimi}</span>
        { opintoOikeus.tutkinto.rakenne
          ?
            <div className="tutkinto-rakenne">
                <label>Suoritustapa
                    <select className="suoritustapa" value={opintoOikeus.suoritustapa} onChange={(event) => opintoOikeusChange.push([opintoOikeus.id, oo => R.merge(oo, {suoritustapa: event.target.value || undefined})] )}>
                        {withEmptyValue(opintoOikeus.tutkinto.rakenne.suoritustavat).map(s => <option key={s.koodi} value={s.koodi}>{s.nimi}</option>)}
                    </select>
                </label>
                <label>Osaamisala
                    <select className="osaamisala" value={opintoOikeus.osaamisala} onChange={(event) => opintoOikeusChange.push([opintoOikeus.id, oo => R.merge(oo, {osaamisala: event.target.value || undefined})] )}>
                        {withEmptyValue(opintoOikeus.tutkinto.rakenne.osaamisalat).map(o => <option key={o.koodi} value={o.koodi}>{o.nimi}</option>)}
                    </select>
                </label>
              { opintoOikeus.suoritustapa
                ? <Rakenneosa
                    selectedTutkinnonOsa={this.state.selectedTutkinnonOsa}
                    tutkinnonOsaBus={this.state.tutkinnonOsaBus}
                    rakenneosa={opintoOikeus.tutkinto.rakenne.suoritustavat.find(x => x.koodi == opintoOikeus.suoritustapa).rakenne}
                    opintoOikeus={opintoOikeus}
                  />
                : null
              }
            </div>
          : null
        }
      </div>
    )
  },
  getInitialState() {
      return {
        tutkinnonOsaBus: Bacon.Bus(),
        selectedTutkinnonOsa: undefined
      }
  },
  componentDidMount() {
      this.state.tutkinnonOsaBus
          .onValue(tutkinnonOsa => {
            this.setState({selectedTutkinnonOsa: tutkinnonOsa})
          })
  }
})

const Rakenneosa = React.createClass({
  render() {
    let { rakenneosa, opintoOikeus, selectedTutkinnonOsa, tutkinnonOsaBus } = this.props
    return rakenneosa.osat
      ? <RakenneModuuli key={rakenneosa.nimi} opintoOikeus={opintoOikeus} rakenneosa={rakenneosa} selectedTutkinnonOsa={selectedTutkinnonOsa} tutkinnonOsaBus={tutkinnonOsaBus}/>
      : <TutkinnonOsa key={rakenneosa.nimi} opintoOikeus={opintoOikeus} tutkinnonOsa={rakenneosa} selectedTutkinnonOsa={selectedTutkinnonOsa} tutkinnonOsaBus={tutkinnonOsaBus}/>
  }
})

const RakenneModuuli = React.createClass({
  render() {
    const { rakenneosa, opintoOikeus, selectedTutkinnonOsa, tutkinnonOsaBus } = this.props
    return (
      <div className="rakenne-moduuli">
        <span className="name">{rakenneosa.nimi}</span>
        <ul className="osat">
          { rakenneosa.osat
            .filter(osa => { return !osa.osaamisalaKoodi || osa.osaamisalaKoodi == opintoOikeus.osaamisala})
            .map((osa, i) => <li key={i}>
              <Rakenneosa
                rakenneosa={osa}
                opintoOikeus={opintoOikeus}
                selectedTutkinnonOsa={selectedTutkinnonOsa}
                tutkinnonOsaBus={tutkinnonOsaBus}/>
            </li>)
          }
        </ul>
      </div>
    )
  }
})

const TutkinnonOsa = React.createClass({
  render() {
    const {tutkinnonOsa, opintoOikeus, selectedTutkinnonOsa, tutkinnonOsaBus} = this.props
    const selected = selectedTutkinnonOsa && tutkinnonOsa.nimi === selectedTutkinnonOsa.nimi
    const arviointiAsteikko = R.find(asteikko => R.equals(asteikko.koodisto, tutkinnonOsa.arviointiAsteikko))(opintoOikeus.tutkinto.rakenne.arviointiAsteikot)
    const arvosanat = arviointiAsteikko ? arviointiAsteikko.arvosanat : undefined
    const addArvosana = (arvosana) => (opintoOikeus) => {
      let suoritukset = opintoOikeus.suoritukset.concat({ koulutusModuuli: tutkinnonOsa.tunniste, arviointi: { asteikko: tutkinnonOsa.arviointiAsteikko, arvosana: arvosana }})
      return R.merge(opintoOikeus, { suoritukset })
    }
    const saveArvosana = (arvosana) => {
      opintoOikeusChange.push([opintoOikeus.id, addArvosana(arvosana)])
      tutkinnonOsaBus.push(undefined) // <- deselect
    }
    const suoritus = R.find(suoritus => R.equals(suoritus.koulutusModuuli, tutkinnonOsa.tunniste))(opintoOikeus.suoritukset)

    return (
      <div className={ selected ? 'tutkinnon-osa selected' : 'tutkinnon-osa'} onClick={() => {if (!selected) tutkinnonOsaBus.push(tutkinnonOsa)}}>
        <span className="name">{tutkinnonOsa.nimi}</span>
        { selected && arvosanat
          ?
            <div className="arviointi">
              <ul className="arvosanat">{
                arvosanat.map((arvosana) => <li className= { arvosana == this.state.valittuArvosana ? 'selected' : '' } key={arvosana.id} onClick={() => this.setState({ valittuArvosana: arvosana })}>{arvosana.nimi}</li>)
              }</ul>
              <button className="button blue" disabled={!this.state.valittuArvosana} onClick={() => saveArvosana(this.state.valittuArvosana)}>Tallenna arvio</button>
            </div>
          : (
            suoritus && suoritus.arviointi
              ? <div className="arviointi"><span className="arvosana">{suoritus.arviointi.arvosana.nimi}</span></div>
              : null
          )
        }
      </div>
    )
  },
  getInitialState() {
    return {}
  }
})