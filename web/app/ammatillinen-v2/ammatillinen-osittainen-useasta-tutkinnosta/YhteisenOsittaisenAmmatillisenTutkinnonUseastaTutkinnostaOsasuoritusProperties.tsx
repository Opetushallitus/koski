import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue
} from '../../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { FormField } from '../../components-v2/forms/FormField'
import {
  BooleanEdit,
  BooleanView
} from '../../components-v2/opiskeluoikeus/BooleanField'
import {
  OrganisaatioEdit,
  OrganisaatioView
} from '../../components-v2/opiskeluoikeus/OrganisaatioField'
import {
  OsaamisenTunnustusEdit,
  OsaamisenTunnustusView
} from '../../components-v2/opiskeluoikeus/TunnustusField'
import { OsaamisenTunnustaminen } from '../../types/fi/oph/koski/schema/OsaamisenTunnustaminen'
import { localize, t } from '../../i18n/i18n'
import { FormListField } from '../../components-v2/forms/FormListField'
import {
  emptyAmmatillisenTutkinnonOsanLisätieto,
  LisätietoEdit,
  LisätietoView
} from '../LisätietoField'
import { ButtonGroup } from '../../components-v2/containers/ButtonGroup'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { append, deleteAt } from '../../util/fp/arrays'
import {
  NäyttöAmmatillinenOsittainenEdit,
  NäyttöAmmatillinenOsittainenView
} from '../Näyttö'
import { OsasuoritusTable } from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { YhteisenTutkinnonOsanOsaAlueenSuoritusProperties } from '../YhteisenTutkinnonOsanOsaAlueenSuoritusProperties'
import React from 'react'
import {
  AmisArvosanaInTableEdit,
  AmisArvosanaInTableView,
  ArviointiEdit,
  ArviointiView,
  emptyArviointi
} from '../Arviointi'
import {
  KoodistoEdit,
  KoodistoView,
  KoodistoViewSpan
} from '../../components-v2/opiskeluoikeus/KoodistoField'
import {
  LaajuusEdit,
  LaajuusView
} from '../../components-v2/opiskeluoikeus/LaajuusField'
import { LaajuusOsaamispisteissä } from '../../types/fi/oph/koski/schema/LaajuusOsaamispisteissa'
import { KoodistoSelect } from '../../components-v2/opiskeluoikeus/KoodistoSelect'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { YhteisenTutkinnonOsanOsaAlueenSuoritus } from '../../types/fi/oph/koski/schema/YhteisenTutkinnonOsanOsaAlueenSuoritus'
import { AmmatillisenTutkinnonOsanOsaAlue } from '../../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsanOsaAlue'
import { Column, ColumnRow } from '../../components-v2/containers/Columns'
import { hasAmmatillinenArviointi } from '../OsasuoritusTables'
import { TestIdLayer, TestIdText } from '../../appstate/useTestId'
import { useTutkinnonOsanOsat } from '../useTutkinnonOsanOsat'
import { AmmatillisenTutkinnonOsittainenSuoritus } from '../../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsittainenSuoritus'
import { YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus } from '../../types/fi/oph/koski/schema/YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'
import { PerusteView } from '../../components-v2/opiskeluoikeus/PerusteField'

type YhteisenAmmatillisenTutkinnonOsasuoritusUseastaTutkinnostaPropertiesProps =
  {
    form: FormModel<AmmatillinenOpiskeluoikeus>
    osasuoritusPath: FormOptic<
      AmmatillinenOpiskeluoikeus,
      YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
    >
    osasuoritus: YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
  }
