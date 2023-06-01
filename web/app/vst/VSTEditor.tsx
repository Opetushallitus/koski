import React from 'react'
import { useSchema } from '../appstate/constraints'
import { KansalainenOnly } from '../components-v2/access/KansalainenOnly'
import {
  ActivePäätasonSuoritus,
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import {
  FormModel,
  FormOptic,
  getValue,
  useForm
} from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { Spacer } from '../components-v2/layout/Spacer'
import { PäätasonSuorituksenSuostumuksenPeruminen } from '../components-v2/opiskeluoikeus/OpiskeluoikeudenSuostumuksenPeruminen'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import { localize, t } from '../i18n/i18n'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import { VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import { KoodiarvotOf } from '../util/koodisto'
import { UusiOpiskeluoikeusjakso } from '../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { assertNever } from '../util/selfcare'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { Trans } from '../components-v2/texts/Trans'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import {
  OsasuoritusRowData,
  OsasuoritusTable,
  osasuoritusTestId
} from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { FormField } from '../components-v2/forms/FormField'
import { LocalizedTextView } from '../components-v2/controls/LocalizedTestField'
import { DateView, DateEdit } from '../components-v2/controls/DateField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import {
  ArvosanaEdit,
  ArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import { lastElement } from '../util/optics'
import {
  OsasuoritusProperty,
  OsasuoritusSubproperty
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { VapaanSivistystyöJotpaKoulutuksenArviointi } from '../types/fi/oph/koski/schema/VapaanSivistystyoJotpaKoulutuksenArviointi'
import { VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi } from '../types/fi/oph/koski/schema/VapaanSivistystyoVapaatavoitteisenKoulutuksenArviointi'
import { todayISODate } from '../date/date'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import {
  SuorituskieliEdit,
  SuorituskieliView
} from '../components-v2/opiskeluoikeus/SuorituskieliField'
import { TodistuksellaNäkyvätLisätiedotEdit } from '../components-v2/opiskeluoikeus/TodistuksellaNäkyvätLisätiedotField'
import { VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { LukutaitokoulutuksenArviointi } from '../types/fi/oph/koski/schema/LukutaitokoulutuksenArviointi'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsaamiskokonaisuudenSuoritus'
import { OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonValinnaistenSuuntautumisopintojenSuoritus'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import { VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import { VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'

type VSTEditorProps =
  AdaptedOpiskeluoikeusEditorProps<VapaanSivistystyönOpiskeluoikeus>

const vstNimi = (opiskeluoikeus: VapaanSivistystyönOpiskeluoikeus) =>
  `${t(
    opiskeluoikeus.suoritukset[0]?.koulutusmoduuli.tunniste.nimi
  )}`.toLowerCase()

const vstSuorituksenNimi = (suoritus: VapaanSivistystyönPäätasonSuoritus) => {
  const titles: Record<
    KoodiarvotOf<VapaanSivistystyönPäätasonSuoritus['tyyppi']>,
    string
  > = {
    vstjotpakoulutus: 'Vapaan sivistystyön koulutus',
    vstlukutaitokoulutus: 'Lukutaitokoulutus oppivelvollisille',
    vstmaahanmuuttajienkotoutumiskoulutus:
      'Kotoutumiskoulutus oppivelvollisille',
    vstoppivelvollisillesuunnattukoulutus:
      'Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille',
    vstvapaatavoitteinenkoulutus: 'Vapaan sivistystyön koulutus'
  }

  return localize(`${t(titles[suoritus.tyyppi.koodiarvo])}`)
}

/**
 * Käytetään oikean opiskeluoikeuden tilaluokan selvittämiseen, jos tiloja voi olla useampia.
 */
const resolveOpiskeluoikeudenTilaClass = (
  päätasonSuoritus: ActivePäätasonSuoritus<VapaanSivistystyönOpiskeluoikeus>
) => {
  const { koodiarvo } = päätasonSuoritus.suoritus.tyyppi
  switch (koodiarvo) {
    case 'vstjotpakoulutus':
      return VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso.className
    case 'vstvapaatavoitteinenkoulutus':
      return VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso.className
    case 'vstmaahanmuuttajienkotoutumiskoulutus':
    case 'vstlukutaitokoulutus':
    case 'vstoppivelvollisillesuunnattukoulutus':
      return OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso.className
    default:
      return assertNever(koodiarvo)
  }
}

export const VSTEditor = (props: VSTEditorProps) => {
  // Opiskeluoikeus
  const opiskeluoikeusSchema = useSchema('VapaanSivistystyönOpiskeluoikeus')

  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)

  // Oppilaitos
  const organisaatio =
    props.opiskeluoikeus.oppilaitos || props.opiskeluoikeus.koulutustoimija

  // Päätason suoritus
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(form)
  const jaksoClass = form.state.tila.opiskeluoikeusjaksot[0].$class

  // Render
  return (
    <>
      <OpiskeluoikeusTitle
        opiskeluoikeus={form.state}
        opiskeluoikeudenNimi={vstNimi(form.state)}
      />
      <EditorContainer
        form={form}
        invalidatable={props.invalidatable}
        oppijaOid={props.oppijaOid}
        suorituksenNimi={vstSuorituksenNimi}
        createOpiskeluoikeusjakso={(seed) => {
          switch (jaksoClass) {
            case VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso.className:
              return VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso(
                seed as UusiOpiskeluoikeusjakso<VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso>
              )
            case OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso.className:
              return OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(
                seed as UusiOpiskeluoikeusjakso<OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso>
              )
            case VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso.className:
              return VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(
                seed as UusiOpiskeluoikeusjakso<VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso>
              )
            default:
              return assertNever(jaksoClass)
          }
        }}
        opiskeluoikeusJaksoClassName={resolveOpiskeluoikeudenTilaClass(
          päätasonSuoritus
        )}
        onChangeSuoritus={setPäätasonSuoritus}
        testId="vst-editor-container"
      >
        <KansalainenOnly>
          <PäätasonSuorituksenSuostumuksenPeruminen
            opiskeluoikeus={form.state}
            suoritus={päätasonSuoritus.suoritus}
          />
        </KansalainenOnly>
        <Spacer />
        <KeyValueTable>
          <KeyValueRow
            label="Oppilaitos / toimipiste"
            testId={`vst.suoritukset.${päätasonSuoritus.index}.oppilaitos`}
          >
            <Trans>{päätasonSuoritus.suoritus.toimipiste.nimi}</Trans>
          </KeyValueRow>
          <KeyValueRow
            label="Koulutus"
            testId={`vst.suoritukset.${päätasonSuoritus.index}.koulutus`}
          >
            <Trans>
              {päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.nimi}
            </Trans>
          </KeyValueRow>
          <KeyValueRow
            label="Koulutusmoduuli"
            indent={2}
            testId={`vst.suoritukset.${päätasonSuoritus.index}.koulutusmoduuli`}
          >
            {päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.koodiarvo}
          </KeyValueRow>
          {'perusteenDiaarinumero' in
            päätasonSuoritus.suoritus.koulutusmoduuli && (
            <KeyValueRow
              label="Peruste"
              indent={2}
              testId={`vst.suoritukset.${päätasonSuoritus.index}.peruste`}
            >
              <span>
                {
                  päätasonSuoritus.suoritus.koulutusmoduuli
                    .perusteenDiaarinumero
                }
              </span>
            </KeyValueRow>
          )}
          {'opintokokonaisuus' in päätasonSuoritus.suoritus.koulutusmoduuli && (
            <KeyValueRow
              label="Opintokokonaisuus"
              indent={2}
              testId={`vst.suoritukset.${päätasonSuoritus.index}.peruste`}
            >
              <a
                href={`${window.ePerusteetBaseUrl}${t(
                  'eperusteet_opintopolku_url_fragment'
                )}${
                  päätasonSuoritus.suoritus.koulutusmoduuli.opintokokonaisuus
                    ?.koodiarvo
                }`}
                target="_blank"
                rel="noopener noreferrer"
              >
                {
                  päätasonSuoritus.suoritus.koulutusmoduuli.opintokokonaisuus
                    ?.koodiarvo
                }{' '}
                <Trans>
                  {
                    päätasonSuoritus.suoritus.koulutusmoduuli.opintokokonaisuus
                      ?.nimi
                  }
                </Trans>
              </a>
            </KeyValueRow>
          )}
          {'laajuus' in päätasonSuoritus.suoritus.koulutusmoduuli && (
            <KeyValueRow
              label="Laajuus"
              indent={2}
              testId={`vst.suoritukset.${päätasonSuoritus.index}.koulutuksen-laajuus`}
            >
              <FormField
                form={form}
                // TODO: Lisää type guard laajuuteen
                path={päätasonSuoritus.path
                  .prop('koulutusmoduuli')
                  // @ts-expect-error
                  .prop('laajuus')}
                // @ts-expect-error
                view={LaajuusView}
              />
            </KeyValueRow>
          )}
          <KeyValueRow
            label="Opetuskieli"
            testId={`vst.suoritukset.${päätasonSuoritus.index}.opetuskieli`}
          >
            <FormField
              form={form}
              path={päätasonSuoritus.path.prop('suorituskieli')}
              // TODO: Path on suorituskieli, mutta käyttöliittymässä käytetään opetuskieltä?
              view={SuorituskieliView}
              edit={SuorituskieliEdit}
            />
          </KeyValueRow>
          <KeyValueRow
            label="Todistuksella näkyvät lisätiedot"
            testId={`vst.suoritukset.${päätasonSuoritus.index}.todistuksella-nakyvat-lisatiedot`}
          >
            <FormField
              form={form}
              path={päätasonSuoritus.path.prop(
                'todistuksellaNäkyvätLisätiedot'
              )}
              // TODO: Oma näkymä
              // TODO: Oma editori
              view={LocalizedTextView}
              edit={TodistuksellaNäkyvätLisätiedotEdit}
            />
          </KeyValueRow>
        </KeyValueTable>
        <Spacer />
        <SuorituksenVahvistusField
          form={form}
          suoritusPath={päätasonSuoritus.path}
          organisaatio={organisaatio}
          disableAdd={
            // TODO: Estetään suorituksen vahvistus validaatioiden avulla
            false
          }
          testId={päätasonSuoritus.testId}
        />
        <Spacer />
        {päätasonSuoritus.suoritus.osasuoritukset &&
          päätasonSuoritus.suoritus.osasuoritukset.length > 0 && (
            <>
              <OsasuoritusTable
                editMode={form.editMode}
                rows={päätasonSuoritus.suoritus.osasuoritukset.map(
                  (os, osasuoritusIndex) => {
                    return osasuoritusToTableRow(
                      form,
                      päätasonSuoritus.path,
                      päätasonSuoritus.index,
                      osasuoritusIndex
                    )
                  }
                )}
                onRemove={() => {
                  // TODO: onRemove handler
                }}
              />
              <Spacer />
              <KeyValueTable>
                <KeyValueRow
                  label="Yhteensä"
                  testId={`vst.suoritukset.${päätasonSuoritus.index}.yhteensa`}
                >
                  {/* TODO: Korjaa tyypitys */}
                  {/* @ts-expect-error */}
                  {päätasonSuoritus.suoritus.osasuoritukset.reduce(
                    (prev: number, curr: any) =>
                      prev + (curr.koulutusmoduuli.laajuus?.arvo || 0),
                    0
                  )}
                </KeyValueRow>
              </KeyValueTable>
            </>
          )}
      </EditorContainer>
    </>
  )
}

type VSTOsasuoritus = NonNullable<
  VapaanSivistystyönPäätasonSuoritus['osasuoritukset']
>[0]

const osasuoritusToTableRow = (
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>,
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönPäätasonSuoritus
  >,
  suoritusIndex: number,
  osasuoritusIndex: number
): OsasuoritusRowData<'Osasuoritus' | 'Laajuus' | 'Arvosana'> => {
  const osasuoritus = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)

  const osasuoritusValue = getValue(osasuoritus)(form.state)

  return {
    suoritusIndex,
    osasuoritusIndex,
    columns: {
      Osasuoritus: (
        <FormField
          form={form}
          path={osasuoritus.path('koulutusmoduuli.tunniste.nimi')}
          view={LocalizedTextView}
          testId={osasuoritusTestId(suoritusIndex, osasuoritusIndex, 'nimi')}
        />
      ),
      Laajuus: (
        <FormField
          form={form}
          path={osasuoritus.path('koulutusmoduuli.laajuus')}
          view={LaajuusView}
          edit={LaajuusOpintopisteissäEdit}
          testId={osasuoritusTestId(suoritusIndex, osasuoritusIndex, 'laajuus')}
        />
      ),
      Arvosana: (
        <FormField
          form={form}
          path={osasuoritus.path('arviointi')}
          view={ArvosanaView}
          edit={(arvosanaProps) => {
            // @ts-expect-error
            if (hasNoArviointi(osasuoritusValue)) {
              return null
            }
            return (
              <ArvosanaEdit
                {...arvosanaProps}
                // @ts-expect-error
                createArviointi={(arvosana) => {
                  console.log('arvosana', arvosana)
                  const arviointi =
                    // @ts-expect-error
                    createVstArviointi(osasuoritusValue)(arvosana)
                  console.log(arviointi)
                  return arviointi
                }}
              />
            )
          }}
          testId={osasuoritusTestId(
            suoritusIndex,
            osasuoritusIndex,
            'arvosana'
          )}
        />
      )
    },
    content: (
      <VSTOsasuoritusProperties form={form} osasuoritusPath={osasuoritus} />
    )
  }
}

type VSTOsasuoritusArvioinnilla = Extract<
  VSTOsasuoritus,
  {
    arviointi?: Arviointi[]
  }
>
type VSTOsasuoritusIlmanArviointia = Exclude<
  VSTOsasuoritus,
  {
    arviointi?: Arviointi[]
  }
>

function hasArviointi(s: VSTOsasuoritus): s is VSTOsasuoritusArvioinnilla {
  return 'arviointi' in s
}

function hasNoArviointi(s: VSTOsasuoritus): s is VSTOsasuoritusIlmanArviointia {
  return !('arviointi' in s)
}

type VSTArviointi = NonNullable<VSTOsasuoritusArvioinnilla['arviointi']>[0]
type CreateVSTArviointi = (
  o: VSTOsasuoritusArvioinnilla
) => (arvosana: any) => VSTArviointi | null

/**
 * Selvittää osasuorituksen tyypin perusteella, minkälaisen arviointiprototypen käyttöliittymälle tarjotaan.
 */
const createVstArviointi: CreateVSTArviointi = (o) => (arvosana) => {
  const c = o.$class
  // TODO: Poista lokitus, kun arviointitoiminto on testattu
  console.log('osasuoritusClass', c)
  switch (c) {
    case VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022.className:
      return VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022({
        arvosana,
        päivä: todayISODate()
      })
    case VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022.className:
      return VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022({
        arvosana,
        päivä: todayISODate()
      })
    case VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022.className:
      return VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022({
        arvosana,
        päivä: todayISODate()
      })
    case VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus.className:
      return VapaanSivistystyöJotpaKoulutuksenArviointi({
        arvosana,
        päivä: todayISODate()
      })
    case VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus.className:
      return LukutaitokoulutuksenArviointi({
        arvosana,
        päivä: todayISODate(),
        // TODO: Tarkista, onko ok
        taitotaso: Koodistokoodiviite({
          koodistoUri: 'arviointiasteikkokehittyvankielitaidontasot',
          koodiarvo: 'A1.1'
        })
      })
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus.className:
      return VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi(
        {
          arvosana,
          päivä: todayISODate()
        }
      )
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus.className:
      return OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(
        {
          arvosana,
          päivä: todayISODate()
        }
      )
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus.className:
      return OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(
        {
          arvosana,
          päivä: todayISODate()
        }
      )
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus.className:
      return OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(
        {
          arvosana,
          päivä: todayISODate()
        }
      )
    case VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus.className:
      return VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi({
        arvosana,
        päivä: todayISODate()
      })
    default:
      return null
  }
}

type VSTOsasuoritusPropertiesProps = {
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<VapaanSivistystyönOpiskeluoikeus, VSTOsasuoritus>
}

const VSTOsasuoritusProperties: React.FC<VSTOsasuoritusPropertiesProps> = (
  props
) => {
  const osasuoritusArvioinnillaPath = props.osasuoritusPath.guard(hasArviointi)
  const viimeisinArviointiPath = osasuoritusArvioinnillaPath
    .prop('arviointi')
    .optional()
    .compose(lastElement())

  const osasuoritus = getValue(props.osasuoritusPath)(props.form.state)

  console.log('osasuoritus', osasuoritus)

  const arvioitu =
    osasuoritus !== undefined &&
    'arviointi' in osasuoritus &&
    osasuoritus?.arviointi !== undefined &&
    osasuoritus?.arviointi.length > 0

  return (
    <div>
      {arvioitu && (
        <OsasuoritusProperty label="Arviointi">
          <OsasuoritusSubproperty label="Arvosana">
            <FormField
              form={props.form}
              path={osasuoritusArvioinnillaPath.prop('arviointi')}
              // TODO: Korjaa tyypitys
              // @ts-expect-error
              view={ArvosanaView}
              edit={(arvosanaProps) => {
                if (hasNoArviointi(osasuoritus)) {
                  return null
                }
                return (
                  <ArvosanaEdit
                    {...arvosanaProps}
                    // @ts-expect-error
                    createArviointi={(arvosana) => {
                      console.log('arvosana', arvosana)
                      const arviointi =
                        createVstArviointi(osasuoritus)(arvosana)
                      console.log(arviointi)
                      return arviointi
                    }}
                  />
                )
              }}
              testId={`vst.arvosana`}
            />
          </OsasuoritusSubproperty>
          <OsasuoritusSubproperty rowNumber={1} label="Päivämäärä">
            <FormField
              form={props.form}
              path={viimeisinArviointiPath.prop('päivä')}
              view={DateView}
              edit={DateEdit}
              testId={`vst.arvostelunPvm`}
            />
          </OsasuoritusSubproperty>
          {'osasuoritukset' in osasuoritus && (
            <OsasuoritusSubproperty rowNumber={2} label="Osasuoritukset">
              <>
                <div>
                  {osasuoritus.osasuoritukset !== undefined && (
                    <span>
                      {'Osasuorituksia (TODO): '}
                      {osasuoritus.osasuoritukset.length}
                    </span>
                  )}
                </div>
              </>
            </OsasuoritusSubproperty>
          )}
        </OsasuoritusProperty>
      )}
    </div>
  )
}
