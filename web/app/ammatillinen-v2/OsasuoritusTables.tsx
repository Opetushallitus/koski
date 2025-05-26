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
import { localize, t } from '../i18n/i18n'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import {
  AmmatillinenArviointi,
  isAmmatillinenArviointi
} from '../types/fi/oph/koski/schema/AmmatillinenArviointi'
import { Finnish } from '../types/fi/oph/koski/schema/Finnish'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import {
  FieldEditorProps,
  FieldViewerProps,
  FormField
} from '../components-v2/forms/FormField'
import {
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import { OsittaisenAmmatillisenTutkinnonOsanSuoritus } from '../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanSuoritus'
import {
  isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus,
  YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
} from '../types/fi/oph/koski/schema/YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'
import {
  OrganisaatioEdit,
  OrganisaatioView
} from '../components-v2/opiskeluoikeus/OrganisaatioField'
import {
  SuorituksenVahvistusEdit,
  SuorituksenVahvistusView
} from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import {
  OsaamisenTunnustusView,
  TunnustusEdit
} from '../components-v2/opiskeluoikeus/TunnustusField'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FormListField } from '../components-v2/forms/FormListField'
import { AmmatillisenTutkinnonOsanLisätieto } from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsanLisatieto'
import { CommonProps } from '../components-v2/CommonProps'
import { EmptyObject } from '../util/objects'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedTextEdit } from '../components-v2/controls/LocalizedTestField'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { append } from '../util/fp/arrays'
import { Näyttö } from '../types/fi/oph/koski/schema/Naytto'
import { MultilineTextEdit } from '../components-v2/controls/TextField'
import { NäytönSuorituspaikka } from '../types/fi/oph/koski/schema/NaytonSuorituspaikka'
import { DateInput } from '../components-v2/controls/DateInput'
import { NäytönSuoritusaika } from '../types/fi/oph/koski/schema/NaytonSuoritusaika'
import { ISO2FinnishDate, todayISODate } from '../date/date'
import { IconButton } from '../components-v2/controls/IconButton'
import { CHARCODE_REMOVE } from '../components-v2/texts/Icon'
import { NäytönArviointi } from '../types/fi/oph/koski/schema/NaytonArviointi'

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
    columns.Arvosana = <>{t(osasuoritus.arviointi?.[0]?.arvosana.nimi)}</>
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
  }
}

type YhteisenAmmatillisenTutkinnonOsasuoritusPropertiesProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
  >
  osasuoritus: YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
}

const YhteisenOsittaisenAmmatillisenTutkinnonOsasuoritusProperties = ({
  form,
  osasuoritusPath,
  osasuoritus
}: YhteisenAmmatillisenTutkinnonOsasuoritusPropertiesProps) => {
  return (
    <>
      <OsasuoritusProperty label={'Pakollinen'}>
        <OsasuoritusPropertyValue>
          <FormField
            form={form}
            view={BooleanView}
            edit={BooleanEdit}
            path={osasuoritusPath.prop('koulutusmoduuli').prop('pakollinen')}
          />
        </OsasuoritusPropertyValue>
      </OsasuoritusProperty>
      <OsasuoritusProperty label={'Oppilaitos / toimipiste'}>
        <OsasuoritusPropertyValue>
          <FormField
            form={form}
            path={osasuoritusPath.prop('toimipiste')}
            view={OrganisaatioView}
            edit={OrganisaatioEdit}
          />
        </OsasuoritusPropertyValue>
      </OsasuoritusProperty>
      <OsasuoritusProperty label={'Vahvistus'}>
        <OsasuoritusPropertyValue>
          <FormField
            form={form}
            path={osasuoritusPath.prop('vahvistus')}
            view={SuorituksenVahvistusView}
            edit={SuorituksenVahvistusEdit}
          />
        </OsasuoritusPropertyValue>
      </OsasuoritusProperty>
      <OsasuoritusProperty label={'Tunnustettu'}>
        <OsasuoritusPropertyValue>
          <FormField
            form={form}
            path={osasuoritusPath.prop('tunnustettu')}
            view={OsaamisenTunnustusView /*TODO custom komponentti amikselle?*/}
            edit={TunnustusEdit}
          />
        </OsasuoritusPropertyValue>
      </OsasuoritusProperty>
      <OsasuoritusProperty label={'Lisätiedot'}>
        <OsasuoritusPropertyValue>
          <FormListField
            removable
            form={form}
            view={LisätietoView}
            edit={LisätietoEdit}
            path={osasuoritusPath.prop('lisätiedot')}
          />
          {form.editMode && (
            <ButtonGroup>
              <FlatButton
                onClick={() =>
                  form.updateAt(
                    osasuoritusPath.prop('lisätiedot').valueOr([]),
                    append(emptyAmmatillisenTutkinnonOsanLisätieto)
                  )
                }
              >
                {t('Lisää')}
              </FlatButton>
            </ButtonGroup>
          )}
        </OsasuoritusPropertyValue>
      </OsasuoritusProperty>
      {(form.editMode || osasuoritus.näyttö) && (
        <OsasuoritusProperty label={'Näyttö'}>
          <OsasuoritusPropertyValue>
            <FormField
              form={form}
              view={NäyttöView}
              edit={NäyttöEdit}
              path={osasuoritusPath.prop('näyttö')}
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      )}
      <OsasuoritusProperty label={'Arviointi'}>
        <OsasuoritusPropertyValue>
          <KeyValueTable>
            <KeyValueRow localizableLabel={'Arvosana'}>
              <FormField
                form={form}
                view={
                  ParasArvosanaView /*TODO halutaanko pystyä editoimaan kaikki?*/
                }
                edit={ParasArvosanaEdit}
                path={osasuoritusPath.prop('arviointi')}
              />
            </KeyValueRow>
          </KeyValueTable>
        </OsasuoritusPropertyValue>
      </OsasuoritusProperty>
    </>
  )
}

