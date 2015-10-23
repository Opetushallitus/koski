import React from 'react'

export const OpintoOikeus = React.createClass({
  render() {
    let {opintoOikeus} = this.props
    return (
      <div className="opintooikeus">
        <h4>Opinto-oikeudet</h4>
        <span className="tutkinto">{opintoOikeus.nimi}</span> <span className="oppilaitos">{opintoOikeus.oppilaitos.nimi}</span>
        { opintoOikeus.rakenne
          ?
            <div>
              <select className="suoritustapa">
                {opintoOikeus.rakenne.suoritustavat.map(s => <option>{s.nimi}</option>)}
              </select>
              <select className="osaamisala">
                {opintoOikeus.rakenne.osaamisalat.map(o => <option>{o.nimi}</option>)}
              </select>
              <RakenneOsa rakenneOsa={opintoOikeus.rakenne.suoritustavat.find(x => x.koodi == 'naytto').rakenne}/>
            </div>
          : null
        }
      </div>
    )
  }
})

const RakenneOsa = React.createClass({
  render() {
    let { rakenneOsa } = this.props
    return rakenneOsa.osat
      ? <div className="rakenne-moduuli">
          <span className="name">{rakenneOsa.nimi}</span>
          <ul className="osat">
            { rakenneOsa.osat.map((osa, i) => <li key={i}><RakenneOsa rakenneOsa={osa} /></li>) }
          </ul>
        </div>
      : <div className="tutkinnon-osa">
          <span className="name">{rakenneOsa.nimi}</span>
        </div>
  }
})