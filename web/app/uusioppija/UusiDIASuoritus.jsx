import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import { koodiarvoMatch, koodistoValues } from './koodisto'
import { makeSuoritus } from './diaSuoritus'
import Suoritustyyppi from './Suoritustyyppi'

export default ({ suoritusAtom, oppilaitosAtom, suorituskieliAtom }) => {
  const suoritustyyppiAtom = Atom()

  const suoritustyypitP = koodistoValues(
    'suorituksentyyppi/diavalmistavavaihe,diatutkintovaihe'
  )
  suoritustyypitP.onValue((tyypit) =>
    suoritustyyppiAtom.set(tyypit.find(koodiarvoMatch('diatutkintovaihe')))
  )

  const suorituskieletP = koodistoValues('kieli/DE')
  suorituskieletP.onValue((kielet) =>
    suorituskieliAtom.set(kielet.find(koodiarvoMatch('DE')))
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
