import React, { ReactNode, useCallback, useState } from 'react'
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
import { finnish, t } from '../i18n/i18n'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import {
  AmmatillinenArviointi,
  isAmmatillinenArviointi
} from '../types/fi/oph/koski/schema/AmmatillinenArviointi'
import { Finnish } from '../types/fi/oph/koski/schema/Finnish'
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
import { FormField } from '../components-v2/forms/FormField'
import {
  LaajuusEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { LaajuusOsaamispisteissä } from '../types/fi/oph/koski/schema/LaajuusOsaamispisteissa'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { YhteinenTutkinnonOsa } from '../types/fi/oph/koski/schema/YhteinenTutkinnonOsa'
import { deleteAt } from '../util/fp/arrays'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../components-v2/containers/Modal'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import { PaikallinenTutkinnonOsa } from '../types/fi/oph/koski/schema/PaikallinenTutkinnonOsa'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { useKoodisto, useKoodistoFiller } from '../appstate/koodisto'
import { TextEdit } from '../components-v2/controls/TextField'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { MuuValtakunnallinenTutkinnonOsa } from '../types/fi/oph/koski/schema/MuuValtakunnallinenTutkinnonOsa'
import { useTutkinnonOsaRyhmät } from './useTutkinnonOsaRyhmät'
import { useTutkinnonOsat } from './useTutkinnonOsat'
import { AmisArvosanaInTableEdit, AmisArvosanaInTableView } from './Arviointi'
import { AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus } from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus'
import {
  isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus,
  YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
} from '../types/fi/oph/koski/schema/YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'
import {
  isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus,
  MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
} from '../types/fi/oph/koski/schema/MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'
import { OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus } from '../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus'
import { YhteisenOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaOsasuoritusProperties } from './YhteisenOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaOsasuoritusProperties'
import { MuunOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaTutkinnonosanSuoritusProperties } from './MuunOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaTutkinnonosanSuoritusProperties'
import { DialogSelect } from '../uusiopiskeluoikeus/components/DialogSelect'
import { AmmatillinenTutkintoKoulutus } from '../types/fi/oph/koski/schema/AmmatillinenTutkintoKoulutus'
import {
  tutkintoKey,
  useTutkinnot
} from '../uusiopiskeluoikeus/opiskeluoikeusSpecificFields/AmmatillinenKoulutusFields'
import { TutkintoPeruste } from '../types/fi/oph/koski/tutkinto/TutkintoPeruste'
import { Spacer } from '../components-v2/layout/Spacer'

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
  const perusteenRyhmät = useTutkinnonOsaRyhmät(
    osittainenPäätasonSuoritus.suoritus.koulutusmoduuli.perusteenDiaarinumero ||
      '',
    osittainenPäätasonSuoritus.suoritus.suoritustapa.koodiarvo
  )

  // Jos kyseessä esim. erikoisammattitutkinto, niin niputetaan kaikki ryhmät yhteen.
  if (perusteenRyhmät.length === 0) {
    return (
      <TableForTutkinnonOsaRyhmä
        form={form}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä={'Tutkinnon osat'}
        perusteenRyhmät={perusteenRyhmät}
      />
    )
  }

  return (
    <>
      <TableForTutkinnonOsaRyhmä
        form={form}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Yhteiset tutkinnon osat"
        perusteenRyhmät={perusteenRyhmät}
      />
      <TableForTutkinnonOsaRyhmä
        form={form}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Ammatilliset tutkinnon osat"
        perusteenRyhmät={perusteenRyhmät}
      />
      <TableForTutkinnonOsaRyhmä
        form={form}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Vapaasti valittavat tutkinnon osat"
        perusteenRyhmät={perusteenRyhmät}
      />
      <TableForTutkinnonOsaRyhmä
        form={form}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Tutkintoa yksilöllisesti laajentavat tutkinnon osat"
        perusteenRyhmät={perusteenRyhmät}
      />
      <TableForTutkinnonOsaRyhmä
        form={form}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Muut suoritukset"
        perusteenRyhmät={perusteenRyhmät}
      />
    </>
  )
}

interface OsasuoritusTablesUseastaTutkinnostaProps {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  oppilaitosOid?: string
  osittainenPäätasonSuoritus: ActivePäätasonSuoritus<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
  >
}

