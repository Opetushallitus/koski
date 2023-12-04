import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'

export const laajuusOpintopisteissa = (arvo: number) =>
  LaajuusOpintopisteissä({
    arvo,
    yksikkö: Koodistokoodiviite({
      koodiarvo: '2',
      koodistoUri: 'opintojenlaajuusyksikko'
    })
  })
