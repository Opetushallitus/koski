import React from 'react'
import { CommonProps } from '../../components-v2/CommonProps'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import { Finnish } from '../../types/fi/oph/koski/schema/Finnish'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuoritus'
import { AddPaikallinenOsasuoritus } from '../common/AddPaikallinenOsasuoritus'
import { laajuusOpintopisteissa } from '../common/constructors'

type AddVapaatavoitteinenAlaosasuoritusProps = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
  >
  level: number
}>

export const AddVapaatavoitteinenAlaosasuoritus: React.FC<
  AddVapaatavoitteinenAlaosasuoritusProps
> = (props) => (
  <AddPaikallinenOsasuoritus
    form={props.form}
    suoritusPath={props.osasuoritusPath}
    createOsasuoritus={(tunniste) =>
      VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus({
        koulutusmoduuli:
          VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus({
            tunniste,
            kuvaus: Finnish({ fi: tunniste.koodiarvo }),
            laajuus: laajuusOpintopisteissa(1)
          })
      })
    }
    preferenceStoreName="vapaansivistystyonvapaatavoitteisenkoulutuksenosasuoritus"
    level={props.level}
    testId={props.testId}
  />
)
