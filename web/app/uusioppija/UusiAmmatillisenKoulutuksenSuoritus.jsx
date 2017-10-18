import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from '../http'
import KoodistoDropdown from '../KoodistoDropdown.jsx'
import TutkintoAutocomplete from '../TutkintoAutocomplete.jsx'
import {doActionWhileMounted, ift} from '../util'
import Suoritustyyppi from './Suoritustyyppi.jsx'
import {koodiarvoMatch, koodistoValues} from './koodisto'

export default ({suoritusAtom, oppilaitosAtom, suorituskieliAtom}) => {
  const suoritustyypitP = koodistoValues('suorituksentyyppi/ammatillinentutkinto,nayttotutkintoonvalmistavakoulutus')
  const tutkintoAtom = Atom()
  const suoritustyyppiAtom = Atom()
  const suoritustapaAtom = Atom()
  suoritustyypitP.onValue(tyypit => suoritustyyppiAtom.set(tyypit.find(koodiarvoMatch('ammatillinentutkinto'))))
  oppilaitosAtom.changes().onValue(() => tutkintoAtom.set(undefined))

  const makeSuoritus = (oppilaitos, suoritustyyppi, tutkinto, suorituskieli, suoritustapa) => {
    let tutkintoData = tutkinto && {
        tunniste: {
          koodiarvo: tutkinto.tutkintoKoodi,
          koodistoUri: 'koulutus'
        },
        perusteenDiaarinumero: tutkinto.diaarinumero
      }
    if (koodiarvoMatch('ammatillinentutkinto')(suoritustyyppi) && tutkinto && oppilaitos && suoritustapa) {
      return {
        koulutusmoduuli: tutkintoData,
        toimipiste : oppilaitos,
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'ammatillinentutkinto'},
        suoritustapa: suoritustapa,
        suorituskieli : suorituskieli
      }
    }
    if (koodiarvoMatch('nayttotutkintoonvalmistavakoulutus')(suoritustyyppi) && tutkinto && oppilaitos) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999904',
            koodistoUri: 'koulutus'
          }
        },
        tutkinto: tutkintoData,
        toimipiste : oppilaitos,
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'nayttotutkintoonvalmistavakoulutus'},
        suorituskieli : suorituskieli
      }
    }
  }
  Bacon.combineWith(oppilaitosAtom, suoritustyyppiAtom, tutkintoAtom, suorituskieliAtom, suoritustapaAtom, makeSuoritus).onValue(suoritus => suoritusAtom.set(suoritus))
  return (<div>
    <Suoritustyyppi suoritustyyppiAtom={suoritustyyppiAtom} suoritustyypitP={suoritustyypitP} title="Suoritustyyppi"/>
    <TutkintoAutocomplete tutkintoAtom={tutkintoAtom} oppilaitosP={oppilaitosAtom}/>
    {
      ift(suoritustyyppiAtom.map(koodiarvoMatch('ammatillinentutkinto')), <Suoritustapa tutkintoP={tutkintoAtom} suoritustapaAtom={suoritustapaAtom}/>)
    }
  </div>)
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