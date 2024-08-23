import React, { useEffect } from 'react'
import { t } from '../../i18n/i18n'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { SuoritusFieldsProps } from '.'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'

export const InternationalSchoolFields = (props: SuoritusFieldsProps) => {
  useEffect(() => {
    switch (props.state.internationalSchoolGrade.value?.koodiarvo) {
      case 'explorer':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
        props.state.päätasonSuoritus.set(PypVuosiluokka)
        break
      case '6':
      case '7':
      case '8':
      case '9':
      case '10':
        props.state.päätasonSuoritus.set(MypVuosiluokka)
        break
      case '11':
      case '12':
        props.state.päätasonSuoritus.set(DiplomaVuosiluokka)
        break
    }
  }, [
    props.state.internationalSchoolGrade.value?.koodiarvo,
    props.state.päätasonSuoritus
  ])

  return (
    <label>
      {t('Grade')}
      <DialogKoodistoSelect
        state={props.state.internationalSchoolGrade}
        koodistoUri="internationalschoolluokkaaste"
        testId="grade"
      />
    </label>
  )
}

const PypVuosiluokka = Koodistokoodiviite({
  koodiarvo: 'internationalschoolpypvuosiluokka',
  koodistoUri: 'suorituksentyyppi'
})

const MypVuosiluokka = Koodistokoodiviite({
  koodiarvo: 'internationalschoolmypvuosiluokka',
  koodistoUri: 'suorituksentyyppi'
})

const DiplomaVuosiluokka = Koodistokoodiviite({
  koodiarvo: 'internationalschooldiplomavuosiluokka',
  koodistoUri: 'suorituksentyyppi'
})
