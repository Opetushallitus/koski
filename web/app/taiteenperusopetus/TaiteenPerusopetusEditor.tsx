import React, { useCallback, useMemo, useState } from 'react'
import { useSchema } from '../appstate/constraints'
import { useKoodistoFiller } from '../appstate/koodisto'
import { assortedPreferenceType, usePreferences } from '../appstate/preferences'
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import { EditorContainer } from '../components-v2/containers/EditorContainer'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FormField } from '../components-v2/forms/FormField'
import { FormModel, FormOptic, useForm } from '../components-v2/forms/FormModel'
import { Spacer } from '../components-v2/layout/Spacer'
import {
  ArvosanaEdit,
  ArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  LaajuusOpintopisteissäEdit,
  laajuusSum,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { PaikallinenOsasuoritusSelect } from '../components-v2/opiskeluoikeus/PaikallinenOsasuoritusSelect'
import {
  SuorituksenVahvistusEdit,
  SuorituksenVahvistusView
} from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { Trans } from '../components-v2/texts/Trans'
import { localize, t } from '../i18n/i18n'
import { HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla } from '../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaTittelillaJaValinnaisellaPaikkakunnalla'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { TaiteenPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeus'
import { TaiteenPerusopetuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeusjakso'
import { TaiteenPerusopetuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaatasonSuoritus'
import { TaiteenPerusopetuksenPaikallinenOpintokokonaisuus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaikallinenOpintokokonaisuus'
import { TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
import { append, deleteAt } from '../util/fp/arrays'
import { usePäätasonSuoritus } from '../util/optics'
import { createTpoArviointi } from './tpoCommon'
import { TpoOsasuoritusProperties } from './TpoOsasuoritusProperties'

export type TaiteenPerusopetusEditorProps = {
  oppijaOid: string
  opiskeluoikeus: TaiteenPerusopetuksenOpiskeluoikeus
}

export const TaiteenPerusopetusEditor = (
  props: TaiteenPerusopetusEditorProps
) => {
  const opiskeluoikeusSchema = useSchema('TaiteenPerusopetuksenOpiskeluoikeus')
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)
  const fillKoodistot = useKoodistoFiller()

  const [suoritusIndex, setSuoritusIndex] = useState(0)
  const päätasonSuoritus = form.state.suoritukset[suoritusIndex]
  const päätasonSuoritusPath =
    usePäätasonSuoritus<TaiteenPerusopetuksenOpiskeluoikeus>(suoritusIndex)

  const [
    osasuorituksetPath,
    suorituksenVahvistusPath,
    opiskeluoikeudenLaajuusPath
  ] = useMemo(
    () => [
      päätasonSuoritusPath.prop('osasuoritukset').optional(), // TODO: Tässä on paljon uudelleenkäytettäviä patheja siirrettäväksi utilseihin
      päätasonSuoritusPath.prop('vahvistus'),
      päätasonSuoritusPath.path('koulutusmoduuli.laajuus')
    ],
    [päätasonSuoritusPath]
  )

  const osasuoritustenLaajuudetPath = useMemo(
    () => osasuorituksetPath.elems().path('koulutusmoduuli.laajuus'),
    [osasuorituksetPath]
  )

  const organisaatio =
    props.opiskeluoikeus.oppilaitos || props.opiskeluoikeus.koulutustoimija

  const osasuoritukset =
    usePreferences<TaiteenPerusopetuksenPaikallinenOpintokokonaisuus>(
      organisaatio?.oid,
      // Ladataan ja tallennetaan osasuoritukset oppimäärän ja taiteenalan perusteella omiin lokeroihin
      assortedPreferenceType(
        'taiteenperusopetus',
        form.state.oppimäärä.koodiarvo,
        päätasonSuoritus?.koulutusmoduuli.taiteenala.koodiarvo
      )
    )

  const storedOsasuoritustunnisteet = useMemo(
    () => osasuoritukset.preferences.map((p) => p.tunniste),
    [osasuoritukset.preferences]
  )

  const onAddOsasuoritus = useCallback(
    async (tunniste: PaikallinenKoodi, isNew: boolean) => {
      const newOsasuoritus = await fillKoodistot(
        TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus({
          koulutusmoduuli: TaiteenPerusopetuksenPaikallinenOpintokokonaisuus({
            tunniste,
            laajuus: LaajuusOpintopisteissä({ arvo: 1 })
          })
        })
      )

      form.updateAt(päätasonSuoritusPath, (a) => ({
        ...a,
        osasuoritukset: append(newOsasuoritus)(a.osasuoritukset)
      }))

      if (isNew) {
        osasuoritukset.store(tunniste.koodiarvo, newOsasuoritus.koulutusmoduuli)
      }
    },
    [fillKoodistot, form, osasuoritukset, päätasonSuoritusPath]
  )

  const onRemoveOsasuoritus = useCallback(
    (osasuoritusIndex: number) => {
      form.updateAt(päätasonSuoritusPath, (a) =>
        a.osasuoritukset
          ? {
              ...a,
              osasuoritukset: deleteAt(a.osasuoritukset, osasuoritusIndex)
            }
          : a
      )
    },
    [form, päätasonSuoritusPath]
  )

  const onRemoveStoredOsasuoritus = useCallback(
    (tunniste: PaikallinenKoodi) => {
      osasuoritukset.remove(tunniste.koodiarvo)
    },
    [osasuoritukset]
  )

  return (
    <>
      <OpiskeluoikeusTitle
        opiskeluoikeus={form.state}
        koulutus={tpoKoulutuksenNimi(form.state)}
      />
      <EditorContainer
        form={form}
        oppijaOid={props.oppijaOid}
        onChangeSuoritus={setSuoritusIndex} // TODO: tästä vois ehkä abstrahoida taas pidemmälle niin, että tuodaan ihan vaan se päätason suoritus
        createOpiskeluoikeusjakso={TaiteenPerusopetuksenOpiskeluoikeusjakso}
        suorituksenNimi={tpoSuorituksenNimi}
      >
        <KeyValueTable>
          <KeyValueRow name="Taiteenala">
            <Trans>{päätasonSuoritus?.koulutusmoduuli.taiteenala.nimi}</Trans>
          </KeyValueRow>
          <KeyValueRow name="Oppimäärä">
            <Trans>{form.state.oppimäärä.nimi}</Trans>
          </KeyValueRow>
          <KeyValueRow name="Oppilaitos">
            <Trans>{form.state.oppilaitos?.nimi}</Trans>
          </KeyValueRow>
          <KeyValueRow name="Laajuus">
            <FormField
              form={form}
              path={opiskeluoikeudenLaajuusPath}
              view={LaajuusView}
              auto={laajuusSum(osasuoritustenLaajuudetPath, form.state)}
            />
          </KeyValueRow>
        </KeyValueTable>

        <Spacer />

        <FormField
          form={form}
          path={suorituksenVahvistusPath}
          optional
          view={SuorituksenVahvistusView}
          edit={(editProps) => (
            <SuorituksenVahvistusEdit
              {...editProps}
              organisaatio={organisaatio}
              vahvistusClass={
                HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla.className
              }
            />
          )}
        />

        {päätasonSuoritus.osasuoritukset && (
          <OsasuoritusTable
            key={suoritusIndex}
            editMode={form.editMode}
            rows={päätasonSuoritus.osasuoritukset.map((_, osasuoritusIndex) =>
              osasuoritusToTableRow(
                form,
                päätasonSuoritusPath,
                osasuoritusIndex
              )
            )}
            onRemove={onRemoveOsasuoritus}
          />
        )}

        {form.editMode && (
          <ColumnRow>
            <Column span={1} spanPhone={0} />
            <Column span={15} spanPhone={24}>
              <PaikallinenOsasuoritusSelect
                key={suoritusIndex}
                tunnisteet={storedOsasuoritustunnisteet}
                onSelect={onAddOsasuoritus}
                onRemove={onRemoveStoredOsasuoritus}
              />
            </Column>
          </ColumnRow>
        )}
      </EditorContainer>
    </>
  )
}

const osasuoritusToTableRow = (
  form: FormModel<TaiteenPerusopetuksenOpiskeluoikeus>,
  suoritusPath: FormOptic<
    TaiteenPerusopetuksenOpiskeluoikeus,
    TaiteenPerusopetuksenPäätasonSuoritus
  >,
  osasuoritusIndex: number
): OsasuoritusRowData<'Kurssi' | 'Laajuus' | 'Arviointi'> => {
  const osasuoritus = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)

  return {
    columns: {
      Kurssi: (
        <FormField
          form={form}
          path={osasuoritus.path('koulutusmoduuli.tunniste.nimi')}
          view={(props) => <Trans>{props.value}</Trans>}
        />
      ),
      Laajuus: (
        <FormField
          form={form}
          path={osasuoritus.path('koulutusmoduuli.laajuus')}
          view={LaajuusView}
          edit={LaajuusOpintopisteissäEdit}
        />
      ),
      Arviointi: (
        <FormField
          form={form}
          path={osasuoritus.prop('arviointi')}
          view={(props) => <ArvosanaView {...props} />}
          edit={(props) => (
            <ArvosanaEdit {...props} createArviointi={createTpoArviointi} />
          )}
        />
      )
    },
    content: (
      <TpoOsasuoritusProperties
        key="lol"
        form={form}
        osasuoritusPath={osasuoritus}
      />
    )
  }
}

const tpoKoulutuksenNimi = (
  opiskeluoikeus: TaiteenPerusopetuksenOpiskeluoikeus
): string => {
  return `${t(opiskeluoikeus.oppimäärä.nimi)}, ${t(
    opiskeluoikeus.suoritukset[0]?.koulutusmoduuli.taiteenala.nimi
  )}`.toLowerCase()
}

const tpoSuorituksenNimi = (
  suoritus: TaiteenPerusopetuksenPäätasonSuoritus
): LocalizedString =>
  localize(
    `${t(suoritus.tyyppi.lyhytNimi) || t(suoritus.tyyppi.nimi)}, ${t(
      suoritus.koulutusmoduuli.taiteenala.nimi
    ).toLowerCase()}`
  )
