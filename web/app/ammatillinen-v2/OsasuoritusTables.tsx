import React, { ReactNode } from 'react'
import {
  FormModel,
  FormOptic,
  getValue
} from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { AmmatillisenTutkinnonOsittainenSuoritus } from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsittainenSuoritus'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { t } from '../i18n/i18n'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import {
  AmmatillinenArviointi,
  isAmmatillinenArviointi
} from '../types/fi/oph/koski/schema/AmmatillinenArviointi'
import { Finnish } from '../types/fi/oph/koski/schema/Finnish'
import { ParasArvosanaView } from '../components-v2/opiskeluoikeus/ArvosanaField'
import { OsittaisenAmmatillisenTutkinnonOsanSuoritus } from '../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanSuoritus'
import {
  isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus,
  YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
} from '../types/fi/oph/koski/schema/YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'
import {
  isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus,
  MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
} from '../types/fi/oph/koski/schema/MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'
import { MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritusProperties } from './MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritusProperties'
import { YhteisenOsittaisenAmmatillisenTutkinnonOsasuoritusProperties } from './YhteisenOsittaisenAmmatillisenTutkinnonOsasuoritusProperties'
import {
  isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus,
  OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
} from '../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
import { OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritusProperties } from './OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritusProperties'
import {
  isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus,
  OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
} from '../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
import { OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritusProperties } from './OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritusProperties'
import { Select, useKoodistoOptions } from '../components-v2/controls/Select'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { FieldViewerProps } from '../components-v2/forms/FormField'
import { CommonProps } from '../components-v2/CommonProps'
import { EmptyObject } from '../util/objects'
import { KeyValueRow } from '../components-v2/containers/KeyValueTable'
import { ISO2FinnishDate } from '../date/date'

interface OsasuoritusTablesProps {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  osittainenPäätasonSuoritus: ActivePäätasonSuoritus<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenSuoritus
  >
}

export const OsasuoritusTables = ({
  form,
  osittainenPäätasonSuoritus
}: OsasuoritusTablesProps) => {
  return (
    <>
      <TableForTutkinnonOsaRyhmä
        form={form}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Yhteiset tutkinnon osat"
      />
      <TableForTutkinnonOsaRyhmä
        form={form}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Ammatilliset tutkinnon osat"
      />
      <TableForTutkinnonOsaRyhmä
        form={form}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Vapaasti valittavat tutkinnon osat"
      />
      <TableForTutkinnonOsaRyhmä
        form={form}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Tutkintoa yksilöllisesti laajentavat tutkinnon osat"
      />
      <TableForTutkinnonOsaRyhmä
        form={form}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Muut suoritukset"
      />
    </>
  )
}

interface TableProps {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  osittainenPäätasonSuoritus: ActivePäätasonSuoritus<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenSuoritus
  >
  ryhmä: string
}

const TableForTutkinnonOsaRyhmä = ({
  form,
  osittainenPäätasonSuoritus,
  ryhmä
}: TableProps) => {
  return (
    <OsasuoritusTable
      editMode={form.editMode}
      rows={
        osittainenPäätasonSuoritus.suoritus.osasuoritukset
          ?.map((s, originalIndex) => ({ s, originalIndex }))
          .filter(
            ({ s }) =>
              (s.tutkinnonOsanRyhmä?.nimi as Finnish | undefined)?.fi ===
                ryhmä ||
              (s.tutkinnonOsanRyhmä === undefined &&
                ryhmä === 'Muut suoritukset')
          )
          .map(({ s, originalIndex }) =>
            tutkinnonOsatToTableRow({
              suoritusIndex: osittainenPäätasonSuoritus.index,
              osasuoritusIndex: originalIndex,
              suoritusPath: osittainenPäätasonSuoritus.path,
              form,
              level: 0,
              tutkinnonOsaRyhmä: ryhmä
            })
          ) || []
      }
    />
  )
}

interface OsasuoritusToTableRowParams<T extends string> {
  suoritusIndex: number
  osasuoritusIndex: number
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenSuoritus
  >
  form: FormModel<AmmatillinenOpiskeluoikeus>
  level: number
  tutkinnonOsaRyhmä: T
}

const tutkinnonOsatToTableRow = <T extends string>({
  suoritusIndex,
  osasuoritusIndex,
  suoritusPath,
  form,
  level,
  tutkinnonOsaRyhmä
}: OsasuoritusToTableRowParams<T>): OsasuoritusRowData<
  T | 'Laajuus' | 'Arvosana'
