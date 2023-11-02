import React from 'react'
import { subTestId } from '../../components-v2/CommonProps'
import { LocalizedTextView } from '../../components-v2/controls/LocalizedTestField'
import { FormField } from '../../components-v2/forms/FormField'
import { FormListField } from '../../components-v2/forms/FormListField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../../components-v2/forms/FormModel'
import {
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../../components-v2/opiskeluoikeus/ArvosanaField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../../components-v2/opiskeluoikeus/LaajuusField'
import { OsasuoritusProperty } from '../../components-v2/opiskeluoikeus/OsasuoritusProperty'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönJotpaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { deleteAt } from '../../util/array'
import { VSTArviointiEdit, VSTArviointiView } from '../VSTArviointiField'
import { createVstArviointi } from '../resolvers'
import { VSTOsasuoritus } from '../typeguards'
import { AddJotpaOsasuoritusView } from './AddJotpaOsasuoritus'

type VSTJotpaPropertiesProps = {
  osasuoritusIndex: number
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus
  >
  createOsasuoritus: (
    path: FormOptic<VapaanSivistystyönPäätasonSuoritus, any>,
    osasuoritus: VSTOsasuoritus
  ) => void
  testId: string
}

export const VSTJotpaProperties: React.FC<VSTJotpaPropertiesProps> = (
  props
) => {
  const osasuoritus = getValue(props.osasuoritusPath)(props.form.state)
  const arvioitu = (osasuoritus?.arviointi?.length || 0) > 0

  return (
    <div>
      {arvioitu && (
        <OsasuoritusProperty label="Arviointi">
          <FormListField
            form={props.form}
            path={props.osasuoritusPath.prop('arviointi')}
            view={VSTArviointiView}
            edit={VSTArviointiEdit}
            editProps={{ osasuoritus }}
            testId={subTestId(props, 'arviointi')}
          />
        </OsasuoritusProperty>
      )}
      <OsasuoritusTable
        testId={props.testId}
        editMode={props.form.editMode}
        addNewOsasuoritusView={AddJotpaOsasuoritusView}
        addNewOsasuoritusViewProps={{
          form: props.form,
          level: props.level + 1,
          createOsasuoritus: props.createOsasuoritus,
          // @ts-expect-error TODO: Tyypitä fiksusti
          pathWithOsasuoritukset: props.osasuoritusPath
        }}
        onRemove={(i) => {
          props.form.updateAt(
            props.osasuoritusPath.prop('osasuoritukset').optional(),
            deleteAt(i)
          )
        }}
        rows={(osasuoritus?.osasuoritukset || []).map(
          (_os, osasuoritusIndex) => {
            return osasuoritusToTableRow({
              level: props.level + 1,
              form: props.form,
              osasuoritusPath: props.osasuoritusPath,
              createOsasuoritus: props.createOsasuoritus,
              // @ts-expect-error
              suoritusPath: props.osasuoritusPath,
              osasuoritusIndex: osasuoritusIndex,
              suoritusIndex: props.osasuoritusIndex,
              testId: String(
                subTestId(props, `osasuoritukset.${osasuoritusIndex}`)
              )
            })
          }
        )}
      />
    </div>
  )
}

interface OsasuoritusToTableRowParams {
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönJotpaKoulutuksenSuoritus
  >
  suoritusIndex: number
  osasuoritusIndex: number
  createOsasuoritus: (
    path: FormOptic<VapaanSivistystyönPäätasonSuoritus, any>,
    osasuoritus: VSTOsasuoritus
  ) => void
  testId: string
}

export const osasuoritusToTableRow = ({
  suoritusPath,
  suoritusIndex,
  osasuoritusIndex,
  form,
  level,
  createOsasuoritus,
  testId
}: OsasuoritusToTableRowParams): OsasuoritusRowData<
  'Osasuoritus' | 'Laajuus' | 'Arvosana'
> => {
  const osasuoritus = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)

  const osasuoritusValue = getValue(osasuoritus)(form.state)

  return {
    suoritusIndex,
    osasuoritusIndex,
    osasuoritusPath: suoritusPath.prop('osasuoritukset').optional(),
    expandable: true,
    columns: {
      Osasuoritus: (
        <FormField
          form={form}
          path={osasuoritus.path('koulutusmoduuli.tunniste.nimi')}
          view={LocalizedTextView}
          testId={`${testId}.nimi`}
        />
      ),
      Laajuus: (
        <FormField
          form={form}
          path={osasuoritus.path('koulutusmoduuli.laajuus')}
          view={LaajuusView}
          edit={LaajuusOpintopisteissäEdit}
          testId={`${testId}.laajuus`}
        />
      ),
      Arvosana: (
        <FormField
          form={form}
          path={osasuoritus.path('arviointi')}
          view={ParasArvosanaView}
          edit={(arvosanaProps) => {
            if (osasuoritusValue === undefined) {
              return null
            }
            return (
              <ParasArvosanaEdit
                {...arvosanaProps}
                createArviointi={(arvosana) => {
                  return createVstArviointi(osasuoritusValue)(arvosana)
                }}
              />
            )
          }}
          testId={`${testId}.arvosana`}
        />
      )
    },
    content: (
      <VSTJotpaProperties
        level={level}
        osasuoritusIndex={osasuoritusIndex}
        form={form}
        osasuoritusPath={osasuoritus}
        createOsasuoritus={createOsasuoritus}
        testId={testId}
      />
    )
  }
}
