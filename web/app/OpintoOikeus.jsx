import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'

export const opintoOikeusChange = Bacon.Bus()

const withEmptyValue = (xs) => [{ koodi: '', nimi: 'Valitse...'}].concat(xs)

// Shows <select> if more than 1 option. If 1 option, automatically selects it and shows it. If zero options, hides the whole thing.
const Dropdown = React.createClass({
  render() {
    let { title, options, value, onChange} = this.props
    return options.length > 0
        ? <label><span>{title}</span>{ options.length > 1
            ? <select className="suoritustapa" value={value} onChange={(event) => onChange(event.target.value)}> {withEmptyValue(options).map(s => <option key={s.koodi} value={s.koodi}>{s.nimi}</option>)} </select>
            : <div>{ options[0].nimi }</div>
          }</label>
        : <div></div>
  },
  componentDidMount() {
    let { options, onChange, value} = this.props
    if (options.length == 1 && value !== options[0].koodi) {
      onChange(options[0].koodi)
    }
  }
})
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
              <Dropdown className="suoritustapa"
                        title="Suoritustapa"
                        options={opintoOikeus.tutkinto.rakenne.suoritustavat.map(s => s.suoritustapa)}
                        value={opintoOikeus.suoritustapa}
                        onChange={(value) => opintoOikeusChange.push([opintoOikeus.id, oo => R.merge(oo, {suoritustapa: value || undefined})] )}
                />
              <Dropdown className="osaamisala"
                        title="Osaamisala"
                        options={opintoOikeus.tutkinto.rakenne.osaamisalat}
                        value={opintoOikeus.osaamisala}
                        onChange={(value) => opintoOikeusChange.push([opintoOikeus.id, oo => R.merge(oo, {osaamisala: value || undefined})] )}
                />
              { opintoOikeus.suoritustapa
                ? <Rakenneosa
                    rakenneosa={opintoOikeus.tutkinto.rakenne.suoritustavat.find(x => x.suoritustapa.koodi == opintoOikeus.suoritustapa).rakenne}
                    opintoOikeus={opintoOikeus}
                  />
                : null
              }
            </div>
          : null
        }
      </div>
    )
  }
})

const Rakenneosa = React.createClass({
  render() {
    let { rakenneosa, opintoOikeus } = this.props
    return rakenneosa.osat
      ? <RakenneModuuli key={rakenneosa.nimi} opintoOikeus={opintoOikeus} rakenneosa={rakenneosa} />
      : <TutkinnonOsa key={rakenneosa.nimi} opintoOikeus={opintoOikeus} tutkinnonOsa={rakenneosa} />
  }
})

const RakenneModuuli = React.createClass({
  render() {
    const { rakenneosa, opintoOikeus } = this.props
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
              />
            </li>)
          }
        </ul>
      </div>
    )
  }
})

const TutkinnonOsa = React.createClass({
  render() {
    const {tutkinnonOsa, opintoOikeus} = this.props
    const arviointiAsteikko = R.find(asteikko => R.equals(asteikko.koodisto, tutkinnonOsa.arviointiAsteikko))(opintoOikeus.tutkinto.rakenne.arviointiAsteikot)
    const arvosanat = arviointiAsteikko ? arviointiAsteikko.arvosanat : undefined

    const addArvosana = (arvosana) => (oOikeus) => {
      let suoritukset = oOikeus.suoritukset.concat({ koulutusModuuli: tutkinnonOsa.tunniste, arviointi: { asteikko: tutkinnonOsa.arviointiAsteikko, arvosana: arvosana }})
      return R.merge(oOikeus, { suoritukset })
    }

    const saveArvosana = (arvosana) => {
      opintoOikeusChange.push([opintoOikeus.id, addArvosana(arvosana)])
    }

    const suoritus = R.find(osanSuoritus => R.equals(osanSuoritus.koulutusModuuli, tutkinnonOsa.tunniste))(opintoOikeus.suoritukset)
    const arviointi = suoritus && suoritus.arviointi

    return (
      <div className={ arviointi ? 'tutkinnon-osa suoritettu' : 'tutkinnon-osa'}>
        <span className="name">{tutkinnonOsa.nimi}</span>
        { arvosanat && !arviointi
          ?
            <div className="arviointi edit">
              <ul className="arvosanat">{
                arvosanat.map((arvosana) => <li className= { arvosana == this.state.valittuArvosana ? 'selected' : '' } key={arvosana.id} onClick={() => this.setState({ valittuArvosana: arvosana })}>{arvosana.nimi}</li>)
              }</ul>
              <button className="button blue" disabled={!this.state.valittuArvosana} onClick={() => saveArvosana(this.state.valittuArvosana)}>Tallenna arvio</button>
            </div>
          : (
            arviointi
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