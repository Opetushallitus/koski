import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'

export const opiskeluOikeusChange = Bacon.Bus()

// Shows <select> if more than 1 option. If 1 option, automatically selects it and shows it. If zero options, hides the whole thing.
const Dropdown = React.createClass({
  render() {
    let { title, options, value, onChange, className} = this.props
    let withEmptyValue = (xs) => [{ koodi: '', nimi: 'Valitse...'}].concat(xs)
    let optionElems = opts => withEmptyValue(opts).map(s => <option key={s.koodi} value={s.koodi}>{s.nimi}</option>)

    return options.length > 1
        ? <label>{title}
            <select
              className={className}
              value={value}
              onChange={(event) => onChange(event.target.value)}>
                {optionElems(options)}
            </select>
          </label>
        : <div></div>
  },
  componentDidMount() {
    let { options, onChange, value} = this.props
    if (options.length == 1 && value !== options[0].koodi) {
      onChange(options[0].koodi)
    }
  }
})

export const OpiskeluOikeus = React.createClass({
  render() {
    let {opiskeluOikeus} = this.props
    return (
      <div className="opiskeluoikeus">
        <h4>Opinto-oikeudet</h4>
        <span className="tutkinto">{opiskeluOikeus.tutkinto.nimi}</span> <span className="oppilaitos">{opiskeluOikeus.oppilaitosOrganisaatio.nimi}</span>
        { opiskeluOikeus.tutkinto.rakenne
          ?
            <div className="tutkinto-rakenne">
              <Dropdown className="suoritustapa"
                        title="Suoritustapa"
                        options={opiskeluOikeus.tutkinto.rakenne.suoritustavat.map(s => s.suoritustapa)}
                        value={opiskeluOikeus.suoritustapa}
                        onChange={(value) => opiskeluOikeusChange.push([opiskeluOikeus.id, oo => R.merge(oo, {suoritustapa: value || undefined})] )}
                />
              <Dropdown className="osaamisala"
                        title="Osaamisala"
                        options={opiskeluOikeus.tutkinto.rakenne.osaamisalat}
                        value={opiskeluOikeus.osaamisala}
                        onChange={(value) => opiskeluOikeusChange.push([opiskeluOikeus.id, oo => R.merge(oo, {osaamisala: value || undefined})] )}
                />
              { opiskeluOikeus.suoritustapa
                ? opiskeluOikeus.tutkinto.rakenne.suoritustavat.find(x => x.suoritustapa.koodi == opiskeluOikeus.suoritustapa).rakenne.osat.map(rakenneOsa => <Rakenneosa
                    rakenneosa={rakenneOsa}
                    opiskeluOikeus={opiskeluOikeus}
                  />)
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
    let { rakenneosa, opiskeluOikeus } = this.props
    return rakenneosa.osat
      ? <RakenneModuuli key={rakenneosa.nimi} opiskeluOikeus={opiskeluOikeus} rakenneosa={rakenneosa} />
      : <TutkinnonOsa key={rakenneosa.nimi} opiskeluOikeus={opiskeluOikeus} tutkinnonOsa={rakenneosa} />
  }
})

const RakenneModuuli = React.createClass({
  render() {
    const { rakenneosa, opiskeluOikeus } = this.props
    return (
      <div className="rakenne-moduuli">
        <span className="name">{rakenneosa.nimi}</span>
        <ul className="osat">
          { rakenneosa.osat
            .filter(osa => { return !osa.osaamisalaKoodi || osa.osaamisalaKoodi == opiskeluOikeus.osaamisala})
            .map((osa, i) => <li key={i}>
              <Rakenneosa
                rakenneosa={osa}
                opiskeluOikeus={opiskeluOikeus}
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
    const {tutkinnonOsa, opiskeluOikeus} = this.props
    const arviointiAsteikko = R.find(asteikko => R.equals(asteikko.koodisto, tutkinnonOsa.arviointiAsteikko))(opiskeluOikeus.tutkinto.rakenne.arviointiAsteikot)
    const arvosanat = arviointiAsteikko ? arviointiAsteikko.arvosanat : undefined

    const addArvosana = (arvosana) => (oOikeus) => {
      let suoritukset = oOikeus.suoritukset.concat({ koulutusModuuli: tutkinnonOsa.tunniste, arviointi: { asteikko: tutkinnonOsa.arviointiAsteikko, arvosana: arvosana }})
      return R.merge(oOikeus, { suoritukset })
    }

    const saveArvosana = (arvosana) => {
      opiskeluOikeusChange.push([opiskeluOikeus.id, addArvosana(arvosana)])
    }

    const suoritus = R.find(osanSuoritus => R.equals(osanSuoritus.koulutusModuuli, tutkinnonOsa.tunniste))(opiskeluOikeus.suoritukset)
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