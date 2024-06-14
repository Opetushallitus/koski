import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from '../util/http'
import { navigateToOppija, showError } from '../util/location'
import { searchStringAtom } from '../virkailija/OppijaHaku'
import { UusiOpiskeluoikeusForm } from '../uusiopiskeluoikeus/UusiOpiskeluoikeusForm'
import UusiHenkilö from './UusiHenkilo'
import Text from '../i18n/Text'

export const UusiOppija = ({ hetu, oid }) => {
  const opiskeluoikeusAtom = Atom()
  const submitBus = Bacon.Bus()
  const opiskeluoikeusValidP = opiskeluoikeusAtom
    .map((oos) => !!oos)
    .skipDuplicates()
  const henkilöAtom = Atom({ hetu, oid })
  const henkilöValidAtom = Atom(false)
  const createOppijaP = Bacon.combineWith(
    henkilöAtom,
    opiskeluoikeusAtom,
    toCreateOppija
  )
  const createOppijaE = submitBus
    .map(createOppijaP)
    .flatMapLatest(postNewOppija)
    .map((oppija) => ({ oid: oppija.henkilö.oid }))

  createOppijaE.onValue((v) => {
    searchStringAtom.set('')
    navigateToOppija(v)
  })

  const inProgressP = submitBus.awaiting(createOppijaE.mapError())

  const submitEnabledP = henkilöValidAtom
    .and(inProgressP.not())
    .and(opiskeluoikeusValidP)

  const buttonTextP = inProgressP.map((inProgress) => (
    <Text name={!inProgress ? 'Lisää opiskelija' : 'Lisätään...'} />
  ))

  return (
    <div className="content-area">
      <form className="main-content oppija uusi-oppija">
        <h2>
          <Text name="Uuden opiskelijan lisäys" />
        </h2>
        <UusiHenkilö {...{ hetu, oid, henkilöAtom, henkilöValidAtom }} />
        <hr />
        <UusiOpiskeluoikeusForm onResult={(oo) => opiskeluoikeusAtom.set(oo)} />
        <button
          type="button"
          className="koski-button"
          disabled={submitEnabledP.not()}
          onClick={() => submitBus.push()}
        >
          {buttonTextP}
        </button>
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

export const postNewOppija = (oppija) =>
  Http.post('/koski/api/oppija', oppija, {
    errorHandler: (e) => {
      if (e.httpStatus === 409) {
        e.text = (
          <Text name="Opiskeluoikeutta ei voida lisätä, koska oppijalla on jo vastaava opiskeluoikeus." />
        )
      }
      showError(e)
    },
    invalidateCache: [
      '/koski/api/oppija',
      '/koski/api/opiskeluoikeus',
      '/koski/api/editor'
    ]
  })