export const OsasuoritusTablesUseastaTutkinnosta = ({
  form,
  oppilaitosOid,
  osittainenPäätasonSuoritus
}: OsasuoritusTablesUseastaTutkinnostaProps) => {
  return (
    <>
      <TableForTutkinnonOsaRyhmäUseastaTutkinnosta
        form={form}
        oppilaitosOid={oppilaitosOid}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Yhteiset tutkinnon osat"
      />
      <TableForTutkinnonOsaRyhmäUseastaTutkinnosta
        form={form}
        oppilaitosOid={oppilaitosOid}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Ammatilliset tutkinnon osat"
      />
      <TableForTutkinnonOsaRyhmäUseastaTutkinnosta
        form={form}
        oppilaitosOid={oppilaitosOid}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Vapaasti valittavat tutkinnon osat"
      />
      <TableForTutkinnonOsaRyhmäUseastaTutkinnosta
        form={form}
        oppilaitosOid={oppilaitosOid}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Tutkintoa yksilöllisesti laajentavat tutkinnon osat"
      />
      <TableForTutkinnonOsaRyhmäUseastaTutkinnosta
        form={form}
        oppilaitosOid={oppilaitosOid}
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
  perusteenRyhmät: Koodistokoodiviite<'tutkinnonosaryhmä'>[]
}

const dummyRow = <T extends string>(
  ryhmä: T
): OsasuoritusRowData<T | 'Laajuus' | 'Arvosana'> => {
  const columns: Partial<Record<'Laajuus' | 'Arvosana' | T, ReactNode>> = {}
  columns[ryhmä] = null
  columns.Laajuus = null
  columns.Arvosana = null

  return {
    suoritusIndex: 0,
    osasuoritusIndex: 0,
    expandable: false,
    columns
  }
}

const TableForTutkinnonOsaRyhmä = ({
  form,
  osittainenPäätasonSuoritus,
  ryhmä,
  perusteenRyhmät
}: TableProps) => {
  const originalIndexMap: Record<number, number> = {}

  const rows = osittainenPäätasonSuoritus.suoritus.osasuoritukset
    ?.map((s, originalIndex) => ({ s, originalIndex }))
    .filter(
      ({ s }) =>
        (s.tutkinnonOsanRyhmä?.nimi as Finnish | undefined)?.fi === ryhmä ||
        ryhmä === 'Tutkinnon osat' ||
        (s.tutkinnonOsanRyhmä === undefined && ryhmä === 'Muut suoritukset')
    )
    .map(({ s, originalIndex }, rowIndex) => {
      originalIndexMap[rowIndex] = originalIndex
      return tutkinnonOsatToTableRow({
        suoritusIndex: osittainenPäätasonSuoritus.index,
        osasuoritusIndex: originalIndex,
        suoritusPath: osittainenPäätasonSuoritus.path,
        form,
        level: 0,
        tutkinnonOsaRyhmä: ryhmä
      })
    })

  const ryhmäPerusteessa =
    perusteenRyhmät.find((r) => (r.nimi as Finnish).fi === ryhmä) !==
      undefined || perusteenRyhmät.length === 0

  if (!ryhmäPerusteessa && (!rows || rows.length === 0)) {
    return null
  }

  return (
    <OsasuoritusTable
      editMode={form.editMode}
      rows={
        (!rows || rows?.length === 0) && form.editMode
          ? [dummyRow(ryhmä)] // Saadaan headeri näkymään editointimoodissa kun osasuorituksia ei ole
          : rows || []
      }
      addNewOsasuoritusView={NewAmisOsasuoritus}
      addNewOsasuoritusViewProps={{
        form,
        ryhmä,
        suoritusPath: osittainenPäätasonSuoritus.path
      }}
      onRemove={
        !rows || rows?.length === 0
          ? undefined
          : (rowIndex) =>
              form.updateAt(osittainenPäätasonSuoritus.path, (pts) => {
                return {
                  ...pts,
                  osasuoritukset: deleteAt(
                    pts.osasuoritukset || [],
                    originalIndexMap[rowIndex]
                  )
                }
              })
      }
      completed={(rowIndex) => {
        const osasuoritus = (osittainenPäätasonSuoritus.suoritus
          .osasuoritukset || [])[originalIndexMap[rowIndex]]
        if (osasuoritus === undefined) {
          return undefined
        }
        return hasAmmatillinenArviointi(osasuoritus)
      }}
    />
  )
}

interface TableUseastaTutkinnostaProps {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  oppilaitosOid?: string
  osittainenPäätasonSuoritus: ActivePäätasonSuoritus<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
  >
  ryhmä: string
}

const TableForTutkinnonOsaRyhmäUseastaTutkinnosta = ({
  form,
  oppilaitosOid,
  osittainenPäätasonSuoritus,
  ryhmä
}: TableUseastaTutkinnostaProps) => {
  const originalIndexMap: Record<number, number> = {}

  const rows = osittainenPäätasonSuoritus.suoritus.osasuoritukset
    ?.map((s, originalIndex) => ({ s, originalIndex }))
    .filter(
      ({ s }) =>
        (s.tutkinnonOsanRyhmä?.nimi as Finnish | undefined)?.fi === ryhmä ||
        (s.tutkinnonOsanRyhmä === undefined && ryhmä === 'Muut suoritukset')
    )
    .map(({ s, originalIndex }, rowIndex) => {
      originalIndexMap[rowIndex] = originalIndex
      return tutkinnonOsatUseastaTutkinnostaToTableRow({
        suoritusIndex: osittainenPäätasonSuoritus.index,
        osasuoritusIndex: originalIndex,
        suoritusPath: osittainenPäätasonSuoritus.path,
        form,
        level: 0,
        tutkinnonOsaRyhmä: ryhmä
      })
    })

  return (
    <OsasuoritusTable
      editMode={form.editMode}
      rows={
        (!rows || rows?.length === 0) && form.editMode
          ? [dummyRow(ryhmä)] // Saadaan headeri näkymään editointimoodissa kun osasuorituksia ei ole
          : rows || []
      }
      addNewOsasuoritusView={NewAmisOsasuoritusTutkinnolla}
      addNewOsasuoritusViewProps={{
        form,
        oppilaitosOid,
        ryhmä,
        suoritusPath: osittainenPäätasonSuoritus.path
      }}
      onRemove={
        !rows || rows?.length === 0
          ? undefined
          : (rowIndex) =>
              form.updateAt(osittainenPäätasonSuoritus.path, (pts) => {
                return {
                  ...pts,
                  osasuoritukset: deleteAt(
                    pts.osasuoritukset || [],
                    originalIndexMap[rowIndex]
                  )
                }
              })
      }
      completed={(rowIndex) => {
        const osasuoritus = (osittainenPäätasonSuoritus.suoritus
          .osasuoritukset || [])[originalIndexMap[rowIndex]]
        if (osasuoritus === undefined) {
          return undefined
        }
        return hasAmmatillinenArviointi(osasuoritus)
      }}
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
    <FormField
      form={form}
      view={LaajuusView}
      edit={LaajuusEdit}
      editProps={{
        createLaajuus: (arvo) => LaajuusOsaamispisteissä({ arvo })
      }}
      path={osasuoritusPath.prop('koulutusmoduuli').prop('laajuus')}
    />
  )

  if (
    isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(
      osasuoritus
    ) ||
    isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(osasuoritus)
  ) {
    columns.Arvosana = (
      <FormField
        form={form}
        view={AmisArvosanaInTableView}
        edit={AmisArvosanaInTableEdit}
        path={(
          osasuoritusPath as FormOptic<AmmatillinenOpiskeluoikeus, any>
        ).prop('arviointi')}
      />
    )
  } else {
    columns.Arvosana = null
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

interface OsasuoritusUseastaTutkinnostaToTableRowParams<T extends string> {
  suoritusIndex: number
  osasuoritusIndex: number
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
  >
  form: FormModel<AmmatillinenOpiskeluoikeus>
  level: number
  tutkinnonOsaRyhmä: T
}

const tutkinnonOsatUseastaTutkinnostaToTableRow = <T extends string>({
  suoritusIndex,
  osasuoritusIndex,
  suoritusPath,
  form,
  level,
  tutkinnonOsaRyhmä
}: OsasuoritusUseastaTutkinnostaToTableRowParams<T>): OsasuoritusRowData<
  T | 'Laajuus' | 'Arvosana'
> => {
  const osasuoritusPath = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)
  const osasuoritus = getValue(osasuoritusPath)(form.state)

  const columns: Partial<Record<'Laajuus' | 'Arvosana' | T, ReactNode>> = {}

  columns[tutkinnonOsaRyhmä] = (
    <span
      data-testid={`suoritus.${suoritusIndex}.osasuoritus.${osasuoritusIndex}.nimi`}
    >
      {t(osasuoritus?.koulutusmoduuli.tunniste.nimi)}
    </span>
  )

  columns.Laajuus = (
    <FormField
      form={form}
      view={LaajuusView}
      edit={LaajuusEdit}
      editProps={{
        createLaajuus: (arvo) => LaajuusOsaamispisteissä({ arvo })
      }}
      path={osasuoritusPath.prop('koulutusmoduuli').prop('laajuus')}
    />
  )

  columns.Arvosana = (
    <FormField
      form={form}
      view={AmisArvosanaInTableView}
      edit={AmisArvosanaInTableEdit}
      path={(
        osasuoritusPath as FormOptic<AmmatillinenOpiskeluoikeus, any>
      ).prop('arviointi')}
    />
  )

  const content =
    osasuoritus &&
    OsasuoritusUseastaTutkinnostaProperties({
      form,
      osasuoritus,
      osasuoritusPath
    })

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

export const hasAmmatillinenArviointi = (
  suoritus: unknown
): suoritus is WithArviointi => {
  const arviointi = (suoritus as any)?.arviointi
  return Array.isArray(arviointi) && isAmmatillinenArviointi(arviointi[0])
}

// TODO hae constraints rajapinnasta, perusteesta, tjs...
const yhteisenTutkinnonOsat = [
  '101053',
  '101054',
  '101055',
  '101056',
  '106727',
  '106728',
  '106729',
  '400012',
  '400013',
  '400014',
  '600001',
  '600002'
]

type YhteisenTutkinnonOsatTunniste = Koodistokoodiviite<
  'tutkinnonosat',
  | '101053'
  | '101054'
  | '101055'
  | '101056'
  | '106727'
  | '106728'
  | '106729'
  | '400012'
  | '400013'
  | '400014'
  | '600001'
  | '600002'
>

const newYhteinenTutkinnonOsa = (tunniste: YhteisenTutkinnonOsatTunniste) => {
  return YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus({
    koulutusmoduuli: YhteinenTutkinnonOsa({ tunniste, pakollinen: false }),
    tutkinnonOsanRyhmä: Koodistokoodiviite({
      koodistoUri: 'ammatillisentutkinnonosanryhma',
      koodiarvo: '2',
      nimi: finnish('Yhteiset tutkinnon osat')
    })
  })
}

const newYhteinenTutkinnonOsaTutkinnolla = (
  tutkinto: TutkintoPeruste,
  tunniste: YhteisenTutkinnonOsatTunniste
) => {
  return YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus(
    {
      koulutusmoduuli: YhteinenTutkinnonOsa({ tunniste, pakollinen: false }),
      tutkinto: AmmatillinenTutkintoKoulutus({
        tunniste: Koodistokoodiviite({
          koodiarvo: tutkinto.tutkintoKoodi,
          koodistoUri: 'koulutus'
        }),
        perusteenDiaarinumero: tutkinto.diaarinumero,
        perusteenNimi: tutkinto.nimi
      }),
      tutkinnonOsanRyhmä: Koodistokoodiviite({
        koodistoUri: 'ammatillisentutkinnonosanryhma',
        koodiarvo: '2',
        nimi: finnish('Yhteiset tutkinnon osat')
      })
    }
  )
}

const newMuuTutkinnonOsaTutkinnolla = (
  tutkinto: TutkintoPeruste,
  tunniste: Koodistokoodiviite<'tutkinnonosat', string>,
  ryhmäKoodi?: Koodistokoodiviite<
    'ammatillisentutkinnonosanryhma',
    '1' | '3' | '4'
  >
) => {
  return MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus(
    {
      koulutusmoduuli: MuuValtakunnallinenTutkinnonOsa({
        tunniste,
        pakollinen: false
      }),
      tutkinto: AmmatillinenTutkintoKoulutus({
        tunniste: Koodistokoodiviite({
          koodiarvo: tutkinto.tutkintoKoodi,
          koodistoUri: 'koulutus'
        }),
        perusteenDiaarinumero: tutkinto.diaarinumero,
        perusteenNimi: tutkinto.nimi
      }),
      tutkinnonOsanRyhmä: ryhmäKoodi
    }
  )
}

type NewAmisOsasuoritusProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  ryhmä: string
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenSuoritus
  >
}

const NewAmisOsasuoritus = ({
  form,
  ryhmä,
  suoritusPath
}: NewAmisOsasuoritusProps) => {
  const ryhmät = useKoodisto('ammatillisentutkinnonosanryhma')
  const ryhmäKoodi =
    (ryhmät !== null
      ? (ryhmät.find((r) => (r.koodiviite.nimi as Finnish).fi === ryhmä)
          ?.koodiviite as
          | Koodistokoodiviite<
              'ammatillisentutkinnonosanryhma',
              '1' | '3' | '4'
            >
          | undefined)
      : undefined) ||
    Koodistokoodiviite({
      koodiarvo: '1',
      koodistoUri: 'ammatillisentutkinnonosanryhma'
    }) // fallbackaa Ammatillisen tutkinnon osiin jos kuuluu "muuhun" ryhmään

  const lisättävätTutkinnonOsat = useTutkinnonOsat(
    getValue(
      suoritusPath.prop('koulutusmoduuli').prop('perusteenDiaarinumero')
    )(form.state),
    ryhmäKoodi?.koodiarvo
  )

  // Näiden memoizeaaminen parantaa KoodistoSelectin suorituskykyä huomattavasti
  const format = useCallback((osa) => osa.koodiarvo + ' ' + t(osa.nimi), [])
  const yhteisetFilter = useCallback(
    (osa) => {
      const osat = lisättävätTutkinnonOsat.osat.map((o) => o.koodiarvo)
      return osat.length === 0
        ? yhteisenTutkinnonOsat.includes(osa.koodiarvo)
        : osat.includes(osa.koodiarvo)
    },
    [lisättävätTutkinnonOsat]
  )
  const ammatillisetFilter = useCallback(
    (osa) => {
      const osat = lisättävätTutkinnonOsat.osat.map((o) => o.koodiarvo)
      return osat.length === 0 || osat.includes(osa.koodiarvo)
    },
    [lisättävätTutkinnonOsat]
  )

  if (ryhmä === 'Yhteiset tutkinnon osat') {
    return (
      <>
        <KoodistoSelect
          addNewText={'Lisää tutkinnon osa'}
          koodistoUri="tutkinnonosat"
          format={format}
          filter={yhteisetFilter}
          onSelect={(tunniste) => {
            const yhteinenTunniste = tunniste as YhteisenTutkinnonOsatTunniste
            tunniste &&
              form.updateAt(
                suoritusPath.prop('osasuoritukset').valueOr([]),
                (a) => [...a, newYhteinenTutkinnonOsa(yhteinenTunniste)]
              )
          }}
          testId="uusi-yhteinen-tutkinnonosa"
        />
      </>
    )
  }
  return (
    <ColumnRow>
      <Column span={12}>
        <KoodistoSelect
          addNewText={'Lisää tutkinnon osa'}
          koodistoUri={'tutkinnonosat'}
          format={format}
          filter={ammatillisetFilter}
          onSelect={(osa) => {
            osa &&
              form.updateAt(
                suoritusPath.prop('osasuoritukset').valueOr([]),
                (a) => [...a, newMuuOsa(osa, ryhmäKoodi)]
              )
          }}
          testId={'uusi-muu-tutkinnonosa'}
        />
      </Column>
      <Column span={6}>
        <NewPaikallinen
          form={form}
          ryhmäKoodi={ryhmäKoodi}
          suoritusPath={suoritusPath}
        />
      </Column>
    </ColumnRow>
  )
}

type NewAmisOsasuoritusTutkinnollaProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  oppilaitosOid?: string
  ryhmä: string
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
  >
}

const NewAmisOsasuoritusTutkinnolla = ({
  form,
  oppilaitosOid,
  ryhmä,
  suoritusPath
}: NewAmisOsasuoritusTutkinnollaProps) => {
  const ryhmät = useKoodisto('ammatillisentutkinnonosanryhma')
  const ryhmäKoodi =
    (ryhmät !== null
      ? (ryhmät.find((r) => (r.koodiviite.nimi as Finnish).fi === ryhmä)
          ?.koodiviite as
          | Koodistokoodiviite<
              'ammatillisentutkinnonosanryhma',
              '1' | '3' | '4'
            >
          | undefined)
      : undefined) || undefined

  if (ryhmä === 'Yhteiset tutkinnon osat') {
    return (
      <ColumnRow>
        <Column span={6}>
          <NewYhteinenTutkinnonOsaTutkinnolla
            form={form}
            oppilaitosOid={oppilaitosOid}
            suoritusPath={suoritusPath}
          />
        </Column>
      </ColumnRow>
    )
  }
  return (
    <ColumnRow>
      <Column span={6}>
        <NewMuuTutkinnonOsaTutkinnolla
          form={form}
          oppilaitosOid={oppilaitosOid}
          ryhmäKoodi={ryhmäKoodi}
          suoritusPath={suoritusPath}
        />
      </Column>
      <Column span={6}>
        <NewPaikallinenOsaTutkinnolla
          form={form}
          oppilaitosOid={oppilaitosOid}
          ryhmäKoodi={ryhmäKoodi}
          suoritusPath={suoritusPath}
        />
      </Column>
    </ColumnRow>
  )
}

const newMuuOsa = (
  osa: Koodistokoodiviite<'tutkinnonosat', string>,
  ryhmä: Koodistokoodiviite<'ammatillisentutkinnonosanryhma', '1' | '3' | '4'>
) => {
  return MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus({
    koulutusmoduuli: MuuValtakunnallinenTutkinnonOsa({
      tunniste: osa,
      pakollinen: false
    }),
    tutkinnonOsanRyhmä: ryhmä
  })
}

const newPaikallinenOsa = (
  osa: string,
  ryhmä: Koodistokoodiviite<'ammatillisentutkinnonosanryhma', '1' | '3' | '4'>
): MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus => {
  return MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus({
    koulutusmoduuli: PaikallinenTutkinnonOsa({
      tunniste: PaikallinenKoodi({ koodiarvo: osa, nimi: finnish(osa) }),
      kuvaus: finnish(osa),
      pakollinen: false
    }),
    tutkinnonOsanRyhmä: ryhmä
  })
}

const newPaikallinenOsaTutkinnolla = (
  tutkinto: TutkintoPeruste,
  osa: string,
  ryhmä?: Koodistokoodiviite<'ammatillisentutkinnonosanryhma', '1' | '3' | '4'>
): MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus => {
  return MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus(
    {
      koulutusmoduuli: PaikallinenTutkinnonOsa({
        tunniste: PaikallinenKoodi({ koodiarvo: osa, nimi: finnish(osa) }),
        kuvaus: finnish(osa),
        pakollinen: false
      }),
      tutkinto: AmmatillinenTutkintoKoulutus({
        tunniste: Koodistokoodiviite({
          koodiarvo: tutkinto.tutkintoKoodi,
          koodistoUri: 'koulutus'
        }),
        perusteenDiaarinumero: tutkinto.diaarinumero,
        perusteenNimi: tutkinto.nimi
      }),
      tutkinnonOsanRyhmä: ryhmä
    }
  )
}

type NewPaikallinenProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  ryhmäKoodi: Koodistokoodiviite<
    'ammatillisentutkinnonosanryhma',
    '1' | '3' | '4'
  >
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenSuoritus
  >
}

