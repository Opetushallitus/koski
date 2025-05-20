export const EPSILON = 0.000001

const formatter = new Intl.NumberFormat('fi-FI', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2
})

export const formatNumber = (n: number): string =>
  isFinite(n) ? `${formatter.format(n)}` : `–`

export const sum = (as: number[]): number => as.reduce((a, n) => a + n, 0)

export const removeFloatingPointDrift = (n: number): number =>
  parseFloat(n.toFixed(6))

export const clamp =
  (min: number, max: number) =>
  (n: number): number =>
    Math.min(max, Math.max(min, n))

export const indexSequence = (
  from: number = 0,
  step: number = 1
): (() => number) => {
  let n = from - step
  return () => (n += step)
}
