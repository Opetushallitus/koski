import React from 'react'

export const OpintoOikeus = React.createClass({
  render() {
    let {opintoOikeus} = this.props
    return (
      <div className="opintooikeus">
        <h4>Opinto-oikeudet</h4>
        <span className="tutkinto">{opintoOikeus.nimi}</span> <span className="oppilaitos">{opintoOikeus.oppilaitos.nimi}</span>
        { opintoOikeus.rakenne
          ?  <RakenneOsa rakenneOsa={opintoOikeus.rakenne}/>
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
            { rakenneOsa.osat.map(osa => <li><RakenneOsa rakenneOsa={osa} /></li>) }
          </ul>
        </div>
      : <div className="tutkinnon-osa">
          <span className="name">{rakenneOsa.nimi}</span>
        </div>
  }
})