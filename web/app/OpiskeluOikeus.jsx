import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'
import * as L from 'partial.lenses'
import Http from './http'
import Dropdown from './Dropdown.jsx'

export const opiskeluOikeusChange = Bacon.Bus()

export const OpiskeluOikeus = React.createClass({
  render() {
    let {opiskeluOikeus, lens} = this.props
    return (
      <div className="opiskeluoikeus">
        {
          opiskeluOikeus.suoritukset.map((suoritus, index) =>  {
            let suoritusLens = L.compose(lens, L.prop('suoritukset'), L.index(index))
            return (
              <div className="suoritus" key={index}>
                <span className="tutkinto">{suoritus.koulutusmoduuli.tunniste.nimi.fi}</span> <span className="oppilaitos">{opiskeluOikeus.oppilaitos.nimi.fi}</span>
                <Todistus suoritus={suoritus} opiskeluOikeus={opiskeluOikeus}/>
                <TutkinnonRakenne suoritus={suoritus} lens={suoritusLens} />
              </div>
            )
          })
        }
      </div>
    )
  }
})

const Todistus = React.createClass({
  render() {
    let {suoritus, opiskeluOikeus} = this.props
    let href = '/tor/todistus/opiskeluoikeus/' + opiskeluOikeus.id
    return suoritus.tila.koodiarvo == 'VALMIS'
      ? <a className="todistus" href={href}>näytä todistus</a>
      : null
  }
})

const TutkinnonRakenne = React.createClass({
  render() {
    let {suoritus, lens} = this.props
    let {rakenne} = this.state
    return (
      rakenne
        ?
          <div className="tutkinto-rakenne">
            <Dropdown className="suoritustapa"
                      title="Suoritustapa"
                      options={rakenne.suoritustavat.map(s => s.suoritustapa)}
                      value={suoritus.suoritustapa ? suoritus.suoritustapa.tunniste.koodiarvo : ''}
                      autoselect={true}
                      onChange={(value) => opiskeluOikeusChange.push([L.compose(lens, L.prop('suoritustapa')),
                              () => {
                                let suoritustapa = value ? {
                                  tunniste: {
                                    koodiarvo: value,
                                    koodistoUri: 'suoritustapa'
                                  }
                                } : undefined
                                return suoritustapa
                              }
                            ])}
              />
            <Dropdown className="osaamisala"
                      title="Osaamisala"
                      options={rakenne.osaamisalat}
                      value={suoritus.osaamisala ? suoritus.osaamisala[0].koodiarvo : ''}
                      onChange={(value) => opiskeluOikeusChange.push([L.compose(lens, L.prop('osaamisala')),
                              () => {
                                let osaamisala = value ? [{
                                    koodiarvo: value,
                                    koodistoUri: 'osaamisala'
                                }] : undefined
                                return osaamisala
                              }
                            ])}
              />
            { suoritus.suoritustapa
              ? rakenne.suoritustavat.find(x => x.suoritustapa.koodiarvo == suoritus.suoritustapa.tunniste.koodiarvo).rakenne.osat.map(rakenneOsa => <Rakenneosa
              rakenneosa={rakenneOsa}
              suoritus={suoritus}
              lens={lens}
              rakenne={rakenne}
              key={rakenneOsa.nimi.fi}
              />)
              : null
            }
          </div>
        : null
    )
  },
  componentDidMount() {
    let {suoritus} = this.props
    let diaarinumero = suoritus.koulutusmoduuli.perusteenDiaarinumero
    if (diaarinumero) {
      Http.get('/tor/api/tutkinto/rakenne/' + encodeURIComponent(diaarinumero)).onValue(rakenne => {
          if (this.isMounted()) {
            this.setState({rakenne: rakenne})
          }
        }
      )
    }
  },
  getInitialState() {
    return {}
  }
})


