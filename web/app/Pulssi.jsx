import './polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import Http from './http'
import './style/pulssi.less'
import Text from './Text.jsx'
import R from 'ramda'

const Pulssi = React.createClass({
  render() {
    let {pulssi} = this.state
    let opiskeluoikeudet = pulssi.opiskeluoikeudet
    let suoritettujenKoulutustenMäärä = opiskeluoikeudet.koulutusmuotoTilastot.reduce((acc, koulutusmuoto) =>
        acc + koulutusmuoto.valmistuneidenMäärä, 0
    )

    let schoolsTotal = R.values(pulssi.oppilaitosMäärät.koulutusmuodoittain).reduce((acc, k) => acc + k, 0)
    let schoolsWhoHaveTransferredData = opiskeluoikeudet.koulutusmuotoTilastot.reduce((acc, koulutusmuoto) => {
      return acc + koulutusmuoto.siirtäneitäOppilaitoksia
    }, 0)

    return (
        <div className="statistics column">
          <h1><span><Text name="Koski"/></span><img src="images/pulssi.png"/><span><Text name="Pulssi"/></span><img className="logo" src="images/oph_fin_vaaka.png" /></h1>
          <div className="top-row three-columns">
            <section className="primary-metric opiskeluoikeudet-total opiskeluoikeudet-panel">
              <h3><Text name="Opiskeluoikeuksien määrä"/></h3>
              <div className="metric-large">{opiskeluoikeudet.opiskeluoikeuksienMäärä}</div>
            </section>
            <section className="primary-metric kattavuus-total kattavuus-panel">
              <h3><Text name="Kattavuus"/></h3>
              <div className="metric-large">{toPercent(schoolsWhoHaveTransferredData / schoolsTotal)}{' %'}</div>
              {schoolsWhoHaveTransferredData}{' / '}{schoolsTotal}
            </section>
            <section className="primary-metric valmiit-tutkinnot-total valmiit-tutkinnot-panel">
              <h3><Text name="Suoritettujen koulutusten määrä"/></h3>
              <div className="metric-large">{suoritettujenKoulutustenMäärä}</div>
            </section>
          </div>
          <div className="three-columns">
            <div className="lower-left-container column">
              <div className="two-columns">
                <section className="opiskeluoikeudet-koulutusmuodoittain opiskeluoikeudet-panel">
                  <ul className="metric-details">
                    {
                      opiskeluoikeudet.koulutusmuotoTilastot && opiskeluoikeudet.koulutusmuotoTilastot.map((stat, i) =>
                          <li key={i}>
                            <span>{stat.koulutusmuoto}</span><span className="metric-value">{stat.opiskeluoikeuksienMäärä}</span>
                          </li>
                      )
                    }
                  </ul>
                </section>
                <section className="kattavuus-koulutusmuodoittain kattavuus-panel">
                  <ul>
                    <li>
                      <Kattavuus koulutusmuoto="Perusopetus" pulssi={pulssi} />
                    </li>
                    <li>
                      <Kattavuus koulutusmuoto="Ammatillinen koulutus" pulssi={pulssi} />
                    </li>
                    <li>
                      <Kattavuus koulutusmuoto="Lukiokoulutus" pulssi={pulssi} />
                    </li>
                  </ul>
                </section>
              </div>
              <div className="two-columns">
                <section className="metric saavutettavuus">
                  <h3><Text name="Saavutettavuus"/></h3>
                  <div className="metric-medium">{pulssi.metriikka.saavutettavuus}{'%'}</div>
                  <div className="description"><Text name="saatavilla viimeisen 30 päivän aikana"/></div>
                </section>
                <section className="metric operaatiot">
                  <h3><Text name="Operaatiot / kk"/></h3>
                  <div className="metric-medium">{R.values(pulssi.metriikka.operaatiot).reduce((acc, määrä) => acc + määrä, 0)}</div>
                  <ul className="metric-details">
                    {
                      R.toPairs(pulssi.metriikka.operaatiot).sort((x , y) => y[1] - x[1]).map((op, i) => {
                        return (
                            <li key={i}>
                              <span>{op[0]}</span><span className="metric-value">{op[1]}</span>
                            </li>
                        )
                      })
                    }
                  </ul>
                </section>
              </div>
            </div>
            <section className="valmiit-tutkinnot-koulutusmuodoittain valmiit-tutkinnot-panel">
              <ul>
                {
                  opiskeluoikeudet.koulutusmuotoTilastot && opiskeluoikeudet.koulutusmuotoTilastot.map((tilasto,i) =>
                      <KoulutusmuotoTilasto key={i} tilasto={tilasto} />
                  )
                }
              </ul>
            </section>
          </div>
        </div>
    )
  },
  componentDidMount() {
    Http.cachedGet('/koski/api/pulssi').onValue(pulssi => this.setState({pulssi}))
    document.title = 'Koski - Pulssi'

  },
  getInitialState() {
    return {
      pulssi: {
        opiskeluoikeudet: {
          koulutusmuotoTilastot: []
        },
        metriikka: {
          saavutettavuus: 0,
          operaatiot: {}
        },
        oppilaitosMäärät: {
          koulutusmuodoittain: []
        }
      }
    }
  }
})

const toPercent = x => Math.round(x * 100 * 10) / 10

const Kattavuus = ({koulutusmuoto, pulssi}) => {
  let kmuoto = pulssi.opiskeluoikeudet.koulutusmuotoTilastot.find(o => o.koulutusmuoto === koulutusmuoto)
  let count =  kmuoto && kmuoto.siirtäneitäOppilaitoksia
  let total = pulssi.oppilaitosMäärät.koulutusmuodoittain[koulutusmuoto]
  let percentage = count && total && toPercent(count / total)

  return (
      <div>
        <span>{koulutusmuoto}</span>
        <span className="metric-value">{`${percentage} %  (${count} / ${total})`}</span>
        <div className="progress-bar">
          <div style={{width: percentage + '%'}} />
        </div>
      </div>
  )
}

const KoulutusmuotoTilasto = ({tilasto}) => {
  let valmiitPercent = toPercent(tilasto.valmistuneidenMäärä / tilasto.opiskeluoikeuksienMäärä)
  return (
      <li>
        <h4>{tilasto.nimi}</h4>
        <div className="progress-bar">
          <div style={{width: valmiitPercent + '%'}} />
        </div>
        <div className="metric-tiny">
          <span>{tilasto.valmistuneidenMäärä}</span>
        </div>

      </li>
  )
}

ReactDOM.render(
    (<div>
      <Pulssi/>
    </div>),
    document.getElementById('content')
)

