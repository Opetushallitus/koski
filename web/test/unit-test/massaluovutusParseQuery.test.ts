import { describe, expect, it } from 'vitest'

import { parseQuery } from '../../app/dokumentaatio/parseQuery'

describe('parseQuery', () => {
  it('hyväksyy sisennyksen sitovilla ja muilla Unicode-välilyönneillä', () => {
    const text =
      '{\n\u00a0\u00a0"type": "x",\n\u202f\u2003"format":\u3000"application/json"\n}'
    expect(parseQuery(text)).toEqual({
      ok: true,
      value: { type: 'x', format: 'application/json' }
    })
  })

  it('ohittaa BOM:n ja nollaleveät välilyönnit', () => {
    expect(parseQuery('\ufeff{"type":\u200b"x"}')).toEqual({
      ok: true,
      value: { type: 'x' }
    })
  })

  it('kertoo virheessä virheellisen merkin koodin', () => {
    const result = parseQuery('{\n\u2028"type": "x"}')
    expect(result.ok).toBe(false)
    expect(!result.ok && result.error).toContain('U+2028 kohdassa 2')
  })
})
