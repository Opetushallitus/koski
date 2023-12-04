import React from 'react'
import { LocalizedTextView } from '../../../components-v2/controls/LocalizedTestField'
import { FormField } from '../../../components-v2/forms/FormField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../../../components-v2/forms/FormModel'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../../../components-v2/opiskeluoikeus/OsasuoritusTable'
import {
  PaikallisenKoulutusmoduulinLaajuusEdit,
  PaikallisenKoulutusmoduulinLaajuusView
} from '../../../components-v2/opiskeluoikeus/PaikallisenKoulutusmoduulinLaajuusField'
import { LaajuusOpintopisteissä } from '../../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import { VapaanSivistystyönOpiskeluoikeus } from '../../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { deleteAt } from '../../../util/array'
import { ArviointiProperty, KuvausProperty } from '../../common/propertyFields'
import { AddValinnaisetOpinnotOsasuoritus } from './AddValinnaisetOpinnotOsasuoritus'

type VSTKoto2022ValinnaisetPropertiesProps = {
  osasuoritusIndex: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
  >
}

export const VSTKoto2022ValinnaisetProperties: React.FC<
  VSTKoto2022ValinnaisetPropertiesProps
> = (props) => {
  const osasuoritus = getValue(props.osasuoritusPath)(props.form.state)

  return (
    <div>
      <ArviointiProperty
        form={props.form}
        path={props.osasuoritusPath}
        arviointi={VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022}
      />
      <OsasuoritusTable
        editMode={props.form.editMode}
        addNewOsasuoritusView={AddValinnaisetOpinnotOsasuoritus}
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
    VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
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
  'Osasuoritus' | 'Laajuus' | 'Arvosana'
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
          path={osasuoritusPath.prop('koulutusmoduuli')}
          view={PaikallisenKoulutusmoduulinLaajuusView}
          edit={PaikallisenKoulutusmoduulinLaajuusEdit}
          editProps={{
            koulutusmoduuli:
              VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022,
            template: LaajuusOpintopisteissä
          }}
        />
      )
    },
    content: <KuvausProperty form={form} path={osasuoritusPath} />
  }
}
