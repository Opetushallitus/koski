import React from 'baret'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import ModalDialog from '../editor/ModalDialog'
import {
  modelData,
  modelItems,
  modelLookup,
  modelProperty,
  modelSet,
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
import { t } from '../i18n/i18n'

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
    // Kielioppiaine
    if (modelProperty(osasuoritus, 'koulutusmoduuli.kieli') !== undefined) {
      keyValues[
        `osasuoritukset.${
          osasuoritus.path[osasuoritus.path.length - 1]
        }.koulutusmoduuli.kieli`
      ] = zeroValue
    }
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
      suoritusPrototypeKey(
        modelData(opiskeluoikeus, 'tyyppi.koodiarvo'),
        suoritus.tyyppi.koodiarvo
      )
    )
    proto = withKoulutusmoduulinTunniste(
      proto,
      suoritus.koulutusmoduuli.tunniste
    )
    proto = withKoulutusmoduulinCurriculum(
      proto,
      suoritus.koulutusmoduuli.curriculum
    )
    if (
      suoritus.koulutusmoduuli.tunniste.koodistoUri ===
      'europeanschoolofhelsinkiluokkaaste'
    ) {
      proto = withAlkamispäivä(proto, suoritus.alkamispäivä)
    }

    proto = copyToimipiste(modelLookup(opiskeluoikeus, 'suoritukset.0'), proto)

    if (
      suoritus.koulutusmoduuli.tunniste.koodistoUri ===
        'europeanschoolofhelsinkiluokkaaste' &&
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
          näytäKoulutusValitsin={true}
          näytäAlkamispäiväValitsin={true}
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
  <Text name="lisää suoritus" />
)
UusiEuropeanSchoolOfHelsinkiVuosiluokanSuoritus.addSuoritusTitleKey =
  'lisää suoritus'

const copyOsasuoritukset = (osasuoritukset, proto) =>
  modelSetValue(proto, osasuoritukset, 'osasuoritukset')

const withKoulutusmoduulinTunniste = (suoritusProto, tunniste) => {
  let kmt = modelLookup(suoritusProto, 'koulutusmoduuli.tunniste')
  kmt = modelSetValue(kmt, {
    data: tunniste,
    value: tunniste,
    title: t(tunniste.nimi)
  })
  return modelSet(suoritusProto, kmt, 'koulutusmoduuli.tunniste')
}

const withKoulutusmoduulinCurriculum = (suoritusProto, curriculum) => {
  let a = modelLookup(suoritusProto, 'koulutusmoduuli.curriculum')
  a = modelSetValue(a, {
    data: curriculum,
    value: curriculum,
    title: t(curriculum.nimi)
  })
  return modelSet(suoritusProto, a, 'koulutusmoduuli.curriculum')
}

const withAlkamispäivä = (suoritusProto, alkamispäivä) => {
  let a = modelLookup(suoritusProto, 'alkamispäivä')
  a = modelSetValue(a, {
    data: alkamispäivä,
    value: alkamispäivä
  })
  return modelSet(suoritusProto, a, 'alkamispäivä')
}
