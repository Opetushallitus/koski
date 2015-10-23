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

export const oppijaP = selectOppijaE.flatMapLatest(oppija =>
  Bacon.once(oppija)
    .concat(opintoOikeusChange.map( changedOpintoOikeus => {
      let changedOppija = Ramda.clone(oppija)
      changedOppija.opintoOikeudet = changedOppija.opintoOikeudet.map(opintoOikeus =>
          opintoOikeus.id == changedOpintoOikeus.id
            ? changedOpintoOikeus
            : opintoOikeus
      )
      return changedOppija
    }))
).toProperty()

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
    let {oppija} = this.props
    return (
      <div className='main-content oppija'>
        <h2>{oppija.sukunimi}, {oppija.etunimet} <span className='hetu'>{oppija.hetu}</span></h2>
        <hr></hr>
        { oppija.opintoOikeudet.map( opintoOikeus =>
          <OpintoOikeus key= { opintoOikeus.nimi } opintoOikeus={ opintoOikeus } />
        ) }
      </div>
    )
  }
})

oppijaP.sampledBy(opintoOikeusChange, (oppija, opintoOikeus) => ({
  oid: oppija.oid,
  opintoOikeudet: [opintoOikeus]
})).onValue(oppijaUpdate => {

  // TODO: handle errors


  const createOppijaS = Http.post('/tor/api/oppija', oppijaUpdate)

  createOppijaS.onValue(oid => console.log("SAVED", oid))

  createOppijaS.onError((error) => {
    console.log("ERROR", error)
  })
})