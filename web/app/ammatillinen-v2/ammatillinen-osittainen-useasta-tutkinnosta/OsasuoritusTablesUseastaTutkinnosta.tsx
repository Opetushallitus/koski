import {
  FormModel,
  FormOptic,
  getValue
} from '../../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { ActivePäätasonSuoritus } from '../../components-v2/containers/EditorContainer'
import { AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus } from '../../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus'
import React, { ReactNode, useCallback, useState } from 'react'
import { Finnish } from '../../types/fi/oph/koski/schema/Finnish'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { deleteAt } from '../../util/fp/arrays'
import {
  dummyRow,
  hasAmmatillinenArviointi,
  yhteisenTutkinnonOsat,
  YhteisenTutkinnonOsatTunniste
} from '../OsasuoritusTables'
import { useKoodisto, useKoodistoFiller } from '../../appstate/koodisto'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Column, ColumnRow } from '../../components-v2/containers/Columns'
import { finnish, t } from '../../i18n/i18n'
import { FormField } from '../../components-v2/forms/FormField'
import {
  LaajuusEdit,
  LaajuusView
} from '../../components-v2/opiskeluoikeus/LaajuusField'
import { LaajuusOsaamispisteissä } from '../../types/fi/oph/koski/schema/LaajuusOsaamispisteissa'
import { AmisArvosanaInTableEdit, AmisArvosanaInTableView } from '../Arviointi'
import { OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus } from '../../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus'
import {
  isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus,
  YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
} from '../../types/fi/oph/koski/schema/YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'
import { YhteisenOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaOsasuoritusProperties } from './YhteisenOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaOsasuoritusProperties'
import {
  isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus,
  MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
} from '../../types/fi/oph/koski/schema/MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'
import { MuunOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaTutkinnonosanSuoritusProperties } from './MuunOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaTutkinnonosanSuoritusProperties'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { TutkintoPeruste } from '../../types/fi/oph/koski/tutkinto/TutkintoPeruste'
import { tutkintoKey, useTutkinnot } from '../useTutkinnot'
import { useTutkinnonOsat } from '../useTutkinnonOsat'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../../components-v2/containers/Modal'
import { DialogSelect } from '../../uusiopiskeluoikeus/components/DialogSelect'
import { Spacer } from '../../components-v2/layout/Spacer'
import { KoodistoSelect } from '../../components-v2/opiskeluoikeus/KoodistoSelect'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import { TextEdit } from '../../components-v2/controls/TextField'
import { PaikallinenTutkinnonOsa } from '../../types/fi/oph/koski/schema/PaikallinenTutkinnonOsa'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { AmmatillinenTutkintoKoulutus } from '../../types/fi/oph/koski/schema/AmmatillinenTutkintoKoulutus'
import { YhteinenTutkinnonOsa } from '../../types/fi/oph/koski/schema/YhteinenTutkinnonOsa'
import { MuuValtakunnallinenTutkinnonOsa } from '../../types/fi/oph/koski/schema/MuuValtakunnallinenTutkinnonOsa'

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
    </>
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
        (s.tutkinnonOsanRyhmä === undefined &&
          ryhmä === 'Ammatilliset tutkinnon osat')
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
          suoritusPath={suoritusPath}
        />
      </Column>
      <Column span={6}>
        <NewPaikallinenOsaTutkinnolla
          form={form}
          oppilaitosOid={oppilaitosOid}
          suoritusPath={suoritusPath}
        />
      </Column>
    </ColumnRow>
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
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
  >
}

