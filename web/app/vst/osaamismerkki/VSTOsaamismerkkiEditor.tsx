import React from 'react'
import { useTree } from '../../appstate/tree'
import { VSTPäätasonSuoritusEditorProps } from '../common/types'
import { VapaanSivistystyönOsaamismerkinSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinSuoritus'
import {
  mapError,
  mapInitial,
  mapLoading,
  mapSuccess,
  useApiWithParams
} from '../../api-fetch'
import { Osaamismerkkikuva } from '../../types/fi/oph/koski/servlet/Osaamismerkkikuva'
import { fetchOsaamismerkkikuva } from '../../util/koskiApi'
import { Trans } from '../../components-v2/texts/Trans'

export type VSTOsaamismerkkiEditor =
  VSTPäätasonSuoritusEditorProps<VapaanSivistystyönOsaamismerkinSuoritus>

export const VSTOsaamismerkkiEditor: React.FC<VSTOsaamismerkkiEditor> = ({
  form,
  oppijaOid,
  päätasonSuoritus,
  invalidatable,
  onChangeSuoritus,
  organisaatio,
  suoritusVahvistettu
}) => {
  const { TreeNode, ...tree } = useTree()

  const kuvaResponse = useApiWithParams(fetchOsaamismerkkikuva, [
    päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.koodiarvo
  ])

  return (
    <TreeNode>
      {mapInitial(kuvaResponse, () => (
        <Trans>{'Haetaan'}</Trans>
      ))}
      {mapLoading(kuvaResponse, () => (
        <Trans>{'Haetaan'}</Trans>
      ))}
      {mapError(kuvaResponse, () => (
        <Trans>{'Tietojen hakeminen epäonnistui'}</Trans>
      ))}
      {mapSuccess(kuvaResponse, (responseData: Osaamismerkkikuva) => (
        <>
          <img
            src={`data:${responseData.mimetype};base64,${responseData.base64data}`}
          />
        </>
      ))}
    </TreeNode>
  )
}
