import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from './http'
import {navigateToOppija, showError} from './location'
import {isValidHetu} from './hetu'
import {Opiskeluoikeus} from './CreateOpiskeluoikeus.jsx'
import {formatISODate} from './date.js'

export const createOppijaContentP = () => Bacon.constant({
  content: (<CreateOppija/>)
})

export const CreateOppija = () => {
  const etunimetAtom = Atom('')
  const kutsumanimiAtom = Atom('')
  const sukunimiAtom = Atom('')
  const hetuAtom = Atom('')
  const opiskeluoikeusAtom = Atom({})
  const createOppijaP = Bacon.combineWith(etunimetAtom, sukunimiAtom, kutsumanimiAtom, hetuAtom.map(h=>h.toUpperCase()), opiskeluoikeusAtom, toCreateOppija)
  const submitBus = Bacon.Bus()
  const createOppijaE = submitBus.map(createOppijaP)
    .flatMapLatest((oppija) => Http.put('/koski/api/oppija', oppija))
    .map(oppija => ({oid: oppija.henkilö.oid}))

  createOppijaE.onValue(navigateToOppija)
  createOppijaE.onError(showError)

  const inProgressP = submitBus.awaiting(createOppijaE.mapError())

  return (<div className='content-area'>
      {
        Bacon.combineWith(createOppijaP, inProgressP, ({ henkilö: {etunimet, sukunimi, kutsumanimi, hetu }, opiskeluoikeudet}, inProgress) => {
          const validKutsumanimi = kutsumanimi && etunimet ? etunimet.split(' ').indexOf(kutsumanimi) > -1 || etunimet.split('-').indexOf(kutsumanimi) > -1: true
          const opiskeluoikeusValid = !!opiskeluoikeudet
          const submitDisabled = !etunimet || !sukunimi || !kutsumanimi || !isValidHetu(hetu) || !validKutsumanimi || inProgress || !opiskeluoikeusValid
          const buttonText = !inProgress ? 'Lisää henkilö' : 'Lisätään...'
          const hetuClassName = !hetu ? 'hetu' : isValidHetu(hetu) ? 'hetu' : 'hetu error'
          const kutsumanimiClassName = validKutsumanimi ? 'kutsumanimi' : 'kutsumanimi error'
          const errors = []

          if(!validKutsumanimi) {
            errors.push(<li key='2' className='kutsumanimi'>Kutsumanimen on oltava yksi etunimistä.</li>)
          }

          return (<form className='main-content oppija uusi-oppija'>
            <label className='etunimet'>
              Etunimet
              <Input atom={etunimetAtom}/>
            </label>
            <label className={kutsumanimiClassName}>
              Kutsumanimi
              <Input atom={kutsumanimiAtom}/>
            </label>
            <label className='sukunimi'>
              Sukunimi
              <Input atom={sukunimiAtom}/>
            </label>
            <label className={hetuClassName}>
              Henkilötunnus
              <Input atom={hetuAtom}/>
            </label>
            <hr/>
            <Opiskeluoikeus opiskeluoikeusAtom={opiskeluoikeusAtom}/>
            <button className='button' disabled={submitDisabled} onClick={() => submitBus.push()}>{buttonText}</button>
            <ul className='error-messages'>
              {errors}
            </ul>
          </form>)
        })
      }
    </div>
  )
}

const Input = ({ atom }) => <input type="text" onChange={ (e) => atom.set(e.target.value) }></input>

const toCreateOppija = (etunimet, sukunimi, kutsumanimi, hetu, opiskeluoikeus) => {
  const {tutkinto, oppilaitos} = opiskeluoikeus || {}
  const date = new Date()
  return {
    henkilö: {
      etunimet: etunimet,
      sukunimi: sukunimi,
      kutsumanimi: kutsumanimi,
      hetu: hetu
    },
    opiskeluoikeudet: tutkinto && oppilaitos && [{
      tyyppi: { 'koodistoUri': 'opiskeluoikeudentyyppi', 'koodiarvo': 'ammatillinenkoulutus'},
      oppilaitos: oppilaitos,
      alkamispäivä: formatISODate(date),
      tila: {
        opiskeluoikeusjaksot: [ { alku: formatISODate(date), tila: { 'koodistoUri': 'koskiopiskeluoikeudentila', 'koodiarvo': 'lasna' } }]
      },
      suoritukset: [{
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: tutkinto.tutkintoKoodi,
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: tutkinto.diaarinumero
        },
        toimipiste : oppilaitos,
        tila: { 'koodistoUri': 'suorituksentila', 'koodiarvo': 'KESKEN'},
        tyyppi: { 'koodistoUri': 'suorituksentyyppi', 'koodiarvo': 'ammatillinentutkinto'}
      }]
    }]
  }
}