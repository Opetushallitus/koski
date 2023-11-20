import React from 'react'
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
import { ArviointiProperty } from '../common/propertyFields'
import {
  VSTSuoritus,
  VSTSuoritusPaikallisillaOsasuorituksilla
} from '../common/types'
import { AddVapaatavoitteinenAlaosasuoritus } from './AddVapaatavoitteinenAlaosasuoritus'

type VSTVapaatavoitteinenPropertiesProps = {
  osasuoritusIndex: number
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTSuoritusPaikallisillaOsasuorituksilla
  >
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
  >
}

export const VSTVapaatavoitteinenProperties: React.FC<
  VSTVapaatavoitteinenPropertiesProps
> = (props) => {
  const osasuoritus = getValue(props.osasuoritusPath)(props.form.state)

  return (
    <div>
      <ArviointiProperty form={props.form} path={props.osasuoritusPath} />
      <OsasuoritusProperty label="">
        <OsasuoritusSubproperty label="Kuvaus">
          <FormField
            form={props.form}
            path={props.osasuoritusPath.prop('koulutusmoduuli').prop('kuvaus')}
            view={KuvausView}
            edit={(kuvausProps) => {
              return <KuvausEdit {...kuvausProps} />
            }}
          />
        </OsasuoritusSubproperty>
      </OsasuoritusProperty>
      <OsasuoritusTable
        editMode={props.form.editMode}
        addNewOsasuoritusView={AddVapaatavoitteinenAlaosasuoritus}
        addNewOsasuoritusViewProps={{
          form: props.form,
          osasuoritusPath: props.osasuoritusPath,
          level: props.level + 1
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
              // @ts-expect-error
              suoritusPath: props.osasuoritusPath,
              osasuoritusIndex: osasuoritusIndex,
              suoritusIndex: props.osasuoritusIndex,
              testId: `osasuoritukset.${osasuoritusIndex}`
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
    VSTSuoritusPaikallisillaOsasuorituksilla
  >
  suoritusIndex: number
  osasuoritusIndex: number
}

export const osasuoritusToTableRow = ({
  suoritusPath,
  suoritusIndex,
  osasuoritusIndex,
  form,
  level
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
          testId="nimi"
        />
      ),
      Laajuus: (
        <FormField
          form={form}
          path={osasuoritus.path('koulutusmoduuli.laajuus')}
          view={LaajuusView}
          edit={LaajuusOpintopisteissäEdit}
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
        />
      )
    },
    content: (
      <VSTVapaatavoitteinenProperties
        level={level}
        osasuoritusIndex={osasuoritusIndex}
        form={form}
        suoritusPath={suoritusPath}
        // @ts-expect-error Korjaa tyypitys
        osasuoritusPath={osasuoritus}
      />
    )
  }
}
