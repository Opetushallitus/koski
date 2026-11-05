import React from 'react'
import { useVirkailijaUser } from '../appstate/user'

// Vanha käyttöliittymä ei näytä koulutuksen koodiarvoa kansalaiselle.
export const KoulutuksenKoodiarvo: React.FC<{ koodiarvo: string }> = ({
  koodiarvo
}) => (useVirkailijaUser() ? <span>{koodiarvo}</span> : null)
