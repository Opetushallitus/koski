import { ConfigForFrontend } from '../types/fi/oph/koski/config/ConfigForFrontend'

export const config = (): ConfigForFrontend => {
  // @ts-expect-error
  return window['config']
}
