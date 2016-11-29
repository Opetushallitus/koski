import React from 'react'
import Bacon from 'baconjs'
import Http from './http'

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
            hakutulokset.map( (opiskeluoikeus) => <tr key={opiskeluoikeus.henkilö.oid}>
              <td className="nimi">{ opiskeluoikeus.henkilö.sukunimi + ', ' + opiskeluoikeus.henkilö.etunimet}</td>
              <td className="tyyppi">{ opiskeluoikeus.tyyppi.nimi.fi }</td>
              <td className="koulutus">{ opiskeluoikeus.suoritukset.map((suoritus, i) => <span key={i}>{suoritus.tyyppi.nimi.fi}</span>) } </td>
              <td className="tutkinto">{ opiskeluoikeus.suoritukset.map((suoritus, i) =>
                <span key={i}>
                  {
                    <span className="koulutusmoduuli">{suoritus.koulutusmoduuli.tunniste.nimi.fi}</span>
                  }
                  {
                    suoritus.osaamiala ? <span className="osaamisala">{suoritus.osaamisala.fi}</span> : null
                  }
                  {
                    suoritus.tutkintonimike ? <span className="tutkintonimike">{suoritus.tutkintonimike.fi}</span> : null
                  }
                </span>
              )}
              </td>
              <td className="tila">{ opiskeluoikeus.tila.nimi.fi }</td>
              <td className="oppilaitos">{ opiskeluoikeus.oppilaitos.nimi.fi }</td>
              <td className="aloitus pvm">{ opiskeluoikeus.alkamispäivä }</td>
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