const LisätietoView = ({
  value
}: CommonProps<
  FieldViewerProps<AmmatillisenTutkinnonOsanLisätieto, EmptyObject>
>) => {
  return (
    <>
      <div>{t(value?.tunniste.nimi)}</div>
      <div>{t(value?.kuvaus)}</div>
    </>
  )
}

const emptyAmmatillisenTutkinnonOsanLisätieto =
  AmmatillisenTutkinnonOsanLisätieto({
    tunniste: Koodistokoodiviite({
      koodistoUri: 'ammatillisentutkinnonosanlisatieto',
      koodiarvo: 'mukautettu'
    }),
    kuvaus: localize('')
  })

const LisätietoEdit = ({
  value,
  onChange
}: FieldEditorProps<AmmatillisenTutkinnonOsanLisätieto, EmptyObject>) => {
  return (
    <>
      <KoodistoSelect
        koodistoUri={'ammatillisentutkinnonosanlisatieto'}
        value={value?.tunniste.koodiarvo}
        onSelect={(tunniste) =>
          tunniste &&
          onChange({
            ...emptyAmmatillisenTutkinnonOsanLisätieto,
            ...value,
            tunniste
          })
        }
        testId={'tunniste'}
      />
      <MultilineTextEdit
        value={t(value?.kuvaus)}
        onChange={(kuvaus) =>
          kuvaus &&
          onChange({
            ...emptyAmmatillisenTutkinnonOsanLisätieto,
            ...value,
            kuvaus: localize(kuvaus)
          })
        }
      />
    </>
  )
}

const NäyttöView = ({
  value
}: CommonProps<FieldViewerProps<Näyttö, EmptyObject>>) => {
  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Kuvaus">{t(value?.kuvaus)}</KeyValueRow>
      <KeyValueRow localizableLabel={'Suorituspaikka'}>
        {t(value?.suorituspaikka?.tunniste.nimi)}
        {': '}
        {t(value?.suorituspaikka?.kuvaus)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Suoritusaika">
        {value?.suoritusaika?.alku && ISO2FinnishDate(value.suoritusaika.alku)}
        {' - '}
        {value?.suoritusaika?.loppu &&
          ISO2FinnishDate(value.suoritusaika.loppu)}
      </KeyValueRow>
      <KeyValueRow localizableLabel={'Työssäoppimisen yhteydessä'}>
        <BooleanView value={value?.työssäoppimisenYhteydessä} />
      </KeyValueRow>
      <KeyValueRow localizableLabel={'Haluaa todistuksen'}>
        <BooleanView value={value?.haluaaTodistuksen} />
      </KeyValueRow>
      <NäytönArviointiView value={value?.arviointi} />
    </KeyValueTable>
  )
}

const emptyNäyttö: Näyttö = Näyttö({})

const emptySuoritusaika: NäytönSuoritusaika = NäytönSuoritusaika({
  alku: todayISODate(),
  loppu: todayISODate()
})

const emptySuoritusPaikka: NäytönSuorituspaikka = NäytönSuorituspaikka({
  tunniste: Koodistokoodiviite({
    koodistoUri: 'ammatillisennaytonsuorituspaikka',
    koodiarvo: 'työpaikka'
  }),
  kuvaus: localize('')
})

