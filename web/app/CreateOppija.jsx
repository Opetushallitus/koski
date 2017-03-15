import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from './http'
import {navigateToOppija, showError} from './location'
import {Opiskeluoikeus} from './CreateOpiskeluoikeus.jsx'

export const CreateOppija = ({hetu}) => {
  const etunimetAtom = Atom('')
  const kutsumanimiAtom = Atom('')
  const sukunimiAtom = Atom('')
  const opiskeluoikeusAtom = Atom()
  const createOppijaP = Bacon.combineWith(etunimetAtom, sukunimiAtom, kutsumanimiAtom, opiskeluoikeusAtom, toCreateOppija(hetu))
  const submitBus = Bacon.Bus()
  const createOppijaE = submitBus.map(createOppijaP)
    .flatMapLatest((oppija) => Http.put('/koski/api/oppija', oppija))
    .map(oppija => ({oid: oppija.henkilö.oid}))
  const opiskeluoikeusValidP = opiskeluoikeusAtom.map(oos => !!oos).skipDuplicates()

  createOppijaE.onValue(navigateToOppija)
  createOppijaE.onError(showError)

  const inProgressP = submitBus.awaiting(createOppijaE.mapError())

  const validKutsumanimiP = Bacon.combineWith(kutsumanimiAtom, etunimetAtom, (kutsumanimi, etunimet) => kutsumanimi && etunimet ? etunimet.split(' ').indexOf(kutsumanimi) > -1 || etunimet.split('-').indexOf(kutsumanimi) > -1: true)

  const kutsumanimiClassNameP = validKutsumanimiP.map(valid => valid ? 'kutsumanimi' : 'kutsumanimi error')

  const submitEnabledP = etunimetAtom.and(sukunimiAtom).and(kutsumanimiAtom).and(validKutsumanimiP).and(inProgressP.not()).and(opiskeluoikeusValidP)

  const buttonTextP = inProgressP.map((inProgress) => !inProgress ? 'Lisää henkilö' : 'Lisätään...')

  const errorsP = validKutsumanimiP.map(valid => valid ? [] : [{field: 'kutsumanimi', message: 'Kutsumanimen on oltava yksi etunimistä.'}])

  return (
    <div className='content-area'>
      <form className='main-content oppija uusi-oppija'>
        <label className='hetu'>
          Henkilötunnus
          <span className='value'>{hetu}</span>
        </label>
        <label className='etunimet'>
          Etunimet
          <Input atom={etunimetAtom}/>
        </label>
        <label className={kutsumanimiClassNameP}>
          Kutsumanimi
          <Input atom={kutsumanimiAtom}/>
        </label>
        <label className='sukunimi'>
          Sukunimi
          <Input atom={sukunimiAtom}/>
        </label>
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

const Input = ({ atom }) => <input type="text" onChange={ (e) => atom.set(e.target.value) }></input>

const toCreateOppija = (hetu) => (etunimet, sukunimi, kutsumanimi, opiskeluoikeus) => {
  return {
    henkilö: {
      etunimet: etunimet,
      sukunimi: sukunimi,
      kutsumanimi: kutsumanimi,
      hetu: hetu
    },
    opiskeluoikeudet: [opiskeluoikeus]
  }
}