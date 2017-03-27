import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from './http'
import {navigateToOppija, showError} from './location'
import Opiskeluoikeus from './CreateOpiskeluoikeus.jsx'
import CreateHenkilö from './CreateHenkilo.jsx'

export const CreateOppija = ({hetu}) => {
  const opiskeluoikeusAtom = Atom()
  const submitBus = Bacon.Bus()
  const opiskeluoikeusValidP = opiskeluoikeusAtom.map(oos => !!oos).skipDuplicates()
  const henkilöAtom = Atom({ hetu })
  const henkilöValidAtom = Atom(false)
  const henkilöErrorsAtom = Atom([])
  const createOppijaP = Bacon.combineWith(henkilöAtom, opiskeluoikeusAtom, toCreateOppija)
  const createOppijaE = submitBus.map(createOppijaP)
    .flatMapLatest((oppija) => Http.put('/koski/api/oppija', oppija))
    .map(oppija => ({oid: oppija.henkilö.oid}))

  createOppijaE.onValue(navigateToOppija)
  createOppijaE.onError(showError)

  const inProgressP = submitBus.awaiting(createOppijaE.mapError())

  const submitEnabledP = henkilöValidAtom.and(inProgressP.not()).and(opiskeluoikeusValidP)

  const buttonTextP = inProgressP.map((inProgress) => !inProgress ? 'Lisää henkilö' : 'Lisätään...')

  const errorsP = henkilöErrorsAtom

  return (
    <div className='content-area'>
      <form className='main-content oppija uusi-oppija'>
        <CreateHenkilö {...{ hetu, henkilöAtom, henkilöValidAtom, henkilöErrorsAtom }}/>
        <hr/>
        <Opiskeluoikeus opiskeluoikeusAtom={opiskeluoikeusAtom}/>
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