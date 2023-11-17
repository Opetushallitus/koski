import React from 'react'
import { CommonProps } from '../../components-v2/CommonProps'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import { VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönJotpaKoulutuksenOsasuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { AddPaikallinenOsasuoritus } from '../common/AddPaikallinenOsasuoritus'
import { laajuusOpintopisteissa } from '../common/constructors'
import { VapaanSivistystyönJotpaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'

type AddJotpaOsasuoritusProps = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönJotpaKoulutuksenSuoritus
  >
}>

export const AddJotpaOsasuoritus: React.FC<AddJotpaOsasuoritusProps> = (
  props
) => (
  <AddPaikallinenOsasuoritus
    form={props.form}
    path={props.osasuoritusPath}
    createOsasuoritus={(tunniste) =>
      VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus({
        koulutusmoduuli: VapaanSivistystyönJotpaKoulutuksenOsasuoritus({
          tunniste,
          laajuus: laajuusOpintopisteissa(1)
        })
      })
    }
    preferenceStoreName="vapaansivistystyonjotpakoulutuksenosasuoritus"
    level={0}
  />
)
