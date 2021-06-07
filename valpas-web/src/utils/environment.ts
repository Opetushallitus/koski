const runningOnJest = process.env.JEST_WORKER_ID !== undefined

export const runningLocally: boolean =
  runningOnJest || ["local", "unittest"].includes(window.environment || "")
