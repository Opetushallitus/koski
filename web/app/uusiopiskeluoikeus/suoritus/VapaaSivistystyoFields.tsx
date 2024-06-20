import React from 'react'
import { Select } from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { usePäätasonSuoritustyypit } from '../state/hooks'
import { SuoritusFieldsProps } from './SuoritusFields'

export const VapaaSivistystyöFields = (props: SuoritusFieldsProps) => {
  const options = usePäätasonSuoritustyypit(props.state)
  const suoritustyyppi = props.state.päätasonSuoritus.value

  return (
    <>
      {t('Suoritustyyppi')}
      <Select
        options={options}
        value={suoritustyyppi && koodistokoodiviiteId(suoritustyyppi)}
        onChange={(opt) => props.state.päätasonSuoritus.set(opt?.value)}
        testId="suoritustyyppi"
      />

      <VstSuoritusFields {...props} />
    </>
  )
}

const VstSuoritusFields = (props: SuoritusFieldsProps) => {
  switch (props.state.päätasonSuoritus.value?.koodiarvo) {
    case 'vstmaahanmuuttajienkotoutumiskoulutus':
      return <VstKotoFields {...props} />
    case 'vstoppivelvollisillesuunnattukoulutus':
      return <VstOvlFields {...props} />
    case 'vstjotpakoulutus':
      return <VstJotpaFields {...props} />
    case 'vstlukutaitokoulutus':
      return <VstLukutaitoFields {...props} />
    case 'vstosaamismerkki':
      return <VstOsaamismerkkiFields {...props} />
    case 'vstvapaatavoitteinenkoulutus':
      return <VstVapaatavoitteinenFields {...props} />
    default:
      return null
  }
}

const VstKotoFields = (props: SuoritusFieldsProps) => {
  return <DialogPerusteSelect state={props.state} default="OPH-649-2022" />
}

const VstOvlFields = (props: SuoritusFieldsProps) => {
  return <DialogPerusteSelect state={props.state} />
}

const VstJotpaFields = (props: SuoritusFieldsProps) => {
  return (
    <>
      {t('Opintokokonaisuus')}
      <DialogKoodistoSelect
        state={props.state.opintokokonaisuus}
        koodistoUri="opintokokonaisuudet"
        testId="opintokokonaisuus"
      />
    </>
  )
}

const VstLukutaitoFields = (props: SuoritusFieldsProps) => {
  return <DialogPerusteSelect state={props.state} />
}

const VstOsaamismerkkiFields = (props: SuoritusFieldsProps) => {
  return (
    <>
      {t('Osaamismerkki')}
      <DialogKoodistoSelect
        state={props.state.osaamismerkki}
        koodistoUri="osaamismerkit"
        testId="osaamismerkki"
      />
    </>
  )
}

const VstVapaatavoitteinenFields = (props: SuoritusFieldsProps) => {
  return (
    <>
      {t('Opintokokonaisuus')}
      <DialogKoodistoSelect
        state={props.state.opintokokonaisuus}
        koodistoUri="opintokokonaisuudet"
        testId="opintokokonaisuus"
      />
    </>
  )
}
