import React from 'baret'
import Bacon from 'baconjs'
import Http from '../util/http'
import {showInternalError} from '../util/location'
import {KelaOppijaHaku} from './KelaOppijaHaku'
import {KelaHenkilo} from './KelaOppija'
import {OpiskeluoikeusTabs} from './OpiskeluoikeusNav'
import {sortOpiskeluoikeudetJaSuoritukset} from './sorting'


export const kelaVirkailijaP = (hetu) => {
  const loadingStream = Bacon.constant(loadingSpinner)
    .toEventStream()
    .merge(Bacon.once(hetu).flatMap(opinnotTaiTyhja).mapError(handleError))

  return Bacon.constant({
    content: (
      <div className='content-area main-content kela'>
        <KelaOppijaHaku/>
        {loadingStream.map(v => v)}
      </div>),
    title: 'Kela'
  })
}

const loadingSpinner = <div className="ajax-indicator-bg"/>

const opinnotTaiTyhja = (hetu) => hetu
  ? Http.post('/koski/api/luovutuspalvelu/kela/hetu', {hetu}, {willHandleErrors: true}).toProperty().map(kelaJson => <KelaOpinnot oppija={kelaJson}/>)
  : <Empty/>

const handleError = (error) => {
  if (error.httpStatus === 404) {
    return <KelaEiOpintoja/>
  } else {
    showInternalError()
    return <Empty/>
  }
}

const KelaEiOpintoja = ({}) => <div><h1>{'Ei opintoja'}</h1></div>

const Empty = ({}) => <div></div>

const KelaOpinnot = ({oppija}) => {
  const opiskeluoikeudet = sortOpiskeluoikeudetJaSuoritukset(oppija.opiskeluoikeudet)
  const henkilo = oppija.henkilö
  return (
    <div className='kela'>
      <KelaHenkilo henkilo={henkilo}/>
      <OpiskeluoikeusTabs opiskeluoikeudet={opiskeluoikeudet}/>
    </div>
  )
}