const NewPaikallinen = ({
  form,
  ryhmäKoodi,
  suoritusPath
}: NewPaikallinenProps) => {
  const [showModal, setShowModal] = useState(false)

  return (
    <>
      <FlatButton onClick={() => setShowModal(true)}>
        {t('Lisää paikallinen tutkinnon osa')}
      </FlatButton>
      {showModal && (
        <NewPaikallinenModal
          onClose={() => setShowModal(false)}
          onSubmit={(osa) => {
            form.updateAt(
              suoritusPath.prop('osasuoritukset').valueOr([]),
              (o) => [...o, newPaikallinenOsa(osa, ryhmäKoodi)]
            )
            setShowModal(false)
          }}
        />
      )}
    </>
  )
}

type NewYhteinenTutkinnonOsaTutkinnollaProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  oppilaitosOid?: string
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
  >
}

const NewYhteinenTutkinnonOsaTutkinnolla = ({
  form,
  oppilaitosOid,
  suoritusPath
}: NewYhteinenTutkinnonOsaTutkinnollaProps) => {
  const [showModal, setShowModal] = useState(false)
  const fillNimet = useKoodistoFiller()

  return (
    <>
      <FlatButton
        testId="lisaa-yhteinen-tutkinnon-osa"
        onClick={() => setShowModal(true)}
      >
        {t('Lisää tutkinnon osa')}
      </FlatButton>
      {showModal && (
        <NewYhteinenTutkinnonOsaTutkinnollaModal
          oppilaitosOid={oppilaitosOid}
          ryhmäKoodiArvo={'2'}
          onClose={() => setShowModal(false)}
          onSubmit={async (tutkinto, tunniste) => {
            const osasuoritus = await fillNimet(
              newYhteinenTutkinnonOsaTutkinnolla(tutkinto, tunniste)
            )
            form.updateAt(
              suoritusPath.prop('osasuoritukset').valueOr([]),
              (a) => [...a, osasuoritus]
            )
            setShowModal(false)
          }}
        />
      )}
    </>
  )
}

