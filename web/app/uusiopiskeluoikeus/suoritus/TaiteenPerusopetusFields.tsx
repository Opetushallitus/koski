import React, { useMemo } from 'react'
import { usePerusteSelectOptions } from '../../appstate/peruste'
import {
  Select,
  groupKoodistoToOptions
} from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { usePäätasonSuoritustyypit } from '../UusiOpiskeluoikeusForm'
import { SuoritusFieldsProps } from './SuoritusFields'
import { useKoodisto } from '../../appstate/koodisto'

const tpoSuoritustyypit = {
  taiteenperusopetusoppimaara_yleinenoppimaara: [
    'suorituksentyyppi_taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot',
    'suorituksentyyppi_taiteenperusopetuksenyleisenoppimaaranteemaopinnot'
  ],
  taiteenperusopetusoppimaara_laajaoppimaara: [
    'suorituksentyyppi_taiteenperusopetuksenlaajanoppimaaranperusopinnot',
    'suorituksentyyppi_taiteenperusopetuksenlaajanoppimaaransyventavatopinnot'
  ]
}

export const TaiteenPerusopetusFields = (props: SuoritusFieldsProps) => {
  const oppimäärät = useKoodisto('taiteenperusopetusoppimaara')
  const oppimääräOptions = useMemo(
    () => (oppimäärät ? groupKoodistoToOptions(oppimäärät) : []),
    [oppimäärät]
  )

  const suoritustyypit = usePäätasonSuoritustyypit(props.state)
  const filteredSuoritustyypit = useMemo(() => {
    const oppimäärä = props.state.tpoOppimäärä.value?.koodiarvo as
      | keyof typeof tpoSuoritustyypit
      | undefined
    const tyypit = (oppimäärä && tpoSuoritustyypit[oppimäärä]) || []
    return suoritustyypit.filter((st) => tyypit.includes(st.key))
  }, [props.state.tpoOppimäärä.value?.koodiarvo, suoritustyypit])

  const perusteOptions = usePerusteSelectOptions(
    props.state.päätasonSuoritus.value?.koodiarvo
  )

  return (
    <>
      {t('Oppimäärä')}
      <Select
        options={oppimääräOptions}
        initialValue="taiteenperusopetusoppimaara_yleinenoppimaara"
        value={
          props.state.tpoOppimäärä.value &&
          koodistokoodiviiteId(props.state.tpoOppimäärä.value)
        }
        onChange={(opt) => props.state.tpoOppimäärä.set(opt?.value)}
        testId="oppimäärä"
      />

      {/* TODO: Koulutuksen toteutustapa */}

      {t('Suoritustyyppi')}
      <Select
        options={filteredSuoritustyypit}
        value={
          props.state.päätasonSuoritus.value &&
          koodistokoodiviiteId(props.state.päätasonSuoritus.value)
        }
        onChange={(opt) => props.state.päätasonSuoritus.set(opt?.value)}
        testId="suoritustyyppi"
      />

      {props.state.peruste.visible && (
        <>
          {t('Peruste')}
          <Select
            options={perusteOptions}
            initialValue="104/011/2014"
            value={props.state.peruste.value?.koodiarvo}
            onChange={(opt) => props.state.peruste.set(opt?.value)}
            disabled={perusteOptions.length < 2}
            testId="peruste"
          />
        </>
      )}

      {/* TODO: Taiteenala */}
    </>
  )
}
