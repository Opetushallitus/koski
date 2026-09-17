import assert from "node:assert/strict";
import { createServer, RequestListener } from "node:http";
import { AddressInfo } from "node:net";
import { test } from "node:test";
import { verifyQaDeployment } from "./qa-deployment";

const expected = "a".repeat(40);

async function fixture(
  handler: RequestListener,
  run: (url: string) => Promise<void>,
) {
  const server = createServer(handler);
  await new Promise<void>((resolve) => server.listen(0, "127.0.0.1", resolve));
  try {
    await run(
      `http://127.0.0.1:${(server.address() as AddressInfo).port}/status`,
    );
  } finally {
    server.closeAllConnections();
    await new Promise<void>((resolve) => server.close(() => resolve()));
  }
}

const options = {
  timeoutMs: 2000,
  intervalMs: 5,
  requestTimeoutMs: 100,
  log: (_message: string) => {},
};

test("immediate match uses cache bypass headers and query parameter", async () => {
  await fixture(
    (req, res) => {
      assert.equal(req.headers["cache-control"], "no-cache, no-store");
      assert.equal(req.headers.pragma, "no-cache");
      assert.ok(
        new URL(req.url!, "http://localhost").searchParams.get(
          "deploymentCheck",
        ),
      );
      res.end(JSON.stringify({ gitCommitHash: expected }));
    },
    (url) => verifyQaDeployment(expected, { ...options, url }),
  );
});

test("retries old, malformed, invalid, mismatching, HTTP and network responses", async () => {
  const bodies = [
    "{}",
    "not JSON",
    "null",
    '{"gitCommitHash":42}',
    '{"gitCommitHash":"unknown"}',
    '{"gitCommitHash":"aaaaaaa"}',
    JSON.stringify({ gitCommitHash: "b".repeat(40) }),
    JSON.stringify({ gitCommitHash: expected.toUpperCase() }),
  ];
  const urls = new Set<string>();
  let attempts = 0;
  await fixture(
    (req, res) => {
      assert.ok(!urls.has(req.url!));
      urls.add(req.url!);
      attempts++;
      if (attempts === 1) req.socket.destroy();
      else if (attempts === 2) {
        res.statusCode = 503;
        res.end();
      } else
        res.end(bodies.shift() ?? JSON.stringify({ gitCommitHash: expected }));
    },
    (url) => verifyQaDeployment(expected, { ...options, url }),
  );
  assert.equal(attempts, 11);
});

test("retries a stalled response body", async () => {
  let attempts = 0;
  await fixture(
    (_req, res) => {
      if (++attempts === 1) {
        res.writeHead(200);
        res.write("{");
      } else res.end(JSON.stringify({ gitCommitHash: expected }));
    },
    (url) => verifyQaDeployment(expected, { ...options, url }),
  );
  assert.equal(attempts, 2);
});

for (const stalled of [false, true]) {
  test(`deadline fails for ${stalled ? "stalled requests" : "mismatches"}`, async () => {
    const start = performance.now();
    await fixture(
      (_req, res) => {
        if (!stalled) res.end("{}");
      },
      (url) =>
        assert.rejects(
          verifyQaDeployment(expected, {
            ...options,
            url,
            timeoutMs: 150,
            requestTimeoutMs: 1000,
          }),
          /Timed out/,
        ),
    );
    assert.ok(performance.now() - start < 1000);
  });
}

test("rejects invalid expected hashes", async () => {
  await assert.rejects(
    verifyQaDeployment("short", options),
    /Invalid expected/,
  );
});
