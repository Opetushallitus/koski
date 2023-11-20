import React from 'react'
import { CommonProps } from '../../../components-v2/CommonProps'
import { FormModel, FormOptic } from '../../../components-v2/forms/FormModel'
import { Koodistokoodiviite } from '../../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenAlaosasuoritus'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022 } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaamisenAlasuorituksenKoulutusmoduuli2022'
import { VapaanSivistystyönOpiskeluoikeus } from '../../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { AddKoodistonOsasuoritus } from '../../common/AddKoodistonOsasuoritus'
import { laajuusOpintopisteissa } from '../../common/constructors'

type AddYhteiskuntaJaTyoelamaosaaminenOsasuoritusProps = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022
  >
}>

export const AddYhteiskuntaJaTyoelamaosaaminenOsasuoritus: React.FC<
  AddYhteiskuntaJaTyoelamaosaaminenOsasuoritusProps
> = (props) => (
  <AddKoodistonOsasuoritus
    form={props.form}
    path={props.osasuoritusPath}
    koodistoUri="vstkoto2022yhteiskuntajatyoosaamiskoulutus"
    createOsasuoritus={createOsasuoritus}
    level={1}
  />
)

const createOsasuoritus = (
  tunniste: Koodistokoodiviite<'vstkoto2022yhteiskuntajatyoosaamiskoulutus'>
): VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =>
  VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus({
    koulutusmoduuli:
      VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022(
        { tunniste, laajuus: laajuusOpintopisteissa(1) }
      )
  })
