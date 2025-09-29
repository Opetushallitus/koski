import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import * as Ord from 'fp-ts/Ord'
import * as string from 'fp-ts/string'
import React from 'react'
import { CommonProps } from '../components-v2/CommonProps'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { DateView } from '../components-v2/controls/DateField'
import { LocalizedTextView } from '../components-v2/controls/LocalizedTestField'
import { FieldViewerProps, FormField } from '../components-v2/forms/FormField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../components-v2/forms/FormModel'
import { Spacer } from '../components-v2/layout/Spacer'
import {
  koodinNimiOnly,
  ParasArvosanaEdit,
  ParasArvosanaPäiväView,
  ParasArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { ISO2FinnishDate } from '../date/date'
import { t } from '../i18n/i18n'
import { EmptyObject } from '../types/EditorModels'
import { KielitutkinnonOpiskeluoikeus } from '../types/fi/oph/koski/schema/KielitutkinnonOpiskeluoikeus'
import { Koulutustoimija } from '../types/fi/oph/koski/schema/Koulutustoimija'
import { Oppilaitos } from '../types/fi/oph/koski/schema/Oppilaitos'
import { ValtionhallinnonKielitutkinnonArviointi } from '../types/fi/oph/koski/schema/ValtionhallinnonKielitutkinnonArviointi'
import { ValtionhallinnonKielitutkinnonKielitaidonSuoritus } from '../types/fi/oph/koski/schema/ValtionhallinnonKielitutkinnonKielitaidonSuoritus'
import { ValtionhallinnonKielitutkinnonSuoritus } from '../types/fi/oph/koski/schema/ValtionhallinnonKielitutkinnonSuoritus'
import { ArviointipäiväOrd } from '../util/arvioinnit'
import { OsasuoritusOf } from '../util/schema'

export type ValtionhallinnonKielitutkintoEditorProps = {
  form: FormModel<KielitutkinnonOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<
    KielitutkinnonOpiskeluoikeus,
    ValtionhallinnonKielitutkinnonSuoritus
  >
  organisaatio?: Oppilaitos | Koulutustoimija
}

export const ValtionhallinnonKielitutkintoEditor: React.FC<
  ValtionhallinnonKielitutkintoEditorProps
> = ({ form, päätasonSuoritus, organisaatio }) => {
  const path = päätasonSuoritus.path
  const suoritus = getValue(path)(form.state)
  const kielitaidonSuoritukset = päätasonSuoritus.suoritus.osasuoritukset || []

  return suoritus ? (
    <>
      <ValtiohallinnonKielitutkinnonTiedot päätasonSuoritus={suoritus} />

      <Spacer />

      <OsasuoritusTable
        editMode={form.editMode}
        rows={kielitaidonSuoritukset.map((_os, osasuoritusIndex) =>
          kielitaitoToTableRow({
            form,
            osasuoritusIndex,
            suoritusIndex: päätasonSuoritus.index,
            suoritusPath: päätasonSuoritus.path
          })
        )}
        completed={isCompletedSuoritus(kielitaidonSuoritukset)}
      />
    </>
  ) : null
}

type ValtiohallinnonKielitutkinnonTiedotProps = {
  päätasonSuoritus: ValtionhallinnonKielitutkinnonSuoritus
}

const ValtiohallinnonKielitutkinnonTiedot: React.FC<
  ValtiohallinnonKielitutkinnonTiedotProps
> = ({ päätasonSuoritus }) => (
  <KeyValueTable>
    <KeyValueRow localizableLabel="Tutkinnon taso">
      {t(päätasonSuoritus.koulutusmoduuli.tunniste.nimi)}
    </KeyValueRow>
    <KeyValueRow localizableLabel="Kieli">
      {t(päätasonSuoritus.koulutusmoduuli.kieli.nimi)}
    </KeyValueRow>
    <KeyValueRow localizableLabel="Suorituspaikkakunta">
      {t(päätasonSuoritus.vahvistus?.paikkakunta?.nimi)}
    </KeyValueRow>
  </KeyValueTable>
)

type KielitaitoToTableRowParams = {
  form: FormModel<KielitutkinnonOpiskeluoikeus>
  suoritusPath: FormOptic<
    KielitutkinnonOpiskeluoikeus,
    ValtionhallinnonKielitutkinnonSuoritus
  >
  suoritusIndex: number
  osasuoritusIndex: number
}

const kielitaitoToTableRow = ({
  suoritusPath,
  suoritusIndex,
  osasuoritusIndex,
  form
}: KielitaitoToTableRowParams): OsasuoritusRowData<
  'Tutkinto' | 'Tutkintopäivä' | 'Arviointipäivä' | 'Arvosana'
> => {
  const osasuoritusPath = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)
  const osasuoritus = getValue(osasuoritusPath)(form.state)

  return {
    suoritusIndex,
    osasuoritusIndex,
    osasuoritusPath: suoritusPath.prop('osasuoritukset').optional(),
    expandable: true,
    columns: {
      Tutkinto: (
        <FormField
          form={form}
          path={osasuoritusPath.path('koulutusmoduuli.tunniste.nimi')}
          view={LocalizedTextView}
          testId="nimi"
        />
      ),
      Tutkintopäivä: (
        <FormField
          form={form}
          path={osasuoritusPath.path('osasuoritukset')}
          view={ViimeisinTutkintopäiväView}
          testId="tutkintopäivä"
        />
      ),
      Arviointipäivä: (
        <FormField
          form={form}
          path={osasuoritusPath.path('arviointi')}
          view={ParasArvosanaPäiväView}
          testId="arviointipäivä"
        />
      ),
      Arvosana: (
        <FormField
          form={form}
          path={osasuoritusPath.path('arviointi')}
          view={ParasArvosanaView}
          edit={ParasArvosanaEdit}
          editProps={{
            suoritusClassName: osasuoritus?.$class,
            format: koodinNimiOnly
          }}
        />
      )
    },
    content: osasuoritus ? (
      <KielitaitoProperties
        form={form}
        osasuoritus={osasuoritus}
        osasuoritusPath={osasuoritusPath}
      />
    ) : (
      <div />
    )
  }
}

