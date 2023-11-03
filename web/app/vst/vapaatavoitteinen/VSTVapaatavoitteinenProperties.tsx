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
import { VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi } from '../../types/fi/oph/koski/schema/VapaanSivistystyoVapaatavoitteisenKoulutuksenArviointi'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { deleteAt } from '../../util/array'
import { createArviointi } from '../common/arviointi'
import { VSTArviointiField } from '../common/propertyFields'
import { VSTSuoritus } from '../common/types'
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
    osasuoritus: VSTSuoritus
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

type OsasuoritusToTableRowParams = {
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
    osasuoritus: VSTSuoritus
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
          edit={(arvosanaProps) => (
            <ParasArvosanaEdit
              {...arvosanaProps}
              createArviointi={createArviointi(
                VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi
              )}
            />
          )}
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
