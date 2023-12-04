import React from 'react'
import { LocalizedTextView } from '../../../components-v2/controls/LocalizedTestField'
import { FormField } from '../../../components-v2/forms/FormField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../../../components-v2/forms/FormModel'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../../../components-v2/opiskeluoikeus/LaajuusField'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import { VapaanSivistystyönOpiskeluoikeus } from '../../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { deleteAt } from '../../../util/array'
import { ArviointiProperty } from '../../common/propertyFields'
import { AddYhteiskuntaJaTyoelamaosaaminenOsasuoritus } from './AddYhteiskuntaJaTyoelamaosaaminenOsasuoritus'
import { VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'

type VSTKoto2022YhteiskuntaJaTyoelamaosaaminenPropertiesProps = {
  osasuoritusIndex: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022
  >
}

export const VSTKoto2022YhteiskuntaJaTyoelamaosaaminenProperties: React.FC<
  VSTKoto2022YhteiskuntaJaTyoelamaosaaminenPropertiesProps
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
        addNewOsasuoritusView={AddYhteiskuntaJaTyoelamaosaaminenOsasuoritus}
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
    VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022
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
    expandable: false,
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
      )
    }
  }
}