const NäyttöEdit = ({
  value,
  onChange
}: FieldEditorProps<Näyttö, EmptyObject>) => {
  if (value === undefined) {
    return (
      <ButtonGroup>
        <FlatButton onClick={() => onChange(emptyNäyttö)}>
          {t('Lisää ammattiosaamisen näyttö')}
        </FlatButton>
      </ButtonGroup>
    )
  }

  return (
    <>
      <KeyValueTable>
        <KeyValueRow localizableLabel="Kuvaus">
          <MultilineTextEdit
            value={t(value?.kuvaus)}
            onChange={(kuvaus) =>
              kuvaus &&
              onChange({ ...emptyNäyttö, ...value, kuvaus: localize(kuvaus) })
            }
          />
        </KeyValueRow>
        <KeyValueRow localizableLabel={'Suorituspaikka'}>
          <KoodistoSelect
            value={value?.suorituspaikka?.tunniste.koodiarvo}
            koodistoUri={'ammatillisennaytonsuorituspaikka'}
            onSelect={(tunniste) =>
              tunniste &&
              onChange({
                ...emptyNäyttö,
                ...value,
                suorituspaikka: {
                  ...emptySuoritusPaikka,
                  ...value?.suorituspaikka,
                  tunniste
                }
              })
            }
            testId={'suorituspaikka'}
          />
          <LocalizedTextEdit
            value={value?.suorituspaikka?.kuvaus}
            onChange={(kuvaus) =>
              kuvaus &&
              onChange({
                ...emptyNäyttö,
                ...value,
                suorituspaikka: {
                  ...emptySuoritusPaikka,
                  ...value?.suorituspaikka,
                  kuvaus
                }
              })
            }
          />
        </KeyValueRow>
        <KeyValueRow localizableLabel="Suoritusaika">
          <div className="AikajaksoEdit">
            <DateInput
              value={value?.suoritusaika?.alku}
              onChange={(alku?: string) => {
                alku &&
                  onChange({
                    ...emptyNäyttö,
                    ...value,
                    suoritusaika: NäytönSuoritusaika({
                      ...emptySuoritusaika,
                      ...value?.suoritusaika,
                      alku
                    })
                  })
              }}
              testId="alku"
            />
            <span className="AikajaksoEdit__separator"> {' - '}</span>
            <DateInput
              value={value?.suoritusaika?.loppu}
              onChange={(loppu?: string) => {
                loppu &&
                  onChange({
                    ...emptyNäyttö,
                    ...value,
                    suoritusaika: NäytönSuoritusaika({
                      ...emptySuoritusaika,
                      ...value?.suoritusaika,
                      loppu
                    })
                  })
              }}
              testId="loppu"
            />
          </div>
        </KeyValueRow>
        <KeyValueRow localizableLabel={'Työssäoppimisen yhteydessä'}>
          <BooleanEdit
            value={value?.työssäoppimisenYhteydessä}
            onChange={(työssäoppimisenYhteydessä) => {
              if (työssäoppimisenYhteydessä !== undefined) {
                onChange({
                  ...emptyNäyttö,
                  ...value,
                  työssäoppimisenYhteydessä
                })
              }
            }}
          />
        </KeyValueRow>
        <KeyValueRow localizableLabel={'Haluaa todistuksen'}>
          <BooleanEdit
            value={value?.haluaaTodistuksen}
            onChange={(haluaaTodistuksen) => {
              if (haluaaTodistuksen !== undefined) {
                onChange({ ...emptyNäyttö, ...value, haluaaTodistuksen })
              }
            }}
          />
        </KeyValueRow>
        {/*TODO arvioinnit*/}
        <NäytönArviointiEdit
          value={value?.arviointi}
          onChange={(arviointi) =>
            arviointi && onChange({ ...emptyNäyttö, ...value, arviointi })
          }
        />
      </KeyValueTable>
      <IconButton
        charCode={CHARCODE_REMOVE}
        label={t('Poista')}
        size="input"
        onClick={() => onChange(undefined)}
        testId="delete"
      />
    </>
  )
}

