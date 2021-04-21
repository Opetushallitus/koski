export const sleep = (time: number) =>
  new Promise((resolve) => setTimeout(resolve, time))

export const eventually = async (
  test: () => Promise<void>,
  timeout = 10000
) => {
  const expirationTime = new Date().getTime() + timeout
  let error = null
  while (true) {
    if (new Date().getTime() > expirationTime) {
      throw error
    }
    try {
      await test()
      return
    } catch (err) {
      error = err
      await sleep(timeout / 10)
    }
  }
}
