import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus } from '../types/fi/oph/koski/schema/YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'
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
  SuorituksenVahvistusEdit,
  SuorituksenVahvistusView
} from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import {
  OsaamisenTunnustusView,
  TunnustusEdit
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
import { append } from '../util/fp/arrays'
import { NäyttöEdit, NäyttöView } from './Näyttö'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import {
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import { OsasuoritusTable } from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { YhteisenTutkinnonOsanOsaAlueenSuoritusProperties } from './YhteisenTutkinnonOsanOsaAlueenSuoritusProperties'
import React from 'react'
import { ArviointiEdit, ArviointiView, emptyArviointi } from './Arviointi'
import {
  KoodistoEdit,
  KoodistoView
} from '../components-v2/opiskeluoikeus/KoodistoField'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from '../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaPaikkakunnalla'

type YhteisenAmmatillisenTutkinnonOsasuoritusPropertiesProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
  >
  osasuoritus: YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
}
export const YhteisenOsittaisenAmmatillisenTutkinnonOsasuoritusProperties = ({
  form,
  osasuoritusPath,
  osasuoritus
}: YhteisenAmmatillisenTutkinnonOsasuoritusPropertiesProps) => {
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
      <OsasuoritusProperty label={'Vahvistus'}>
        <OsasuoritusPropertyValue>
          <FormField
            form={form}
            path={osasuoritusPath.prop('vahvistus')}
            view={SuorituksenVahvistusView}
            edit={SuorituksenVahvistusEdit}
            editProps={{
              vahvistusClass:
                HenkilövahvistusValinnaisellaPaikkakunnalla.className
            }}
          />
        </OsasuoritusPropertyValue>
      </OsasuoritusProperty>
      {(form.editMode || osasuoritus.tunnustettu) && (
        <OsasuoritusProperty label={'Tunnustettu'}>
          <OsasuoritusPropertyValue>
            <FormField
              form={form}
              path={osasuoritusPath.prop('tunnustettu')}
              view={
                OsaamisenTunnustusView /*TODO custom komponentti amikselle?*/
              }
              editProps={{
                createEmptyTunnustus: () =>
                  OsaamisenTunnustaminen({ selite: localize('') })
              }}
              edit={TunnustusEdit}
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
        </OsasuoritusPropertyValue>
      </OsasuoritusProperty>
      <OsasuoritusTable
        editMode={form.editMode}
        rows={
          osasuoritus.osasuoritukset?.map((s, index) => {
            return {
              suoritusIndex: 1,
              osasuoritusIndex: index,
              columns: {
                'Osa-alue': t(s.koulutusmoduuli.tunniste.nimi),
                Laajuus:
                  s.koulutusmoduuli.laajuus &&
                  `${s.koulutusmoduuli.laajuus?.arvo} ${t(s.koulutusmoduuli.laajuus?.yksikkö.lyhytNimi)}`,
                Arvosana: <ParasArvosanaView value={s.arviointi} />
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
      />
    </>
  )
}
