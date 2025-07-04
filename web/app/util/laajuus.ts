import { t } from '../i18n/i18n'
import { Laajuus } from '../types/fi/oph/koski/schema/Laajuus'
import { LaajuusTunneissa } from '../types/fi/oph/koski/schema/LaajuusTunneissa'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'

export const createLaajuusTunneissa = (arvo: number) =>
  LaajuusTunneissa({ arvo })

export const createLaajuusOpintopisteissä = (arvo: number) =>
  LaajuusOpintopisteissä({ arvo })

export const formatLaajuus = (laajuus?: Laajuus): string =>
  laajuus ? `${laajuus.arvo} ${t(laajuus.yksikkö.nimi)}` : ''
