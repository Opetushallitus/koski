import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import R from 'ramda'
import Http from '../http'
import {navigateToOppija, showError} from '../location'
import UusiOpiskeluoikeus from './UusiOpiskeluoikeus.jsx'
import UusiHenkilö from './UusiHenkilo.jsx'

export const UusiOppija = ({hetu}) => {
  const opiskeluoikeusAtom = Atom()
  const submitBus = Bacon.Bus()
  const opiskeluoikeusValidP = opiskeluoikeusAtom.map(oos => !!oos).skipDuplicates()
  const henkilöAtom = Atom({ hetu })
  const existingHenkilöP = Http.cachedGet('/koski/api/henkilo/hetu/' + hetu).map('.0')
  existingHenkilöP.filter(R.identity).onValue((henkilö) => henkilöAtom.set(henkilö))
  const henkilöValidAtom = Atom(false)
  const henkilöErrorsAtom = Atom([])
  const createOppijaP = Bacon.combineWith(henkilöAtom, opiskeluoikeusAtom, toCreateOppija)
  const createOppijaE = submitBus.map(createOppijaP)
    .flatMapLatest(putOppija)
    .map(oppija => ({oid: oppija.henkilö.oid}))

  createOppijaE.onValue(navigateToOppija)

  const inProgressP = submitBus.awaiting(createOppijaE.mapError())

  const submitEnabledP = henkilöValidAtom.and(inProgressP.not()).and(opiskeluoikeusValidP)

  const buttonTextP = inProgressP.map((inProgress) => !inProgress ? 'Lisää henkilö' : 'Lisätään...')

  const errorsP = henkilöErrorsAtom

  return (
    <div className='content-area'>
      <form className='main-content oppija uusi-oppija'>
        <h2>Uuden opiskelijan lisäys</h2>
        <UusiHenkilö {...{ hetu, henkilöAtom, henkilöValidAtom, henkilöErrorsAtom }}/>
        <hr/>
        <UusiOpiskeluoikeus opiskeluoikeusAtom={opiskeluoikeusAtom}/>
        {
          // TODO: attribute lifting doesn't seem to work in phantom
          submitEnabledP.map((enabled) => <button className='button' disabled={!enabled} onClick={() => submitBus.push()}>{buttonTextP}</button>)
        }
        <ul className='error-messages'>
          {errorsP.map(errors => errors.map(({ field, message }, i) => <li key={i} className={field}>{message}</li>))}
        </ul>
      </form>
    </div>
  )
}

const toCreateOppija = (henkilö, opiskeluoikeus) => {
  return {
    henkilö,
    opiskeluoikeudet: [opiskeluoikeus]
  }
}

export const putOppija = (oppija) => Http.put('/koski/api/oppija', oppija, { errorHandler: showError, invalidateCache: ['/koski/api/oppija', '/koski/api/opiskeluoikeus', '/koski/api/editor']})