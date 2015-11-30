import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'
import Http from './http'
import Dropdown from './Dropdown.jsx'

export const opiskeluOikeusChange = Bacon.Bus()

export const OpiskeluOikeus = React.createClass({
  render() {
    let {opiskeluOikeus} = this.props
    let {rakenne} = this.state
    return (
        rakenne
          ? <div className="opiskeluoikeus">
              <h4>Opinto-oikeudet</h4>
              <span className="tutkinto">{opiskeluOikeus.suoritus.koulutusmoduulitoteutus.koulutusmoduuli.tunniste.nimi}</span> <span className="oppilaitos">{opiskeluOikeus.oppilaitos.nimi}</span>
              <div className="tutkinto-rakenne">
                <Dropdown className="suoritustapa"
                          title="Suoritustapa"
                          options={rakenne.suoritustavat.map(s => s.suoritustapa)}
                          value={opiskeluOikeus.suoritus.koulutusmoduulitoteutus.suoritustapa ? opiskeluOikeus.suoritus.koulutusmoduulitoteutus.suoritustapa.tunniste.koodiarvo : ''}
                          onChange={(value) => opiskeluOikeusChange.push([opiskeluOikeus.id,
                            oo => {
                              oo.suoritus.koulutusmoduulitoteutus.suoritustapa = value ? {
                                tunniste: {
                                  koodiarvo: value,
                                  koodistoUri: 'suoritustapa'
                                }
                              } : undefined
                              return oo
                            }
                          ])}
                />
                <Dropdown className="osaamisala"
                          title="Osaamisala"
                          options={rakenne.osaamisalat}
                          value={opiskeluOikeus.suoritus.koulutusmoduulitoteutus.osaamisala ? opiskeluOikeus.suoritus.koulutusmoduulitoteutus.osaamisala[0].koodiarvo : ''}
                          onChange={(value) => opiskeluOikeusChange.push([opiskeluOikeus.id,
                            oo => {
                              oo.suoritus.koulutusmoduulitoteutus.osaamisala = value ? [{
                                  koodiarvo: value,
                                  koodistoUri: 'osaamisala'
                              }] : undefined
                              return oo
                            }
                          ])}
                  />
                { opiskeluOikeus.suoritus.koulutusmoduulitoteutus.suoritustapa
                  ? rakenne.suoritustavat.find(x => x.suoritustapa.koodiarvo == opiskeluOikeus.suoritus.koulutusmoduulitoteutus.suoritustapa.tunniste.koodiarvo).rakenne.osat.map(rakenneOsa => <Rakenneosa
                      rakenneosa={rakenneOsa}
                      opiskeluOikeus={opiskeluOikeus}
                      rakenne={rakenne}
                    />)
                  : null
                }
              </div>
            </div>
          : null
    )
  },
  componentDidMount() {
    let {opiskeluOikeus} = this.props
    let diaarinumero = opiskeluOikeus.suoritus.koulutusmoduulitoteutus.koulutusmoduuli.perusteenDiaarinumero
    Http.get('/tor/api/tutkinto/rakenne/' + encodeURIComponent(diaarinumero)).log().onValue(rakenne =>
      this.setState({rakenne: rakenne})
    )
  },
  getInitialState() {
    return {}
  }
})

const Rakenneosa = React.createClass({
  render() {
    let { rakenneosa, opiskeluOikeus, rakenne } = this.props
    return rakenneosa.osat
      ? <RakenneModuuli key={rakenneosa.nimi} opiskeluOikeus={opiskeluOikeus} rakenneosa={rakenneosa} rakenne={rakenne}/>
      : <TutkinnonOsa key={rakenneosa.nimi} opiskeluOikeus={opiskeluOikeus} tutkinnonOsa={rakenneosa} rakenne={rakenne}/>
  }
})

const RakenneModuuli = React.createClass({
  render() {
    let { rakenneosa, opiskeluOikeus, rakenne } = this.props
    return (
      <div className="rakenne-moduuli">
        <span className="name">{rakenneosa.nimi}</span>
        <ul className="osat">
          { rakenneosa.osat
            .filter(osa => {
              let osaamisala = opiskeluOikeus.suoritus.koulutusmoduulitoteutus.osaamisala ? opiskeluOikeus.suoritus.koulutusmoduulitoteutus.osaamisala[0].koodiarvo : undefined
              return !osa.osaamisalaKoodi || osa.osaamisalaKoodi == osaamisala
            })
            .map((osa, i) => <li key={i}>
              <Rakenneosa
                rakenneosa={osa}
                opiskeluOikeus={opiskeluOikeus}
                rakenne={rakenne}
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
    const {tutkinnonOsa, opiskeluOikeus, rakenne} = this.props
    const arviointiAsteikko = R.find(asteikko => R.equals(asteikko.koodisto, tutkinnonOsa.arviointiAsteikko))(rakenne.arviointiAsteikot)
    const arvosanat = arviointiAsteikko ? arviointiAsteikko.arvosanat : undefined

    const addArvosana = (arvosana) => (oOikeus) => {
      let suoritukset = (oOikeus.suoritus.osasuoritukset ? oOikeus.suoritus.osasuoritukset : []).concat(
        {
          koulutusmoduulitoteutus: {
            koulutusmoduuli: {
              tunniste: tutkinnonOsa.tunniste,
              // TODO, hardcoded
              pakollinen: true,
              // TODO, hardcoded
              laajuus: {
                "arvo" : 11.0,
                "yksikkö" : {
                  "koodiarvo" : "6",
                  "koodistoUri" : "opintojenlaajuusyksikko",
                }
              }
            }
          },
          arviointi: [
            {
              arvosana: {
                koodiarvo: arvosana.koodiarvo,
                koodistoUri: tutkinnonOsa.arviointiAsteikko.koodistoUri
              }
            }
          ],
          // TODO, is this really required?
          toimipiste: oOikeus.suoritus.toimipiste
        }
      )
      oOikeus.suoritus.osasuoritukset = suoritukset
      return oOikeus
    }

    const saveArvosana = (arvosana) => {
      opiskeluOikeusChange.push([opiskeluOikeus.id, addArvosana(arvosana)])
    }

    const suoritus = R.find(osanSuoritus => R.equals(osanSuoritus.koulutusmoduulitoteutus.koulutusmoduuli.tunniste, tutkinnonOsa.tunniste))(opiskeluOikeus.suoritus.osasuoritukset ? opiskeluOikeus.suoritus.osasuoritukset : [])
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
              ? <div className="arviointi"><span className="arvosana">{arvosanat.find(arvosana => arvosana.koodiarvo == suoritus.arviointi[arviointi.length - 1].arvosana.koodiarvo).nimi}</span></div>
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