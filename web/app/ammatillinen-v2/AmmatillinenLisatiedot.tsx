import {
  FormModel,
  FormOptic,
  getValue
} from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import React from 'react'
import { AmmatillisenOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/AmmatillisenOpiskeluoikeudenLisatiedot'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FormField } from '../components-v2/forms/FormField'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import { FormListField } from '../components-v2/forms/FormListField'
import {
  AikajaksoEdit,
  AikajaksoView
} from '../components-v2/opiskeluoikeus/AikajaksoField'
import { Aikajakso } from '../types/fi/oph/koski/schema/Aikajakso'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { t } from '../i18n/i18n'
import { todayISODate } from '../date/date'
import { append } from '../util/fp/arrays'
import {
  emptyUlkomaanjakso,
  UlkomaanjaksoEdit,
  UlkomaanjaksoView
} from '../components-v2/opiskeluoikeus/UlkomaanjaksoField'

interface AmmatillinenLisatiedotProps {
  form: FormModel<AmmatillinenOpiskeluoikeus>
}

export const AmmatillinenLisatiedot: React.FC<AmmatillinenLisatiedotProps> = ({
  form
}) => {
  const emptyLisatiedot = AmmatillisenOpiskeluoikeudenLisätiedot()
  const lisatiedotPath = form.root.prop('lisätiedot').valueOr(emptyLisatiedot)
  const lisätiedot = getValue(lisatiedotPath)(form.state)

  const AikajaksoRow: React.FC<{
    localizableLabel: string
    path: keyof AmmatillisenOpiskeluoikeudenLisätiedot
  }> = ({ localizableLabel, path }) => {
    const aikajaksoPath = lisatiedotPath.prop(path) as FormOptic<
      AmmatillinenOpiskeluoikeus,
      Aikajakso[] | undefined
    >
    return (
      <KeyValueRow localizableLabel={localizableLabel} largeLabel>
        <FormListField
          form={form}
          view={AikajaksoView}
          edit={AikajaksoEdit}
          path={aikajaksoPath}
          editProps={{
            createAikajakso: Aikajakso
          }}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  aikajaksoPath.valueOr([]),
                  append(Aikajakso({ alku: todayISODate() }))
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>
    )
  }

  return (
    <KeyValueTable>
      <KeyValueRow
        localizableLabel="Oikeus maksuttomaan asuntolapaikkaan"
        largeLabel
      >
        <FormField
          form={form}
          view={BooleanView}
          edit={BooleanEdit}
          path={lisatiedotPath.prop('oikeusMaksuttomaanAsuntolapaikkaan')}
        />
      </KeyValueRow>

      <AikajaksoRow localizableLabel={'Majoitus'} path={'majoitus'} />
      <AikajaksoRow
        localizableLabel={'Sisäoppilaitosmainen majoitus'}
        path={'sisäoppilaitosmainenMajoitus'}
      />
      <AikajaksoRow
        localizableLabel={
          'Vaativan erityisen tuen yhteydessä järjestettävä majoitus'
        }
        path={'vaativanErityisenTuenYhteydessäJärjestettäväMajoitus'}
      />
      <AikajaksoRow
        localizableLabel={'Erityinen tuki'}
        path={'erityinenTuki'}
      />
      <AikajaksoRow
        localizableLabel={'Vaativan erityisen tuen erityinen tehtävä'}
        path={'vaativanErityisenTuenErityinenTehtävä'}
      />

      <KeyValueRow localizableLabel="Ulkomaan jaksot" largeLabel>
        <FormListField
          form={form}
          path={lisatiedotPath.prop('ulkomaanjaksot')}
          view={UlkomaanjaksoView}
          edit={UlkomaanjaksoEdit}
          testId="ulkomaanjaksot"
          removable
        />
        {/*TODO lisää placeholder / label tänne?*/}
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

      {/*TODO Hojks*/}

      <AikajaksoRow
        localizableLabel={'Vaikeasti vammaisille järjestetty opetus'}
        path={'vaikeastiVammainen'}
      />
    </KeyValueTable>
  )
}