const NäytönArviointiView = ({
  value
}: CommonProps<FieldViewerProps<NäytönArviointi, EmptyObject>>) => {
  return (
    <>
      <KeyValueRow localizableLabel="Arvosana">
        {t(value?.arvosana.nimi)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arviointipäivä">
        {ISO2FinnishDate(value?.päivä)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arvioinnista päättäneet">
        {value?.arvioinnistaPäättäneet?.map((a) => t(a.nimi)).join(', ')}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arviointikeskusteluun osallistuneet">
        {value?.arviointikeskusteluunOsallistuneet
          ?.map((a) => t(a.nimi))
          .join(', ')}
      </KeyValueRow>
    </>
  )
}

const emptyNäytönArviointi: NäytönArviointi = NäytönArviointi({
  päivä: todayISODate(),
  arvosana: Koodistokoodiviite({
    koodistoUri: 'arviointiasteikkoammatillinenhyvaksyttyhylatty',
    koodiarvo: 'hyväksytty'
  })
})

const NäytönArviointiEdit = ({
  value,
  onChange
}: FieldEditorProps<NäytönArviointi, EmptyObject>) => {
  return (
    <>
      <KeyValueRow localizableLabel="Arvosana">
        {t(value?.arvosana.nimi)}
        {/*TODO multi koodisto select*/}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arviointipäivä">
        <DateInput
          value={value?.päivä}
          onChange={(päivä) =>
            päivä && onChange({ ...emptyNäytönArviointi, ...value, päivä })
          }
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arvioinnista päättäneet">
        {value?.arvioinnistaPäättäneet?.map((a, index) => (
          <div className={'AikajaksoEdit'}>
            <KoodistoSelect
              koodistoUri={'ammatillisennaytonarvioinnistapaattaneet'}
              value={a.koodiarvo}
              onSelect={(val) =>
                val &&
                value.arvioinnistaPäättäneet &&
                onChange({
                  ...emptyNäytönArviointi,
                  ...value,
                  arvioinnistaPäättäneet: [
                    ...value.arvioinnistaPäättäneet.slice(0, index),
                    val,
                    ...value.arvioinnistaPäättäneet.slice(index + 1)
                  ]
                })
              }
              testId={'ammatillisennaytonarvioinnistapaattaneet'}
            />
            <IconButton
              charCode={CHARCODE_REMOVE}
              label={t('Poista')}
              size="input"
              onClick={() =>
                value.arvioinnistaPäättäneet &&
                onChange({
                  ...emptyNäytönArviointi,
                  ...value,
                  arvioinnistaPäättäneet: [
                    ...value.arvioinnistaPäättäneet.slice(0, index),
                    ...value.arvioinnistaPäättäneet.slice(index + 1)
                  ]
                })
              }
              testId="delete"
            />
          </div>
        ))}
        <KoodistoSelect
          koodistoUri={'ammatillisennaytonarvioinnistapaattaneet'}
          zeroValueOption
          onSelect={(val) =>
            val &&
            onChange({
              ...emptyNäytönArviointi,
              ...value,
              arvioinnistaPäättäneet: [
                ...(value?.arvioinnistaPäättäneet || []),
                val
              ]
            })
          }
          testId={'ammatillisennaytonarvioinnistapaattaneet-uusi'}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arviointikeskusteluun osallistuneet">
        {value?.arviointikeskusteluunOsallistuneet?.map((a, index) => (
          <div className={'AikajaksoEdit'}>
            <KoodistoSelect
              koodistoUri={
                'ammatillisennaytonarviointikeskusteluunosallistuneet'
              }
              value={a.koodiarvo}
              onSelect={(val) =>
                val &&
                value.arviointikeskusteluunOsallistuneet &&
                onChange({
                  ...emptyNäytönArviointi,
                  ...value,
                  arviointikeskusteluunOsallistuneet: [
                    ...value.arviointikeskusteluunOsallistuneet.slice(0, index),
                    val,
                    ...value.arviointikeskusteluunOsallistuneet.slice(index + 1)
                  ]
                })
              }
              testId={'ammatillisennaytonarviointikeskusteluunosallistuneet'}
            />
            <IconButton
              charCode={CHARCODE_REMOVE}
              label={t('Poista')}
              size="input"
              onClick={() =>
                value.arviointikeskusteluunOsallistuneet &&
                onChange({
                  ...emptyNäytönArviointi,
                  ...value,
                  arviointikeskusteluunOsallistuneet: [
                    ...value.arviointikeskusteluunOsallistuneet.slice(0, index),
                    ...value.arviointikeskusteluunOsallistuneet.slice(index + 1)
                  ]
                })
              }
              testId="delete"
            />
          </div>
        ))}
        <KoodistoSelect
          koodistoUri={'ammatillisennaytonarviointikeskusteluunosallistuneet'}
          zeroValueOption
          onSelect={(val) =>
            val &&
            onChange({
              ...emptyNäytönArviointi,
              ...value,
              arviointikeskusteluunOsallistuneet: [
                ...(value?.arviointikeskusteluunOsallistuneet || []),
                val
              ]
            })
          }
          testId={'ammatillisennaytonarviointikeskusteluunosallistuneet-uusi'}
        />
      </KeyValueRow>
    </>
  )
}