const NewMuuTutkinnonOsaTutkinnolla = ({
  form,
  oppilaitosOid,
  suoritusPath
}: NewMuuTutkinnonOsaTutkinnollaProps) => {
  const [showModal, setShowModal] = useState(false)
  const fillNimet = useKoodistoFiller()

  return (
    <>
      <FlatButton
        testId="lisaa-ammatillisen-tutkinnon-osa"
        onClick={() => setShowModal(true)}
      >
        {t('Lisää tutkinnon osa')}
      </FlatButton>
      {showModal && (
        <NewMuuTutkinnonOsaTutkinnollaModal
          oppilaitosOid={oppilaitosOid}
          onClose={() => setShowModal(false)}
          onSubmit={async (tutkinto, tunniste) => {
            const osasuoritus = await fillNimet(
              newMuuTutkinnonOsaTutkinnolla(tutkinto, tunniste)
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
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
  >
}

const NewPaikallinenOsaTutkinnolla = ({
  form,
  oppilaitosOid,
  suoritusPath
}: NewPaikallinenOsaTutkinnollaProps) => {
  const [showModal, setShowModal] = useState(false)
  const fillNimet = useKoodistoFiller()

  return (
    <>
      <FlatButton
        testId="lisaa-paikallinen-osa"
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
              newPaikallinenOsaTutkinnolla(tutkinto, osa)
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
  ).osat.map((o) => o.koodiarvo)

  // Näiden memoizeaaminen parantaa KoodistoSelectin suorituskykyä huomattavasti
  const format = useCallback((osa) => osa.koodiarvo + ' ' + t(osa.nimi), [])
  const yhteisetFilter = useCallback(
    (osa) => {
      return lisättävätTutkinnonOsat.length === 0
        ? false
        : lisättävätTutkinnonOsat.includes(osa.koodiarvo)
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
          filter={yhteisetFilter}
          zeroValueOption={lisättävätTutkinnonOsat.length === 0}
          onSelect={(tutkinnonOsa) => {
            const yhteinenTunniste =
              tutkinnonOsa as YhteisenTutkinnonOsatTunniste
            tutkinnonOsa && setTunniste(yhteinenTunniste)
          }}
          testId="uusi-yhteinen-tutkinnonosa"
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

type NewMuuTutkinnonOsaTutkinnollaModalProps = {
  oppilaitosOid?: string
  onClose: () => void
  onSubmit: (
    tutkinto: TutkintoPeruste,
    osa: Koodistokoodiviite<'tutkinnonosat', string>
  ) => void
}

const NewMuuTutkinnonOsaTutkinnollaModal = ({
  oppilaitosOid,
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
    '1' // Ammatillisen tutkinnon osan ryhmä
  ).osat.map((o) => o.koodiarvo)

  // Näiden memoizeaaminen parantaa KoodistoSelectin suorituskykyä huomattavasti
  const format = useCallback((osa) => osa.koodiarvo + ' ' + t(osa.nimi), [])
  const ammatillisetFilter = useCallback(
    (osa) => {
      return lisättävätTutkinnonOsat.length === 0
        ? false
        : lisättävätTutkinnonOsat.includes(osa.koodiarvo) &&
            !yhteisenTutkinnonOsat.includes(osa.koodiarvo)
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
          onSelect={(tutkinnonOsa) => tutkinnonOsa && setTunniste(tutkinnonOsa)}
          testId="uusi-muu-tutkinnonosa"
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
            onSearch={tutkinnot.setDebounceQuery}
            testId="tutkinto"
          />
        </label>
        <Spacer />
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
  tunniste: Koodistokoodiviite<'tutkinnonosat', string>
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
      tutkinnonOsanRyhmä: Koodistokoodiviite({
        koodiarvo: '1', // Ammatillisen tutkinnon osan ryhmä
        koodistoUri: 'ammatillisentutkinnonosanryhma'
      })
    }
  )
}

const newPaikallinenOsaTutkinnolla = (
  tutkinto: TutkintoPeruste,
  osa: string
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
      tutkinnonOsanRyhmä: Koodistokoodiviite({
        koodiarvo: '1', // Ammatillisen tutkinnon osan ryhmä
        koodistoUri: 'ammatillisentutkinnonosanryhma'
      })
    }
  )
}