export const YhteisenOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaOsasuoritusProperties =
  ({
    form,
    osasuoritusPath,
    osasuoritus
  }: YhteisenAmmatillisenTutkinnonOsasuoritusUseastaTutkinnostaPropertiesProps) => {
    return (
      <>
        {(form.editMode || osasuoritus.suorituskieli) && (
          <OsasuoritusProperty label={'Suorituskieli'}>
            <OsasuoritusPropertyValue>
              <FormField
                form={form}
                view={KoodistoView}
                edit={KoodistoEdit}
                path={osasuoritusPath.prop('suorituskieli')}
                editProps={{ koodistoUri: 'kieli', zeroValueOption: true }}
              />
            </OsasuoritusPropertyValue>
          </OsasuoritusProperty>
        )}
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
        <OsasuoritusProperty label={'Tutkinto'}>
          <OsasuoritusPropertyValue>
            <TestIdText id="tutkintoNimi">
              {t(osasuoritus.tutkinto.perusteenNimi)}
            </TestIdText>
            {` (${osasuoritus.tutkinto.tunniste.koodiarvo}) `}
            <FormField
              form={form}
              path={osasuoritusPath
                .prop('tutkinto')
                .prop('perusteenDiaarinumero')}
              view={PerusteView}
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
        {(form.editMode || osasuoritus.tunnustettu) && (
          <OsasuoritusProperty label={'Tunnustettu'}>
            <OsasuoritusPropertyValue>
              <FormField
                form={form}
                path={osasuoritusPath.prop('tunnustettu')}
                view={OsaamisenTunnustusView}
                editProps={{
                  createEmptyTunnustus: () =>
                    OsaamisenTunnustaminen({ selite: localize('') })
                }}
                edit={OsaamisenTunnustusEdit}
              />
            </OsasuoritusPropertyValue>
          </OsasuoritusProperty>
        )}
        {(form.editMode || osasuoritus.lisätiedot) && (
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
        )}
        {(form.editMode || osasuoritus.näyttö) && (
          <OsasuoritusProperty label={'Näyttö'}>
            <OsasuoritusPropertyValue>
              <FormField
                form={form}
                view={NäyttöAmmatillinenOsittainenView}
                edit={NäyttöAmmatillinenOsittainenEdit}
                path={osasuoritusPath.prop('näyttö')}
              />
            </OsasuoritusPropertyValue>
          </OsasuoritusProperty>
        )}
        <OsasuoritusProperty label={'Arviointi'}>
          <OsasuoritusPropertyValue>
            <TestIdLayer id="arviointi">
              <FormListField
                removable
                form={form}
                view={ArviointiView}
                edit={ArviointiEdit}
                path={osasuoritusPath.prop('arviointi')}
              />
              {form.editMode && (
                <ButtonGroup>
                  <FlatButton
                    testId="lisää-arviointi"
                    onClick={() =>
                      form.updateAt(
                        osasuoritusPath.prop('arviointi').valueOr([]),
                        append(emptyArviointi)
                      )
                    }
                  >
                    {t('Lisää')}
                  </FlatButton>
                </ButtonGroup>
              )}
            </TestIdLayer>
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
        <OsasuoritusTable
          editMode={form.editMode}
          rows={
            osasuoritus.osasuoritukset?.map((s, index) => {
              const kielillinenKoulutusmoduuliPath = osasuoritusPath
                .prop('osasuoritukset')
                .valueOr([])
                .at(index)
                .prop('koulutusmoduuli') as unknown as FormOptic<
                AmmatillinenOpiskeluoikeus,
                KielillinenKoulutusmoduuli
              >

              return {
                suoritusIndex: 1,
                osasuoritusIndex: index,
                columns: {
                  'Osa-alue':
                    oppiaineToKielikoodistoMap[
                      s.koulutusmoduuli.tunniste.koodiarvo
                    ] !== undefined ? (
                      <>
                        <span>
                          {t(s.koulutusmoduuli.tunniste.nimi)}
                          {', '}
                        </span>
                        <FormField
                          form={form}
                          view={KoodistoViewSpan}
                          edit={KoodistoEdit}
                          path={kielillinenKoulutusmoduuliPath.prop('kieli')}
                          editProps={{
                            koodistoUri:
                              oppiaineToKielikoodistoMap[
                                s.koulutusmoduuli.tunniste.koodiarvo
                              ],
                            zeroValueOption: true
                          }}
                        />
                      </>
                    ) : (
                      t(s.koulutusmoduuli.tunniste.nimi)
                    ),
                  Laajuus: (
                    <FormField
                      form={form}
                      view={LaajuusView}
                      edit={LaajuusEdit}
                      editProps={{
                        createLaajuus: (arvo) =>
                          LaajuusOsaamispisteissä({ arvo })
                      }}
                      path={osasuoritusPath
                        .prop('osasuoritukset')
                        .valueOr([])
                        .at(index)
                        .prop('koulutusmoduuli')
                        .prop('laajuus')}
                    />
                  ),
                  Arvosana: (
                    <FormField
                      form={form}
                      view={AmisArvosanaInTableView}
                      edit={AmisArvosanaInTableEdit}
                      path={osasuoritusPath
                        .prop('osasuoritukset')
                        .valueOr([])
                        .at(index)
                        .prop('arviointi')}
                    />
                  )
                },
                content: (
                  <YhteisenTutkinnonOsanOsaAlueenSuoritusProperties
                    form={form}
                    osasuoritusPath={osasuoritusPath
                      .prop('osasuoritukset')
                      .valueOr([])
                      .at(index)}
                    osasuoritus={s}
                  />
                ),
                expandable: true
              }
            }) || []
          }
          onRemove={(rowIndex) => {
            form.updateAt(osasuoritusPath, (os) => {
              return {
                ...os,
                osasuoritukset: deleteAt(os.osasuoritukset || [], rowIndex)
              }
            })
          }}
          completed={(rowIndex) => {
            const s = (osasuoritus.osasuoritukset || [])[rowIndex]
            if (s === undefined) {
              return undefined
            }
            return hasAmmatillinenArviointi(s)
          }}
          addNewOsasuoritusView={NewYhteisenTutkinnonOsanOsaAlueenSuoritus}
          addNewOsasuoritusViewProps={{
            form,
            suoritusPath: osasuoritusPath,
            osasuoritus
          }}
        />
      </>
    )
  }

