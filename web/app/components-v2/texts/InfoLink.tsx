import React, { useMemo } from 'react'
import { CommonProps } from '../CommonProps'
import { t } from '../../i18n/i18n'
import { Infobox } from '../../components/Infobox'
import { useSchema } from '../../appstate/constraints'
import { isObjectConstraint } from '../../types/fi/oph/koski/typemodel/ObjectConstraint'
import { isOptionalConstraint } from '../../types/fi/oph/koski/typemodel/OptionalConstraint'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'

export type InfoLinkProps = CommonProps<{
  koulutusmoduuliClass: KoulutusmoduuliClassName
}>

export const InfoLink: React.FC<InfoLinkProps> = (props) => {
  const { infoDescription, infoLinkTitle, infoLinkUrl } = useInfoLink(
    props.koulutusmoduuliClass
  )
  return infoLinkUrl ? (
    <Infobox>
      <>
        {t(`infoDescription:${infoDescription}`)}
        <br />
        <a
          href={t(`infoLinkUrl:${infoLinkUrl}`)}
          target="_blank"
          rel="noopener noreferrer"
        >
          {t(`infoLinkTitle:${infoLinkTitle}`)}
        </a>
      </>
    </Infobox>
  ) : null
}

type KoulutusmoduuliClassName =
  Opiskeluoikeus['suoritukset'][number]['koulutusmoduuli']['$class']

function useInfoLink(className: KoulutusmoduuliClassName) {
  const schema = useSchema(className)
  const { infoLinkTitle, infoLinkUrl, infoDescription } = useMemo(() => {
    if (schema === null) {
      return {}
    }
    if (
      isObjectConstraint(schema) &&
      'opintokokonaisuus' in schema.properties &&
      isOptionalConstraint(schema.properties.opintokokonaisuus)
    ) {
      const {
        infoLinkTitle: infoTitle,
        infoLinkUrl: infoUrl,
        infoDescription: infoDesc
      } = schema.properties.opintokokonaisuus
      return {
        infoLinkTitle: infoTitle,
        infoLinkUrl: infoUrl,
        infoDescription: infoDesc
      }
    }
    return {}
  }, [schema])

  return { infoLinkTitle, infoLinkUrl, infoDescription }
}
