import {
  FormModel,
  FormOptic,
  getValue
} from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import React from 'react'
import { AmmatillisenOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/AmmatillisenOpiskeluoikeudenLisatiedot'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import {
  FieldEditorProps,
  FieldViewerProps,
  FormField
} from '../components-v2/forms/FormField'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import { FormListField } from '../components-v2/forms/FormListField'
import {
  AikajaksoEdit,
  AikajaksoView
} from '../components-v2/opiskeluoikeus/AikajaksoField'
import { Aikajakso } from '../types/fi/oph/koski/schema/Aikajakso'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { emptyLocalizedString, t } from '../i18n/i18n'
import { ISO2FinnishDate, todayISODate } from '../date/date'
import { append } from '../util/fp/arrays'
import {
  emptyUlkomaanjakso,
  UlkomaanjaksoEdit,
  UlkomaanjaksoView
} from '../components-v2/opiskeluoikeus/UlkomaanjaksoField'
import { TestIdText } from '../appstate/useTestId'
import { Hojks } from '../types/fi/oph/koski/schema/Hojks'
import { CommonProps } from '../components-v2/CommonProps'
import { EmptyObject } from '../util/objects'
import { DateInput } from '../components-v2/controls/DateInput'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { OsaAikaisuusJakso } from '../types/fi/oph/koski/schema/OsaAikaisuusJakso'
import { NumberField } from '../components-v2/controls/NumberField'
import { OpiskeluvalmiuksiaTukevienOpintojenJakso } from '../types/fi/oph/koski/schema/OpiskeluvalmiuksiaTukevienOpintojenJakso'
import {
  LocalizedTextEdit,
  LocalizedTextView
} from '../components-v2/controls/LocalizedTestField'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import {
  emptyMaksuttomuuus,
  MaksuttomuusEdit,
  MaksuttomuusView
} from '../components-v2/opiskeluoikeus/MaksuttomuusField'
import { IconButton } from '../components-v2/controls/IconButton'
import { CHARCODE_REMOVE } from '../components-v2/texts/Icon'

interface AmmatillinenLisatiedotProps {
  form: FormModel<AmmatillinenOpiskeluoikeus>
}