const Rakenneosa = React.createClass({
  render() {
    let { rakenneosa, suoritus, lens, rakenne } = this.props
    return rakenneosa.osat
      ? <RakenneModuuli key={rakenneosa.nimi.fi} suoritus={suoritus} lens={lens} rakenneosa={rakenneosa} rakenne={rakenne}/>
      : <TutkinnonOsa key={rakenneosa.nimi.fi} suoritus={suoritus} lens={lens} tutkinnonOsa={rakenneosa} rakenne={rakenne}/>
  }
})

const RakenneModuuli = React.createClass({
  render() {
    let { rakenneosa, suoritus, lens, rakenne } = this.props
    return (
      <div className="rakenne-moduuli">
        <span className="name">{rakenneosa.nimi.fi}</span>
        <ul className="osat">
          { rakenneosa.osat
            .filter(osa => {
              let osaamisala = suoritus.osaamisala ? suoritus.osaamisala[0].koodiarvo : undefined
              return !osa.osaamisalaKoodi || osa.osaamisalaKoodi == osaamisala
            })
            .map((osa, i) => <li key={i}>
              <Rakenneosa
                rakenneosa={osa}
                suoritus={suoritus}
                lens={lens}
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
    const {tutkinnonOsa, suoritus, lens, rakenne} = this.props
    const arviointiAsteikko = R.find(asteikko => R.equals(asteikko.koodisto, tutkinnonOsa.arviointiAsteikko))(rakenne.arviointiAsteikot)
    const arvosanat = arviointiAsteikko ? arviointiAsteikko.arvosanat : undefined
    const laajuudenYksikkö = R.find(tapa => {
      return tapa.suoritustapa.koodiarvo == suoritus.suoritustapa.tunniste.koodiarvo
    })(rakenne.suoritustavat).laajuusYksikkö

    const osaSuorituksetLens = L.compose(lens, L.prop('osasuoritukset'))

    const addArvosana = (arvosana) => (osasuoritukset) => {
      // Lisää aina uuden osasuorituksen, ts. ei tue arvosanan päivitystä
      return (osasuoritukset || []).concat([{
        koulutusmoduuli: {
          tunniste: tutkinnonOsa.tunniste,
          pakollinen: tutkinnonOsa.pakollinen,
          laajuus: tutkinnonOsa.laajuus ? {
            arvo : tutkinnonOsa.laajuus,
            yksikkö : laajuudenYksikkö
          } : undefined
        },
        tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN' },
        arviointi: [
          {
            arvosana: arvosana
          }
        ],
        toimipiste: suoritus.toimipiste,
        tyyppi: { 'koodistoUri': 'suorituksentyyppi', 'koodiarvo': 'ammatillisentutkinnonosa'}
      }])
    }

    const saveArvosana = (arvosana) => {
      opiskeluOikeusChange.push([osaSuorituksetLens, addArvosana(arvosana)])
    }

    const osasuoritus = R.find(osanSuoritus => R.equals(osanSuoritus.koulutusmoduuli.tunniste, tutkinnonOsa.tunniste))(suoritus.osasuoritukset ? suoritus.osasuoritukset : [])

    const arviointi = osasuoritus && osasuoritus.arviointi

    return (
      <div className={ arviointi ? 'tutkinnon-osa suoritettu' : 'tutkinnon-osa'}>
        <span className="name">{tutkinnonOsa.nimi.fi}</span>
        { arvosanat && !arviointi
          ?
            <div className="arviointi edit">
              <ul className="arvosanat">{
                arvosanat.map((arvosana) => <li className= { arvosana == this.state.valittuArvosana ? 'selected' : '' } key={arvosana.koodiarvo} onClick={() => this.setState({ valittuArvosana: arvosana })}>{arvosana.lyhytNimi.fi}</li>)
              }</ul>
              <button className="button blue" disabled={!this.state.valittuArvosana} onClick={() => saveArvosana(this.state.valittuArvosana)}>Tallenna arvio</button>
            </div>
          : (
            arviointi
              ? <div className="arviointi"><span className="arvosana">{arviointi[arviointi.length - 1].arvosana.lyhytNimi.fi}</span></div>
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