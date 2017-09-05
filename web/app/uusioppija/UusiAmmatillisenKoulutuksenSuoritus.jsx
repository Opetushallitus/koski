import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Autocomplete from '../Autocomplete.jsx'
import Http from '../http'
import Text from '../Text.jsx'
import KoodistoDropdown from '../KoodistoDropdown.jsx'
import {doActionWhileMounted} from '../util'

export default ({suoritusAtom, oppilaitosAtom, suorituskieliAtom}) => {
  const tutkintoAtom = Atom()
  const suoritustapaAtom = Atom()
  oppilaitosAtom.changes().onValue(() => tutkintoAtom.set(undefined))

  const makeSuoritus = (oppilaitos, tutkinto, suorituskieli, suoritustapa) => {
    if (tutkinto && oppilaitos && suoritustapa) {
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
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'ammatillinentutkinto'},
        suoritustapa: suoritustapa,
        suorituskieli : suorituskieli
      }
    }
  }
  Bacon.combineWith(oppilaitosAtom, tutkintoAtom, suorituskieliAtom, suoritustapaAtom, makeSuoritus).onValue(suoritus => suoritusAtom.set(suoritus))
  return (<div>
    <Tutkinto tutkintoAtom={tutkintoAtom} oppilaitosP={oppilaitosAtom}/>
    <Suoritustapa tutkintoP={tutkintoAtom} suoritustapaAtom={suoritustapaAtom}/>
  </div>)
}

const Tutkinto = ({tutkintoAtom, oppilaitosP}) =>{
  return (<div>
    {
      Bacon.combineWith(oppilaitosP, tutkintoAtom, (oppilaitos, tutkinto) =>
        oppilaitos && (
          <label className='tutkinto'>
            <Text name="Tutkinto"/>
            <Autocomplete
              resultAtom={tutkintoAtom}
              fetchItems={(value) => (value.length >= 3)
                  ? Http.cachedGet('/koski/api/tutkinnonperusteet/oppilaitos/' + oppilaitos.oid + '?query=' + value)
                  : Bacon.constant([])}
              disabled={!oppilaitos}
              selected={tutkinto}
            />
          </label>
        )
      )
    }
  </div> )
}

const Suoritustapa = ({tutkintoP, suoritustapaAtom}) => {
  return (<div>{
    tutkintoP.flatMapLatest(tutkinto => {
      let suoritustavatP = (tutkinto
          ? Http.cachedGet(`/koski/api/tutkinnonperusteet/suoritustavat/${encodeURIComponent(tutkinto.diaarinumero)}`)
          : Bacon.constant([])
      ).toProperty()
      return (<span><KoodistoDropdown
        className="suoritustapa"
        title="Suoritustapa"
        options={suoritustavatP}
        selected={suoritustapaAtom}
      /> { doActionWhileMounted( Bacon.combineAsArray(suoritustapaAtom, suoritustavatP), ([suoritustapa, suoritustavat]) => {
          let currentOneFound = suoritustapa && suoritustavat.map(k => k.koodiarvo).includes(suoritustapa.koodiarvo)
          if (!currentOneFound) {
            if (suoritustavat.length == 1) {
              suoritustapaAtom.set(suoritustavat[0])
            } else if (suoritustapa) {
              suoritustapaAtom.set(undefined)
            }
          }
        }
      )}</span>)
    })
  }</div>)
}