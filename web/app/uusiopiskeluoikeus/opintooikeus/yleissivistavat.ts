const contains =
  (...as: string[]) =>
  (a?: string): boolean =>
    a !== undefined && as.includes(a)

export const isUskonnonOppiaine = contains('KT')
export const isVieraanKielenOppiaine = contains(
  'A1',
  'A2',
  'B1',
  'B2',
  'B3',
  'AOM'
)
export const isÄidinkielenOppiaine = contains('AI')
export const isEiTiedossaOlevaOppiaine = contains('XX')
export const isMuuPerusopetuksenOppiaine = (koodiarvo: string) =>
  !isUskonnonOppiaine(koodiarvo) &&
  !isVieraanKielenOppiaine(koodiarvo) &&
  !isÄidinkielenOppiaine(koodiarvo) &&
  !isEiTiedossaOlevaOppiaine(koodiarvo)

export const isMatematiikanOppiaine = contains('MA')

export const isMuuLukionOppiaine = (koodiarvo: string) =>
  !isUskonnonOppiaine(koodiarvo) &&
  !isVieraanKielenOppiaine(koodiarvo) &&
  !isÄidinkielenOppiaine(koodiarvo) &&
  !isEiTiedossaOlevaOppiaine(koodiarvo) &&
  !isMatematiikanOppiaine(koodiarvo)
