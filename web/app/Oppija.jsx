import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import {routeP} from './router'
import {CreateOppija} from './CreateOppija.jsx'
import { modelTitle, modelLookup, modelSet, objectLookup } from './EditorModel.js'
import {OppijaEditor} from './OppijaEditor.jsx'
import * as L from 'partial.lenses'
import R from 'ramda'

export const selectOppijaE = routeP.map('.oppijaId').flatMap(oppijaId => {
  return oppijaId
    ? Bacon.once({loading: true}).concat(Http.get(`/koski/api/editor/${oppijaId}`))
    : Bacon.once({ empty: true})
})

export const updateResultE = Bacon.Bus()
export const opiskeluOikeusChange = Bacon.Bus()

const opiskeluOikeusIdLens = (id) => (L.compose(L.prop('opiskeluoikeudet'), L.find(R.whereEq({id}))))

export const oppijaP = Bacon.update({ loading: true },
  selectOppijaE, (previous, oppija) => oppija,
  updateResultE.map('.opiskeluoikeudet').flatMap(Bacon.fromArray), (currentOppija, {id, versionumero}) => {
    //const applyChange = (lens, change, oppija) => L.modify(lens, change, oppija)
    //return applyChange(L.compose(opiskeluOikeusIdLens(id), L.prop('versionumero')), () => versionumero, currentOppija)
    return currentOppija // TODO this is broken: server sends data, not models
  },
  opiskeluOikeusChange, (currentOppija, [context, value]) => {
    var modifiedModel = modelSet(currentOppija, context.path, value)
    return modifiedModel
  }
)

updateResultE.plug(oppijaP
  .sampledBy(opiskeluOikeusChange, (oppija, [context, value]) => ({oppija, context}))
  .flatMapLatest(({oppija, context: {path}}) => {
    let opiskeluoikeusPath = path.split('.').slice(0, 4)
    var oppijaData = oppija.value.data
    let opiskeluoikeus = objectLookup(oppijaData, opiskeluoikeusPath.join('.'))
    console.log("should update ", opiskeluoikeusPath, opiskeluoikeus)
    let oppijaUpdate = {
      henkilö: {oid: oppijaData.henkilö.oid},
      opiskeluoikeudet: [opiskeluoikeus]
    }
    return Http.put('/koski/api/oppija', oppijaUpdate)
  })
)

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
    let henkilö = modelLookup(oppija, 'henkilö')
    return (
      <div className='main-content oppija'>
        <h2>{modelTitle(henkilö, 'sukunimi')}, {modelTitle(henkilö, 'etunimet')} <span className='hetu'>{modelTitle(henkilö, 'hetu')}</span></h2>
        <hr></hr>
        <h4>Opiskeluoikeudet</h4>
        <OppijaEditor model={editor} />
      </div>
    )
  }
})