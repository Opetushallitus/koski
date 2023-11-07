import React from 'react'
import { CommonProps } from '../../components-v2/CommonProps'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import { VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönJotpaKoulutuksenOsasuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { AddPaikallinenOsasuoritus } from '../common/AddPaikallinenOsasuoritus'
import { laajuusOpintopisteissa } from '../common/constructors'

type AddJotpaOsasuoritusProps = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus
  >
  level: number
}>

export const AddJotpaAlaosasuoritus: React.FC<AddJotpaOsasuoritusProps> = (
  props
) => (
  <AddPaikallinenOsasuoritus
    form={props.form}
    suoritusPath={props.osasuoritusPath}
    createOsasuoritus={(tunniste) =>
      VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus({
        koulutusmoduuli: VapaanSivistystyönJotpaKoulutuksenOsasuoritus({
          tunniste,
          laajuus: laajuusOpintopisteissa(1)
        })
      })
    }
    preferenceStoreName="vapaansivistystyonjotpakoulutuksenosasuoritus"
    level={props.level}
    testId={props.testId}
  />
)
