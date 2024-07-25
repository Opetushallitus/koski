import { flow } from 'fp-ts/lib/function'
import React, { useMemo } from 'react'
import { useSchema } from '../../appstate/constraints'
import { useKoodisto } from '../../appstate/koodisto'
import { groupKoodistoToOptions } from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { NuortenPerusopetuksenOppiaineenOppimääränSuoritus } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOppiaineenOppimaaranSuoritus'
import * as C from '../../util/constraints'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { DialogSelect } from '../components/DialogSelect'
import { SuoritusFieldsProps } from './SuoritusFields'

const piilotettavatPtsTyypit = ['perusopetuksenvuosiluokka']

export const PerusopetusFields = (props: SuoritusFieldsProps) => {
  return (
    <>
      <label>
        {t('Oppimäärä')}
        <DialogPäätasonSuoritusSelect
          state={props.state}
          default="perusopetuksenoppimaara"
          hiddenOptions={piilotettavatPtsTyypit}
          testId="oppimäärä"
        />
      </label>

      {props.state.oppiaine.visible && (
        <PerusopetuksenOppiaineSelect
          state={props.state}
          koulutusmoduuliClassName="NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine"
        />
      )}

      <DialogPerusteSelect state={props.state} default="104/011/2014" />
    </>
  )
}

export type PerusopetuksenOppiaineSelectProps = SuoritusFieldsProps & {
  koulutusmoduuliClassName: string
}

export const PerusopetuksenOppiaineSelect = (
  props: PerusopetuksenOppiaineSelectProps
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
