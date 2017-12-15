import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import TutkintoAutocomplete from '../virkailija/TutkintoAutocomplete'
import {ift} from '../util/util'
import Suoritustyyppi from './Suoritustyyppi'
import {koodiarvoMatch, koodistoValues} from './koodisto'
import SuoritustapaDropdown from './SuoritustapaDropdown'
import Text from '../i18n/Text'
import {setPeruste} from '../suoritus/PerusteDropdown'

export default ({suoritusAtom, oppilaitosAtom, suorituskieliAtom}) => {
  const suoritustyypitP = koodistoValues('suorituksentyyppi/ammatillinentutkinto,nayttotutkintoonvalmistavakoulutus,ammatillinentutkintoosittainen,valma,telma')
  const tutkintoAtom = Atom()
  const suoritustyyppiAtom = Atom()
  const suoritustapaAtom = Atom()
  const perusteAtom = Atom()
  suoritustyypitP.onValue(tyypit => {
    suoritustyyppiAtom.set(tyypit.find(koodiarvoMatch('ammatillinentutkinto', 'ammatillinentutkintoosittainen', 'valma', 'telma')))
    setPeruste(perusteAtom, tyypit.find(koodiarvoMatch('valma', 'telma')))
  })
  oppilaitosAtom.changes().onValue(() => tutkintoAtom.set(undefined))

  const makeSuoritus = (oppilaitos, suoritustyyppi, tutkinto, suorituskieli, suoritustapa, peruste) => {
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
    if (koodiarvoMatch('ammatillinentutkintoosittainen')(suoritustyyppi) && tutkinto && oppilaitos && suoritustapa) {
      return {
        koulutusmoduuli: tutkintoData,
        toimipiste : oppilaitos,
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'ammatillinentutkintoosittainen'},
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
    if (koodiarvoMatch('valma')(suoritustyyppi) && oppilaitos && peruste) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999901',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        toimipiste : oppilaitos,
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'valma'},
        suorituskieli : suorituskieli
      }
    }
    if (koodiarvoMatch('telma')(suoritustyyppi) && oppilaitos && peruste) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999903',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        toimipiste : oppilaitos,
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'telma'},
        suorituskieli : suorituskieli
      }
    }
  }

  Bacon.combineWith(oppilaitosAtom, suoritustyyppiAtom, tutkintoAtom, suorituskieliAtom, suoritustapaAtom, perusteAtom, makeSuoritus).onValue(suoritus => suoritusAtom.set(suoritus))
  return (<div>
    <Suoritustyyppi suoritustyyppiAtom={suoritustyyppiAtom} suoritustyypitP={suoritustyypitP} title="Suoritustyyppi"/>

    <div className="tutkinto-autocomplete">
      {
        ift(oppilaitosAtom.and(suoritustyyppiAtom.map(koodiarvoMatch('valma', 'telma')).not()), <TutkintoAutocomplete tutkintoAtom={tutkintoAtom} oppilaitosP={oppilaitosAtom} title={<Text name="Tutkinto"/>}/>)
      }
    </div>

    {
      ift(suoritustyyppiAtom.map(koodiarvoMatch('ammatillinentutkinto')), <SuoritustapaDropdown diaarinumero={tutkintoAtom.map('.diaarinumero')} suoritustapaAtom={suoritustapaAtom} title="Suoritustapa"/>)
    }
  </div>)
}