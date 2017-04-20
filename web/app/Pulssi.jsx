import './polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import Http from './http'
import './style/pulssi.less'

const Pulssi = React.createClass({
  render() {
    let {stats} = this.state
    let opiskeluoikeudet = stats.opiskeluoikeudet
    let valmiidenTutkintojenMäärä =  opiskeluoikeudet.määrätKoulutusmuodoittain.reduce((acc, koulutusmuoto) =>
      acc + koulutusmuoto.määrätTiloittain.find(tila => tila.nimi === 'valmistunut').opiskeluoikeuksienMäärä, 0
    )

    return (
      <div className="statistics column">
        <div className="top-row three-columns">
          <section className="primary-metric opiskeluoikeudet-total">
            <h3>Opiskeluoikeuksien määrä</h3>
            <div className="metric-large">{opiskeluoikeudet.opiskeluoikeuksienMäärä}</div>
          </section>
          <section className="primary-metric kattavuus">
            <h3>Kattavuus</h3>
            <div className="metric-large">N/A</div>
            1102 / 8582
          </section>
          <section className="primary-metric valmiit-tutkinnot">
            <h3>Valmiiden tutkintojen määrä</h3>
            <div className="metric-large">{valmiidenTutkintojenMäärä}</div>
            {toPercent(valmiidenTutkintojenMäärä / opiskeluoikeudet.opiskeluoikeuksienMäärä)} %
          </section>
        </div>
        <div className="expanding three-columns">
          <div className="lower-left-container column">
            <div className="two-columns">
              <section className="opiskeluoikeudet-total">
                <ul>
                  {
                    opiskeluoikeudet.määrätKoulutusmuodoittain && opiskeluoikeudet.määrätKoulutusmuodoittain.map(stat =>
                      <li>
                        <span>{stat.nimi}</span><span className="metric-value">{stat.opiskeluoikeuksienMäärä}</span>
                      </li>
                    )
                  }
                </ul>
              </section>
              <section className="kattavuus">kattavuus</section>
            </div>
            <div className="extra-metrics expanding column">
              <div className="two-columns">
                <section>a</section>
                <section>b</section>
              </div>
              <div className="two-columns expanding">
                <section>c</section>
                <section>d</section>
              </div>
            </div>
          </div>
          <section className="valmiit-tutkinnot">
            <ul>
              {
                opiskeluoikeudet.määrätKoulutusmuodoittain && opiskeluoikeudet.määrätKoulutusmuodoittain.map(koulutusmuoto =>
                  <KoulutusmuotoTilasto koulutusmuoto={koulutusmuoto} />
                )
              }
            </ul>
          </section>
        </div>
      </div>
    )
  },
  componentDidMount() {
    Http.cachedGet('/koski/api/pulssi').onValue(stats => this.setState({stats}))
  },
  getInitialState() {
    return {stats: { opiskeluoikeudet: { määrätKoulutusmuodoittain: [] } } }
  }
})

const toPercent = x => Math.round(x * 100 * 10) / 10

const KoulutusmuotoTilasto = ({koulutusmuoto}) => {
  let opiskeluoikeusMääräValmiit = koulutusmuoto.määrätTiloittain.find(tila => tila.nimi === 'valmistunut').opiskeluoikeuksienMäärä
  let opiskeluoikeusMääräKaikki = koulutusmuoto.määrätTiloittain.reduce((acc, n) => acc + n.opiskeluoikeuksienMäärä, 0)
  let opiskeluoikeusMääräEiValmiit = opiskeluoikeusMääräKaikki - opiskeluoikeusMääräValmiit
  let valmiitPercent = toPercent(opiskeluoikeusMääräValmiit / opiskeluoikeusMääräKaikki)
  return (
    <li>
      <h3>{koulutusmuoto.nimi}</h3>
      <div className="progress-bar">
        <div style={{width: valmiitPercent + '%'}}></div>
      </div>
      <div className="metric-tiny">
        <span>{opiskeluoikeusMääräValmiit} ({valmiitPercent} %)</span>
        <span className="metric-value">{opiskeluoikeusMääräEiValmiit} ({100 - valmiitPercent} %)</span>
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

