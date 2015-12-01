import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import {routeP} from './router'
import {CreateOppija} from './CreateOppija.jsx'
import {OpiskeluOikeus, opiskeluOikeusChange} from './OpiskeluOikeus.jsx'
import Ramda from 'ramda'

var selectOppijaE = routeP.map('.oppijaId').flatMap(oppijaId => {
  return oppijaId ? Bacon.once({loading: true}).concat(Http.get(`/tor/api/oppija/${oppijaId}`)) : Bacon.once({ empty: true})
})

export const oppijaP = Bacon.update({ loading: true },
  selectOppijaE, (previous, oppija) => oppija,
  opiskeluOikeusChange, (currentOppija, [opiskeluOikeusId, change]) => {
    let changedOppija = Ramda.clone(currentOppija)
    changedOppija.opiskeluoikeudet = changedOppija.opiskeluoikeudet.map(opiskeluOikeus =>
        opiskeluOikeus.id == opiskeluOikeusId
          ? change(opiskeluOikeus)
          : opiskeluOikeus
    )
    return changedOppija
  }
)

export const updateResultE = oppijaP.sampledBy(opiskeluOikeusChange).flatMapLatest(oppijaUpdate => Http.put('/tor/api/oppija', oppijaUpdate))

export const uusiOppijaP = routeP.map(route => { return !!route.uusiOppija })

export const Oppija = ({oppija}) =>
  oppija.valittuOppija.loading
    ? <Loading/>
    : (!oppija.valittuOppija.empty
      ? <ExistingOppija oppija={oppija.valittuOppija}/>
      : (
      oppija.uusiOppija
        ? <CreateOppija/>
        : <div></div>
      ))

const Loading = () => <div className='main-content oppija loading'></div>

const ExistingOppija = React.createClass({
  render() {
    let {oppija: { henkilö: henkilö, opiskeluoikeudet: opiskeluoikeudet}} = this.props
    return (
      <div className='main-content oppija'>
        <h2>{henkilö.sukunimi}, {henkilö.etunimet} <span className='hetu'>{henkilö.hetu}</span></h2>
        <hr></hr>
        { opiskeluoikeudet.map( opiskeluOikeus =>
          <OpiskeluOikeus key={opiskeluOikeus.id} opiskeluOikeus={ opiskeluOikeus } />
        ) }
      </div>
    )
  }
})