type NewMuuTutkinnonOsaTutkinnollaProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  oppilaitosOid?: string
  ryhmäKoodi?: Koodistokoodiviite<
    'ammatillisentutkinnonosanryhma',
    '1' | '3' | '4'
  >
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
  >
}

const NewMuuTutkinnonOsaTutkinnolla = ({
  form,
  oppilaitosOid,
  ryhmäKoodi,
  suoritusPath
}: NewMuuTutkinnonOsaTutkinnollaProps) => {
  const [showModal, setShowModal] = useState(false)
  const fillNimet = useKoodistoFiller()

  return (
    <>
      <FlatButton
        testId={
          ryhmäKoodi
            ? `lisaa-muu-tutkinnon-osa-${ryhmäKoodi?.koodiarvo}`
            : 'lisaa-muu-osa'
        }
        onClick={() => setShowModal(true)}
      >
        {t('Lisää tutkinnon osa')}
      </FlatButton>
      {showModal && (
        <NewMuuTutkinnonOsaTutkinnollaModal
          oppilaitosOid={oppilaitosOid}
          ryhmäKoodiArvo={ryhmäKoodi?.koodiarvo}
          onClose={() => setShowModal(false)}
          onSubmit={async (tutkinto, tunniste) => {
            const osasuoritus = await fillNimet(
              newMuuTutkinnonOsaTutkinnolla(tutkinto, tunniste, ryhmäKoodi)
            )
            form.updateAt(
              suoritusPath.prop('osasuoritukset').valueOr([]),
              (a) => [...a, osasuoritus]
            )
            setShowModal(false)
          }}
        />
      )}
    </>
  )
}

type NewPaikallinenOsaTutkinnollaProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  oppilaitosOid?: string
  ryhmäKoodi?: Koodistokoodiviite<
    'ammatillisentutkinnonosanryhma',
    '1' | '3' | '4'
  >
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
  >
}

const NewPaikallinenOsaTutkinnolla = ({
  form,
  oppilaitosOid,
  ryhmäKoodi,
  suoritusPath
}: NewPaikallinenOsaTutkinnollaProps) => {
  const [showModal, setShowModal] = useState(false)
  const fillNimet = useKoodistoFiller()

  return (
    <>
      <FlatButton
        testId={
          ryhmäKoodi
            ? `lisaa-paikallinen-osa-${ryhmäKoodi?.koodiarvo}`
            : 'lisaa-paikallinen-osa-muut'
        }
        onClick={() => setShowModal(true)}
      >
        {t('Lisää paikallinen tutkinnon osa')}
      </FlatButton>
      {showModal && (
        <NewPaikallinenOsaTutkinnollaModal
          oppilaitosOid={oppilaitosOid}
          onClose={() => setShowModal(false)}
          onSubmit={async (tutkinto, osa) => {
            const osasuoritus = await fillNimet(
              newPaikallinenOsaTutkinnolla(tutkinto, osa, ryhmäKoodi)
            )
            form.updateAt(
              suoritusPath.prop('osasuoritukset').valueOr([]),
              (o) => [...o, osasuoritus]
            )
            setShowModal(false)
          }}
        />
      )}
    </>
  )
}

