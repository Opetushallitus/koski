import React from 'react'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { FormField } from '../components-v2/forms/FormField'
import { FormListField } from '../components-v2/forms/FormListField'
import { FormModel } from '../components-v2/forms/FormModel'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import {
  emptyUlkomaanjakso,
  UlkomaanjaksoEdit,
  UlkomaanjaksoView
} from '../components-v2/opiskeluoikeus/UlkomaanjaksoField'
import { t } from '../i18n/i18n'
import { AhvenanmaanAmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AhvenanmaanAmmatillinenOpiskeluoikeus'
import { AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/AhvenanmaanAmmatillisenOpiskeluoikeudenLisatiedot'
import { append } from '../util/fp/arrays'

interface AhvenanmaanAmmatillinenLisatiedotProps {
  form: FormModel<AhvenanmaanAmmatillinenOpiskeluoikeus>
}

export const AhvenanmaanAmmatillinenLisatiedot: React.FC<
  AhvenanmaanAmmatillinenLisatiedotProps
> = ({ form }) => {
  const lisatiedotPath = form.root
    .prop('lisätiedot')
    .valueOr(AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot())

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Ulkomaanjaksot" largeLabel>
        <FormListField
          form={form}
          path={lisatiedotPath.prop('ulkomaanjaksot')}
          view={UlkomaanjaksoView}
          edit={UlkomaanjaksoEdit}
          testId="ulkomaanjaksot"
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  lisatiedotPath.prop('ulkomaanjaksot').valueOr([]),
                  append(emptyUlkomaanjakso)
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <KeyValueRow localizableLabel="Koulutusvienti" largeLabel>
        <FormField
          form={form}
          view={BooleanView}
          viewProps={{ hideFalse: true }}
          edit={BooleanEdit}
          path={lisatiedotPath.prop('koulutusvienti')}
        />
      </KeyValueRow>

      <KeyValueRow
        localizableLabel="Siirtynyt tutkinnon uusiin perusteisiin"
        largeLabel
      >
        <FormField
          form={form}
          view={BooleanView}
          viewProps={{ hideFalse: true }}
          edit={BooleanEdit}
          path={lisatiedotPath.prop('siirtynytUusiinTutkinnonPerusteisiin')}
        />
      </KeyValueRow>
    </KeyValueTable>
  )
}
