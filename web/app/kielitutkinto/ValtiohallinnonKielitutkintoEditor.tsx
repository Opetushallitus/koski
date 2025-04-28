import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import React from 'react'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { LocalizedTextView } from '../components-v2/controls/LocalizedTestField'
import { FormField } from '../components-v2/forms/FormField'
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
import { SuorituksenVahvistus } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { t } from '../i18n/i18n'
import { KielitutkinnonOpiskeluoikeudenTila } from '../types/fi/oph/koski/schema/KielitutkinnonOpiskeluoikeudenTila'
import { KielitutkinnonOpiskeluoikeus } from '../types/fi/oph/koski/schema/KielitutkinnonOpiskeluoikeus'
import { Koulutustoimija } from '../types/fi/oph/koski/schema/Koulutustoimija'
import { Oppilaitos } from '../types/fi/oph/koski/schema/Oppilaitos'
import { ValtionhallinnonKielitutkinnonKielitaidonSuoritus } from '../types/fi/oph/koski/schema/ValtionhallinnonKielitutkinnonKielitaidonSuoritus'
import { ValtionhallinnonKielitutkinnonSuoritus } from '../types/fi/oph/koski/schema/ValtionhallinnonKielitutkinnonSuoritus'
import { OsasuoritusOf } from '../util/schema'
import { ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus } from '../types/fi/oph/koski/schema/ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus'
import { ValtionhallinnonKielitutkinnonArviointi } from '../types/fi/oph/koski/schema/ValtionhallinnonKielitutkinnonArviointi'
import { ISO2FinnishDate } from '../date/date'

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
> = ({ päätasonSuoritus }) => {
  const vastaanottajat = päätasonSuoritus.vahvistus?.myöntäjäHenkilöt

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Tutkinnon taso">
        {t(päätasonSuoritus.koulutusmoduuli.tunniste.nimi)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Kieli">
        {t(päätasonSuoritus.koulutusmoduuli.kieli.nimi)}
      </KeyValueRow>
      {päätasonSuoritus.koulutusmoduuli.tunniste.koodiarvo ===
        'hyvajatyydyttava' && (
        <>
          <KeyValueRow localizableLabel="Tutkintosuorituksen vastaanottaja">
            {vastaanottajat && vastaanottajat.map((v) => v.nimi).join(', ')}
          </KeyValueRow>
          <KeyValueRow localizableLabel="Suorituspaikkakunta">
            {t(päätasonSuoritus.vahvistus?.paikkakunta?.nimi)}
          </KeyValueRow>
        </>
      )}
    </KeyValueTable>
  )
}

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
  'Tutkinto' | 'Tutkintopäivä' | 'Arvosana'
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
          path={osasuoritusPath.path('arviointi')}
          view={ParasArvosanaPäiväView}
          testId="nimi"
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
  'Osakoe' | 'Tutkintopäivä' | 'Arvosana'
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
      {arvioinnit.map((arviointi, index) => (
        <KeyValueTable key={index}>
          <KeyValueRow localizableLabel="Arvosana" indent={indentation}>
            {t(arviointi.arvosana.nimi)}
          </KeyValueRow>
          <KeyValueRow localizableLabel="Arviointipäivä" indent={indentation}>
            {ISO2FinnishDate(arviointi.päivä)}
          </KeyValueRow>
        </KeyValueTable>
      ))}
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
