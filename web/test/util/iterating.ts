export const foreachAsync =
  <T>(ts: T[]) =>
  async (f: (t: T, index: number) => Promise<void>) => {
    let i = 0
    for (const t of ts) {
      await f(t, i)
      i++
    }
  }

export const repeatAsync =
  (count: number) => async (f: (index: number) => Promise<void>) => {
    for (let i = 0; i < count; i++) {
      await f(i)
    }
  }
