import React from 'react'
import { DateEdit, DateView } from '../components-v2/controls/DateField'
import { FormField } from '../components-v2/forms/FormField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../components-v2/forms/FormModel'
import {
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue,
  OsasuoritusSubproperty
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import {
  TunnustusEdit,
  TunnustusView
} from '../components-v2/opiskeluoikeus/TunnustusField'
import { TaiteenPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeus'
import { TaiteenPerusopetuksenOsasuorituksenTunnustus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOsasuorituksenTunnustus'
import { TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
import { lastElement } from '../util/optics'
import { createTpoArviointi } from './tpoCommon'

export type TpoOsasuoritusPropertiesProps = {
  form: FormModel<TaiteenPerusopetuksenOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    TaiteenPerusopetuksenOpiskeluoikeus,
    TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus
  >
  testId: string
}

export const TpoOsasuoritusProperties: React.FC<
  TpoOsasuoritusPropertiesProps
> = (props) => {
  const viimeisinArviointiPath = props.osasuoritusPath
    .prop('arviointi')
    .optional()
    .compose(lastElement())

  const tunnustusPath = props.osasuoritusPath.prop('tunnustettu')

  const osasuoritus = getValue(props.osasuoritusPath)(props.form.state)
  const arvioitu = Boolean(osasuoritus?.arviointi)
  const tunnustettu = Boolean(osasuoritus?.tunnustettu)

  return (
    <div>
      {arvioitu && (
        <OsasuoritusProperty label="Arviointi">
          <OsasuoritusSubproperty label="Arvosana">
            <FormField
              form={props.form}
              path={props.osasuoritusPath.prop('arviointi')}
              view={ParasArvosanaView}
              edit={ParasArvosanaEdit}
              editProps={{
                // @ts-expect-error TypeScript ei tajua, että tämä on oikeasti ok. createTpoArviointi -funktion parametrin tyyppi on väärä.
                createArviointi: createTpoArviointi
              }}
              testId={`${props.testId}.arvosana`}
            />
          </OsasuoritusSubproperty>
          <OsasuoritusSubproperty rowNumber={1} label="Päivämäärä" key="pvm">
            <FormField
              form={props.form}
              path={viimeisinArviointiPath.prop('päivä')}
              view={DateView}
              edit={DateEdit}
              testId={`${props.testId}.arvostelunPvm`}
            />
          </OsasuoritusSubproperty>
        </OsasuoritusProperty>
      )}
      {(tunnustettu || props.form.editMode) && (
        <OsasuoritusProperty label="Tunnustettu">
          <OsasuoritusPropertyValue>
            <FormField
              form={props.form}
              path={tunnustusPath}
              view={TunnustusView}
              edit={TunnustusEdit}
              testId={`${props.testId}.tunnustettu`}
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      )}
    </div>
  )
}
