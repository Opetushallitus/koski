import { createControl } from './controls'

export const LaajuusInput = createControl((self, child) => ({
  input: child('input'),
  unit: child('unit')
}))
