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
import { useKoodisto } from '../appstate/koodisto'
import { TextEdit } from '../components-v2/controls/TextField'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { MuuValtakunnallinenTutkinnonOsa } from '../types/fi/oph/koski/schema/MuuValtakunnallinenTutkinnonOsa'
import { useTutkinnonOsaRyhmät } from './useTutkinnonOsaRyhmät'
import { useTutkinnonOsat } from './useTutkinnonOsat'
import { AmisArvosanaInTableEdit, AmisArvosanaInTableView } from './Arviointi'
import { tutkintoKey, useTutkinnot } from './useTutkinnot'
import { DialogSelect } from '../uusiopiskeluoikeus/components/DialogSelect'
import { TutkintoPeruste } from '../types/fi/oph/koski/tutkinto/TutkintoPeruste'
import { AmmatillinenTutkintoKoulutus } from '../types/fi/oph/koski/schema/AmmatillinenTutkintoKoulutus'
import { Spacer } from '../components-v2/layout/Spacer'

interface OsasuoritusTablesProps {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  oppilaitosOid?: string
  osittainenPäätasonSuoritus: ActivePäätasonSuoritus<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenSuoritus
  >
}

export const OsasuoritusTables = ({
  form,
  oppilaitosOid,
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
        oppilaitosOid={oppilaitosOid}
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
        oppilaitosOid={oppilaitosOid}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Ammatilliset tutkinnon osat"
        perusteenRyhmät={perusteenRyhmät}
      />
      <TableForTutkinnonOsaRyhmä
        form={form}
        oppilaitosOid={oppilaitosOid}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Yhteiset tutkinnon osat"
        perusteenRyhmät={perusteenRyhmät}
        forceOpen={form.editMode}
      />
      <TableForTutkinnonOsaRyhmä
        form={form}
        oppilaitosOid={oppilaitosOid}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Vapaasti valittavat tutkinnon osat"
        perusteenRyhmät={perusteenRyhmät}
      />
      <TableForTutkinnonOsaRyhmä
        form={form}
        oppilaitosOid={oppilaitosOid}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Tutkintoa yksilöllisesti laajentavat tutkinnon osat"
        perusteenRyhmät={perusteenRyhmät}
      />
      <TableForTutkinnonOsaRyhmä
        form={form}
        oppilaitosOid={oppilaitosOid}
        osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        ryhmä="Muut suoritukset"
        perusteenRyhmät={perusteenRyhmät}
      />
    </>
  )
}

interface TableProps {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  oppilaitosOid?: string
  osittainenPäätasonSuoritus: ActivePäätasonSuoritus<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenSuoritus
  >
  ryhmä: string
  perusteenRyhmät: Koodistokoodiviite<'tutkinnonosaryhmä'>[]
  forceOpen?: boolean
}

