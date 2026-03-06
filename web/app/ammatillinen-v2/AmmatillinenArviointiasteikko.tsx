import React from 'react'
import { t } from '../i18n/i18n'
import { AmmatillinenArviointi } from '../types/fi/oph/koski/schema/AmmatillinenArviointi'

type SuoritusWithArviointi = {
  arviointi?: Array<AmmatillinenArviointi>
  osasuoritukset?: Array<SuoritusWithArviointi>
}

type AmmatillinenArviointiasteikkoProps = {
  suoritus: SuoritusWithArviointi
}

export const AmmatillinenArviointiasteikko: React.FC<
  AmmatillinenArviointiasteikkoProps
> = ({ suoritus }) => {
  const asteikko = käytössäOlevaAsteikko(suoritus)
  if (!asteikko) return null

  return (
    <div className="ammatillinenarviointiasteikko">
      <h5>
        {t('Tutkinnon osien arviointiasteikko')}
        {':'}
      </h5>
      {t(asteikko)}
    </div>
  )
}

const käytössäOlevaAsteikko = (
  suoritus: SuoritusWithArviointi
): string | undefined => {
  const asteikot = rekursiivisetAsteikot(suoritus)

  if (asteikot.includes('arviointiasteikkoammatillinen15')) {
    return '1-5, Hylätty tai Hyväksytty'
  }
  if (asteikot.includes('arviointiasteikkoammatillinent1k3')) {
    return '1-3, Hylätty tai Hyväksytty'
  }
  if (asteikot.includes('arviointiasteikkoammatillinenhyvaksyttyhylatty')) {
    return 'Hylätty tai Hyväksytty'
  }
  return undefined
}

const rekursiivisetAsteikot = (suoritus: SuoritusWithArviointi): string[] => {
  const osasuoritukset = suoritus.osasuoritukset ?? []
  return osasuoritukset.flatMap((os) => {
    const viimeisinArviointi = os.arviointi?.at(-1)
    const asteikko = viimeisinArviointi?.arvosana.koodistoUri
    return [
      ...(asteikko ? [asteikko] : []),
      ...rekursiivisetAsteikot(os)
    ]
  })
}