type NewYhteisenTutkinnonUseastaTutkinnostaOsanOsaAlueenSuoritusProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
  >
  osasuoritus: YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
}

const NewYhteisenTutkinnonOsanOsaAlueenSuoritus = ({
  form,
  suoritusPath,
  osasuoritus
}: NewYhteisenTutkinnonUseastaTutkinnostaOsanOsaAlueenSuoritusProps) => {
  const lisättävätOsat = useTutkinnonOsanOsat(
    (form.state.suoritukset[0] as AmmatillisenTutkinnonOsittainenSuoritus)
      .koulutusmoduuli.perusteenDiaarinumero || '',
    osasuoritus.koulutusmoduuli.tunniste.koodiarvo
  ).map((k) => k.koodiarvo)

  return (
    <ColumnRow indent={2}>
      <Column span={12}>
        <KoodistoSelect
          addNewText="Lisää tutkinnon osan osa-alue"
          koodistoUri="ammatillisenoppiaineet"
          filter={(oppiaine) =>
            lisättävätOsat.length === 0 ||
            lisättävätOsat.includes(oppiaine.koodiarvo)
          }
          format={(osa) => osa.koodiarvo + ' ' + t(osa.nimi)}
          onSelect={(tunniste) => {
            tunniste &&
              form.updateAt(
                suoritusPath.prop('osasuoritukset').valueOr([]),
                (a) => [...a, newYhteisenOsanOsaAlueenSuoritus(tunniste)]
              )
          }}
          testId="uusi-yhteinen-osan-osa-alue"
        />
      </Column>
    </ColumnRow>
  )
}

export const newYhteisenOsanOsaAlueenSuoritus = (
  tunniste: Koodistokoodiviite<'ammatillisenoppiaineet'>
): YhteisenTutkinnonOsanOsaAlueenSuoritus => {
  return YhteisenTutkinnonOsanOsaAlueenSuoritus({
    koulutusmoduuli: newMahdollisestiKielillinenKoulutusmoduuli(tunniste)
  })
}

const newMahdollisestiKielillinenKoulutusmoduuli = (
  tunniste: Koodistokoodiviite<'ammatillisenoppiaineet'>
): AmmatillisenTutkinnonOsanOsaAlue => {
  return {
    tunniste,
    pakollinen: false,
    $class:
      'fi.oph.koski.schema.ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue'
  } as AmmatillisenTutkinnonOsanOsaAlue
}

export const oppiaineToKielikoodistoMap: Record<string, string> = {
  VK: 'kielivalikoima',
  TK1: 'kielivalikoima',
  TK2: 'kielivalikoima',
  VVTK: 'kielivalikoima',
  VVTK26: 'kielivalikoima',
  VVAI: 'kielivalikoima',
  VVAI22: 'kielivalikoima',
  VVAI26: 'kielivalikoima',
  VVVK: 'kielivalikoima',
  VVVK26: 'kielivalikoima',
  AI: 'oppiaineaidinkielijakirjallisuus'
}

export type KielillinenKoulutusmoduuli = {
  kieli: Koodistokoodiviite
}
