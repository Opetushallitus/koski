import './polyfills/polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import Http from './util/http'
import './style/pulssi.less'
import Text from './i18n/Text'
import R from 'ramda'

class Pulssi extends React.Component {

  constructor(props) {
    super(props)
    this.state = {
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

  render() {
    let {pulssi} = this.state
    let opiskeluoikeudet = pulssi.opiskeluoikeudet
    let suoritettujenKoulutustenMäärä = opiskeluoikeudet.koulutusmuotoTilastot.reduce((acc, koulutusmuoto) =>
        acc + koulutusmuoto.valmistuneidenMäärä, 0
    )

    let schoolsTotal = R.values(pulssi.oppilaitosMäärät.koulutusmuodoittain).reduce((acc, k) => acc + k, 0)

    return (
        <div className="column">
          <h1><span><Text name="Koski"/></span><img src="images/pulssi.png"/><span><Text name="Pulssi"/></span><img className="logo" src="images/oph_fin_vaaka.png" /></h1>
          <div className="statistics-wrapper">
            <div className="column">
              <section className="opiskeluoikeudet-panel">
                <div className="primary-metric opiskeluoikeudet-total">
                  <h3><Text name="Opiskeluoikeuksien määrä"/></h3>
                  <div className="metric-large">{opiskeluoikeudet.opiskeluoikeuksienMäärä}</div>
                </div>
                <div className="metric-details opiskeluoikeudet-koulutusmuodoittain">
                  <ul>
                    {
                      opiskeluoikeudet.koulutusmuotoTilastot && opiskeluoikeudet.koulutusmuotoTilastot.map((stat, i) =>
                          (<li key={i}>
                            <span>{stat.koulutusmuoto}</span><span className="metric-value">{stat.opiskeluoikeuksienMäärä}</span>
                          </li>)
                      )
                    }
                  </ul>
                </div>
              </section>
              <section className="metric saavutettavuus">
                <h3><Text name="Saavutettavuus"/></h3>
                <div className="metric-medium">{pulssi.metriikka.saavutettavuus}{'%'}</div>
                <span className="description">
                  <Text name="saatavilla viimeisen 30 päivän aikana"/>
                </span>
              </section>
            </div>
            <div className="column">
              <section className="kattavuus-panel">
                <div className="primary-metric kattavuus-total">
                  <h3><Text name="Kattavuus"/></h3>
                  <div className="metric-large">{toPercent(opiskeluoikeudet.siirtäneitäOppilaitoksiaYhteensä / schoolsTotal)}{' %'}</div>
                  {opiskeluoikeudet.siirtäneitäOppilaitoksiaYhteensä}{' / '}{schoolsTotal}
                </div>
                <div className="metric-details kattavuus-koulutusmuodoittain">
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
                </div>
              </section>
              <section className="metric operaatiot">
                <h3><Text name="Operaatiot / kk"/></h3>
                <div className="metric-medium">{R.values(pulssi.metriikka.operaatiot).reduce((acc, määrä) => acc + määrä, 0)}</div>
                <div className="operaatiot-details">
                  <ul>
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
                </div>
              </section>
            </div>
            <div className="column">
              <section className="valmiit-tutkinnot-panel">
                <div className="primary-metric valmiit-tutkinnot-total">
                  <h3><Text name="Suoritettujen koulutusten määrä"/></h3>
                  <div className="metric-large">{suoritettujenKoulutustenMäärä}</div>
                </div>
                <div className="metric-details valmiit-tutkinnot-koulutusmuodoittain">
                  <ul>
                    {
                      opiskeluoikeudet.koulutusmuotoTilastot && opiskeluoikeudet.koulutusmuotoTilastot.map((tilasto,i) =>
                        <KoulutusmuotoTilasto key={i} tilasto={tilasto} />
                      )
                    }
                  </ul>
                </div>
              </section>
            </div>
          </div>
        </div>
    )
  }

  componentDidMount() {
    Http.cachedGet('/koski/api/pulssi').onValue(pulssi => this.setState({pulssi}))
    document.title = 'Koski - Pulssi'

  }
}

const toPercent = x => Math.min(100, Math.round(x * 100 * 10) / 10)

const Kattavuus = ({koulutusmuoto, pulssi}) => {
  let kmuoto = pulssi.opiskeluoikeudet.koulutusmuotoTilastot.find(o => o.koulutusmuoto === koulutusmuoto)
  let count =  (kmuoto && kmuoto.siirtäneitäOppilaitoksia) || 0
  let total = pulssi.oppilaitosMäärät.koulutusmuodoittain[koulutusmuoto]
  let percentage = (count && total && toPercent(count / total)) || 0

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
        <span>{tilasto.koulutusmuoto}</span>
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

