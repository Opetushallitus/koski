import React, { useEffect } from 'react'
import { useHasOwnOrganisaatiot } from '../appstate/organisaatioHierarkia'
import { DateEdit } from '../components-v2/controls/DateField'
import { KieliSelect } from '../components-v2/controls/KieliSelect'
import { Select } from '../components-v2/controls/Select'
import { t } from '../i18n/i18n'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { koodistokoodiviiteId } from '../util/koodisto'
import { DialogMaksuttomuusSelect } from './components/DialogMaksuttomuusSelect'
import { DialogSelect } from './components/DialogSelect'
import { HankintakoulutusSelect } from './components/HankintakoulutusSelect'
import { OppilaitosSearch } from './components/OppilaitosSearch'
import { OppilaitosSelect, OrgType } from './components/OppilaitosSelect'
import {
  useDefaultKieli,
  useJotpaAsianumero,
  useOpintojenRahoitus,
  useOpiskeluoikeudenTilat,
  useOpiskeluoikeustyypit
} from './state/hooks'
import { useUusiOpiskeluoikeusDialogState } from './state/state'
import { SuoritusFields } from './suoritus/SuoritusFields'
import { prefillOsasuoritukset } from './opintooikeus/createOpiskeluoikeus'

export type UusiOpiskeluoikeusFormProps = {
  onResult: (opiskeluoikeus?: Opiskeluoikeus) => void
}

const valittavatOrganisaatiotyypit: OrgType[] = [
  'OPPILAITOS',
  'OPPISOPIMUSTOIMIPISTE',
  'VARHAISKASVATUKSEN_TOIMIPAIKKA'
]

export const UusiOpiskeluoikeusForm = (props: UusiOpiskeluoikeusFormProps) => {
  const state = useUusiOpiskeluoikeusDialogState()
  const opiskeluoikeustyypit = useOpiskeluoikeustyypit(state.oppilaitos.value)
  const tilat = useOpiskeluoikeudenTilat(state)
  const opintojenRahoitukset = useOpintojenRahoitus(state)
  const jotpaAsianumerot = useJotpaAsianumero(state)
  const defaultKieli = useDefaultKieli(state)
  const spesifienOppilaitostenKäyttäjä = useHasOwnOrganisaatiot()

  useEffect(() => {
    if (state.result) {
      prefillOsasuoritukset(state.result).then(props.onResult)
    } else {
      props.onResult(undefined)
    }
  }, [props, state.result])

  return (
    <section className="UusiOppijaForm">
      {state.oppilaitos.visible && (
        <>
          <HankintakoulutusSelect state={state} />

          <label>
            {t('Oppilaitos')}
            {state.hankintakoulutus.value || !spesifienOppilaitostenKäyttäjä ? (
              <OppilaitosSearch
                value={state.oppilaitos.value}
                onChange={state.oppilaitos.set}
                orgTypes={valittavatOrganisaatiotyypit}
                hankintakoulutus={state.hankintakoulutus.value}
              />
            ) : (
              <OppilaitosSelect
                value={state.oppilaitos.value}
                onChange={state.oppilaitos.set}
                orgTypes={valittavatOrganisaatiotyypit}
              />
            )}
          </label>
        </>
      )}

      {state.opiskeluoikeus.visible && (
        <label>
          {t('Opiskeluoikeus')}
          <Select
            inlineOptions
            options={opiskeluoikeustyypit}
            initialValue={opiskeluoikeustyypit[0]?.key}
            value={
              state.opiskeluoikeus.value &&
              koodistokoodiviiteId(state.opiskeluoikeus.value)
            }
            onChange={(opt) => state.opiskeluoikeus.set(opt?.value)}
            testId="opiskeluoikeus"
          />
        </label>
      )}

      {state.päätasonSuoritus.visible && <SuoritusFields state={state} />}

      {state.suorituskieli.visible && (
        <label>
          {t('Suorituskieli')}
          <KieliSelect
            initialValue={defaultKieli}
            value={
              state.suorituskieli.value &&
              koodistokoodiviiteId(state.suorituskieli.value)
            }
            onChange={(opt) => state.suorituskieli.set(opt?.value)}
            testId="suorituskieli"
          />
        </label>
      )}

      {state.aloituspäivä.visible && (
        <label>
          {'Aloituspäivä'}
          <DateEdit
            value={state.aloituspäivä.value}
            onChange={state.aloituspäivä.set}
          />
        </label>
      )}

      {state.tila.visible && (
        <label>
          {t('Opiskeluoikeuden tila')}
          <DialogSelect
            options={tilat.options}
            initialValue={tilat.initialValue}
            value={state.tila.value && koodistokoodiviiteId(state.tila.value)}
            onChange={(opt) => state.tila.set(opt?.value)}
            testId="tila"
          />
        </label>
      )}

      {opintojenRahoitukset.options.length > 0 && (
        <label>
          {t('Opintojen rahoitus')}
          <DialogSelect
            options={opintojenRahoitukset.options}
            initialValue={opintojenRahoitukset.initialValue}
            value={
              state.opintojenRahoitus.value &&
              koodistokoodiviiteId(state.opintojenRahoitus.value)
            }
            onChange={(opt) => state.opintojenRahoitus.set(opt?.value)}
            testId="opintojenRahoitus"
          />
        </label>
      )}

      {jotpaAsianumerot.options.length > 0 && (
        <label>
          {t('JOTPA asianumero')}
          <DialogSelect
            options={jotpaAsianumerot.options}
            initialValue={jotpaAsianumerot.initialValue}
            value={
              state.jotpaAsianumero.value &&
              koodistokoodiviiteId(state.jotpaAsianumero.value)
            }
            onChange={(opt) => state.jotpaAsianumero.set(opt?.value)}
            testId="jotpaAsianumero"
          />
        </label>
      )}

      {state.maksuton.visible && <DialogMaksuttomuusSelect state={state} />}
    </section>
  )
}
