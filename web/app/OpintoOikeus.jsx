import React from 'react'

export const OpintoOikeus = React.createClass({
  render() {
    let {opintooikeus} = this.props
    return (
      <div className="opintooikeus">
        <h4>Opinto-oikeudet</h4>
        <span className="tutkinto">{opintooikeus.nimi}</span> <span className="oppilaitos">{opintooikeus.oppilaitos.nimi}</span>
      </div>
    )
  }
})