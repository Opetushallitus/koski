import { flow } from 'fp-ts/lib/function'
import React, { useMemo } from 'react'
import { useSchema } from '../../appstate/constraints'
import {
  KoodistokoodiviiteKoodistonNimellä,
  KoodistokoodiviiteKoodistonNimelläOrd,
  useKoodisto
} from '../../appstate/koodisto'
import {
  SelectOption,
  groupKoodistoToOptions
} from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import * as C from '../../util/constraints'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { DialogSelect } from '../components/DialogSelect'
import { SuoritusFieldsProps } from '../opiskeluoikeusSpecificFields'
import * as A from 'fp-ts/Array'
import * as Ord from 'fp-ts/Ord'
import * as string from 'fp-ts/string'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { valuesFirst } from '../../util/array'

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
    () =>
      koodisto
        ? groupKoodistoToOptions(koodisto, [
            EiTiedossaEnsinOrd,
            KoodistokoodiviiteKoodistonNimelläOrd
          ])
        : [],
    [koodisto]
  )

  return options
}

const EiTiedossaEnsinOrd = Ord.contramap(
  (k: KoodistokoodiviiteKoodistonNimellä) => k.koodiviite.koodiarvo
)(valuesFirst('XX'))
