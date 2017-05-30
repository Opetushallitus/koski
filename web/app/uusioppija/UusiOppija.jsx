import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from '../http'
import {navigateToOppija, showError} from '../location'
import UusiOpiskeluoikeus from './UusiOpiskeluoikeus.jsx'
import UusiHenkilö from './UusiHenkilo.jsx'
import Text from '../Text.jsx'

export const UusiOppija = ({hetu, oid}) => {
  const opiskeluoikeusAtom = Atom()
  const submitBus = Bacon.Bus()
  const opiskeluoikeusValidP = opiskeluoikeusAtom.map(oos => !!oos).skipDuplicates()
  const henkilöAtom = Atom({ hetu: hetu, oid: oid  })
  const henkilöValidAtom = Atom(false)
  const henkilöErrorsAtom = Atom([])
  const createOppijaP = Bacon.combineWith(henkilöAtom, opiskeluoikeusAtom, toCreateOppija)
  const createOppijaE = submitBus.map(createOppijaP)
    .flatMapLatest(postNewOppija)
    .map(oppija => ({oid: oppija.henkilö.oid}))

  createOppijaE.onValue(navigateToOppija)

  const inProgressP = submitBus.awaiting(createOppijaE.mapError())

  const submitEnabledP = henkilöValidAtom.and(inProgressP.not()).and(opiskeluoikeusValidP)

  const buttonTextP = inProgressP.map((inProgress) => <Text name={!inProgress ? 'Lisää henkilö' : 'Lisätään...'}/>)

  const errorsP = henkilöErrorsAtom

  return (
    <div className='content-area'>
      <form className='main-content oppija uusi-oppija'>
        <h2><Text name="Uuden opiskelijan lisäys"/></h2>
        <UusiHenkilö {...{ hetu, oid, henkilöAtom, henkilöValidAtom, henkilöErrorsAtom }}/>
        <hr/>
        <UusiOpiskeluoikeus opiskeluoikeusAtom={opiskeluoikeusAtom}/>
        <button className='button' disabled={submitEnabledP.not()} onClick={() => submitBus.push()}>{buttonTextP}</button>
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

export const postNewOppija = (oppija) => Http.post('/koski/api/oppija', oppija, {
  errorHandler: (e) => {
    if (e.httpStatus == 409) {
      e.text = <Text name='Opiskeluoikeutta ei voida lisätä, koska oppijalla on jo vastaava opiskeluoikeus.'/>
    }
    showError(e)
  },
  invalidateCache: ['/koski/api/oppija', '/koski/api/opiskeluoikeus', '/koski/api/editor']
})