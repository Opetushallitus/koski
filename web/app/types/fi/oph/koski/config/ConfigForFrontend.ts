import { Rajapäivät } from './Rajapaivat'

/**
 * ConfigForFrontend
 *
 * @see `fi.oph.koski.config.ConfigForFrontend`
 */
export type ConfigForFrontend = {
  $class: 'fi.oph.koski.config.ConfigForFrontend'
  rajapäivät: Rajapäivät
}

export const ConfigForFrontend = (o: {
  rajapäivät: Rajapäivät
}): ConfigForFrontend => ({
  $class: 'fi.oph.koski.config.ConfigForFrontend',
  ...o
})

ConfigForFrontend.className = 'fi.oph.koski.config.ConfigForFrontend' as const

export const isConfigForFrontend = (a: any): a is ConfigForFrontend =>
  a?.$class === 'fi.oph.koski.config.ConfigForFrontend'
