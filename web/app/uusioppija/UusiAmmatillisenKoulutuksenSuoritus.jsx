import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Autocomplete from '../Autocomplete.jsx'
import Http from '../http'

export default ({suoritusAtom, oppilaitosAtom}) => {
  const tutkintoAtom = Atom()
  oppilaitosAtom.changes().onValue(() => tutkintoAtom.set(undefined))

  const makeSuoritus = (oppilaitos, tutkinto) => {
    if (tutkinto && oppilaitos) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: tutkinto.tutkintoKoodi,
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: tutkinto.diaarinumero
        },
        toimipiste : oppilaitos,
        tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'ammatillinentutkinto'}
      }
    }
  }
  Bacon.combineWith(oppilaitosAtom, tutkintoAtom, makeSuoritus).onValue(suoritus => suoritusAtom.set(suoritus))
  return <Tutkinto tutkintoAtom={tutkintoAtom} oppilaitosP={oppilaitosAtom}/>
}

const Tutkinto = ({tutkintoAtom, oppilaitosP}) =>{
  return (<div>
    {
      Bacon.combineWith(oppilaitosP, tutkintoAtom, (oppilaitos, tutkinto) =>
        oppilaitos && (
          <label className='tutkinto'>Tutkinto<Autocomplete
            resultAtom={tutkintoAtom}
            fetchItems={(value) => (value.length >= 3)
                ? Http.cachedGet('/koski/api/tutkinnonperusteet/oppilaitos/' + oppilaitos.oid + '?query=' + value)
                : Bacon.constant([])}
            disabled={!oppilaitos}
            selected={tutkinto}
          /></label>
        )
      )
    }
  </div> )
}