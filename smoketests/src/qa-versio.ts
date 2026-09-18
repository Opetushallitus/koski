import { randomUUID } from "node:crypto";
import { performance } from "node:perf_hooks";
import { setTimeout as sleep } from "node:timers/promises";

const fullHash = /^[0-9a-fA-F]{40}$/;

export async function verifyQaVersion(
  expected: string,
  {
    url = "https://koski.testiopintopolku.fi/koski/api/status",
    timeoutMs = 5 * 60 * 1000,
    intervalMs = 10 * 1000,
    requestTimeoutMs = 10 * 1000,
    log = console.log,
  } = {},
): Promise<void> {
  if (!fullHash.test(expected)) {
    throw new Error(`Invalid expected Git commit hash: ${expected}`);
  }
  const deadline = performance.now() + timeoutMs;
  log(`Waiting for QA Git commit ${expected}`);
  while (performance.now() < deadline) {
    const requestUrl = new URL(url);
    requestUrl.searchParams.set("deploymentCheck", randomUUID());
    const controller = new AbortController();
    const timer = setTimeout(
      () => controller.abort(),
      Math.max(1, Math.min(requestTimeoutMs, deadline - performance.now())),
    );
    try {
      const response = await fetch(requestUrl, {
        headers: { "Cache-Control": "no-cache, no-store", Pragma: "no-cache" },
        signal: controller.signal,
      });
      if (!response.ok) {
        await response.body?.cancel();
        throw new Error(`HTTP ${response.status}`);
      }
      const status: unknown = await response.json();
      const observed =
        typeof status === "object" &&
        status !== null &&
        "gitCommitHash" in status
          ? status.gitCommitHash
          : undefined;
      log(`Expected ${expected}, observed ${JSON.stringify(observed)}`);
      if (
        typeof observed === "string" &&
        fullHash.test(observed) &&
        observed === expected &&
        performance.now() < deadline
      ) {
        return;
      }
    } catch (error) {
      log(`Expected ${expected}, status request failed: ${String(error)}`);
    } finally {
      clearTimeout(timer);
    }
    const remaining = deadline - performance.now();
    if (remaining > 0) await sleep(Math.min(intervalMs, remaining));
  }
  throw new Error(`Timed out waiting for QA Git commit ${expected}`);
}

if (require.main === module) {
  verifyQaVersion(process.env.EXPECTED_COMMIT_HASH ?? "").catch((error) => {
    console.error(error);
    process.exitCode = 1;
  });
}
