import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {koodiarvoMatch, koodistoValues} from './koodisto'
import {makeSuoritus} from './vapaanSivistystyönSuoritus'
import Suoritustyyppi from './Suoritustyyppi'
import Peruste from './Peruste'

export default ({suoritusAtom, oppilaitosAtom, suorituskieliAtom}) => {
  const suoritustyyppiAtom = Atom()
  const perusteAtom = Atom()

  const suoritustyypitP = koodistoValues('suorituksentyyppi/vstoppivelvollisillesuunnattukoulutus,KOTOhaaraTODO')
  suoritustyypitP.onValue(tyypit => suoritustyyppiAtom.set(tyypit.find(koodiarvoMatch('vstoppivelvollisillesuunnattukoulutus'))))

  const suorituskieletP = koodistoValues('kieli/FI')
  suorituskieletP.onValue(kielet => suorituskieliAtom.set(kielet.find(koodiarvoMatch('FI'))))

  Bacon.combineWith(
    oppilaitosAtom, suoritustyyppiAtom, perusteAtom, suorituskieliAtom,
    makeSuoritus
  ).onValue(suoritus => suoritusAtom.set(suoritus))

  return (<div>
      <Peruste {...{suoritusTyyppiP: suoritustyyppiAtom, perusteAtom}} />
      <Suoritustyyppi suoritustyyppiAtom={suoritustyyppiAtom} suoritustyypitP={suoritustyypitP} title="Suoritustyyppi"/>
    </div>
  )
}
