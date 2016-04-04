import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import {routeP} from './router'
import {CreateOppija} from './CreateOppija.jsx'
import {OpiskeluOikeus, opiskeluOikeusChange} from './OpiskeluOikeus.jsx'
import * as L from "partial.lenses"
import R from 'ramda'

export const selectOppijaE = routeP.map('.oppijaId').flatMap(oppijaId => {
  return oppijaId ? Bacon.once({loading: true}).concat(Http.get(`/tor/api/oppija/${oppijaId}`)) : Bacon.once({ empty: true})
})

export const updateResultE = Bacon.Bus()

const applyChange = (oppija, lens, change) => {
  let currentValue = L.view(lens, oppija)
  let newValue = change(currentValue)
  let newOppija = L.set(lens, newValue, oppija)
  return newOppija
}

const opiskeluOikeusIdLens = (id) => (L.compose(L.prop('opiskeluoikeudet'), L.find(R.whereEq({id}))))

export const oppijaP = Bacon.update({ loading: true },
  selectOppijaE, (previous, oppija) => oppija,
  updateResultE.map('.opiskeluoikeudet').flatMap(Bacon.fromArray), (currentOppija, {id, versionumero}) => {
    return applyChange(currentOppija, L.compose(opiskeluOikeusIdLens(id), L.prop('versionumero')), () => versionumero)
  },
  opiskeluOikeusChange, (currentOppija, [lens, change]) => applyChange(currentOppija, lens, change)
)

updateResultE.plug(oppijaP.sampledBy(opiskeluOikeusChange).flatMapLatest(oppijaUpdate => Http.put('/tor/api/oppija', oppijaUpdate)))

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
        <h4>Opiskeluoikeudet</h4>
        { opiskeluoikeudet.map( opiskeluOikeus =>
          <OpiskeluOikeus key={opiskeluOikeus.id} opiskeluOikeus={ opiskeluOikeus } lens= { opiskeluOikeusIdLens(opiskeluOikeus.id) } />
        ) }
      </div>
    )
  }
})