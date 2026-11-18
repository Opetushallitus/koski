import React from 'react'
import { LukionMuuModuuliMuissaOpinnoissa2019 } from '../types/fi/oph/koski/schema/LukionMuuModuuliMuissaOpinnoissa2019'
import { LukionModuulinSuoritusMuissaOpinnoissa2019 } from '../types/fi/oph/koski/schema/LukionModuulinSuoritusMuissaOpinnoissa2019'
import { LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 } from '../types/fi/oph/koski/schema/LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019'
import { UusiLukionModuuliModaali } from './UusiLukionModuuli'

export const UusiLukionMuuSuoritusModuuli: React.FC<{
  olemassaOlevatKoodiarvot: string[]
  onAdd: (
    kurssi: LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019
  ) => void
  onClose: () => void
}> = ({ olemassaOlevatKoodiarvot, onAdd, onClose }) => (
  <UusiLukionModuuliModaali
    olemassaOlevatKoodiarvot={olemassaOlevatKoodiarvot}
    buildValtakunnallinenSuoritus={(tunniste, laajuus) =>
      LukionModuulinSuoritusMuissaOpinnoissa2019({
        koulutusmoduuli: LukionMuuModuuliMuissaOpinnoissa2019({
          tunniste,
          laajuus,
          pakollinen: false
        })
      })
    }
    onAdd={onAdd}
    onClose={onClose}
  />
)