type KielitaitoPropertiesProps = {
  form: FormModel<KielitutkinnonOpiskeluoikeus>
  osasuoritus: ValtionhallinnonKielitutkinnonKielitaidonSuoritus
  osasuoritusPath: FormOptic<
    KielitutkinnonOpiskeluoikeus,
    ValtionhallinnonKielitutkinnonKielitaidonSuoritus
  >
}

const KielitaitoProperties: React.FC<KielitaitoPropertiesProps> = ({
  form,
  osasuoritus,
  osasuoritusPath
}) => {
  const osakokeet = osasuoritus.osasuoritukset || []

  return (
    <>
      <Spacer />
      <OsasuoritusTable
        editMode={form.editMode}
        rows={osakokeet.map((_os, osakoeIndex) =>
          osakoeToTableRow({
            form,
            kielitaidonSuoritusPath: osasuoritusPath,
            suoritusIndex: 0,
            osasuoritusIndex: osakoeIndex
          })
        )}
        completed={isCompletedSuoritus(osakokeet)}
      />
    </>
  )
}

type OsakoeToTableRowParams = {
  form: FormModel<KielitutkinnonOpiskeluoikeus>
  kielitaidonSuoritusPath: FormOptic<
    KielitutkinnonOpiskeluoikeus,
    ValtionhallinnonKielitutkinnonKielitaidonSuoritus
  >
  suoritusIndex: number
  osasuoritusIndex: number
}

const osakoeToTableRow = ({
  kielitaidonSuoritusPath,
  suoritusIndex,
  osasuoritusIndex,
  form
}: OsakoeToTableRowParams): OsasuoritusRowData<
  'Osakoe' | 'Tutkintopäivä' | 'Arviointipäivä' | 'Arvosana'
