import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsaaPienemmanKokonaisuudenSuoritus'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { FormField } from '../components-v2/forms/FormField'
import {
  OsaamisenTunnustusEdit,
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
import React from 'react'
import { ArviointiEdit, ArviointiView, emptyArviointi } from './Arviointi'
import {
  KoodistoEdit,
  KoodistoView
} from '../components-v2/opiskeluoikeus/KoodistoField'

type AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritusPropertiesProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus
  >
  osasuoritus: AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus
}
export const AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritusProperties =
  ({
    form,
    osasuoritusPath,
    osasuoritus
  }: AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritusPropertiesProps) => {
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
      </>
    )
  }
