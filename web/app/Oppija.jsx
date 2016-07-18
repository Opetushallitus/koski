import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import {routeP} from './router'
import {CreateOppija} from './CreateOppija.jsx'
import { modelData } from './EditorModel.js'
import {OppijaEditor} from './OppijaEditor.jsx'
import * as L from 'partial.lenses'
import R from 'ramda'

export const selectOppijaE = routeP.map('.oppijaId').flatMap(oppijaId => {
  return oppijaId
    ? Bacon.once({loading: true}).concat(Http.get(`/koski/api/editor/${oppijaId}`))
    : Bacon.once({ empty: true})
})

export const updateResultE = Bacon.Bus()
const opiskeluOikeusChange = Bacon.never() // Right now, there's no editing

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

export const oppijaStateP = Bacon.combineTemplate({
    valittuOppija: oppijaP,
    uusiOppija: uusiOppijaP
})

export const Oppija = ({oppija}) =>
  oppija.valittuOppija.loading
    ? <Loading/>
    : (!oppija.valittuOppija.empty
      ? <ExistingOppija oppija={oppija.valittuOppija} editor={oppija.valittuOppija}/>
      : (
      oppija.uusiOppija
        ? <CreateOppija/>
        : <div></div>
      ))

const Loading = () => <div className='main-content oppija loading'></div>

const ExistingOppija = React.createClass({
  render() {
    let {oppija, editor} = this.props
    let henkilö = modelData(oppija, 'henkilö')
    return (
      <div className='main-content oppija'>
        <h2>{henkilö.sukunimi}, {henkilö.etunimet} <span className='hetu'>{henkilö.hetu}</span></h2>
        <hr></hr>
        <h4>Opiskeluoikeudet</h4>
        <OppijaEditor model={editor} />
      </div>
    )
  }
})