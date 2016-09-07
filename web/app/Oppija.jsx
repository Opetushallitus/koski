import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import {locationP} from './location'
import {CreateOppija} from './CreateOppija.jsx'
import { modelTitle, modelLookup, modelSet, objectLookup } from './EditorModel'
import { editorMapping } from './OppijaEditor.jsx'
import { Editor } from './GenericEditor.jsx'
import * as L from 'partial.lenses'
import R from 'ramda'

const oppijaIdP = locationP.map(location => {
  const match = location.match(new RegExp('/koski/oppija/(.*)'))
  return match ? match[1] : undefined
})

export const uusiOppijaP = locationP.map(location => location === '/koski/uusioppija')

export const selectOppijaE = oppijaIdP.flatMap(oppijaId => {
  return oppijaId
    ? Bacon.once({loading: true}).concat(Http.get(`/koski/api/editor/${oppijaId}`))
    : Bacon.once({ empty: true})
})

export const updateResultE = Bacon.Bus()
export const opiskeluOikeusChange = Bacon.Bus()

export const oppijaP = Bacon.update({ loading: true },
  selectOppijaE, (previous, oppija) => oppija,
  updateResultE.map('.opiskeluoikeudet').flatMap(Bacon.fromArray), (currentOppija, {id, versionumero}) => {
    let correctId = R.whereEq({id})
    let containsOpiskeluoikeus = (oppilaitos) => oppilaitos.opiskeluoikeudet.find(correctId)
    let lens = L.compose('value', 'data', 'opiskeluoikeudet', L.find(containsOpiskeluoikeus), 'opiskeluoikeudet', L.find(correctId), 'versionumero')
    return L.set(lens, versionumero, currentOppija)
  },
  opiskeluOikeusChange, (currentOppija, [context, value]) => {
    var modifiedModel = modelSet(currentOppija, context.path, value)
    return modifiedModel
  }
)

updateResultE.plug(oppijaP
  .sampledBy(opiskeluOikeusChange, (oppija, [context]) => ({oppija, context}))
  .flatMapLatest(({oppija, context: {path}}) => {
    let opiskeluoikeusPath = path.split('.').slice(0, 4)
    var oppijaData = oppija.value.data
    let opiskeluoikeus = objectLookup(oppijaData, opiskeluoikeusPath.join('.'))
    let oppijaUpdate = {
      henkilö: {oid: oppijaData.henkilö.oid},
      opiskeluoikeudet: [opiskeluoikeus]
    }
    return Http.put('/koski/api/oppija', oppijaUpdate)
  })
)


export const oppijaStateP = Bacon.combineTemplate({
    valittuOppija: oppijaP,
    uusiOppija: uusiOppijaP
})

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

export const ExistingOppija = React.createClass({
  render() {
    let {oppija} = this.props
    let henkilö = modelLookup(oppija, 'henkilö')
    return (
      <div className='main-content oppija'>
        <h2>{modelTitle(henkilö, 'sukunimi')}, {modelTitle(henkilö, 'etunimet')} <span className='hetu'>{modelTitle(henkilö, 'hetu')}</span></h2>
        <hr></hr>
        <h4>Opiskeluoikeudet</h4>
        {
          oppija
            ? <Editor model={oppija} editorMapping={editorMapping} />
            : null
        }
      </div>
    )
  }
})