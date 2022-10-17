import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Peruste from './Peruste'
import Suoritustyyppi from './Suoritustyyppi'
import { koodistoValues } from './koodisto'
import { ift } from '../util/util'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'
import Http from '../util/http'
import { useBaconProperty } from '../util/hooks'
import { modelProperty } from '../editor/EditorModel'

const editorPrototypeP = (modelName) => {
  const url = `/koski/api/editor/prototype/fi.oph.koski.schema.${encodeURIComponent(
    modelName
  )}`
  return Http.cachedGet(url, {
    errorMapper: (e) => {
      switch (e.errorStatus) {
        case 404:
          return null
        case 500:
          return null
        default:
          return new Bacon.Error(e)
      }
    }
  }).toProperty()
}

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

export default ({
  suoritusAtom,
  suoritustyyppiAtom,
  oppilaitosAtom,
  suorituskieliAtom,
  opintokokonaisuusAtom
}) => {
  const perusteAtom = Atom()

  const suoritustyypitP = koodistoValues(
    'suorituksentyyppi/vstoppivelvollisillesuunnattukoulutus,vstmaahanmuuttajienkotoutumiskoulutus,vstlukutaitokoulutus,vstvapaatavoitteinenkoulutus'
  )
  const opintokokonaisuudetP = koodistoValues('opintokokonaisuudet')

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
        if (tyyppi === 'vstvapaatavoitteinenkoulutus')
          return (
            <Opintokokonaisuus
              opintokokonaisuusAtom={opintokokonaisuusAtom}
              opintokokonaisuudetP={opintokokonaisuudetP}
            />
          )
        return null
      })}
      {ift(
        suoritustyyppiAtom,
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
