import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import {routeP} from './router'
import {CreateOppija} from './CreateOppija.jsx'
import {OpiskeluOikeus, opiskeluOikeusChange} from './OpiskeluOikeus.jsx'
import * as L from 'partial.lenses'
import R from 'ramda'

export const selectOppijaE = routeP.map('.oppijaId').flatMap(oppijaId => {
  return oppijaId ? Bacon.once({loading: true}).concat(Http.get(`/koski/api/oppija/${oppijaId}`)) : Bacon.once({ empty: true})
})

export const updateResultE = Bacon.Bus()

const applyChange = (lens, change, oppija) => L.modify(lens, change, oppija)

const opiskeluOikeusIdLens = (id) => (L.compose(L.prop('opiskeluoikeudet'), L.find(R.whereEq({id}))))

export const oppijaP = Bacon.update({ loading: true },
  selectOppijaE, (previous, oppija) => oppija,
  updateResultE.map('.opiskeluoikeudet').flatMap(Bacon.fromArray), (currentOppija, {id, versionumero}) => {
    return applyChange(L.compose(opiskeluOikeusIdLens(id), L.prop('versionumero')), () => versionumero, currentOppija)
  },
  opiskeluOikeusChange, (currentOppija, [lens, change]) => applyChange(lens, change, currentOppija)
)

updateResultE.plug(oppijaP.sampledBy(opiskeluOikeusChange).flatMapLatest(oppijaUpdate => Http.put('/koski/api/oppija', oppijaUpdate)))

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
        <ul className="oppilaitokset">
        { R.toPairs(R.groupBy((opiskeluOikeus => opiskeluOikeus.oppilaitos.oid), opiskeluoikeudet)).map( ([, opiskeluOikeudet]) =>
          <li className="oppilaitos" key={opiskeluOikeudet[0].oppilaitos.oid}>
            <span className="oppilaitos">{opiskeluOikeudet[0].oppilaitos.nimi.fi}</span><Opintosuoritusote oppija={henkilö} oppilaitos={opiskeluOikeudet[0].oppilaitos} tyyppi={opiskeluOikeudet[0].tyyppi.koodiarvo}/>
            {
              opiskeluOikeudet.map( opiskeluOikeus =>
                  <OpiskeluOikeus key={opiskeluOikeus.id} oppija={ henkilö } opiskeluOikeus={ opiskeluOikeus } lens= { opiskeluOikeusIdLens(opiskeluOikeus.id) } />
              )
            }
          </li>
        ) }
        </ul>
      </div>
    )
  }
})


const Opintosuoritusote = React.createClass({
  render() {
    let {oppilaitos, oppija, tyyppi} = this.props
    if (tyyppi == 'korkeakoulutus') {
      let href = '/koski/opintosuoritusote/' + oppija.oid + '/' + oppilaitos.oid
      return <a className="opintosuoritusote" href={href}>näytä opintosuoritusote</a>
    } else {
      return null
    }
  }
})