export const AmmatillinenLisatiedot: React.FC<AmmatillinenLisatiedotProps> = ({
  form
}) => {
  const emptyLisatiedot = AmmatillisenOpiskeluoikeudenLisätiedot()
  const lisatiedotPath = form.root.prop('lisätiedot').valueOr(emptyLisatiedot)
  const lisätiedot = getValue(lisatiedotPath)(form.state)

  const AikajaksoRow: React.FC<{
    localizableLabel: string
    path: keyof AmmatillisenOpiskeluoikeudenLisätiedot
  }> = ({ localizableLabel, path }) => {
    const aikajaksoPath = lisatiedotPath.prop(path) as FormOptic<
      AmmatillinenOpiskeluoikeus,
      Aikajakso[] | undefined
    >
    return (
      <KeyValueRow localizableLabel={localizableLabel} largeLabel>
        <FormListField
          form={form}
          view={AikajaksoView}
          edit={AikajaksoEdit}
          path={aikajaksoPath}
          editProps={{
            createAikajakso: Aikajakso
          }}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  aikajaksoPath.valueOr([]),
                  append(Aikajakso({ alku: todayISODate() }))
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>
    )
  }

  return (
    <KeyValueTable>
      <KeyValueRow
        localizableLabel="Oikeus maksuttomaan asuntolapaikkaan"
        largeLabel
      >
        <FormField
          form={form}
          view={BooleanView}
          edit={BooleanEdit}
          path={lisatiedotPath.prop('oikeusMaksuttomaanAsuntolapaikkaan')}
        />
      </KeyValueRow>

      <AikajaksoRow localizableLabel={'Majoitus'} path={'majoitus'} />
      <AikajaksoRow
        localizableLabel={'Sisäoppilaitosmainen majoitus'}
        path={'sisäoppilaitosmainenMajoitus'}
      />
      <AikajaksoRow
        localizableLabel={
          'Vaativan erityisen tuen yhteydessä järjestettävä majoitus'
        }
        path={'vaativanErityisenTuenYhteydessäJärjestettäväMajoitus'}
      />
      <AikajaksoRow
        localizableLabel={'Erityinen tuki'}
        path={'erityinenTuki'}
      />
      <AikajaksoRow
        localizableLabel={'Vaativan erityisen tuen erityinen tehtävä'}
        path={'vaativanErityisenTuenErityinenTehtävä'}
      />

      <KeyValueRow localizableLabel="Ulkomaanjaksot" largeLabel>
        <FormListField
          form={form}
          path={lisatiedotPath.prop('ulkomaanjaksot')}
          view={UlkomaanjaksoView}
          edit={UlkomaanjaksoEdit}
          testId="ulkomaanjaksot"
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  lisatiedotPath.prop('ulkomaanjaksot').valueOr([]),
                  append(emptyUlkomaanjakso)
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <KeyValueRow localizableLabel="Hojks" largeLabel>
        {lisätiedot?.hojks ? (
          <FormField
            form={form}
            view={HojksView}
            edit={HojksEdit}
            path={lisatiedotPath.prop('hojks')}
          />
        ) : (
          form.editMode && (
            <ButtonGroup>
              <FlatButton
                onClick={() =>
                  form.updateAt(lisatiedotPath.prop('hojks'), () => emptyHojks)
                }
              >
                {t('Lisää')}
              </FlatButton>
            </ButtonGroup>
          )
        )}
      </KeyValueRow>

      <AikajaksoRow
        localizableLabel={'Vaikeasti vammaisille järjestetty opetus'}
        path={'vaikeastiVammainen'}
      />

      <AikajaksoRow
        localizableLabel={'Vammainen ja avustaja'}
        path={'vammainenJaAvustaja'}
      />

      <KeyValueRow localizableLabel="Osa-aikaisuusjaksot" largeLabel>
        <FormListField
          form={form}
          view={OsaAikaisuusView}
          edit={OsaAikaisuusEdit}
          path={lisatiedotPath.prop('osaAikaisuusjaksot')}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  lisatiedotPath.prop('osaAikaisuusjaksot').valueOr([]),
                  append(emptyOsaAikausuus)
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <KeyValueRow
        localizableLabel="Opiskeluvalmiuksia tukevat opinnot"
        largeLabel
      >
        <FormListField
          form={form}
          view={OpiskeluvalmiuksiaTuvkevienOpintojenJaksoView}
          edit={OpiskeluvalmiuksiaTuvkevienOpintojenJaksoEdit}
          path={lisatiedotPath.prop('opiskeluvalmiuksiaTukevatOpinnot')}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  lisatiedotPath
                    .prop('opiskeluvalmiuksiaTukevatOpinnot')
                    .valueOr([]),
                  append(emptyOpiskeluvalmiuksiaTuvkevienOpintojenJakso)
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <KeyValueRow localizableLabel="Henkilöstökoulutus" largeLabel>
        <FormField
          form={form}
          view={BooleanView}
          edit={BooleanEdit}
          path={lisatiedotPath.prop('henkilöstökoulutus')}
        />
      </KeyValueRow>

      <AikajaksoRow
        localizableLabel={'Vankilaopetuksessa'}
        path={'vankilaopetuksessa'}
      />

      <KeyValueRow localizableLabel="Koulutusvienti" largeLabel>
        <FormField
          form={form}
          view={BooleanView}
          edit={BooleanEdit}
          path={lisatiedotPath.prop('koulutusvienti')}
        />
      </KeyValueRow>

      <KeyValueRow localizableLabel="Koulutuksen maksuttomuus" largeLabel>
        <FormListField
          form={form}
          path={lisatiedotPath.prop('maksuttomuus')}
          view={MaksuttomuusView}
          edit={MaksuttomuusEdit}
          testId="maksuttomuus"
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  lisatiedotPath.prop('maksuttomuus').valueOr([]),
                  append(emptyMaksuttomuuus)
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <AikajaksoRow
        localizableLabel={'Oikeutta maksuttomuuteen pidennetty'}
        path={'oikeuttaMaksuttomuuteenPidennetty'}
      />

      <KeyValueRow localizableLabel="JOTPA asianumero" largeLabel>
        {form.editMode ? (
          <KoodistoSelect
            koodistoUri="jotpaasianumero"
            addNewText={t('Ei valintaa')}
            zeroValueOption
            onSelect={(koodiviite) => {
              form.updateAt(lisatiedotPath, (lisatiedot) => {
                return {
                  ...emptyLisatiedot,
                  ...lisatiedot,
                  jotpaAsianumero: koodiviite
                }
              })
            }}
            value={
              getValue(lisatiedotPath.prop('jotpaAsianumero'))(form.state)
                ?.koodiarvo
            }
            testId="jotpaasianumero"
          />
        ) : (
          <LocalizedTextView
            value={
              getValue(lisatiedotPath.prop('jotpaAsianumero'))(form.state)?.nimi
            }
          />
        )}
      </KeyValueRow>

      <KeyValueRow
        localizableLabel="Siirtynyt tutkinnon uusiin perusteisiin"
        largeLabel
      >
        <FormField
          form={form}
          view={BooleanView}
          edit={BooleanEdit}
          path={lisatiedotPath.prop('siirtynytUusiinTutkinnonPerusteisiin')}
        />
      </KeyValueRow>
    </KeyValueTable>
  )
}

