import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'

export const opintoOikeusChange = Bacon.Bus()

const changeOpintoOikeus = (opintoOikeus, change) => opintoOikeusChange.push(R.merge(opintoOikeus, change))

export const OpintoOikeus = React.createClass({
  render() {
    let {opintoOikeus} = this.props
    return (
      <div className="opintooikeus">
        <h4>Opinto-oikeudet</h4>
        <span className="tutkinto">{opintoOikeus.tutkinto.nimi}</span> <span className="oppilaitos">{opintoOikeus.oppilaitosOrganisaatio.nimi}</span>
        { opintoOikeus.rakenne
          ?
            <div className="tutkinto-rakenne">
                <label>Suoritustapa
                    <select className="suoritustapa" value={opintoOikeus.suoritustapa} onChange={(event) => changeOpintoOikeus(opintoOikeus, {'suoritustapa': event.target.value || undefined })}>
                        {withEmptyValue(opintoOikeus.rakenne.suoritustavat).map(s => <option key={s.koodi} value={s.koodi}>{s.nimi}</option>)}
                    </select>
                </label>
                <label>Osaamisala
                    <select className="osaamisala" value={opintoOikeus.osaamisala} onChange={(event) => changeOpintoOikeus(opintoOikeus, {'osaamisala': event.target.value || undefined })}>
                        {withEmptyValue(opintoOikeus.rakenne.osaamisalat).map(o => <option key={o.koodi} value={o.koodi}>{o.nimi}</option>)}
                    </select>
                </label>
              { opintoOikeus.suoritustapa
                ? <Rakenneosa rakenneosa={opintoOikeus.rakenne.suoritustavat.find(x => x.koodi == opintoOikeus.suoritustapa).rakenne} osaamisala={opintoOikeus.osaamisala}/>
                : null
              }
            </div>
          : null
        }
      </div>
    )
  }
})

const withEmptyValue = (xs) => [{ koodi: '', nimi: 'Valitse...'}].concat(xs)

const Rakenneosa = React.createClass({
  render() {
    let { rakenneosa, osaamisala } = this.props
    return rakenneosa.osat
      ? <div className="rakenne-moduuli">
          <span className="name">{rakenneosa.nimi}</span>
          <ul className="osat">
            { rakenneosa.osat
                .filter(osa => { return !osa.osaamisalaKoodi || osa.osaamisalaKoodi == osaamisala})
                .map((osa, i) => <li key={i}><Rakenneosa rakenneosa={osa} osaamisala={osaamisala} /></li>)
            }
          </ul>
        </div>
      : <div className="tutkinnon-osa">
          <span className="name">{rakenneosa.nimi}</span>
        </div>
  }
})