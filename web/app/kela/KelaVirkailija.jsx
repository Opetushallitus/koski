import React from 'baret'
import Bacon from 'baconjs'
import Http from '../util/http'
import * as R from 'ramda'
import {showInternalError} from '../util/location'
import {KelaOppijaHaku} from './KelaOppijaHaku'
import {KelaHenkilo} from './KelaOppija'
import {OpiskeluoikeusTabs} from './OpiskeluoikeusNav'
import {sortOpiskeluoikeudetJaSuoritukset} from './sorting'


export const kelaVirkailijaP = (path) => {
  const loadingOppijaStream = Bacon.once(fetchKelaOppijaJson(path))
    .flatMap(oppijaP => oppijaP.map(json => json ? <KelaOpinnot oppija={json}/> : <Empty/>))
    .mapError(handleError)

  const loadingStream = Bacon.constant(loadingSpinner)
    .toEventStream()
    .merge(loadingOppijaStream)

  return Bacon.constant({
    content: (
      <div className='content-area main-content kela'>
        <KelaOppijaHaku/>
        {loadingStream.map(v => v)}
      </div>),
    title: 'Kela'
  })
}

const fetchKelaOppijaJson = (path) => {
  if (path.includes('koski/kela/versiohistoria/')) {
    const [oppijaOid, opiskeluoikeusOid, versio] = R.takeLast(3, path.split('/'))
    return opiskeluoikeudenVersio(oppijaOid, opiskeluoikeusOid, versio)
  } else {
    const hetu = (path.match(new RegExp('/koski/kela/(.*)')) || [])[1]
    return hetu ? kaikkiUusimmatOpiskeluoikeudet(hetu) : Bacon.constant(undefined)
  }
}

const kaikkiUusimmatOpiskeluoikeudet = hetu =>
  Http.post('/koski/api/luovutuspalvelu/kela/hetu', {hetu}, {willHandleErrors: true}).toProperty()

const opiskeluoikeudenVersio = (oppijaOid, opiskeluoikeusOid, versio) =>
  Http.get(`/koski/api/luovutuspalvelu/kela/versiohistoria/${oppijaOid}/${opiskeluoikeusOid}/${versio}`, {willHandleErrors: true}).toProperty()

const handleError = (error) => {
  if (error.httpStatus === 404) {
    return <KelaEiOpintoja/>
  } else {
    showInternalError()
    return <Empty/>
  }
}

const loadingSpinner = <div className="ajax-indicator-bg"/>

const KelaEiOpintoja = ({}) => <div><h1>{'Ei opintoja'}</h1></div>

const Empty = ({}) => <div></div>

const KelaOpinnot = ({oppija}) => {
  const opiskeluoikeudet = sortOpiskeluoikeudetJaSuoritukset(oppija.opiskeluoikeudet)
  const henkilo = oppija.henkilö
  return (
    <div className='kela'>
      <KelaHenkilo henkilo={henkilo}/>
      <OpiskeluoikeusTabs opiskeluoikeudet={opiskeluoikeudet} henkilo={henkilo}/>
    </div>
  )
}

KelaOpinnot.displayName = 'KelaOpinnot'