const HojksView = <T extends Hojks>({
  value
}: CommonProps<FieldViewerProps<T | undefined, EmptyObject>>) => {
  return (
    <div>
      <TestIdText id="opetusryhmä">{t(value?.opetusryhmä.nimi)}</TestIdText>{' '}
      {' - '}
      <TestIdText id="alku">
        {value?.alku && ISO2FinnishDate(value.alku)}
      </TestIdText>{' '}
      {' - '}
      <TestIdText id="loppu">
        {value?.loppu && ISO2FinnishDate(value.loppu)}
      </TestIdText>
    </div>
  )
}

const emptyHojks: Hojks = Hojks({
  opetusryhmä: Koodistokoodiviite({
    koodiarvo: '',
    koodistoUri: 'opetusryhma'
  }),
  alku: todayISODate()
})

const HojksEdit = ({
  value,
  onChange
}: CommonProps<FieldEditorProps<Hojks | undefined, EmptyObject>>) => {
  return (
    <div className="Removable">
      <div className="AikajaksoEdit Removable__content">
        <KoodistoSelect
          koodistoUri={'opetusryhma'}
          value={value?.opetusryhmä.koodiarvo}
          onSelect={(
            opetusryhmä: Koodistokoodiviite<'opetusryhma'> | undefined
          ) => {
            opetusryhmä && onChange({ ...emptyHojks, ...value, opetusryhmä })
          }}
          testId={'opetusryhmä'}
        />
        <span className="AikajaksoEdit__separator"> {' - '}</span>
        <DateInput
          value={value?.alku}
          onChange={(alku?: string) => {
            alku && onChange({ ...emptyHojks, ...value, alku })
          }}
          testId="alku"
        />
        <span className="AikajaksoEdit__separator"> {' - '}</span>
        <DateInput
          value={value?.loppu}
          onChange={(loppu?: string) => {
            loppu && onChange({ ...emptyHojks, ...value, loppu })
          }}
          testId="loppu"
        />
      </div>
      <IconButton
        charCode={CHARCODE_REMOVE}
        label={t('Poista')}
        onClick={() => onChange(undefined)}
        size="input"
      />
    </div>
  )
}

