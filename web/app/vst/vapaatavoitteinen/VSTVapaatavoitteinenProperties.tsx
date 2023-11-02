import React from 'react'
import { subTestId } from '../../components-v2/CommonProps'
import { LocalizedTextView } from '../../components-v2/controls/LocalizedTestField'
import { FormField } from '../../components-v2/forms/FormField'
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
  KuvausEdit,
  KuvausView
} from '../../components-v2/opiskeluoikeus/KuvausField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../../components-v2/opiskeluoikeus/LaajuusField'
import {
  OsasuoritusProperty,
  OsasuoritusSubproperty
} from '../../components-v2/opiskeluoikeus/OsasuoritusProperty'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { deleteAt } from '../../util/array'
import { VSTArviointiField } from '../common/propertyFields'
import { createVstArviointi } from '../resolvers'
import { VSTOsasuoritus, isVSTOsasuoritusArvioinnilla } from '../typeguards'
import { AddVapaatavoitteinenOsasuoritus } from './AddVapaatavoitteinenOsasuoritus'

type VSTVapaatavoitteinenPropertiesProps = {
  osasuoritusIndex: number
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
  >
  createOsasuoritus: (
    path: FormOptic<VapaanSivistystyönPäätasonSuoritus, any>,
    osasuoritus: VSTOsasuoritus
  ) => void
  allOpen: boolean
  testId: string
}

export const VSTVapaatavoitteinenProperties: React.FC<
  VSTVapaatavoitteinenPropertiesProps
> = (props) => {
  const osasuoritus = getValue(props.osasuoritusPath)(props.form.state)
  const arvioitu = (osasuoritus?.arviointi?.length || 0) > 0

  return (
    <div>
      <VSTArviointiField
        form={props.form}
        path={props.osasuoritusPath}
        testId={props.testId}
      />
      <OsasuoritusProperty label="">
        <OsasuoritusSubproperty label="Kuvaus">
          <FormField
            form={props.form}
            path={props.osasuoritusPath.prop('koulutusmoduuli').prop('kuvaus')}
            view={KuvausView}
            edit={(kuvausProps) => {
              return <KuvausEdit {...kuvausProps} />
            }}
            testId={subTestId(props, 'kuvaus')}
          />
        </OsasuoritusSubproperty>
      </OsasuoritusProperty>
      <OsasuoritusTable
        testId={props.testId}
        editMode={props.form.editMode}
        addNewOsasuoritusView={AddVapaatavoitteinenOsasuoritus}
        addNewOsasuoritusViewProps={{
          form: props.form,
          level: props.level + 1,
          createOsasuoritus: props.createOsasuoritus,
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
              allOsasuorituksetOpen: props.allOpen,
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
    VapaanSivistystyönPäätasonSuoritus
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
            if (!isVSTOsasuoritusArvioinnilla(osasuoritusValue)) {
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
      <VSTVapaatavoitteinenProperties
        level={level}
        osasuoritusIndex={osasuoritusIndex}
        form={form}
        // @ts-expect-error Korjaa tyypitys
        osasuoritusPath={osasuoritus}
        createOsasuoritus={createOsasuoritus}
        testId={testId}
      />
    )
  }
}
