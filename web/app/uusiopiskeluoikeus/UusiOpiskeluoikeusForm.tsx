import React, { useEffect } from 'react'
import { Checkbox } from '../components-v2/controls/Checkbox'
import { DateEdit } from '../components-v2/controls/DateField'
import { KieliSelect } from '../components-v2/controls/KieliSelect'
import { Select } from '../components-v2/controls/Select'
import { t } from '../i18n/i18n'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { koodistokoodiviiteId } from '../util/koodisto'
import { DialogMaksuttomuusSelect } from './DialogMaksuttomuusSelect'
import { DialogKoodistoSelect } from './components/DialogKoodistoSelect'
import { OppilaitosSearch } from './components/OppilaitosSearch'
import { OppilaitosSelect, OrgType } from './components/OppilaitosSelect'
import {
  useJotpaAsianumero,
  useOpintojenRahoitus,
  useOpiskeluoikeudenTilat,
  useOpiskeluoikeustyypit
} from './hooks'
import { useUusiOpiskeluoikeusDialogState } from './state'
import { SuoritusFields } from './suoritus/SuoritusFields'

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

  useEffect(() => props.onResult(state.result), [props, state.result])

  return (
    <section>
      {state.oppilaitos.visible && (
        <>
          {t('Hankintakoulutus')}
          {state.hankintakoulutus.value !== 'tpo' && (
            <Checkbox
              label={t(
                'Esiopetus ostetaan oman organisaation ulkopuolelta ostopalveluna tai palvelusetelinä'
              )}
              checked={state.hankintakoulutus.value === 'esiopetus'}
              onChange={(opt) =>
                state.hankintakoulutus.set(opt ? 'esiopetus' : undefined)
              }
            />
          )}
          {state.hankintakoulutus.value !== 'esiopetus' && (
            <Checkbox
              label={t(
                'Taiteen perusopetus hankintakoulutuksena järjestetään oman organisaation ulkopuolelta'
              )}
              checked={state.hankintakoulutus.value === 'tpo'}
              onChange={(opt) =>
                state.hankintakoulutus.set(opt ? 'tpo' : undefined)
              }
            />
          )}
          {state.varhaiskasvatuksenJärjestämistapa.visible && (
            <>
              {t('Varhaiskasvatuksen järjestämismuoto')}
              <DialogKoodistoSelect
                state={state.varhaiskasvatuksenJärjestämistapa}
                koodistoUri="vardajarjestamismuoto"
                koodiarvot={['JM02', 'JM03']}
                testId="varhaiskasvatuksenJärjestämismuoto"
              />
            </>
          )}

          {t('Oppilaitos')}
          {state.hankintakoulutus.value ? (
            <OppilaitosSearch
              value={state.oppilaitos.value}
              onChange={state.oppilaitos.set}
              orgTypes={valittavatOrganisaatiotyypit}
            />
          ) : (
            <OppilaitosSelect
              value={state.oppilaitos.value}
              onChange={state.oppilaitos.set}
              orgTypes={valittavatOrganisaatiotyypit}
            />
          )}
        </>
      )}

      {state.opiskeluoikeus.visible && (
        <>
          {t('Opiskeluoikeus')}
          <Select
            options={opiskeluoikeustyypit}
            initialValue={opiskeluoikeustyypit[0]?.key}
            value={
              state.opiskeluoikeus.value &&
              koodistokoodiviiteId(state.opiskeluoikeus.value)
            }
            onChange={(opt) => state.opiskeluoikeus.set(opt?.value)}
            testId="opiskeluoikeus"
          />
        </>
      )}

      {state.päätasonSuoritus.visible && <SuoritusFields state={state} />}

      {state.suorituskieli.visible && (
        <>
          {t('Suorituskieli')}
          <KieliSelect
            initialValue="kieli_FI"
            value={
              state.suorituskieli.value &&
              koodistokoodiviiteId(state.suorituskieli.value)
            }
            onChange={(opt) => state.suorituskieli.set(opt?.value)}
            testId="suorituskieli"
          />
        </>
      )}

      {state.aloituspäivä.visible && (
        <>
          {'Aloituspäivä'}
          <DateEdit
            value={state.aloituspäivä.value}
            onChange={state.aloituspäivä.set}
          />
        </>
      )}

      {state.tila.visible && (
        <>
          {t('Opiskeluoikeuden tila')}
          <Select
            options={tilat.options}
            initialValue={tilat.initialValue}
            value={state.tila.value && koodistokoodiviiteId(state.tila.value)}
            onChange={(opt) => state.tila.set(opt?.value)}
            testId="tila"
          />
        </>
      )}

      {opintojenRahoitukset.options.length > 0 && (
        <>
          {t('Opintojen rahoitus')}
          <Select
            options={opintojenRahoitukset.options}
            initialValue={opintojenRahoitukset.initialValue}
            value={
              state.opintojenRahoitus.value &&
              koodistokoodiviiteId(state.opintojenRahoitus.value)
            }
            onChange={(opt) => state.opintojenRahoitus.set(opt?.value)}
            testId="opintojenRahoitus"
          />
        </>
      )}

      {jotpaAsianumerot.options.length > 0 && (
        <>
          {t('JOTPA asianumero')}
          <Select
            options={jotpaAsianumerot.options}
            initialValue={jotpaAsianumerot.initialValue}
            value={
              state.jotpaAsianumero.value &&
              koodistokoodiviiteId(state.jotpaAsianumero.value)
            }
            onChange={(opt) => state.jotpaAsianumero.set(opt?.value)}
            testId="jotpaAsianumero"
          />
        </>
      )}

      {state.maksuton.visible && <DialogMaksuttomuusSelect state={state} />}
    </section>
  )
}
