import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from '../util/http'
import { koodiarvoMatch, koodistoValues } from './koodisto'
import { makeSuoritus, oppiaineetP } from './PerusopetuksenSuoritus'
import Suoritustyyppi from './Suoritustyyppi'
import Peruste from './Peruste'
import Oppiaine from './Oppiaine'

export default ({ suoritusAtom, oppilaitosAtom, suorituskieliAtom }) => {
  const suoritustyyppiAtom = Atom()
  const oppiaineenSuoritusAtom = Atom()
  const perusteAtom = Atom()
  const suoritustyypitP = koodistoValues(
    'suorituksentyyppi/aikuistenperusopetuksenoppimaara,aikuistenperusopetuksenoppimaaranalkuvaihe,perusopetuksenoppiaineenoppimaara'
  )
  suoritustyypitP.onValue((tyypit) =>
    suoritustyyppiAtom.set(
      tyypit.find(koodiarvoMatch('aikuistenperusopetuksenoppimaara'))
    )
  )

  const suoritusPrototypeP = suoritustyyppiAtom
    .map('.koodiarvo')
    .flatMap((suorituksenTyyppi) => {
      if (suorituksenTyyppi == 'perusopetuksenoppiaineenoppimaara') {
        return Http.cachedGet(
          '/koski/api/editor/prototype/fi.oph.koski.schema.PerusopetuksenOppiaineenOppimääränSuoritus'
        )
      }
    })
    .toProperty()

  Bacon.combineWith(
    oppilaitosAtom,
    suoritustyyppiAtom,
    perusteAtom,
    oppiaineetP(suoritustyyppiAtom),
    suorituskieliAtom,
    oppiaineenSuoritusAtom,
    makeSuoritus
  ).onValue((suoritus) => suoritusAtom.set(suoritus))

  return (
    <span>
      <Suoritustyyppi
        suoritustyyppiAtom={suoritustyyppiAtom}
        suoritustyypitP={suoritustyypitP}
        title="Oppimäärä"
      />
      {suoritustyyppiAtom.map((tyyppi) =>
        koodiarvoMatch(
          'perusopetuksenoppimaara',
          'aikuistenperusopetuksenoppimaara',
          'aikuistenperusopetuksenoppimaaranalkuvaihe'
        )(tyyppi) ? (
          <Peruste {...{ suoritusTyyppiP: suoritustyyppiAtom, perusteAtom }} />
        ) : (
          <Oppiaine
            suoritusPrototypeP={suoritusPrototypeP}
            oppiaineenSuoritusAtom={oppiaineenSuoritusAtom}
            perusteAtom={perusteAtom}
            oppilaitos={oppilaitosAtom}
            suorituskieli={suorituskieliAtom}
          />
        )
      )}
    </span>
  )
}
