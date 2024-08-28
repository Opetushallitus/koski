import React from 'react'
import { t } from '../../i18n/i18n'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { SuoritusFieldsProps } from '.'
import { KoodistokoodiviiteKoodistonNimellä } from '../../appstate/koodisto'

export const VapaaSivistystyöFields = (props: SuoritusFieldsProps) => (
  <>
    <label>
      {t('Suoritustyyppi')}
      <DialogPäätasonSuoritusSelect
        state={props.state}
        testId="suoritustyyppi"
      />
    </label>
    <VstSuoritusFields {...props} />
  </>
)

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
    <label>
      {t('Opintokokonaisuus')}
      <DialogKoodistoSelect
        state={props.state.opintokokonaisuus}
        koodistoUri="opintokokonaisuudet"
        testId="opintokokonaisuus"
      />
    </label>
  )
}

const VstLukutaitoFields = (props: SuoritusFieldsProps) => {
  return <DialogPerusteSelect state={props.state} />
}

const VstOsaamismerkkiFields = (props: SuoritusFieldsProps) => {
  return (
    <label>
      {t('Osaamismerkki')}
      <DialogKoodistoSelect
        state={props.state.osaamismerkki}
        koodistoUri="osaamismerkit"
        testId="osaamismerkki"
        formatLabel={formatOsaamismerkkiName}
      />
    </label>
  )
}

const formatOsaamismerkkiName = (
  koodi: KoodistokoodiviiteKoodistonNimellä
): string => `${koodi.koodiviite.koodiarvo} ${t(koodi.koodiviite.nimi)}`

const VstVapaatavoitteinenFields = (props: SuoritusFieldsProps) => {
  return (
    <label>
      {t('Opintokokonaisuus')}
      <DialogKoodistoSelect
        state={props.state.opintokokonaisuus}
        koodistoUri="opintokokonaisuudet"
        testId="opintokokonaisuus"
      />
    </label>
  )
}
