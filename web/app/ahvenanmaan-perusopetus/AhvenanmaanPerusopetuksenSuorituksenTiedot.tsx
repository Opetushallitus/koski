import React, { useCallback } from 'react'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import {
  KeyColumnedValuesRow,
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { Checkbox } from '../components-v2/controls/Checkbox'
import { DateEdit, DateView } from '../components-v2/controls/DateField'
import {
  LocalizedTextEdit,
  LocalizedTextView
} from '../components-v2/controls/LocalizedTestField'
import { TextEdit, TextView } from '../components-v2/controls/TextField'
import { FormField } from '../components-v2/forms/FormField'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import {
  KoodistoEdit,
  KoodistoView
} from '../components-v2/opiskeluoikeus/KoodistoField'
import { PerusteView } from '../components-v2/opiskeluoikeus/PerusteField'
import {
  OrganisaatioEdit,
  OrganisaatioView
} from '../components-v2/opiskeluoikeus/OrganisaatioField'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { TestIdText } from '../appstate/useTestId'
import { t } from '../i18n/i18n'
import { AhvenanmaanPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeus'
import {
  AhvenanmaanPerusopetuksenOppimääränSuoritus,
  isAhvenanmaanPerusopetuksenOppimääränSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOppimaaranSuoritus'
import {
  AhvenanmaanPerusopetuksenVuosiluokanSuoritus,
  isAhvenanmaanPerusopetuksenVuosiluokanSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenVuosiluokanSuoritus'
import { HenkilövahvistusPaikkakunnalla } from '../types/fi/oph/koski/schema/HenkilovahvistusPaikkakunnalla'

type AhvenanmaanPerusopetuksenSuorituksenTiedotProps = {
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<AhvenanmaanPerusopetuksenOpiskeluoikeus>
}

export const AhvenanmaanPerusopetuksenSuorituksenTiedot: React.FC<
  AhvenanmaanPerusopetuksenSuorituksenTiedotProps
> = ({ form, päätasonSuoritus }) => {
  const path = päätasonSuoritus.path
  const suoritus = päätasonSuoritus.suoritus
  const luokalleSiirtymisenTeksti = getLuokalleSiirtymisenTeksti(suoritus)

  return (
    <>
      {/* labelWidth pitää pisimmätkin nimet yhdellä rivillä ja tasaa arvot
          samaan sarakkeeseen vanhan käyttöliittymän tapaan. */}
      <KeyValueTable labelWidth={6}>
        <KeyColumnedValuesRow
          localizableName={
            isAhvenanmaanPerusopetuksenVuosiluokanSuoritus(suoritus)
              ? 'Luokka-aste'
              : 'Koulutus'
          }
          columnSpans={{ default: [3, '*'], phone: [24, '*'] }}
        >
          {[
            <TestIdText key="tunniste" id="koulutus">
              {t(suoritus.koulutusmoduuli.tunniste.nimi)}{' '}
              {suoritus.koulutusmoduuli.tunniste.koodiarvo}
            </TestIdText>,
            // Ahvenanmaan ops (ÅLR2020/9841) ei ole ePerusteissa, joten
            // diaarinumero näytetään vain luettavana (ei muokattava pudotus).
            <PerusteView
              key="peruste"
              value={suoritus.koulutusmoduuli.perusteenDiaarinumero}
            />
          ]}
        </KeyColumnedValuesRow>

        {isAhvenanmaanPerusopetuksenVuosiluokanSuoritus(suoritus) && (
          <VuosiluokanLuokkaRow form={form} path={path} />
        )}

        <KeyValueRow localizableLabel="Oppilaitos / toimipiste">
          <FormField
            form={form}
            path={path.prop('toimipiste')}
            view={OrganisaatioView}
            edit={OrganisaatioEdit}
          />
        </KeyValueRow>

        {isAhvenanmaanPerusopetuksenVuosiluokanSuoritus(suoritus) && (
          <VuosiluokanKoulutustiedot
            form={form}
            path={path}
            suoritus={suoritus}
          />
        )}

        {isAhvenanmaanPerusopetuksenOppimääränSuoritus(suoritus) && (
          <OppimääränTiedot form={form} path={path} />
        )}

        <KeyValueRow localizableLabel="Suorituskieli">
          <FormField
            form={form}
            path={path.prop('suorituskieli')}
            view={KoodistoView}
            edit={KoodistoEdit}
            editProps={{ koodistoUri: 'kieli' }}
            testId="suorituskieli"
          />
        </KeyValueRow>

        {isAhvenanmaanPerusopetuksenVuosiluokanSuoritus(suoritus) && (
          <VuosiluokanLisätiedot form={form} path={path} suoritus={suoritus} />
        )}

        {(form.editMode || suoritus.todistuksellaNäkyvätLisätiedot) && (
          <KeyValueRow localizableLabel="Todistuksella näkyvät lisätiedot">
            <FormField
              form={form}
              path={path.prop('todistuksellaNäkyvätLisätiedot')}
              optional
              view={LocalizedTextView}
              viewProps={{ style: { whiteSpace: 'pre-line' } }}
              edit={LocalizedTextEdit}
              editProps={{ large: true }}
              testId="todistuksellaNäkyvätLisätiedot"
            />
          </KeyValueRow>
        )}
      </KeyValueTable>

      <SuorituksenVahvistusField
        form={form}
        suoritusPath={päätasonSuoritus.path}
        organisaatio={form.state.oppilaitos}
        disableAdd={false}
        vahvistusClass={HenkilövahvistusPaikkakunnalla.className}
        statusInfo={
          luokalleSiirtymisenTeksti && (
            <TestIdText id="luokalleSiirtyminen">
              {t(luokalleSiirtymisenTeksti)}
            </TestIdText>
          )
        }
        modalBodyExtra={
          isAhvenanmaanPerusopetuksenVuosiluokanSuoritus(suoritus) && (
            <LuokalleSiirtyminenModalCheckbox
              form={form}
              path={path}
              suoritus={suoritus}
            />
          )
        }
      />
    </>
  )
}

const getLuokalleSiirtymisenTeksti = (suoritus: object): string | undefined => {
  if (!isAhvenanmaanPerusopetuksenVuosiluokanSuoritus(suoritus))
    return undefined
  return suoritus.jääLuokalle
    ? 'Ei siirretä seuraavalle luokalle'
    : 'Siirretään seuraavalle luokalle'
}

const LuokalleSiirtyminenModalCheckbox: React.FC<{
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  path: any
  suoritus: AhvenanmaanPerusopetuksenVuosiluokanSuoritus
}> = ({ form, path, suoritus }) => {
  const vuosiluokkaPath = path as unknown as FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
    AhvenanmaanPerusopetuksenVuosiluokanSuoritus
  >

  const onChange = useCallback(
    (siirretäänSeuraavalleLuokalle: boolean) => {
      form.updateAt(
        vuosiluokkaPath.prop('jääLuokalle'),
        () => !siirretäänSeuraavalleLuokalle
      )
    },
    [form, vuosiluokkaPath]
  )

  return (
    <Checkbox
      label="Siirretään seuraavalle luokalle"
      checked={!suoritus.jääLuokalle}
      onChange={onChange}
      testId="luokalleSiirtyminen"
    />
  )
}

const VuosiluokanLuokkaRow: React.FC<{
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  path: any
}> = ({ form, path }) => {
  const vuosiluokkaPath = path as unknown as FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
    AhvenanmaanPerusopetuksenVuosiluokanSuoritus
  >
  return (
    <KeyValueRow localizableLabel="Luokka">
      <FormField
        form={form}
        path={vuosiluokkaPath.prop('luokka')}
        view={TextView}
        edit={TextEdit}
        testId="luokka"
      />
    </KeyValueRow>
  )
}

const VuosiluokanKoulutustiedot: React.FC<{
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  path: any
  suoritus: AhvenanmaanPerusopetuksenVuosiluokanSuoritus
}> = ({ form, path, suoritus }) => {
  const vuosiluokkaPath = path as unknown as FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
    AhvenanmaanPerusopetuksenVuosiluokanSuoritus
  >
  return (
    <>
      {(form.editMode || suoritus.alkamispäivä) && (
        <KeyValueRow localizableLabel="Alkamispäivä">
          <FormField
            form={form}
            path={vuosiluokkaPath.prop('alkamispäivä')}
            optional
            view={DateView}
            edit={DateEdit}
            testId="alkamispäivä"
          />
        </KeyValueRow>
      )}
      {(form.editMode || suoritus.suoritustapa) && (
        <KeyValueRow localizableLabel="Suoritustapa">
          <FormField
            form={form}
            path={vuosiluokkaPath.prop('suoritustapa')}
            optional
            view={KoodistoView}
            edit={KoodistoEdit}
            editProps={{
              koodistoUri: 'perusopetuksensuoritustapa',
              zeroValueOption: true
            }}
            testId="suoritustapa"
          />
        </KeyValueRow>
      )}
    </>
  )
}

const VuosiluokanLisätiedot: React.FC<{
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  path: any
  suoritus: AhvenanmaanPerusopetuksenVuosiluokanSuoritus
}> = ({ form, path, suoritus }) => {
  const vuosiluokkaPath = path as unknown as FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
    AhvenanmaanPerusopetuksenVuosiluokanSuoritus
  >

  if (!form.editMode && !suoritus.jääLuokalle) return null

  return (
    <KeyValueRow localizableLabel="Jää luokalle">
      <FormField
        form={form}
        path={vuosiluokkaPath.prop('jääLuokalle')}
        view={BooleanView}
        edit={BooleanEdit}
        testId="jääLuokalle"
      />
    </KeyValueRow>
  )
}

const OppimääränTiedot: React.FC<{
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  path: any
}> = ({ form, path }) => {
  const oppimääräPath = path as unknown as FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
    AhvenanmaanPerusopetuksenOppimääränSuoritus
  >
  return (
    <KeyValueRow localizableLabel="Suoritustapa">
      <FormField
        form={form}
        path={oppimääräPath.prop('suoritustapa')}
        view={KoodistoView}
        edit={KoodistoEdit}
        editProps={{ koodistoUri: 'perusopetuksensuoritustapa' }}
        testId="suoritustapa"
      />
    </KeyValueRow>
  )
}
