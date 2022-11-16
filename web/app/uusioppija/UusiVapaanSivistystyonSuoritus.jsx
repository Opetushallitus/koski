import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Peruste from './Peruste'
import Suoritustyyppi from './Suoritustyyppi'
import { koodistoValues } from './koodisto'
import { ift } from '../util/util'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'
import { useBaconProperty } from '../util/hooks'
import { modelProperty } from '../editor/EditorModel'
import { editorPrototypeP } from '../util/properties'

const toInfoProperty = (optionalPrototype) => ({
  model: {
    ...optionalPrototype
  }
})

const Opintokokonaisuus = ({ opintokokonaisuusAtom, opintokokonaisuudetP }) => {
  const editorPrototypeValue = useBaconProperty(
    editorPrototypeP('VapaanSivistystyönVapaatavoitteinenKoulutus')
  )
  if (!editorPrototypeValue) {
    return null
  }
  const opintokokonaisuusProperty = modelProperty(
    editorPrototypeValue,
    'opintokokonaisuus'
  )
  return (
    <div>
      <KoodistoDropdown
        enableFilter={true}
        className="opintokokonaisuus"
        showKoodiarvo
        title="Opintokokonaisuus"
        options={opintokokonaisuudetP}
        selected={opintokokonaisuusAtom}
        property={toInfoProperty(
          opintokokonaisuusProperty.model.optionalPrototype
        )}
      />
    </div>
  )
}

export const opintokokonaisuudellisetVstSuoritustyypit = [
  'vstvapaatavoitteinenkoulutus',
  'vstjotpakoulutus'
]

export default ({
  suoritusAtom,
  suoritustyyppiAtom,
  oppilaitosAtom,
  suorituskieliAtom,
  opintokokonaisuusAtom
}) => {
  const perusteAtom = Atom()
  const näytäPerustekenttä = Atom(false)

  const suoritustyypitP = koodistoValues(
    'suorituksentyyppi/vstoppivelvollisillesuunnattukoulutus,vstmaahanmuuttajienkotoutumiskoulutus,vstlukutaitokoulutus,vstvapaatavoitteinenkoulutus,vstjotpakoulutus'
  )
  const opintokokonaisuudetP = koodistoValues('opintokokonaisuudet')

  suoritustyyppiAtom.onValue((tyyppi) => {
    näytäPerustekenttä.set(
      tyyppi &&
        !opintokokonaisuudellisetVstSuoritustyypit.includes(tyyppi.koodiarvo)
    )
  })

  Bacon.combineWith(
    oppilaitosAtom,
    suoritustyyppiAtom,
    perusteAtom,
    suorituskieliAtom,
    opintokokonaisuusAtom,
    makeSuoritus
  ).onValue((suoritus) => suoritusAtom.set(suoritus))

  return (
    <div>
      <Suoritustyyppi
        suoritustyyppiAtom={suoritustyyppiAtom}
        suoritustyypitP={suoritustyypitP}
        title="Suoritustyyppi"
      />
      {suoritustyyppiAtom.map('.koodiarvo').map((tyyppi) => {
        if (opintokokonaisuudellisetVstSuoritustyypit.includes(tyyppi))
          return (
            <Opintokokonaisuus
              opintokokonaisuusAtom={opintokokonaisuusAtom}
              opintokokonaisuudetP={opintokokonaisuudetP}
            />
          )
        return null
      })}
      {ift(
        näytäPerustekenttä,
        <Peruste {...{ suoritusTyyppiP: suoritustyyppiAtom, perusteAtom }} />
      )}
    </div>
  )
}

const makeSuoritus = (
  oppilaitos,
  suoritustyyppi,
  peruste,
  suorituskieli,
  opintokokonaisuus
) => {
  switch (suoritustyyppi?.koodiarvo) {
    case 'vstoppivelvollisillesuunnattukoulutus':
      return {
        suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999909',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        toimipiste: oppilaitos,
        tyyppi: suoritustyyppi
      }
    case 'vstmaahanmuuttajienkotoutumiskoulutus':
      return {
        suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999910',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        toimipiste: oppilaitos,
        tyyppi: suoritustyyppi
      }
    case 'vstlukutaitokoulutus':
      return {
        suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999911',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        toimipiste: oppilaitos,
        tyyppi: suoritustyyppi
      }
    case 'vstvapaatavoitteinenkoulutus':
    case 'vstjotpakoulutus':
      return {
        suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '099999',
            koodistoUri: 'koulutus'
          },
          opintokokonaisuus
        },
        toimipiste: oppilaitos,
        tyyppi: suoritustyyppi
      }
    default:
      return undefined
  }
}
