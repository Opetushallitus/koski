import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import { navigateToOppija } from './location'
import { ISO2FinnishDate } from './date'

export const Oppijataulukko = React.createClass({
  render() {
    let { hakutulokset } = this.state || { }
    return (<div className="oppijataulukko">{ hakutulokset ? (
      <table>
        <thead>
          <tr>
            <th className="nimi">Nimi</th>
            <th className="tyyppi">Opiskeluoikeuden tyyppi</th>
            <th className="koulutus">Koulutus</th>
            <th className="tutkinto">Tutkinto / osaamisala / nimike</th>
            <th className="tila">Tila</th>
            <th className="oppilaitos">Oppilaitos</th>
            <th className="aloitus">Aloitus pvm</th>
            <th className="luokka">Luokka / ryhmä</th>
          </tr>
        </thead>
        <tbody>
          {
            hakutulokset.map( (opiskeluoikeus, i) => <tr key={i}>
              <td className="nimi"><a href={`/koski/oppija/${opiskeluoikeus.henkilö.oid}`} onClick={(e) => navigateToOppija(opiskeluoikeus.henkilö, e)}>{ opiskeluoikeus.henkilö.sukunimi + ', ' + opiskeluoikeus.henkilö.etunimet}</a></td>
              <td className="tyyppi">{ opiskeluoikeus.tyyppi.nimi.fi }</td>
              <td className="koulutus">{ opiskeluoikeus.suoritukset.map((suoritus, j) => <span key={j}>{suoritus.tyyppi.nimi.fi}</span>) } </td>
              <td className="tutkinto">{ opiskeluoikeus.suoritukset.map((suoritus, j) =>
                <span key={j}>
                  {
                    <span className="koulutusmoduuli">{suoritus.koulutusmoduuli.tunniste.nimi.fi}</span>
                  }
                  {
                    (suoritus.osaamisala || []).map((osaamisala, k) => <span className="osaamisala" key={k}>{osaamisala.nimi.fi}</span>)
                  }
                  {
                    (suoritus.tutkintonimike || []).map((nimike, k) => <span className="tutkintonimike" key={k}>{nimike.nimi.fi}</span>)
                  }
                </span>
              )}
              </td>
              <td className="tila">{ opiskeluoikeus.tila.nimi.fi }</td>
              <td className="oppilaitos">{ opiskeluoikeus.oppilaitos.nimi.fi }</td>
              <td className="aloitus pvm">{ ISO2FinnishDate(opiskeluoikeus.alkamispäivä) }</td>
              <td className="luokka">{ opiskeluoikeus.luokka }</td>
            </tr>)
          }
          </tbody>
        </table>) : <div className="ajax-indicator-bg">Ladataan...</div> }</div>)
  },
  componentDidMount() {
    this.hakuehtoP = Bacon.constant('')
    this.perustiedotP = this.hakuehtoP.flatMap((hakuehto) => Http.get('/koski/api/opiskeluoikeus/perustiedot')).toProperty()
    this.perustiedotP.onValue((hakutulokset) => this.setState({hakutulokset}))
  }
})