type NewPaikallinenModalProps = {
  onClose: () => void
  onSubmit: (osa: string) => void
}

const NewPaikallinenModal = ({
  onClose,
  onSubmit
}: NewPaikallinenModalProps) => {
  const [osa, setOsa] = useState('')

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Paikallisen tutkinnon osan lisäys')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Tutkinnon osan nimi')}
          <TextEdit onChange={(o) => setOsa(o ? o : '')} value={osa} />
        </label>
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton onClick={() => onSubmit(osa)}>
          {t('Lisää tutkinnon osa')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

type NewTutkinnonOsaTutkinnollaModalProps = {
  oppilaitosOid?: string
  ryhmäKoodiArvo: string
  onClose: () => void
  onSubmit: (
    tutkinto: TutkintoPeruste,
    tunniste: YhteisenTutkinnonOsatTunniste
  ) => void
}

const NewYhteinenTutkinnonOsaTutkinnollaModal = ({
  oppilaitosOid,
  ryhmäKoodiArvo,
  onClose,
  onSubmit
}: NewTutkinnonOsaTutkinnollaModalProps) => {
  const [tunniste, setTunniste] = useState<
    YhteisenTutkinnonOsatTunniste | undefined
  >(undefined)
  const [tutkinto, setTutkinto] = useState<TutkintoPeruste | undefined>(
    undefined
  )
  const tutkinnot = useTutkinnot(oppilaitosOid)
  const lisättävätTutkinnonOsat = useTutkinnonOsat(
    tutkinto?.diaarinumero,
    ryhmäKoodiArvo
  )

  // Näiden memoizeaaminen parantaa KoodistoSelectin suorituskykyä huomattavasti
  const format = useCallback((osa) => osa.koodiarvo + ' ' + t(osa.nimi), [])
  const yhteisetFilter = useCallback(
    (osa) => {
      const osat = lisättävätTutkinnonOsat.osat.map((o) => o.koodiarvo)
      return osat.length === 0
        ? yhteisenTutkinnonOsat.includes(osa.koodiarvo)
        : osat.includes(osa.koodiarvo)
    },
    [lisättävätTutkinnonOsat]
  )

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Tutkinnon osan lisäys')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Tutkinto')}
          <DialogSelect
            options={tutkinnot.options}
            value={tutkinto && tutkintoKey(tutkinto)}
            onChange={(opt) => {
              setTutkinto(opt?.value)
              setTunniste(undefined)
            }}
            onSearch={tutkinnot.setQuery}
            testId="tutkinto"
          />
        </label>
        <KoodistoSelect
          addNewText={'Lisää tutkinnon osa'}
          koodistoUri="tutkinnonosat"
          format={format}
          value={tunniste?.koodiarvo}
          filter={yhteisetFilter}
          onSelect={(tutkinnonOsa) => {
            const yhteinenTunniste =
              tutkinnonOsa as YhteisenTutkinnonOsatTunniste
            tutkinnonOsa && setTunniste(yhteinenTunniste)
          }}
          testId="uusi-yhteinen-tutkinnonosa"
        />
        <Spacer />
        <Spacer />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={tutkinto === undefined || tunniste === undefined}
          onClick={() => {
            if (tutkinto && tunniste) {
              onSubmit(tutkinto, tunniste)
            }
          }}
          testId="confirm"
        >
          {t('Lisää tutkinnon osa')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

type NewMuuTutkinnonOsaTutkinnollaModalProps = {
  oppilaitosOid?: string
  ryhmäKoodiArvo?: string
  onClose: () => void
  onSubmit: (
    tutkinto: TutkintoPeruste,
    osa: Koodistokoodiviite<'tutkinnonosat', string>
  ) => void
}

const NewMuuTutkinnonOsaTutkinnollaModal = ({
  oppilaitosOid,
  ryhmäKoodiArvo,
  onClose,
  onSubmit
}: NewMuuTutkinnonOsaTutkinnollaModalProps) => {
  const [tunniste, setTunniste] = useState<
    Koodistokoodiviite<'tutkinnonosat', string> | undefined
  >(undefined)
  const [tutkinto, setTutkinto] = useState<TutkintoPeruste | undefined>(
    undefined
  )
  const tutkinnot = useTutkinnot(oppilaitosOid)
  const lisättävätTutkinnonOsat = useTutkinnonOsat(
    tutkinto?.diaarinumero,
    ryhmäKoodiArvo ? ryhmäKoodiArvo : '1' // jos ryhmä on tyhjä, feikkaa se tulosten suodattamiseksi
  )

  // Näiden memoizeaaminen parantaa KoodistoSelectin suorituskykyä huomattavasti
  const format = useCallback((osa) => osa.koodiarvo + ' ' + t(osa.nimi), [])
  const ammatillisetFilter = useCallback(
    (osa) => {
      const osat = lisättävätTutkinnonOsat.osat.map((o) => o.koodiarvo)
      return (
        osat.length === 0 ||
        (osat.includes(osa.koodiarvo) &&
          !yhteisenTutkinnonOsat.includes(osa.koodiarvo))
      )
    },
    [lisättävätTutkinnonOsat]
  )

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Tutkinnon osan lisäys')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Tutkinto')}
          <DialogSelect
            options={tutkinnot.options}
            value={tutkinto && tutkintoKey(tutkinto)}
            onChange={(opt) => {
              setTutkinto(opt?.value)
              setTunniste(undefined)
            }}
            onSearch={tutkinnot.setQuery}
            testId="tutkinto"
          />
        </label>
        <KoodistoSelect
          addNewText={'Lisää tutkinnon osa'}
          koodistoUri="tutkinnonosat"
          format={format}
          value={tunniste?.koodiarvo}
          filter={ammatillisetFilter}
          onSelect={(tutkinnonOsa) => tutkinnonOsa && setTunniste(tutkinnonOsa)}
          testId="uusi-muu-tutkinnonosa"
        />
        <Spacer />
        <Spacer />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={tutkinto === undefined || tunniste === undefined}
          onClick={() => {
            if (tutkinto && tunniste) {
              onSubmit(tutkinto, tunniste)
            }
          }}
          testId="confirm"
        >
          {t('Lisää tutkinnon osa')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

type NewPaikallinenOsaTutkinnollaModalProps = {
  oppilaitosOid?: string
  onClose: () => void
  onSubmit: (tutkinto: TutkintoPeruste, osa: string) => void
}

const NewPaikallinenOsaTutkinnollaModal = ({
  oppilaitosOid,
  onClose,
  onSubmit
}: NewPaikallinenOsaTutkinnollaModalProps) => {
  const [osa, setOsa] = useState('')
  const [tutkinto, setTutkinto] = useState<TutkintoPeruste | undefined>(
    undefined
  )
  const tutkinnot = useTutkinnot(oppilaitosOid)

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Paikallisen tutkinnon osan lisäys')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Tutkinto')}
          <DialogSelect
            options={tutkinnot.options}
            value={tutkinto && tutkintoKey(tutkinto)}
            onChange={(opt) => setTutkinto(opt?.value)}
            onSearch={tutkinnot.setQuery}
            testId="tutkinto"
          />
        </label>
        <label>
          {t('Tutkinnon osan nimi')}
          <TextEdit
            testId="paikallisen-osan-nimi"
            onChange={(o) => setOsa(o ? o : '')}
            value={osa}
          />
        </label>
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={tutkinto === undefined || osa === ''}
          onClick={() => {
            if (tutkinto) {
              onSubmit(tutkinto, osa)
            }
          }}
          testId="confirm"
        >
          {t('Lisää tutkinnon osa')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
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

type OsasuoritusUseastaTutkinnostaPropertiesProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  osasuoritus: OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus
  osasuoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus
  >
}

const OsasuoritusUseastaTutkinnostaProperties = ({
  form,
  osasuoritus,
  osasuoritusPath
}: OsasuoritusUseastaTutkinnostaPropertiesProps) => {
  if (
    isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus(
      osasuoritus
    )
  ) {
    const yhteinenPath = osasuoritusPath as unknown as FormOptic<
      AmmatillinenOpiskeluoikeus,
      YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
    >
    return (
      <YhteisenOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaOsasuoritusProperties
        form={form}
        osasuoritusPath={yhteinenPath}
        osasuoritus={osasuoritus}
      />
    )
  } else if (
    isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus(
      osasuoritus
    )
  ) {
    const muunPath = osasuoritusPath as unknown as FormOptic<
      AmmatillinenOpiskeluoikeus,
      MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
    >
    return (
      <MuunOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaTutkinnonosanSuoritusProperties
        form={form}
        osasuoritusPath={muunPath}
        osasuoritus={osasuoritus}
      />
    )
  }
}
