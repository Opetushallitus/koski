export const EPSILON = 0.000001

export const formatNumber = (n: number): string =>
  isFinite(n) ? `${n}`.replace('.', ',') : `–`

export const sum = (as: number[]): number => as.reduce((a, n) => a + n, 0)

export const removeFloatingPointDrift = (n: number): number =>
  parseFloat(n.toFixed(6))

export const clamp =
  (min: number, max: number) =>
  (n: number): number =>
    Math.min(max, Math.max(min, n))
