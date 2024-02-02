import React from 'react'
import { VapaanSivistystyönOsaamismerkinSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinSuoritus'
import {
  mapError,
  mapInitial,
  mapLoading,
  mapSuccess,
  useApiWithParams
} from '../../api-fetch'
import { fetchOsaamismerkkikuva } from '../../util/koskiApi'
import { Trans } from '../../components-v2/texts/Trans'
import { CommonProps } from '../../components-v2/CommonProps'
import { ActivePäätasonSuoritus } from '../../components-v2/containers/EditorContainer'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { Osaamismerkkikuva } from '../../types/fi/oph/koski/servlet/Osaamismerkkikuva'
import { t, tExists } from '../../i18n/i18n'

export type OsaamismerkkiKuva = CommonProps<{
  päätasonSuoritus: ActivePäätasonSuoritus<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönOsaamismerkinSuoritus
  >
}>

export const OsaamismerkkiKuva: React.FC<OsaamismerkkiKuva> = ({
  päätasonSuoritus
}) => {
  const kuvaResponse = useApiWithParams(fetchOsaamismerkkikuva, [
    päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.koodiarvo
  ])

  const altText = t('Osaamismerkki')

  return (
    <>
      {mapInitial(kuvaResponse, () => (
        <Trans>{'Haetaan kuvaa'}</Trans>
      ))}
      {mapLoading(kuvaResponse, () => (
        <Trans>{'Haetaan kuvaa'}</Trans>
      ))}
      {mapError(kuvaResponse, () => (
        <Trans>{'Kuvan hakeminen epäonnistui'}</Trans>
      ))}
      {mapSuccess(kuvaResponse, (responseData: Osaamismerkkikuva) => (
        <img
          className="osaamismerkki-img"
          src={`data:${responseData.mimetype};base64,${responseData.base64data}`}
          alt={altText}
        />
      ))}
    </>
  )
}
