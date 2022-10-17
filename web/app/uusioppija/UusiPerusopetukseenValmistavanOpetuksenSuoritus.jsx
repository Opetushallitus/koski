import React from 'baret'
import Atom from 'bacon.atom'
import Bacon from 'baconjs'
import Peruste from './Peruste'

export default ({ suoritusAtom, oppilaitosAtom, suorituskieliAtom }) => {
  const perusteAtom = Atom()
  const makeSuoritus = (oppilaitos, peruste, suorituskieli) => {
    if (oppilaitos) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999905',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        toimipiste: oppilaitos,
        tyyppi: {
          koodistoUri: 'suorituksentyyppi',
          koodiarvo: 'perusopetukseenvalmistavaopetus'
        },
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
