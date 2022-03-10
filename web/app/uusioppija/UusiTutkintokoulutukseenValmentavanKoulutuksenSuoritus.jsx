import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Peruste from './Peruste'

export const UusiTutkintokoulutukseenValmentavanKoulutuksenSuoritus = ({suoritusAtom, oppilaitosAtom, suorituskieliAtom}) => {
  const perusteAtom = Atom()

  const suoritusP = Bacon.combineWith(oppilaitosAtom, perusteAtom, suorituskieliAtom, makeSuoritus)
  suoritusP.onValue(suoritus => suoritusAtom.set(suoritus))

  return (
    <div>
      <Peruste {...{suoritusTyyppiP: suoritusP.map('.tyyppi'), perusteAtom}} />
    </div>
  )
}

const makeSuoritus = (oppilaitos, peruste, suorituskieli) => {
  if (oppilaitos) {
    return (
      {
        suorituskieli: suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999908',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        toimipiste: oppilaitos,
        tyyppi: {
          koodiarvo: 'tuvakoulutuksensuoritus',
          koodistoUri: 'suorituksentyyppi'
        }
      }
    )
  }
}

export const getTuvaLisätiedot = (tuvaJärjestämislupa) => {
  if(!tuvaJärjestämislupa) return {}

  switch(tuvaJärjestämislupa.koodiarvo){
    case 'ammatillinen': return {lisätiedot: {koulutusvienti : false, pidennettyPäättymispäivä: false}}
    case 'lukio': return {lisätiedot: {pidennettyPäättymispäivä: false}}
    case 'perusopetus': return {lisätiedot: {pidennettyPäättymispäivä: false}}
    default: return {}
  }
}
