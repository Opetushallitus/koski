import React, { useCallback, useContext, useEffect, useMemo } from 'react'
import { useSchema } from '../appstate/constraints'
import { useKoodistoFiller } from '../appstate/koodisto'
import { OpiskeluoikeusContext } from '../appstate/opiskeluoikeus'
import { OpenAllButton, useTree } from '../appstate/tree'
import { KansalainenOnly } from '../components-v2/access/KansalainenOnly'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FormField } from '../components-v2/forms/FormField'
import { FormOptic, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { Spacer } from '../components-v2/layout/Spacer'
import {
  LaajuusView,
  laajuusSum
} from '../components-v2/opiskeluoikeus/LaajuusField'
import {
  OpintokokonaisuusEdit,
  OpintokokonaisuusView
} from '../components-v2/opiskeluoikeus/OpintokokonaisuusField'
import { PäätasonSuorituksenSuostumuksenPeruminen } from '../components-v2/opiskeluoikeus/OpiskeluoikeudenSuostumuksenPeruminen'
import {
  ToimipisteEdit,
  ToimipisteView
} from '../components-v2/opiskeluoikeus/OpiskeluoikeudenToimipiste'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import { OsasuoritusTable } from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import {
  PerusteEdit,
  PerusteView
} from '../components-v2/opiskeluoikeus/PerusteField'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import {
  SuorituskieliEdit,
  SuorituskieliView
} from '../components-v2/opiskeluoikeus/SuorituskieliField'
import {
  TodistuksellaNäkyvätLisätiedotEdit,
  TodistuksellaNäkyvätLisätiedotView
} from '../components-v2/opiskeluoikeus/TodistuksellaNäkyvätLisätiedotField'
import { Trans } from '../components-v2/texts/Trans'
import { Infobox } from '../components/Infobox'
import { t } from '../i18n/i18n'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { parasArviointi } from '../util/arvioinnit'
import { append } from '../util/fp/arrays'
import { formatNumber, sum } from '../util/numbers'
import { VSTLisatiedot } from './VSTLisatiedot'
import {
  AddNewVSTOsasuoritusView,
  osasuoritusToTableRow
} from './VSTOsasuoritusProperties'
import { useInfoLink } from './infoLinkHook'
import {
  createVstOpiskeluoikeusjakso,
  kaikkiOsasuorituksetVahvistettu,
  resolveDiaarinumero,
  resolveOpiskeluoikeudenTilaClass,
  vstNimi,
  vstSuorituksenNimi
} from './resolvers'
import {
  VSTOsasuoritus,
  hasOpintokokonaisuus,
  isLaajuuksellinenVSTKoulutusmoduuli,
  isPerusteellinenVSTKoulutusmoduuli,
  isVSTOsasuoritusArvioinnilla
} from './typeguards'

type VSTEditorProps =
  AdaptedOpiskeluoikeusEditorProps<VapaanSivistystyönOpiskeluoikeus>

export const VSTEditor: React.FC<VSTEditorProps> = (props) => {
  // Opiskeluoikeus
  const opiskeluoikeusSchema = useSchema(
    VapaanSivistystyönOpiskeluoikeus.className
  )

  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)

  const fillKoodistot = useKoodistoFiller()

  const { setOrganisaatio } = useContext(OpiskeluoikeusContext)

  // Oppilaitos
  const organisaatio =
    props.opiskeluoikeus.oppilaitos || props.opiskeluoikeus.koulutustoimija

  useEffect(() => {
    setOrganisaatio(organisaatio)
  }, [organisaatio, setOrganisaatio])

  // Päätason suoritus
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(form)

  const { infoDescription, infoLinkTitle, infoLinkUrl } = useInfoLink(
    päätasonSuoritus.suoritus.koulutusmoduuli.$class
  )

  const appendOsasuoritus = useCallback(
    (
      // TODO: Path-tyypitys
      path: FormOptic<VapaanSivistystyönPäätasonSuoritus, any>,
      uusiOsasuoritus: VSTOsasuoritus
    ) => {
      if (form.editMode) {
        form.updateAt(path, (osasuoritus) => ({
          ...osasuoritus,
          osasuoritukset: append(uusiOsasuoritus)(osasuoritus.osasuoritukset)
        }))
      }
    },
    [form]
  )

  const createOsasuoritus = useCallback(
    (suoritusPath: any, osasuoritus: VSTOsasuoritus) => {
      fillKoodistot(osasuoritus)
        .then((filledOsasuoritus) => {
          appendOsasuoritus(suoritusPath, filledOsasuoritus)
        })
        .catch(console.error)
    },
    [appendOsasuoritus, fillKoodistot]
  )

  const rootLevel = 0
  const { TreeNode, ...tree } = useTree()

  const suorituksenVahvistus = useMemo(() => {
    if (päätasonSuoritus.suoritus.osasuoritukset === undefined) {
      return false
    }
    const kaikkiArvioinnit = päätasonSuoritus.suoritus.osasuoritukset.flatMap(
      (osasuoritus) => {
        if (isVSTOsasuoritusArvioinnilla(osasuoritus)) {
          if ('arviointi' in osasuoritus) {
            return parasArviointi<Arviointi>(osasuoritus.arviointi || [])
          } else {
            return undefined
          }
        } else {
          return []
        }
      }
    )
    return !kaikkiArvioinnit.every((a) => a !== undefined)
  }, [päätasonSuoritus.suoritus.osasuoritukset])

  // Render
  return (
    <TreeNode>
      <OpiskeluoikeusTitle
        opiskeluoikeus={form.state}
        opiskeluoikeudenNimi={vstNimi(form.state)}
      />
      <EditorContainer
        form={form}
        invalidatable={props.invalidatable}
        oppijaOid={props.oppijaOid}
        suorituksenNimi={vstSuorituksenNimi}
        suorituksetVahvistettu={kaikkiOsasuorituksetVahvistettu(form.state)}
        createOpiskeluoikeusjakso={createVstOpiskeluoikeusjakso(
          päätasonSuoritus
        )}
        opiskeluoikeusJaksoClassName={resolveOpiskeluoikeudenTilaClass(
          päätasonSuoritus
        )}
        lisätiedotContainer={VSTLisatiedot}
        onChangeSuoritus={setPäätasonSuoritus}
        testId={`${päätasonSuoritus.testId}.editor-container`}
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
            testId={`${päätasonSuoritus.testId}.toimipiste`}
          >
            <FormField
              form={form}
              path={päätasonSuoritus.path.prop('toimipiste')}
              view={ToimipisteView}
              edit={ToimipisteEdit}
              editProps={{
                onChangeToimipiste: (data: any) => {
                  form.updateAt(
                    päätasonSuoritus.path.prop('toimipiste').optional(),
                    () => data
                  )
                }
              }}
            />
          </KeyValueRow>
          <KeyValueRow
            label="Koulutus"
            testId={`${päätasonSuoritus.testId}.koulutusmoduuli.tunniste`}
          >
            <Trans>
              {päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.nimi}
            </Trans>
          </KeyValueRow>
          <KeyValueRow
            label="Koulutusmoduuli"
            indent={2}
            testId={`${päätasonSuoritus.testId}.koulutusmoduuli.tunniste.koodiarvo`}
          >
            {päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.koodiarvo}
          </KeyValueRow>
          {isPerusteellinenVSTKoulutusmoduuli(
            päätasonSuoritus.suoritus.koulutusmoduuli
          ) && (
            <KeyValueRow
              label="Peruste"
              indent={2}
              testId={`${päätasonSuoritus.testId}.peruste`}
            >
              <FormField
                form={form}
                path={päätasonSuoritus.path
                  .prop('koulutusmoduuli')
                  .guard(isPerusteellinenVSTKoulutusmoduuli)
                  .prop('perusteenDiaarinumero')
                  .optional()}
                view={PerusteView}
                edit={PerusteEdit}
                testId={`${päätasonSuoritus.testId}.peruste.koulutusmoduuli`}
                editProps={{
                  diaariNumero: resolveDiaarinumero(
                    päätasonSuoritus.suoritus.koulutusmoduuli
                  )
                }}
              />
            </KeyValueRow>
          )}
          {hasOpintokokonaisuus(päätasonSuoritus.suoritus) && (
            <>
              <KeyValueRow
                label="Opintokokonaisuus"
                indent={2}
                testId={`${päätasonSuoritus.testId}.opintokokonaisuus`}
              >
                <FormField
                  form={form}
                  path={päätasonSuoritus.path
                    .guard(hasOpintokokonaisuus)
                    .prop('koulutusmoduuli')
                    .prop('opintokokonaisuus')
                    .optional()}
                  view={OpintokokonaisuusView}
                  edit={OpintokokonaisuusEdit}
                />
              </KeyValueRow>
              {infoLinkTitle !== undefined &&
                infoLinkUrl !== undefined &&
                infoDescription !== undefined && (
                  <Infobox>
                    <>
                      {t(`infoDescription:${infoDescription}`)}
                      <br />
                      <a
                        href={t(`infoLinkUrl:${infoLinkUrl}`)}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        {t(`infoLinkTitle:${infoLinkTitle}`)}
                      </a>
                    </>
                  </Infobox>
                )}
            </>
          )}
          {isLaajuuksellinenVSTKoulutusmoduuli(
            päätasonSuoritus.suoritus.koulutusmoduuli
          ) && (
            <KeyValueRow label="Laajuus" indent={2}>
              <FormField
                form={form}
                path={päätasonSuoritus.path
                  .prop('koulutusmoduuli')
                  .guard(isLaajuuksellinenVSTKoulutusmoduuli)
                  .prop('laajuus')}
                view={LaajuusView}
                auto={laajuusSum(
                  päätasonSuoritus.path
                    .prop('osasuoritukset')
                    .elems()
                    .path('koulutusmoduuli.laajuus'),
                  form.state
                )}
                testId={`${päätasonSuoritus.testId}.laajuus`}
              />
            </KeyValueRow>
          )}
          <KeyValueRow
            label="Opetuskieli"
            testId={`${päätasonSuoritus.testId}.opetuskieli`}
          >
            <FormField
              form={form}
              path={päätasonSuoritus.path.prop('suorituskieli')}
              view={SuorituskieliView}
              edit={SuorituskieliEdit}
            />
          </KeyValueRow>
          <KeyValueRow
            label="Todistuksella näkyvät lisätiedot"
            testId={`${päätasonSuoritus.testId}.todistuksella-nakyvat-lisatiedot`}
          >
            <FormField
              form={form}
              path={päätasonSuoritus.path.prop(
                'todistuksellaNäkyvätLisätiedot'
              )}
              view={TodistuksellaNäkyvätLisätiedotView}
              edit={TodistuksellaNäkyvätLisätiedotEdit}
            />
          </KeyValueRow>
        </KeyValueTable>
        <Spacer />
        <SuorituksenVahvistusField
          form={form}
          suoritusPath={päätasonSuoritus.path}
          organisaatio={organisaatio}
          disableAdd={suorituksenVahvistus}
          testId={päätasonSuoritus.testId}
        />
        <Spacer />

        {päätasonSuoritus.suoritus.osasuoritukset && (
          <OpenAllButton {...tree} />
        )}

        <Spacer />
        <OsasuoritusTable
          testId={päätasonSuoritus.testId}
          editMode={form.editMode}
          level={rootLevel}
          addNewOsasuoritusView={AddNewVSTOsasuoritusView}
          addNewOsasuoritusViewProps={{
            form,
            createOsasuoritus,
            level: rootLevel,
            // Polku, johon uusi osasuoritus lisätään. Polun tulee sisältää "osasuoritukset"-property.
            // @ts-expect-error Korjaa tyyppi
            pathWithOsasuoritukset: päätasonSuoritus.path
          }}
          completed={(rowIndex) => {
            const osasuoritus = (päätasonSuoritus.suoritus.osasuoritukset ||
              [])[rowIndex]
            if (!osasuoritus) {
              return false
            }
            if (
              osasuoritus.$class ===
              VSTKotoutumiskoulutuksenOhjauksenSuoritus2022.className
            ) {
              return true
            }
            if (!isVSTOsasuoritusArvioinnilla(osasuoritus)) {
              // Palauttamalla undefined ei näytetä kesken tai valmistunut -merkkiä
              return undefined
            }
            return (
              osasuoritus.arviointi !== undefined &&
              osasuoritus.arviointi.length > 0
            )
          }}
          rows={(päätasonSuoritus.suoritus.osasuoritukset || []).map(
            (_os, osasuoritusIndex) =>
              osasuoritusToTableRow({
                level: rootLevel,
                form,
                createOsasuoritus,
                osasuoritusIndex,
                suoritusIndex: päätasonSuoritus.index,
                suoritusPath: päätasonSuoritus.path,
                testId: `${päätasonSuoritus.testId}.osasuoritukset.${osasuoritusIndex}`
              })
          )}
          onRemove={(i) => {
            form.updateAt(
              päätasonSuoritus.path.prop('osasuoritukset').optional(),
              (osasuoritukset) =>
                (osasuoritukset as any[]).filter((_, index) => index !== i)
            )
          }}
        />
        <KeyValueTable>
          <KeyValueRow
            label="Yhteensä"
            testId={`${päätasonSuoritus.testId}.yhteensa`}
          >
            {laajuudetYhteensä(päätasonSuoritus.suoritus)}
          </KeyValueRow>
        </KeyValueTable>
      </EditorContainer>
    </TreeNode>
  )
}

const laajuudetYhteensä = (pts: VapaanSivistystyönPäätasonSuoritus): string => {
  const n = formatNumber(
    sum(
      (pts.osasuoritukset || []).map(
        (os) => os.koulutusmoduuli.laajuus?.arvo || 0
      )
    )
  )
  const yksikkö =
    pts.osasuoritukset?.[0]?.koulutusmoduuli.laajuus?.yksikkö.lyhytNimi || ''

  return `${n} ${t(yksikkö)}`
}
