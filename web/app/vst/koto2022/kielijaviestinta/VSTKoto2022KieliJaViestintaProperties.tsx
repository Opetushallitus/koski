import React from 'react'
import { LocalizedTextView } from '../../../components-v2/controls/LocalizedTestField'
import { FormField } from '../../../components-v2/forms/FormField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../../../components-v2/forms/FormModel'
import {
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../../../components-v2/opiskeluoikeus/ArvosanaField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../../../components-v2/opiskeluoikeus/LaajuusField'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { todayISODate } from '../../../date/date'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenArviointi'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import { VapaanSivistystyönOpiskeluoikeus } from '../../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { deleteAt } from '../../../util/array'
import { ArvosanaOf } from '../../../util/schema'
import {
  ArviointiProperty,
  TaitotasoProperty
} from '../../common/propertyFields'
import { AddKieliJaViestintaOsasuoritus } from './AddKieliJaViestintaOsasuoritus'

type VSTKoto2022KieliJaViestintaPropertiesProps = {
  osasuoritusIndex: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
  >
}

export const VSTKoto2022KieliJaViestintaProperties: React.FC<
  VSTKoto2022KieliJaViestintaPropertiesProps
> = (props) => {
  const osasuoritus = getValue(props.osasuoritusPath)(props.form.state)

  return (
    <div>
      <ArviointiProperty form={props.form} path={props.osasuoritusPath} />
      <OsasuoritusTable
        editMode={props.form.editMode}
        addNewOsasuoritusView={AddKieliJaViestintaOsasuoritus}
        addNewOsasuoritusViewProps={{
          form: props.form,
          osasuoritusPath: props.osasuoritusPath
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
              form: props.form,
              suoritusPath: props.osasuoritusPath,
              osasuoritusIndex: osasuoritusIndex,
              suoritusIndex: props.osasuoritusIndex
            })
          }
        )}
      />
    </div>
  )
}

interface OsasuoritusToTableRowParams {
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
  >
  suoritusIndex: number
  osasuoritusIndex: number
}

export const osasuoritusToTableRow = ({
  suoritusPath,
  suoritusIndex,
  osasuoritusIndex,
  form
}: OsasuoritusToTableRowParams): OsasuoritusRowData<
  'Osasuoritus' | 'Laajuus' | 'Taitotaso'
> => {
  const osasuoritusPath = suoritusPath
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
          path={osasuoritusPath.path('koulutusmoduuli.tunniste.nimi')}
          view={LocalizedTextView}
          testId="nimi"
        />
      ),
      Laajuus: (
        <FormField
          form={form}
          path={osasuoritusPath.path('koulutusmoduuli.laajuus')}
          view={LaajuusView}
          edit={LaajuusOpintopisteissäEdit}
        />
      ),
      Taitotaso: (
        <FormField
          form={form}
          path={osasuoritusPath.path('arviointi')}
          view={ParasArvosanaView}
          edit={(arvosanaProps) => (
            <ParasArvosanaEdit
              {...arvosanaProps}
              createArviointi={createKielitaitoarviointi}
            />
          )}
        />
      )
    },
    content: <TaitotasoProperty form={form} path={osasuoritusPath} />
  }
}

const createKielitaitoarviointi = (
  arvosana: ArvosanaOf<VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi>
) =>
  VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi({
    arvosana,
    arviointipäivä: todayISODate()
  })
