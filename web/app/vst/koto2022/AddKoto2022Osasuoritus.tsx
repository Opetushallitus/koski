import React from 'react'
import { CommonProps } from '../../components-v2/CommonProps'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import { VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022'
import { VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { AddKoodistonOsasuoritus } from '../common/AddKoodistonOsasuoritus'

type AddKoto2022OsasuoritusProps = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022
  >
}>

export const AddKoto2022Osasuoritus: React.FC<AddKoto2022OsasuoritusProps> = (
  props
) => (
  <AddKoodistonOsasuoritus
    form={props.form}
    path={props.osasuoritusPath}
    koodistoUri="vstkoto2022kokonaisuus"
    createOsasuoritus={createOsasuoritus}
    level={0}
  />
)

const createOsasuoritus = (
  koodiviite: Koodistokoodiviite<'vstkoto2022kokonaisuus'>
): VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022 => {
  switch (koodiviite.koodiarvo) {
    case 'kielijaviestintaosaaminen':
      return VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022()
    case 'ohjaus':
      return VSTKotoutumiskoulutuksenOhjauksenSuoritus2022()
    case 'valinnaisetopinnot':
      return VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022()
    case 'yhteiskuntajatyoelamaosaaminen':
      return VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022()
    default:
      throw new Error(
        `Tuntematon osasuorituksen tunniste: ${koodiviite.koodiarvo}`
      )
  }
}