export const dummyRow = <T extends string>(
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
  oppilaitosOid,
  osittainenPäätasonSuoritus,
  ryhmä,
  perusteenRyhmät,
  forceOpen
}: TableProps) => {
  const originalIndexMap: Record<number, number> = {}

  const matchFilter = (s: OsittaisenAmmatillisenTutkinnonOsanSuoritus) =>
    (s.tutkinnonOsanRyhmä?.nimi as Finnish | undefined)?.fi === ryhmä ||
    ryhmä === 'Tutkinnon osat' ||
    (s.tutkinnonOsanRyhmä === undefined && ryhmä === 'Muut suoritukset')

  const rows = osittainenPäätasonSuoritus.suoritus.osasuoritukset
    ?.map((s, originalIndex) => ({ s, originalIndex }))
    .filter(({ s }) => matchFilter(s))
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
      forceOpen={forceOpen}
      rows={
        (!rows || rows?.length === 0) && form.editMode
          ? [dummyRow(ryhmä)] // Saadaan headeri näkymään editointimoodissa kun osasuorituksia ei ole
          : rows || []
      }
      addNewOsasuoritusView={NewAmisOsasuoritus}
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
        if (
          osasuoritus === undefined ||
          isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(
            osasuoritus
          ) ||
          isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
            osasuoritus
          )
        ) {
          // Näiden tyyppien tila tulee niiden alaosasuorituksista, ei
          // ylätason arvioinnista, joten päärivillä ei näytetä
          // valmis/kesken-merkkiä.
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
export const yhteisenTutkinnonOsat = [
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

export type YhteisenTutkinnonOsatTunniste = Koodistokoodiviite<
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

type NewAmisOsasuoritusProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  oppilaitosOid?: string
  ryhmä: string
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenSuoritus
  >
}

const NewAmisOsasuoritus = ({
  form,
  oppilaitosOid,
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
  const osasuoritukset =
    getValue(suoritusPath.prop('osasuoritukset'))(form.state) || []
  const onReformi =
    getValue(suoritusPath.prop('suoritustapa').prop('koodiarvo'))(
      form.state
    ) === 'reformi'
  const onAmmatillisetRyhmä = ryhmäKoodi.koodiarvo === '1'
  const showLisääKorkeakouluopinto =
    onAmmatillisetRyhmä &&
    onReformi &&
    !osasuoritukset.some(
      isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
    )
  const showLisääJatkoOpinto =
    onAmmatillisetRyhmä &&
    onReformi &&
    !osasuoritukset.some(
      isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
    )

  return (
    <>
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
        <Column span={6}>
          <NewToisestaTutkinnosta
            form={form}
            oppilaitosOid={oppilaitosOid}
            ryhmäKoodi={ryhmäKoodi}
            suoritusPath={suoritusPath}
          />
        </Column>
      </ColumnRow>
      {showLisääKorkeakouluopinto && (
        <FlatButton
          withAddIcon
          onClick={() =>
            form.updateAt(
              suoritusPath.prop('osasuoritukset').valueOr([]),
              (a) => [
                ...a,
                OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus({
                  tutkinnonOsanRyhmä: ryhmäKoodi as Koodistokoodiviite<
                    'ammatillisentutkinnonosanryhma',
                    '1'
                  >
                })
              ]
            )
          }
          testId="uusi-korkeakouluopinto"
        >
          {t('Lisää korkeakouluopintoja')}
        </FlatButton>
      )}
      {showLisääJatkoOpinto && (
        <FlatButton
          withAddIcon
          onClick={() =>
            form.updateAt(
              suoritusPath.prop('osasuoritukset').valueOr([]),
              (a) => [
                ...a,
                OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
                  {
                    tutkinnonOsanRyhmä: ryhmäKoodi as Koodistokoodiviite<
                      'ammatillisentutkinnonosanryhma',
                      '1'
                    >
                  }
                )
              ]
            )
          }
          testId="uusi-jatko-opintovalmiuksia"
        >
          {t(
            'Lisää yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja'
          )}
        </FlatButton>
      )}
    </>
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
      <FlatButton withAddIcon onClick={() => setShowModal(true)}>
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

const newMuuOsaToisestaTutkinnosta = (
  tutkinto: TutkintoPeruste,
  osa: Koodistokoodiviite<'tutkinnonosat', string>,
  ryhmä: Koodistokoodiviite<'ammatillisentutkinnonosanryhma', '1' | '3' | '4'>
): MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus => {
  return MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus({
    koulutusmoduuli: MuuValtakunnallinenTutkinnonOsa({
      tunniste: osa,
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
  })
}

type NewToisestaTutkinnostaProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  oppilaitosOid?: string
  ryhmäKoodi: Koodistokoodiviite<
    'ammatillisentutkinnonosanryhma',
    '1' | '3' | '4'
  >
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenSuoritus
  >
}

const NewToisestaTutkinnosta = ({
  form,
  oppilaitosOid,
  ryhmäKoodi,
  suoritusPath
}: NewToisestaTutkinnostaProps) => {
  const [showModal, setShowModal] = useState(false)

  return (
    <>
      <FlatButton withAddIcon onClick={() => setShowModal(true)}>
        {t('Lisää tutkinnon osa toisesta tutkinnosta')}
      </FlatButton>
      {showModal && (
        <NewToisestaTutkinnostaModal
          oppilaitosOid={oppilaitosOid}
          ryhmäKoodiArvo={ryhmäKoodi.koodiarvo}
          onClose={() => setShowModal(false)}
          onSubmit={(tutkinto, osa) => {
            form.updateAt(
              suoritusPath.prop('osasuoritukset').valueOr([]),
              (a) => [
                ...a,
                newMuuOsaToisestaTutkinnosta(tutkinto, osa, ryhmäKoodi)
              ]
            )
            setShowModal(false)
          }}
        />
      )}
    </>
  )
}

type NewToisestaTutkinnostaModalProps = {
  oppilaitosOid?: string
  ryhmäKoodiArvo: string
  onClose: () => void
  onSubmit: (
    tutkinto: TutkintoPeruste,
    osa: Koodistokoodiviite<'tutkinnonosat', string>
  ) => void
}

const NewToisestaTutkinnostaModal = ({
  oppilaitosOid,
  ryhmäKoodiArvo,
  onClose,
  onSubmit
}: NewToisestaTutkinnostaModalProps) => {
  const [tunniste, setTunniste] = useState<
    Koodistokoodiviite<'tutkinnonosat', string> | undefined
  >(undefined)
  const [tutkinto, setTutkinto] = useState<TutkintoPeruste | undefined>(
    undefined
  )
  const tutkinnot = useTutkinnot(oppilaitosOid)
  const lisättävätTutkinnonOsat = useTutkinnonOsat(
    tutkinto?.diaarinumero,
    ryhmäKoodiArvo
  ).osat.map((o) => o.koodiarvo)

  const format = useCallback((osa) => osa.koodiarvo + ' ' + t(osa.nimi), [])
  const ammatillisetFilter = useCallback(
    (osa) => {
      return lisättävätTutkinnonOsat.length === 0
        ? !yhteisenTutkinnonOsat.includes(osa.koodiarvo)
        : lisättävätTutkinnonOsat.includes(osa.koodiarvo) &&
            !yhteisenTutkinnonOsat.includes(osa.koodiarvo)
    },
    [lisättävätTutkinnonOsat]
  )

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Tutkinnon osan lisäys toisesta tutkinnosta')}</ModalTitle>
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
            onSearch={tutkinnot.setDebounceQuery}
            testId="tutkinto"
          />
        </label>
        <Spacer />
        <KoodistoSelect
          addNewText={'Lisää tutkinnon osa'}
          koodistoUri="tutkinnonosat"
          format={format}
          value={tunniste?.koodiarvo}
          filter={ammatillisetFilter}
          zeroValueOption={lisättävätTutkinnonOsat.length === 0}
          onSelect={(osa) => osa && setTunniste(osa)}
          testId="uusi-muu-tutkinnonosa-toisesta-tutkinnosta"
        />
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
