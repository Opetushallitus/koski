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
import { append } from '../util/fp/arrays'
import { NäyttöEdit, NäyttöView } from './Näyttö'
import React from 'react'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { YhteisenTutkinnonOsanOsaAlueenSuoritus } from '../types/fi/oph/koski/schema/YhteisenTutkinnonOsanOsaAlueenSuoritus'
import {
  KoodistoEdit,
  KoodistoView
} from '../components-v2/opiskeluoikeus/KoodistoField'
import { ArviointiEdit, ArviointiView, emptyArviointi } from './Arviointi'
import { TestIdLayer } from '../appstate/useTestId'

type YhteisenTutkinnonOsanOsaAlueenSuoritusPropertiesProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    YhteisenTutkinnonOsanOsaAlueenSuoritus
  >
  osasuoritus: YhteisenTutkinnonOsanOsaAlueenSuoritus
}
export const YhteisenTutkinnonOsanOsaAlueenSuoritusProperties = ({
  form,
  osasuoritusPath,
  osasuoritus
}: YhteisenTutkinnonOsanOsaAlueenSuoritusPropertiesProps) => {
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
    </>
  )
}
