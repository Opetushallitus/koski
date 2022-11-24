import React from 'baret'
import * as L from 'partial.lenses'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import ModalDialog from '../editor/ModalDialog'
import {
  modelData,
  modelItems,
  modelLookup,
  modelSet,
  modelSetData,
  modelSetValue,
  modelSetValues
} from '../editor/EditorModel'
import {
  copyToimipiste,
  hasSuorituskieli,
  newSuoritusProto
} from '../suoritus/Suoritus'
import { suoritusPrototypeKey } from '../esh/europeanschoolofhelsinkiSuoritus'
import UusiEuropeanSchoolOfHelsinkiSuoritus from '../uusioppija/UusiEuropeanSchoolOfHelsinkiSuoritus'
import {
  eiOsasuorituksiaEshLuokkaAsteet,
  luokkaAsteenOsasuoritukset
} from '../esh/esh'
import { zeroValue } from '../editor/EnumEditor'

const fetchOsasuorituksetTemplate = (model) =>
  luokkaAsteenOsasuoritukset(
    modelData(model, 'koulutusmoduuli.tunniste.koodiarvo')
  )

const tyhjääOsasuoritustenLaajuudet = (proto) => {
  const keyValues = {}
  for (let i = 0; i < modelItems(proto, 'osasuoritukset').length; i++) {
    keyValues[`osasuoritukset.${i}.koulutusmoduuli.laajuus.arvo`] = null
  }
  return modelSetValues(proto, keyValues)
}

const tyhjääOsasuoritustenSuorituskielet = (proto) => {
  const suorituskielellisetOsasuoritukset = modelItems(
    proto,
    'osasuoritukset'
  ).filter((suoritus) => hasSuorituskieli(suoritus))

  const keyValues = {}
  suorituskielellisetOsasuoritukset.forEach((osasuoritus) => {
    keyValues[
      `osasuoritukset.${
        osasuoritus.path[osasuoritus.path.length - 1]
      }.suorituskieli`
    ] = zeroValue
  })

  return modelSetValues(proto, keyValues)
}

export const UusiEuropeanSchoolOfHelsinkiVuosiluokanSuoritus = ({
  opiskeluoikeus,
  resultCallback
}) => {
  const suoritusAtom = Atom()
  const oppilaitosAtom = Atom(modelData(opiskeluoikeus, 'oppilaitos'))
  const lisääSuoritus = () => {
    const suoritus = suoritusAtom.get()
    let proto = newSuoritusProto(
      opiskeluoikeus,
      suoritusPrototypeKey(suoritus.tyyppi.koodiarvo)
    )
    proto = withKoulutusmoduulinTunniste(
      proto,
      suoritus.koulutusmoduuli.tunniste
    )

    proto = copyToimipiste(modelLookup(opiskeluoikeus, 'suoritukset.0'), proto)

    if (
      !eiOsasuorituksiaEshLuokkaAsteet.includes(
        suoritus.koulutusmoduuli.tunniste.koodiarvo
      )
    ) {
      fetchOsasuorituksetTemplate(proto).onValue((osasuorituksetTemplate) => {
        proto = copyOsasuoritukset(osasuorituksetTemplate.value, proto)
        proto = tyhjääOsasuoritustenLaajuudet(proto)
        proto = tyhjääOsasuoritustenSuorituskielet(proto)
        resultCallback(proto)
      })
    } else {
      resultCallback(proto)
    }
  }

  return (
    <div>
      <ModalDialog
        className="lisaa-suoritus-modal"
        onDismiss={resultCallback}
        onSubmit={lisääSuoritus}
        okTextKey="Lisää"
      >
        <h2>
          <Text name="Suorituksen lisäys" />
        </h2>
        <UusiEuropeanSchoolOfHelsinkiSuoritus
          suoritusAtom={suoritusAtom}
          oppilaitosAtom={oppilaitosAtom}
        />
      </ModalDialog>
    </div>
  )
}

UusiEuropeanSchoolOfHelsinkiVuosiluokanSuoritus.canAddSuoritus = (
  opiskeluoikeus
) =>
  modelData(opiskeluoikeus, 'tyyppi.koodiarvo') === 'europeanschoolofhelsinki'

UusiEuropeanSchoolOfHelsinkiVuosiluokanSuoritus.addSuoritusTitle = () => (
  <Text name="lisää vuosiluokan suoritus" />
)

const copyOsasuoritukset = (osasuoritukset, proto) =>
  modelSetValue(proto, osasuoritukset, 'osasuoritukset')

const withKoulutusmoduulinTunniste = (suoritusProto, tunniste) => {
  let kmt = modelLookup(suoritusProto, 'koulutusmoduuli.tunniste')
  kmt = modelSetData(kmt, tunniste)
  kmt = L.set(['title'], tunniste.nimi.fi, kmt)
  return modelSet(suoritusProto, kmt, 'koulutusmoduuli.tunniste')
}
