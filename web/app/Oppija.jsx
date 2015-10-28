import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import {routeP} from './router'
import {CreateOppija} from './CreateOppija.jsx'
import {OpintoOikeus, opintoOikeusChange} from './OpintoOikeus.jsx'
import Ramda from 'ramda'

var selectOppijaE = routeP.map('.oppijaId').flatMap(oppijaId => {
  return oppijaId ? Bacon.once(undefined).concat(Http.get(`/tor/api/oppija/${oppijaId}`)) : Bacon.once(undefined)
})

export const oppijaP = Bacon.update(undefined,
  selectOppijaE, (previous, oppija) => oppija,
  opintoOikeusChange, (currentOppija, [opintoOikeusId, change]) => {
    let changedOppija = Ramda.clone(currentOppija)
    changedOppija.opintoOikeudet = changedOppija.opintoOikeudet.map(opintoOikeus =>
        opintoOikeus.id == opintoOikeusId
          ? change(opintoOikeus)
          : opintoOikeus
    )
    return changedOppija
  }
)

export const updateResultE = oppijaP.sampledBy(opintoOikeusChange).flatMapLatest(oppijaUpdate => Http.post('/tor/api/oppija', oppijaUpdate))

export const uusiOppijaP = routeP.map(route => { return !!route.uusiOppija })

export const loadingOppijaP = routeP.awaiting(oppijaP.mapError())

export const Oppija = ({oppija}) =>
  oppija.loading
    ? <Loading/>
    : (oppija.valittuOppija
      ? <ExistingOppija oppija={oppija.valittuOppija}/>
      : (
      oppija.uusiOppija
        ? <CreateOppija/>
        : <div></div>
      ))

const Loading = () => <div className='main-content oppija loading'></div>

const ExistingOppija = React.createClass({
  render() {
    let {oppija: { henkilo: henkilo, opintoOikeudet: opintoOikeudet}} = this.props
    return (
      <div className='main-content oppija'>
        <h2>{henkilo.sukunimi}, {henkilo.etunimet} <span className='hetu'>{henkilo.hetu}</span></h2>
        <hr></hr>
        { opintoOikeudet.map( opintoOikeus =>
          <OpintoOikeus key= { opintoOikeus.nimi } opintoOikeus={ opintoOikeus } />
        ) }
      </div>
    )
  }
})