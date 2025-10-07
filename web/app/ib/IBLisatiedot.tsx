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
  AikajaksoEdit,
  AikajaksoView
} from '../components-v2/opiskeluoikeus/AikajaksoField'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import {
  emptyMaksuttomuuus,
  MaksuttomuusEdit,
  MaksuttomuusView
} from '../components-v2/opiskeluoikeus/MaksuttomuusField'
import { todayISODate } from '../date/date'
import { t } from '../i18n/i18n'
import { Aikajakso } from '../types/fi/oph/koski/schema/Aikajakso'
import { IBOpiskeluoikeus } from '../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { LukionOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeudenLisatiedot'
import { Maksuttomuus } from '../types/fi/oph/koski/schema/Maksuttomuus'
import { OikeuttaMaksuttomuuteenPidennetty } from '../types/fi/oph/koski/schema/OikeuttaMaksuttomuuteenPidennetty'
import { append } from '../util/fp/arrays'
import {
  emptyUlkomaanjakso,
  UlkomaanjaksoEdit,
  UlkomaanjaksoView
} from '../components-v2/opiskeluoikeus/UlkomaanjaksoField'
import {
  emptyErityisenKoulutustehtävänJakso,
  ErityisenKoulutustehtävänJaksoEdit,
  ErityisenKoulutustehtävänJaksoView
} from '../components-v2/opiskeluoikeus/ErityisenKoulutustehtavanJaksoField'

interface IBLisätiedotProps {
  form: FormModel<IBOpiskeluoikeus>
}

export const IBLisätiedot: React.FC<IBLisätiedotProps> = ({ form }) => {
  const path = form.root.prop('lisätiedot').valueOr(emptyLisätiedot)

  const addMaksuttomuusjakso = () => {
    form.updateAt(
      path.prop('maksuttomuus').valueOr([]),
      append(emptyMaksuttomuuus)
    )
  }

  const addOikeuttaMaksuttomuuteenPidennetty = () => {
    form.updateAt(
      path.prop('oikeuttaMaksuttomuuteenPidennetty').valueOr([]),
      append(
        OikeuttaMaksuttomuuteenPidennetty({
          alku: todayISODate(),
          loppu: todayISODate()
        })
      )
    )
  }

  const addUlkomaanjakso = () => {
    form.updateAt(
      path.prop('ulkomaanjaksot').valueOr([]),
      append(emptyUlkomaanjakso)
    )
  }

  const addSisäoppilaitosmainenMajoitus = () => {
    form.updateAt(
      path.prop('sisäoppilaitosmainenMajoitus').valueOr([]),
      append(Aikajakso({ alku: todayISODate() }))
    )
  }

  const addErityisenKoulutustehtävänJakso = () => {
    form.updateAt(
      path.prop('erityisenKoulutustehtävänJaksot').valueOr([]),
      append(emptyErityisenKoulutustehtävänJakso)
    )
  }

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Pidennetty päättymispäivä" largeLabel>
        <FormField
          form={form}
          path={path.prop('pidennettyPäättymispäivä')}
          view={BooleanView}
          edit={BooleanEdit}
          testId="pidennettyPäättymispäivä"
        />
      </KeyValueRow>

      <KeyValueRow localizableLabel="Ulkomainen vaihto-opiskelija" largeLabel>
        <FormField
          form={form}
          path={path.prop('ulkomainenVaihtoopiskelija')}
          view={BooleanView}
          edit={BooleanEdit}
          testId="ulkomainenVaihtoopiskelija"
        />
      </KeyValueRow>

      <KeyValueRow
        localizableLabel="Erityisen koulutustehtävän jaksot"
        largeLabel
      >
        <FormListField
          form={form}
          path={path.prop('erityisenKoulutustehtävänJaksot')}
          view={ErityisenKoulutustehtävänJaksoView}
          edit={ErityisenKoulutustehtävänJaksoEdit}
          testId="erityisenKoulutustehtävänJaksot"
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton onClick={addErityisenKoulutustehtävänJakso}>
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <KeyValueRow localizableLabel="Ulkomaanjaksot" largeLabel>
        <FormListField
          form={form}
          path={path.prop('ulkomaanjaksot')}
          view={UlkomaanjaksoView}
          edit={UlkomaanjaksoEdit}
          testId="ulkomaanjaksot"
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton onClick={addUlkomaanjakso}>{t('Lisää')}</FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <KeyValueRow localizableLabel="Sisäoppilaitosmainen majoitus" largeLabel>
        <FormListField
          form={form}
          path={path.prop('sisäoppilaitosmainenMajoitus')}
          view={AikajaksoView}
          edit={AikajaksoEdit}
          editProps={{
            createAikajakso: Aikajakso
          }}
          testId="sisäoppilaitosmainenMajoitus"
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton onClick={addSisäoppilaitosmainenMajoitus}>
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <KeyValueRow localizableLabel="Maksuttomuus" largeLabel>
        <FormListField
          form={form}
          path={path.prop('maksuttomuus')}
          view={MaksuttomuusView}
          edit={MaksuttomuusEdit}
          testId="maksuttomuus"
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton onClick={addMaksuttomuusjakso}>{t('Lisää')}</FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <KeyValueRow
        localizableLabel="Oikeutta maksuttomuuteen pidennetty"
        largeLabel
      >
        <FormListField
          form={form}
          path={path.prop('oikeuttaMaksuttomuuteenPidennetty')}
          view={AikajaksoView}
          edit={AikajaksoEdit}
          editProps={{
            createAikajakso: ({ alku, loppu }) =>
              loppu
                ? OikeuttaMaksuttomuuteenPidennetty({ alku, loppu })
                : undefined
          }}
          testId="oikeuttaMaksuttomuuteenPidennetty"
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton onClick={addOikeuttaMaksuttomuuteenPidennetty}>
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>
    </KeyValueTable>
  )
}

const emptyLisätiedot = LukionOpiskeluoikeudenLisätiedot()
