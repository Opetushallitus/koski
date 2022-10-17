import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Peruste from './Peruste'

import { perusteenDiaarinumeroToOppimäärä } from '../lukio/lukio'

export default ({ suoritusAtom, oppilaitosAtom, suorituskieliAtom }) => {
  const perusteAtom = Atom()
  const makeSuoritus = (oppilaitos, peruste, suorituskieli) => {
    if (oppilaitos) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999906',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        oppimäärä: {
          koodiarvo: perusteenDiaarinumeroToOppimäärä(peruste),
          koodistoUri: 'lukionoppimaara'
        },
        toimipiste: oppilaitos,
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'luva' },
        suorituskieli
      }
    }
  }
  const suoritusP = Bacon.combineWith(
    oppilaitosAtom,
    perusteAtom,
    suorituskieliAtom,
    makeSuoritus
  )
  suoritusP
    .filter('.koulutusmoduuli.perusteenDiaarinumero')
    .onValue((suoritus) => suoritusAtom.set(suoritus))
  return (
    <Peruste {...{ suoritusTyyppiP: suoritusP.map('.tyyppi'), perusteAtom }} />
  )
}
