import Atom from 'bacon.atom'
import Bacon from 'baconjs'
import {setPeruste} from '../suoritus/PerusteDropdown'

export const VARHAISKASVATUKSEN_TOIMIPAIKKA = 'VARHAISKASVATUKSEN_TOIMIPAIKKA'

export const esiopetuksenSuoritus = (suoritusAtom, oppilaitosAtom, organisaatiotyypitAtom, suorituskieliAtom) => {
  const perusteAtom = Atom()
  const makeSuoritus = (oppilaitos, organisaatiotyypit, peruste, suorituskieli) => {
    if (oppilaitos) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: tunnisteenKoodiarvo(organisaatiotyypit),
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        toimipiste: oppilaitos,
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'esiopetuksensuoritus'},
        suorituskieli : suorituskieli
      }
    }
  }

  let suoritusP = Bacon.combineWith(oppilaitosAtom, organisaatiotyypitAtom, perusteAtom, suorituskieliAtom, makeSuoritus)
  suoritusP.map('.tyyppi').onValue(suoritustyyppi => setPeruste(perusteAtom, suoritustyyppi))
  suoritusP.filter('.koulutusmoduuli.perusteenDiaarinumero').onValue(suoritus => suoritusAtom.set(suoritus))
}

const tunnisteenKoodiarvo = organisaatioTyypit =>
  organisaatioTyypit && organisaatioTyypit.includes(VARHAISKASVATUKSEN_TOIMIPAIKKA) ? '001102' : '001101'