> => {
  const osasuoritusPath = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)
  const osasuoritus = getValue(osasuoritusPath)(form.state)

  const columns: Partial<Record<'Laajuus' | 'Arvosana' | T, ReactNode>> = {}

  columns[tutkinnonOsaRyhmä] = (
    <>{t(osasuoritus?.koulutusmoduuli.tunniste.nimi)}</>
  )

  columns.Laajuus = (
    <>
      {osasuoritus?.koulutusmoduuli.laajuus?.arvo}{' '}
      {t(osasuoritus?.koulutusmoduuli.laajuus?.yksikkö.lyhytNimi)}
    </>
  )

  if (hasArviointi(osasuoritus)) {
    columns.Arvosana = <ParasArvosanaView value={osasuoritus.arviointi} />
  }

  const content =
    osasuoritus && OsasuoritusProperties({ form, osasuoritus, osasuoritusPath })

  return {
    suoritusIndex,
    osasuoritusIndex,
    expandable: true,
    columns,
    content
  }
}

type WithArviointi = {
  arviointi: AmmatillinenArviointi[]
}

const hasArviointi = (suoritus: unknown): suoritus is WithArviointi => {
  const arviointi = (suoritus as any)?.arviointi
  return Array.isArray(arviointi) && isAmmatillinenArviointi(arviointi[0])
}

export const ArviointiView = ({
  value
}: CommonProps<FieldViewerProps<AmmatillinenArviointi, EmptyObject>>) => {
  return (
    <>
      <KeyValueRow localizableLabel="Arvosana">
        {t(value?.arvosana.nimi)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arviointipäivä">
        {ISO2FinnishDate(value?.päivä)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arvioijat">
        {value?.arvioitsijat?.map((a) => (
          <>
            {a.nimi}
            <br />
          </>
        ))}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Kuvaus">{t(value?.kuvaus)}</KeyValueRow>
    </>
  )
}

type AmisArvosanaSelectProps = {
  value?: string
  onChange?: (
    value?: Koodistokoodiviite<
      | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
      | 'arviointiasteikkoammatillinent1k3'
      | 'arviointiasteikkoammatillinen15',
      string
    >
  ) => void
}

const AmisArvosanaSelect = ({ value, onChange }: AmisArvosanaSelectProps) => {
  const options = useKoodistoOptions(
    'arviointiasteikkoammatillinenhyvaksyttyhylatty',
    'arviointiasteikkoammatillinent1k3',
    'arviointiasteikkoammatillinen15'
  )
  return (
    <Select
      options={options}
      value={value}
      onChange={(a) => onChange && onChange(a?.value)}
      testId="arvosana"
    />
  )
}

type OsasuoritusPropertiesProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  osasuoritus: OsittaisenAmmatillisenTutkinnonOsanSuoritus
  osasuoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    OsittaisenAmmatillisenTutkinnonOsanSuoritus
  >
}

const OsasuoritusProperties = ({
  form,
  osasuoritus,
  osasuoritusPath
}: OsasuoritusPropertiesProps) => {
  if (
    isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(osasuoritus)
  ) {
    const yhteinenPath = osasuoritusPath as unknown as FormOptic<
      AmmatillinenOpiskeluoikeus,
      YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
    >
    return (
      <YhteisenOsittaisenAmmatillisenTutkinnonOsasuoritusProperties
        form={form}
        osasuoritusPath={yhteinenPath}
        osasuoritus={osasuoritus}
      />
    )
  } else if (
    isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(osasuoritus)
  ) {
    const muunPath = osasuoritusPath as unknown as FormOptic<
      AmmatillinenOpiskeluoikeus,
      MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
    >
    return (
      <MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritusProperties
        form={form}
        osasuoritusPath={muunPath}
        osasuoritus={osasuoritus}
      />
    )
  } else if (
    isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(osasuoritus)
  ) {
    const korkeakouluPath = osasuoritusPath as unknown as FormOptic<
      AmmatillinenOpiskeluoikeus,
      OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
    >
    return (
      <OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritusProperties
        form={form}
        osasuoritusPath={korkeakouluPath}
        osasuoritus={osasuoritus}
      />
    )
  } else if (
    isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
      osasuoritus
    )
  ) {
    const jatkoPath = osasuoritusPath as unknown as FormOptic<
      AmmatillinenOpiskeluoikeus,
      OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
    >
    return (
      <OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritusProperties
        form={form}
        osasuoritusPath={jatkoPath}
        osasuoritus={osasuoritus}
      />
    )
  }
}
