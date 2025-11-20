import React from 'react'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FormModel, getValue } from '../components-v2/forms/FormModel'
import { Spacer } from '../components-v2/layout/Spacer'
import { ISO2FinnishDate } from '../date/date'
import { t } from '../i18n/i18n'
import { KielitutkinnonOpiskeluoikeudenTila } from '../types/fi/oph/koski/schema/KielitutkinnonOpiskeluoikeudenTila'
import { KielitutkinnonOpiskeluoikeus } from '../types/fi/oph/koski/schema/KielitutkinnonOpiskeluoikeus'
import { Koulutustoimija } from '../types/fi/oph/koski/schema/Koulutustoimija'
import { Oppilaitos } from '../types/fi/oph/koski/schema/Oppilaitos'
import { YleisenKielitutkinnonOsakokeenSuoritus } from '../types/fi/oph/koski/schema/YleisenKielitutkinnonOsakokeenSuoritus'
import { YleisenKielitutkinnonSuoritus } from '../types/fi/oph/koski/schema/YleisenKielitutkinnonSuoritus'
import { ykiParasArvosana } from './yleinenKielitutkinto'
import { YleinenKielitutkintoTodistusLataus } from './YleinenKielitutkintoTodistusLataus'

export type YleinenKielitutkintoEditorProps = {
  form: FormModel<KielitutkinnonOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<
    KielitutkinnonOpiskeluoikeus,
    YleisenKielitutkinnonSuoritus
  >
  organisaatio?: Oppilaitos | Koulutustoimija
}

export const YleinenKielitutkintoEditor: React.FC<
  YleinenKielitutkintoEditorProps
> = ({ form, päätasonSuoritus, organisaatio }) => {
  const path = päätasonSuoritus.path
  const suoritus = getValue(path)(form.state)

  const hasFeatureFlagPdfTodistus =
    localStorage.getItem('pdf-todistus') !== null ||
    new URLSearchParams(window.location.search).has('pdf-todistus')

  return suoritus ? (
    <>
      <YleisenKielitutkinnonTiedot
        päätasonSuoritus={suoritus}
        tila={form.state.tila}
      />

      <Spacer />

      <KeyValueTable>
        {suoritus.osasuoritukset?.map((os, index) => (
          <YleisenKielitutkinnonOsanSuoritusEditor osa={os} key={index} />
        ))}
        {suoritus.yleisarvosana && (
          <>
            <Spacer />
            <KeyValueRow localizableLabel="Yleisarvosana">
              {t(suoritus.yleisarvosana?.nimi)}
            </KeyValueRow>
          </>
        )}
      </KeyValueTable>

      {form.state.oid && hasFeatureFlagPdfTodistus && (
        <YleinenKielitutkintoTodistusLataus
          opiskeluoikeusOid={form.state.oid}
        />
      )}
    </>
  ) : null
}

type YleisenKielitutkinnonTiedotProps = {
  päätasonSuoritus: YleisenKielitutkinnonSuoritus
  tila: KielitutkinnonOpiskeluoikeudenTila
}

const YleisenKielitutkinnonTiedot: React.FC<
  YleisenKielitutkinnonTiedotProps
> = ({ päätasonSuoritus, tila }) => (
  <KeyValueTable>
    <KeyValueRow localizableLabel="Taso">
      {t(päätasonSuoritus.koulutusmoduuli.tunniste.nimi)}
    </KeyValueRow>
    <KeyValueRow localizableLabel="Kieli">
      {t(päätasonSuoritus.koulutusmoduuli.kieli.nimi)}
    </KeyValueRow>
    <KeyValueRow localizableLabel="Testipaikka">
      {t(päätasonSuoritus.toimipiste.nimi)}
    </KeyValueRow>
    <KeyValueRow localizableLabel="Testipäivä">
      {ISO2FinnishDate(getJaksonAlkupäivä(tila, 'lasna'))}
    </KeyValueRow>
    <KeyValueRow localizableLabel="Arviointipäivä">
      {ISO2FinnishDate(getJaksonAlkupäivä(tila, 'hyvaksytystisuoritettu'))}
    </KeyValueRow>
  </KeyValueTable>
)

type YleisenKielitutkinnonOsanSuoritusEditorProps = {
  osa: YleisenKielitutkinnonOsakokeenSuoritus
}

const YleisenKielitutkinnonOsanSuoritusEditor: React.FC<
  YleisenKielitutkinnonOsanSuoritusEditorProps
> = ({ osa }) => {
  const arviointi = osa.arviointi && ykiParasArvosana(osa.arviointi)
  return (
    <KeyValueRow localizableLabel={osa.koulutusmoduuli.tunniste.nimi}>
      {t(arviointi?.arvosana.nimi)}
    </KeyValueRow>
  )
}

export const getJaksonAlkupäivä = (
  tila: KielitutkinnonOpiskeluoikeudenTila,
  koodiarvo: string
): string | undefined =>
  tila.opiskeluoikeusjaksot.find((jakso) => jakso.tila.koodiarvo === koodiarvo)
    ?.alku
