import React, { ReactNode, useState } from 'react'
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
  ryhmä
}: TableProps) => {
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
      return tutkinnonOsatToTableRow({
        suoritusIndex: osittainenPäätasonSuoritus.index,
        osasuoritusIndex: originalIndex,
        suoritusPath: osittainenPäätasonSuoritus.path,
        form,
        level: 0,
        tutkinnonOsaRyhmä: ryhmä
      })
    })

  if (!form.editMode && (!rows || rows.length === 0)) {
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
      addNewOsasuoritusView={() => (
        <NewAmisOsasuoritus
          form={form}
          ryhmä={ryhmä}
          suoritusPath={osittainenPäätasonSuoritus.path}
        />
      )}
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

  if (hasAmmatillinenArviointi(osasuoritus)) {
    columns.Arvosana = <ParasArvosanaView value={osasuoritus.arviointi} />
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
    ryhmät !== null
      ? (ryhmät.find((r) => (r.koodiviite.nimi as Finnish).fi === ryhmä)
          ?.koodiviite as Koodistokoodiviite<
          'ammatillisentutkinnonosanryhma',
          '1' | '3' | '4'
        >)
      : undefined

  if (!ryhmäKoodi) {
    return null
  }

  if (ryhmä === 'Yhteiset tutkinnon osat') {
    return (
      <>
        <KoodistoSelect
          addNewText={'Lisää tutkinnon osa'}
          koodistoUri="tutkinnonosat"
          format={(osa) => osa.koodiarvo + ' ' + t(osa.nimi)}
          filter={(osa) => yhteisenTutkinnonOsat.includes(osa.koodiarvo)}
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
          format={(osa) => osa.koodiarvo + ' ' + t(osa.nimi)}
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
