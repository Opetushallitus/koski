import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsaaPienemmanKokonaisuudenSuoritus'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { FormField } from '../components-v2/forms/FormField'
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
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import {
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import React from 'react'

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
        <OsasuoritusProperty label={'Arviointi'}>
          <OsasuoritusPropertyValue>
            <KeyValueTable>
              <KeyValueRow localizableLabel={'Arvosana'}>
                <FormField
                  form={form}
                  view={
                    ParasArvosanaView /*TODO halutaanko pystyä editoimaan kaikki?*/
                  }
                  edit={ParasArvosanaEdit}
                  path={osasuoritusPath.prop('arviointi')}
                />
              </KeyValueRow>
            </KeyValueTable>
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      </>
    )
  }
