import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus } from '../types/fi/oph/koski/schema/MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { FormField } from '../components-v2/forms/FormField'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import {
  OrganisaatioEdit,
  OrganisaatioView
} from '../components-v2/opiskeluoikeus/OrganisaatioField'
import {
  OsaamisenTunnustusEdit,
  OsaamisenTunnustusView
} from '../components-v2/opiskeluoikeus/TunnustusField'
import { OsaamisenTunnustaminen } from '../types/fi/oph/koski/schema/OsaamisenTunnustaminen'
import { localize, t } from '../i18n/i18n'
import { FormListField } from '../components-v2/forms/FormListField'
import {
  emptyAmmatillisenTutkinnonOsanLisätieto,
  LisätietoEdit,
  LisätietoView
} from './LisätietoField'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { append, deleteAt } from '../util/fp/arrays'
import { NäyttöEdit, NäyttöView } from './Näyttö'
import { OsasuoritusTable } from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritusProperties } from './AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritusProperties'
import React from 'react'
import {
  KoodistoEdit,
  KoodistoView
} from '../components-v2/opiskeluoikeus/KoodistoField'
import {
  AmisArvosanaInTableEdit,
  AmisArvosanaInTableView,
  ArviointiEdit,
  ArviointiView,
  emptyArviointi
} from './Arviointi'
import {
  LaajuusEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { LaajuusOsaamispisteissä } from '../types/fi/oph/koski/schema/LaajuusOsaamispisteissa'
import { hasAmmatillinenArviointi } from './OsasuoritusTables'
import { TestIdLayer, TestIdText } from '../appstate/useTestId'
import { MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus } from '../types/fi/oph/koski/schema/MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'
import { PerusteView } from '../components-v2/opiskeluoikeus/PerusteField'

type MuunOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaTutkinnonosanSuoritusPropertiesProps =
  {
    form: FormModel<AmmatillinenOpiskeluoikeus>
    osasuoritusPath: FormOptic<
      AmmatillinenOpiskeluoikeus,
      MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
    >
    osasuoritus: MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
  }
export const MuunOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaTutkinnonosanSuoritusProperties =
  ({
    form,
    osasuoritusPath,
    osasuoritus
  }: MuunOsittaisenAmmatillisenTutkinnonUseastaTutkinnostaTutkinnonosanSuoritusPropertiesProps) => {
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
              {t(osasuoritus.tutkinto.tunniste.nimi)}
            </TestIdText>{' '}
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
                view={NäyttöView}
                edit={NäyttöEdit}
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
        {(form.editMode || osasuoritus.korotettu !== undefined) && (
          <OsasuoritusProperty label={'Korotettu suoritus'}>
            <OsasuoritusPropertyValue>
              <FormField
                form={form}
                view={KoodistoView}
                edit={KoodistoEdit}
                editProps={{
                  koodistoUri: 'ammatillisensuorituksenkorotus',
                  zeroValueOption: true
                }}
                path={osasuoritusPath.prop('korotettu')}
              />
            </OsasuoritusPropertyValue>
          </OsasuoritusProperty>
        )}
        <OsasuoritusTable
          editMode={form.editMode}
          rows={
            osasuoritus.osasuoritukset?.map((s, index) => {
              return {
                suoritusIndex: 1,
                osasuoritusIndex: index,
                columns: {
                  'Osa-alue': t(s.koulutusmoduuli.tunniste.nimi),
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
                  <AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritusProperties
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
          completed={(rowIndex) => {
            const s = (osasuoritus.osasuoritukset || [])[rowIndex]
            if (s === undefined) {
              return undefined
            }
            return hasAmmatillinenArviointi(s)
          }}
          onRemove={(rowIndex) => {
            form.updateAt(osasuoritusPath, (os) => {
              return {
                ...os,
                osasuoritukset: deleteAt(os.osasuoritukset || [], rowIndex)
              }
            })
          }}
        />
      </>
    )
  }