const OsaAikaisuusView = <T extends OsaAikaisuusJakso>({
  value
}: CommonProps<FieldViewerProps<T | undefined, EmptyObject>>) => {
  return (
    <div>
      <TestIdText id="alku">
        {value?.alku && ISO2FinnishDate(value.alku)}
      </TestIdText>{' '}
      {' - '}
      <TestIdText id="loppu">
        {value?.loppu && ISO2FinnishDate(value.loppu)}
      </TestIdText>
      {' - '}
      <TestIdText id="osaAikaisuus">{value?.osaAikaisuus}</TestIdText>
      {'%'}
    </div>
  )
}

const emptyOsaAikausuus = OsaAikaisuusJakso({
  alku: todayISODate(),
  osaAikaisuus: 0
})

const OsaAikaisuusEdit = ({
  value,
  onChange
}: FieldEditorProps<OsaAikaisuusJakso | undefined, EmptyObject>) => {
  return (
    <div className="AikajaksoEdit">
      <DateInput
        value={value?.alku}
        onChange={(alku?: string) => {
          alku && onChange({ ...emptyOsaAikausuus, ...value, alku })
        }}
        testId="alku"
      />
      <span className="AikajaksoEdit__separator"> {' - '}</span>
      <DateInput
        value={value?.loppu}
        onChange={(loppu?: string) => {
          loppu && onChange({ ...emptyOsaAikausuus, ...value, loppu })
        }}
        testId="loppu"
      />
      <NumberField
        value={value?.osaAikaisuus}
        onChange={(osaAikaisuus?: number) => {
          osaAikaisuus &&
            onChange({ ...emptyOsaAikausuus, ...value, osaAikaisuus })
        }}
      />
      {'%'}
    </div>
  )
}

const OpiskeluvalmiuksiaTuvkevienOpintojenJaksoView = <
  T extends OpiskeluvalmiuksiaTukevienOpintojenJakso
>({
  value
}: CommonProps<FieldViewerProps<T | undefined, EmptyObject>>) => {
  return (
    <div>
      <TestIdText id="alku">
        {value?.alku && ISO2FinnishDate(value.alku)}
      </TestIdText>{' '}
      {' - '}
      <TestIdText id="loppu">
        {value?.loppu && ISO2FinnishDate(value.loppu)}
      </TestIdText>
      {' - '}
      <TestIdText id="kuvaus">{t(value?.kuvaus)}</TestIdText>
    </div>
  )
}

const emptyOpiskeluvalmiuksiaTuvkevienOpintojenJakso =
  OpiskeluvalmiuksiaTukevienOpintojenJakso({
    alku: todayISODate(),
    loppu: todayISODate(),
    kuvaus: emptyLocalizedString
  })

const OpiskeluvalmiuksiaTuvkevienOpintojenJaksoEdit = ({
  value,
  onChange
}: FieldEditorProps<
  OpiskeluvalmiuksiaTukevienOpintojenJakso | undefined,
  EmptyObject
>) => {
  return (
    <div className="AikajaksoEdit">
      <DateInput
        value={value?.alku}
        onChange={(alku?: string) => {
          alku &&
            onChange({
              ...emptyOpiskeluvalmiuksiaTuvkevienOpintojenJakso,
              ...value,
              alku
            })
        }}
        testId="alku"
      />
      <span className="AikajaksoEdit__separator"> {' - '}</span>
      <DateInput
        value={value?.loppu}
        onChange={(loppu?: string) => {
          loppu &&
            onChange({
              ...emptyOpiskeluvalmiuksiaTuvkevienOpintojenJakso,
              ...value,
              loppu
            })
        }}
        testId="loppu"
      />
      <LocalizedTextEdit
        value={value?.kuvaus}
        onChange={(kuvaus?: LocalizedString) => {
          kuvaus &&
            onChange({
              ...emptyOpiskeluvalmiuksiaTuvkevienOpintojenJakso,
              ...value,
              kuvaus
            })
        }}
        testId="kuvaus"
        placeholder={t('Kuvaus')}
      />
    </div>
  )
}
