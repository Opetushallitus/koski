import './polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import Http from './http'
import './style/pulssi.less'

const Pulssi = React.createClass({
  render() {
    let {stats, opiskeluoikeusTyypit} = this.state
    let opiskeluoikeudet = stats.opiskeluoikeudet
    let valmiidenTutkintojenMäärä = opiskeluoikeudet.määrätKoulutusmuodoittain.reduce((acc, koulutusmuoto) =>
        acc + koulutusmuoto.määrätTiloittain.find(tila => tila.nimi === 'valmistunut').opiskeluoikeuksienMäärä, 0
    )

    return (
        <div className="statistics column">
          <h1><span>Koski</span><img src="images/pulssi.png"/><span>Pulssi</span><img className="logo" src="images/oph_fin_vaaka.png" /></h1>
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
                  <ul className="metric-details">
                    {
                      opiskeluoikeudet.määrätKoulutusmuodoittain && opiskeluoikeudet.määrätKoulutusmuodoittain.map((stat, i) =>
                          <li key={i}>
                            <span>{opiskeluoikeusTyypit[stat.nimi]}</span><span className="metric-value">{stat.opiskeluoikeuksienMäärä}</span>
                          </li>
                      )
                    }
                  </ul>
                </section>
                <section className="kattavuus">
                  <ul>
                    <li>
                      <Kattavuus title="Perusopetus" opiskeluoikeudet={opiskeluoikeudet} koulutusmuoto="perusopetus"/>
                    </li>
                    <li>
                      <Kattavuus title="Ammatillinen koulutus" opiskeluoikeudet={opiskeluoikeudet} koulutusmuoto="ammatillinenkoulutus"/>
                    </li>
                    <li>
                      <Kattavuus title="Lukiokoulutus" opiskeluoikeudet={opiskeluoikeudet} koulutusmuoto="lukiokoulutus"/>
                    </li>
                  </ul>
                </section>
              </div>
              <div className="two-columns expanding">
                <section className="metric saavutettavuus">
                  <h3>Saavutettavuus</h3>
                  <div className="metric-medium">99.999%</div>
                  <div className="description">Kuinka suuren osan ajasta palvelu on ollut saatavilla</div>
                </section>
                <section className="metric operaatiot">
                  <h3>Operaatiot / kk</h3>
                  <div className="metric-medium">{stats.operaatiot.reduce((acc, op) => acc + op.määrä, 0)}</div>
                  <ul className="metric-details">
                    {
                      stats.operaatiot.sort((x , y) => y.määrä - x.määrä).map((op, i) => {
                        return (
                            <li key={i}>
                              <span>{op.nimi}</span><span className="metric-value">{op.määrä}</span>
                            </li>
                        )
                      })
                    }
                  </ul>
                </section>
              </div>
            </div>
            <section className="valmiit-tutkinnot">
              <ul>
                {
                  opiskeluoikeudet.määrätKoulutusmuodoittain && opiskeluoikeudet.määrätKoulutusmuodoittain.map((koulutusmuoto,i) =>
                      <KoulutusmuotoTilasto key={i} koulutusmuoto={koulutusmuoto} opiskeluoikeusTyypit={opiskeluoikeusTyypit}/>
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
    Http.cachedGet('/koski/api/koodisto/opiskeluoikeudentyyppi/latest')
        .map(tyypit => {
          return tyypit.reduce((acc, t) => {
            acc[t.koodiArvo] = t.metadata.find(m => m.kieli === 'FI').nimi
            return acc
          }, {})
        })
        .onValue(opiskeluoikeusTyypit => this.setState({opiskeluoikeusTyypit}))
  },
  getInitialState() {
    return {
      stats: {
        opiskeluoikeudet: {
          määrätKoulutusmuodoittain: []
        },
        operaatiot: []
      },
      opiskeluoikeusTyypit: {}
    }
  }
})

const toPercent = x => Math.round(x * 100 * 10) / 10

const Kattavuus = ({title, opiskeluoikeudet, koulutusmuoto}) => {

  let kmuoto = opiskeluoikeudet.määrätKoulutusmuodoittain.find(o => o.nimi === koulutusmuoto)
  let count =  kmuoto && kmuoto.siirtäneitäOppilaitoksia
  let total = 1000 // hardcoded for now
  let percentage = count && toPercent(count / total)
  return (
      <div>
        <span>{title}</span>
        <span className="metric-value">{percentage} %  ({count} / {total})</span>
        <div className="progress-bar">
          <div style={{width: percentage + '%'}} />
        </div>
      </div>
  )
}

const KoulutusmuotoTilasto = ({koulutusmuoto, opiskeluoikeusTyypit}) => {
  let opiskeluoikeusMääräValmiit = koulutusmuoto.määrätTiloittain.find(tila => tila.nimi === 'valmistunut').opiskeluoikeuksienMäärä
  let opiskeluoikeusMääräKaikki = koulutusmuoto.määrätTiloittain.reduce((acc, n) => acc + n.opiskeluoikeuksienMäärä, 0)
  let opiskeluoikeusMääräEiValmiit = opiskeluoikeusMääräKaikki - opiskeluoikeusMääräValmiit
  let valmiitPercent = toPercent(opiskeluoikeusMääräValmiit / opiskeluoikeusMääräKaikki)
  return (
      <li>
        <h4>{opiskeluoikeusTyypit[koulutusmuoto.nimi]}</h4>
        <div className="progress-bar">
          <div style={{width: valmiitPercent + '%'}} />
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

