import * as A from 'fp-ts/Array'
import * as Eq from 'fp-ts/Eq'
import * as string from 'fp-ts/string'
import React, { useMemo } from 'react'
import { usePerusteSelectOptions } from '../../appstate/peruste'
import { Select, SelectOption } from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { usePäätasonSuoritustyypit } from '../UusiOpiskeluoikeusForm'
import { UusiOpiskeluoikeusDialogState } from '../state'
import { SuoritusFieldsProps } from './SuoritusFields'
import { DialogPerusteSelect } from '../DialogPerusteSelect'
import { DialogKoodistoSelect } from '../DialogKoodistoSelect'

export const VapaaSivistystyöFields = (props: SuoritusFieldsProps) => {
  const options = useVstSuoritustyypit(props.state)
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

const SelectOptionKeyEq = <O extends SelectOption<any>>() =>
  Eq.contramap((o: O) => o.key)(string.Eq)

const distinctKeys = <O extends SelectOption<any>>(os: O[]) =>
  A.uniq(SelectOptionKeyEq<O>())(os)

const useVstSuoritustyypit = (state: UusiOpiskeluoikeusDialogState) => {
  const options = usePäätasonSuoritustyypit(state)
  return useMemo(() => distinctKeys(options), [options])
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
