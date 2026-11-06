import React from 'react'
import { useVirkailijaUser } from '../../appstate/user'

// Vanha käyttöliittymä ei näytä koulutuksen koodiarvoa kansalaiselle eikä
// suoritusjaossa (TunnisteenKoodiarvoEditor).
export const useKoulutuksenKoodiarvoNäkyy = (): boolean =>
  useVirkailijaUser() !== null

export const KoulutuksenKoodiarvo: React.FC<{ koodiarvo: string }> = ({
  koodiarvo
}) => (useKoulutuksenKoodiarvoNäkyy() ? <span>{koodiarvo}</span> : null)
