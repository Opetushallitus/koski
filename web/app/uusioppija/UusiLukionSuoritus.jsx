import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from '../util/http'
import { koodiarvoMatch, koodistoValues } from './koodisto'
import { makeSuoritus } from './lukionSuoritus'
import Suoritustyyppi from './Suoritustyyppi'
import Peruste from './Peruste'
import Oppiaine from './Oppiaine'

export default ({ suoritusAtom, oppilaitosAtom, suorituskieliAtom }) => {
  const suoritustyyppiAtom = Atom()
  const oppiaineenSuoritusAtom = Atom()
  const perusteAtom = Atom()

  const suoritustyypitP = koodistoValues(
    'suorituksentyyppi/lukionoppimaara,lukionoppiaineenoppimaara,lukionaineopinnot'
  )
  suoritustyypitP.onValue((tyypit) =>
    suoritustyyppiAtom.set(tyypit.find(koodiarvoMatch('lukionoppimaara')))
  )

  const suoritusPrototypeP = suoritustyyppiAtom
    .map('.koodiarvo')
    .flatMap((suorituksenTyyppi) => {
      if (suorituksenTyyppi == 'lukionoppiaineenoppimaara') {
        return Http.cachedGet(
          '/koski/api/editor/prototype/fi.oph.koski.schema.LukionOppiaineenOppimääränSuoritus2015'
        )
      }
    })
    .toProperty()

  Bacon.combineWith(
    oppilaitosAtom,
    suoritustyyppiAtom,
    perusteAtom,
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
        koodiarvoMatch('lukionoppiaineenoppimaara')(tyyppi) ? (
          <Oppiaine
            suoritusPrototypeP={suoritusPrototypeP}
            oppiaineenSuoritusAtom={oppiaineenSuoritusAtom}
            perusteAtom={perusteAtom}
            oppilaitos={oppilaitosAtom}
            suorituskieli={suorituskieliAtom}
          />
        ) : (
          <Peruste {...{ suoritusTyyppiP: suoritustyyppiAtom, perusteAtom }} />
        )
      )}
    </span>
  )
}
