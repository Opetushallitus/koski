import React from 'baret'
import Text from '../i18n/Text'
import { lang } from '../i18n/i18n'
import {
  modelItems,
  modelLookup,
  modelSet,
  modelSetTitle,
  modelSetValue
} from '../editor/EditorModel'
import { toKoodistoEnumValue } from '../koodisto/koodistot'
import { koodistoValues } from '../uusioppija/koodisto'
import { isKorkeakouluOpintosuoritus } from './TutkinnonOsa'

export const AMMATILLISET_TUTKINNON_OSAT = '1'

export const LisääKorkeakouluopintoSuoritus = ({
  parentSuoritus,
  suoritusPrototypes,
  groupTitles,
  groupId,
  addSuoritus
}) => {
  const alreadyAdded = modelItems(parentSuoritus, 'osasuoritukset').some(
    isKorkeakouluOpintosuoritus
  )
  const suoritusPrototype = suoritusPrototypes.find(isKorkeakouluOpintosuoritus)

  const add = () =>
    koodistoNimi('tutkinnonosatvalinnanmahdollisuus/2').onValue(
      (suorituksenNimi) => {
        addSuoritus(
          createSuoritusWithTutkinnonOsanRyhmä(
            suoritusPrototype,
            groupTitles[AMMATILLISET_TUTKINNON_OSAT],
            suorituksenNimi
          )
        )
      }
    )

  return alreadyAdded ||
    !suoritusPrototype ||
    groupId !== AMMATILLISET_TUTKINNON_OSAT ? null : (
    <div className="korkeakouluopinto">
      <a className="add-link" onClick={add}>
        <Text name="Lisää korkeakouluopintoja" />
      </a>
    </div>
  )
}

export const koodistoNimi = (koodisto) =>
  koodistoValues(koodisto)
    .map('.0')
    .map((x) => x.nimi[lang] || x.nimi.fi)

export const createSuoritusWithTutkinnonOsanRyhmä = (
  suoritusPrototype,
  tutkinnonOsanRyhmänNimi,
  suorituksenNimi
) => {
  const tutkinnonOsanRyhmä = toKoodistoEnumValue(
    'ammatillisentutkinnonosanryhma',
    AMMATILLISET_TUTKINNON_OSAT,
    tutkinnonOsanRyhmänNimi
  )
  const suoritus = modelSetValue(
    suoritusPrototype,
    tutkinnonOsanRyhmä,
    'tutkinnonOsanRyhmä'
  )
  return modelSet(
    suoritus,
    modelSetTitle(modelLookup(suoritus, 'koulutusmoduuli'), suorituksenNimi),
    'koulutusmoduuli'
  )
}
