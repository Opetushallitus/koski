import { flow } from 'fp-ts/lib/function'
import React, { useMemo } from 'react'
import { useSchema } from '../../appstate/constraints'
import { useKoodisto } from '../../appstate/koodisto'
import { groupKoodistoToOptions } from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import * as C from '../../util/constraints'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { DialogSelect } from '../components/DialogSelect'
import { SuoritusFieldsProps } from '../suoritus/SuoritusFields'

export type YleissivistäväOppiaineSelectProps = SuoritusFieldsProps & {
  koulutusmoduuliClassName: string
}

export const YleissivistäväOppiaineSelect = (
  props: YleissivistäväOppiaineSelectProps
) => {
  const oppiaineet = useOppiaineOptions(props.koulutusmoduuliClassName)

  return (
    <>
      <label>
        {t('Oppiaine')}
        <DialogSelect
          options={oppiaineet}
          value={
            props.state.oppiaine.value &&
            koodistokoodiviiteId(props.state.oppiaine.value)
          }
          onChange={(opt) => props.state.oppiaine.set(opt?.value)}
          testId="oppiaine"
        />
      </label>

      {props.state.kieliaineenKieli.visible && (
        <label>
          {t('Kieli')}
          <DialogKoodistoSelect
            state={props.state.kieliaineenKieli}
            koodistoUri="kielivalikoima"
            testId="kieliaineenKieli"
          />
        </label>
      )}

      {props.state.äidinkielenKieli.visible && (
        <label>
          {t('Kieli')}
          <DialogKoodistoSelect
            state={props.state.äidinkielenKieli}
            koodistoUri="oppiaineaidinkielijakirjallisuus"
            testId="kieliaineenKieli"
          />
        </label>
      )}
    </>
  )
}
const parseOppiainekoodit = flow(
  C.asList,
  C.path('tunniste.koodiarvo'),
  C.allAllowedStrings
)
const useOppiaineOptions = (koulutusmoduuliClassName: string) => {
  const schema = useSchema(koulutusmoduuliClassName)

  const koodit = useMemo(() => parseOppiainekoodit(schema), [schema])

  const koodisto = useKoodisto(
    koodit && 'koskioppiaineetyleissivistava',
    koodit
  )

  const options = useMemo(
    () => (koodisto ? groupKoodistoToOptions(koodisto) : []),
    [koodisto]
  )

  return options
}
