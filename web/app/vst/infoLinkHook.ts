import { useMemo } from 'react'
import { useSchema } from '../appstate/constraints'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { isObjectConstraint } from '../types/fi/oph/koski/typemodel/ObjectConstraint'
import { isOptionalConstraint } from '../types/fi/oph/koski/typemodel/OptionalConstraint'

type KoulutusmoduuliClassName =
  Opiskeluoikeus['suoritukset'][number]['koulutusmoduuli']['$class']

export function useInfoLink(className: KoulutusmoduuliClassName) {
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