> => {
  const osakoePath = kielitaidonSuoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)
  const osakoe = getValue(osakoePath)(form.state)

  return {
    suoritusIndex,
    osasuoritusIndex,
    osasuoritusPath: kielitaidonSuoritusPath.prop('osasuoritukset').optional(),
    expandable: !!osakoe?.arviointi,
    columns: {
      Osakoe: (
        <FormField
          form={form}
          path={osakoePath.path('koulutusmoduuli.tunniste.nimi')}
          view={LocalizedTextView}
          testId="nimi"
        />
      ),
      Tutkintopäivä: (
        <FormField
          form={form}
          path={osakoePath.path('alkamispäivä')}
          view={DateView}
        />
      ),
      Arviointipäivä: (
        <FormField
          form={form}
          path={osakoePath.path('arviointi')}
          view={ParasArvosanaPäiväView}
        />
      ),
      Arvosana: (
        <FormField
          form={form}
          path={osakoePath.path('arviointi')}
          view={ParasArvosanaView}
          edit={ParasArvosanaEdit}
          editProps={{
            suoritusClassName: osakoe?.$class,
            format: koodinNimiOnly
          }}
        />
      )
    },
    content: osakoe?.arviointi ? (
      <OsakokeenArvioinnit arvioinnit={osakoe?.arviointi} />
    ) : undefined
  }
}

type OsakokeenArvioinnitProps = {
  arvioinnit: ValtionhallinnonKielitutkinnonArviointi[]
}

const OsakokeenArvioinnit: React.FC<OsakokeenArvioinnitProps> = ({
  arvioinnit
}) => {
  const indentation = 4

  return (
    <>
      <Spacer />
      {pipe(
        arvioinnit,
        A.sort(Ord.reverse(ArviointipäiväOrd)),
        A.mapWithIndex((index, arviointi) => (
          <KeyValueTable key={index}>
            <KeyValueRow localizableLabel="Arviointipäivä" indent={indentation}>
              {ISO2FinnishDate(arviointi.päivä)}
            </KeyValueRow>
            <KeyValueRow localizableLabel="Arvosana" indent={indentation}>
              {t(arviointi.arvosana.nimi)}
            </KeyValueRow>
          </KeyValueTable>
        )),
        A.intersperse(<Spacer />)
      )}
      <Spacer />
    </>
  )
}

const isCompletedSuoritus =
  (
    suoritukset:
      | ValtionhallinnonKielitutkinnonKielitaidonSuoritus[]
      | OsasuoritusOf<ValtionhallinnonKielitutkinnonKielitaidonSuoritus>[]
  ) =>
  (index: number): boolean =>
    pipe(
      suoritukset[index],
      (a) => a.arviointi || [],
      A.last,
      O.map((a) => a.arvosana.koodiarvo !== 'hylatty'),
      O.getOrElse(() => false)
    )

// Tutkintopäivä viewer

const ValtionhallinnonKielitutkinnonKielitaidonSuoritusOrd = Ord.contramap(
  (a: ValtionhallinnonKielitutkinnonKielitaidonSuoritus) =>
    a.alkamispäivä || '9999-99-99'
)(string.Ord)

export type ViimeisinTutkintopäiväViewProps = CommonProps<
  FieldViewerProps<
    ValtionhallinnonKielitutkinnonKielitaidonSuoritus[],
    EmptyObject
  >
>

export const ViimeisinTutkintopäiväView: React.FC<
  ViimeisinTutkintopäiväViewProps
> = (props) => {
  const viimeisinOsakoe = pipe(
    props.value || [],
    A.sort(ValtionhallinnonKielitutkinnonKielitaidonSuoritusOrd),
    A.last,
    O.toUndefined
  )

  return <DateView value={viimeisinOsakoe?.alkamispäivä} />
}
