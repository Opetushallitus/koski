import { arrayOf, BuiltIdNode } from './builder'
import { Button } from './Button'
import { createControl, FormField } from './controls'
import { Input } from './Input'
import { Label } from './Label'
import { Select } from './Select'

export const Tunnustettu = createControl((self, child) => ({
  value: () => child('selite.input').inputValue(),
  set: async (value: string) => {
    const btn = child('add')
    await btn.click()
    const input = child('selite.input')
    await input.fill(value)
  }
}))
