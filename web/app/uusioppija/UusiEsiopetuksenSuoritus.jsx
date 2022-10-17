import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import { makeSuoritus } from './esiopetuksenSuoritus'
import Peruste from './Peruste'

export default ({
  suoritusAtom,
  oppilaitosAtom,
  organisaatiotyypitAtom,
  suorituskieliAtom
}) => {
  const perusteAtom = Atom('102/011/2014') // Valitaan oletuksena eräs vanhempi peruste, joka on yleisempi.

  Bacon.combineWith(
    oppilaitosAtom,
    organisaatiotyypitAtom,
    perusteAtom,
    suorituskieliAtom,
    makeSuoritus
  ).onValue((suoritus) => suoritusAtom.set(suoritus))

  const suoritusP = Bacon.combineWith(
    oppilaitosAtom,
    organisaatiotyypitAtom,
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
