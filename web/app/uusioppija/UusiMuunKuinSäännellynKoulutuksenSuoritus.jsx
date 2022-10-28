import Bacon from 'baconjs'
import React from 'baret'
import { modelProperty } from '../editor/EditorModel'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'
import { useBaconProperty } from '../util/hooks'
import Http from '../util/http'
import { koodistoValues } from './koodisto'

// TODO: Tää on kopypasta tuosta vst:n vastaavasta, siivoa tämä ja tee tästä kaunis

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
    editorPrototypeP('MuuKuinSäänneltyKoulutus')
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
  oppilaitosAtom,
  suorituskieliAtom,
  opintokokonaisuusAtom
}) => {
  const opintokokonaisuudetP = koodistoValues('opintokokonaisuudet')

  Bacon.combineWith(
    oppilaitosAtom,
    suorituskieliAtom,
    opintokokonaisuusAtom,
    makeSuoritus
  ).onValue((suoritus) => suoritusAtom.set(suoritus))

  return (
    <Opintokokonaisuus
      opintokokonaisuusAtom={opintokokonaisuusAtom}
      opintokokonaisuudetP={opintokokonaisuudetP}
    />
  )
}

const makeSuoritus = (oppilaitos, suorituskieli, opintokokonaisuus) => {
  return {
    suorituskieli,
    koulutusmoduuli: {
      tunniste: {
        koodiarvo: '999951',
        koodistoUri: 'koulutus'
      },
      opintokokonaisuus
    },
    toimipiste: oppilaitos,
    tyyppi: {
      koodiarvo: 'muukuinsaanneltykoulutus',
      koodistoUri: 'suorituksentyyppi'
    }
  }
}
