import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import { koodiarvoMatch, koodistoValues } from './koodisto'
import { makeSuoritus } from './ibSuoritus'
import Suoritustyyppi from './Suoritustyyppi'

export default ({ suoritusAtom, oppilaitosAtom, suorituskieliAtom }) => {
  const suoritustyyppiAtom = Atom()

  const suoritustyypitP = koodistoValues(
    'suorituksentyyppi/ibtutkinto,preiboppimaara'
  )
  suoritustyypitP.onValue((tyypit) =>
    suoritustyyppiAtom.set(tyypit.find(koodiarvoMatch('ibtutkinto')))
  )

  Bacon.combineWith(
    oppilaitosAtom,
    suoritustyyppiAtom,
    suorituskieliAtom,
    makeSuoritus
  ).onValue((suoritus) => suoritusAtom.set(suoritus))

  return (
    <Suoritustyyppi
      suoritustyyppiAtom={suoritustyyppiAtom}
      suoritustyypitP={suoritustyypitP}
      title="Oppimäärä"
    />
  )
}
