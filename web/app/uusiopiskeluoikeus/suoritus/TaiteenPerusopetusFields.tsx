import React, { useEffect, useMemo } from 'react'
import { useKoodisto } from '../../appstate/koodisto'
import {
  Select,
  groupKoodistoToOptions
} from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { DialogKoodistoSelect } from '../DialogKoodistoSelect'
import { DialogPerusteSelect } from '../DialogPerusteSelect'
import { usePäätasonSuoritustyypit } from '../UusiOpiskeluoikeusForm'
import { UusiOpiskeluoikeusDialogState } from '../state'
import { SuoritusFieldsProps } from './SuoritusFields'

export const TaiteenPerusopetusFields = (props: SuoritusFieldsProps) => {
  const { suoritustyypit, perusteenDiaarinumero } =
    useTpoSuorituksetJaPerusteenDiaarinumero(props.state)

  const hankintakoulutus = props.state.hankintakoulutus.value === 'tpo'

  const toteutustavat = useKoodisto('taiteenperusopetuskoulutuksentoteutustapa')
  const toteutustavatOptions = useMemo(
    () => (toteutustavat ? groupKoodistoToOptions(toteutustavat) : []),
    [toteutustavat]
  )
  useEffect(() => {
    const koodiarvo = hankintakoulutus
      ? 'hankintakoulutus'
      : 'itsejarjestettykoulutus'

    const toteutustapa = toteutustavat
      ?.map((k) => k.koodiviite)
      .find((k) => k.koodiarvo === koodiarvo)

    props.state.tpoToteutustapa.set(toteutustapa)
  }, [hankintakoulutus, props.state.tpoToteutustapa, toteutustavat])

  return (
    <>
      {t('Oppimäärä')}
      <DialogKoodistoSelect
        state={props.state.tpoOppimäärä}
        koodistoUri="taiteenperusopetusoppimaara"
        default="yleinenoppimaara"
        testId="oppimäärä"
      />

      {props.state.tpoOppimäärä.value && (
        <>
          {t('Suoritustyyppi')}
          <Select
            options={suoritustyypit}
            value={
              props.state.päätasonSuoritus.value &&
              koodistokoodiviiteId(props.state.päätasonSuoritus.value)
            }
            onChange={(opt) => props.state.päätasonSuoritus.set(opt?.value)}
            testId="suoritustyyppi"
          />
        </>
      )}

      {props.state.tpoToteutustapa.value && (
        <>
          {t('Koulutuksen toteutustapa')}
          <Select
            options={toteutustavatOptions}
            value={
              props.state.tpoToteutustapa.value &&
              koodistokoodiviiteId(props.state.tpoToteutustapa.value)
            }
            onChange={() => {}}
            disabled
            testId="toteutustapa"
          />
        </>
      )}

      <DialogPerusteSelect
        state={props.state}
        default={perusteenDiaarinumero}
      />

      {t('Taiteenala')}
      <DialogKoodistoSelect
        state={props.state.tpoTaiteenala}
        koodistoUri="taiteenperusopetustaiteenala"
        testId="taiteenala"
      />
    </>
  )
}

type TpoOppimäärä = {
  suoritustyypit: string[]
  perusteenDiaarinumero: string
}

const tpoSuoritustyypit: Record<string, TpoOppimäärä> = {
  yleinenoppimaara: {
    suoritustyypit: [
      'suorituksentyyppi_taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot',
      'suorituksentyyppi_taiteenperusopetuksenyleisenoppimaaranteemaopinnot'
    ],
    perusteenDiaarinumero: 'OPH-2069-2017'
  },
  laajaoppimaara: {
    suoritustyypit: [
      'suorituksentyyppi_taiteenperusopetuksenlaajanoppimaaranperusopinnot',
      'suorituksentyyppi_taiteenperusopetuksenlaajanoppimaaransyventavatopinnot'
    ],
    perusteenDiaarinumero: 'OPH-2068-2017'
  }
}

const useTpoSuorituksetJaPerusteenDiaarinumero = (
  state: UusiOpiskeluoikeusDialogState
) => {
  const suoritustyypit = usePäätasonSuoritustyypit(state)
  const result = useMemo(() => {
    const oppimäärä = state.tpoOppimäärä.value?.koodiarvo as
      | keyof typeof tpoSuoritustyypit
      | undefined
    const op = oppimäärä ? tpoSuoritustyypit[oppimäärä] : undefined

    return {
      suoritustyypit: suoritustyypit.filter((st) =>
        op?.suoritustyypit.includes(st.key)
      ),
      perusteenDiaarinumero: op?.perusteenDiaarinumero
    }
  }, [state.tpoOppimäärä.value?.koodiarvo, suoritustyypit])

  const peruste = useMemo(
    () =>
      result.perusteenDiaarinumero
        ? { koodiarvo: result.perusteenDiaarinumero, koodistoUri: 'peruste' }
        : undefined,
    [result.perusteenDiaarinumero]
  )

  useEffect(() => {
    state.peruste.set(peruste)
  }, [peruste, state.peruste])

  return result
}
