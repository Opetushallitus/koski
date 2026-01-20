#!/usr/bin/env node
/**
 * Simple mTLS proxy that emulates AWS ALB behavior for local development.
 *
 * Listens on HTTPS (default 7022), verifies client certificates, and forwards
 * requests to Koski backend with ALB-style headers:
 * - x-amzn-mtls-clientcert-subject
 * - x-amzn-mtls-clientcert-issuer
 * - x-amzn-mtls-clientcert-serial-number
 */

import https from 'node:https'
import http from 'node:http'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

const PROXY_PORT = process.env.PROXY_PORT || 7022
const KOSKI_BACKEND = process.env.KOSKI_BACKEND || 'http://localhost:7021'

// Paths to test certificates (lives under server/testca)
const CERT_BASE = path.resolve(__dirname, '../server/testca')

const HEALTHCHECK_PORT = process.env.HEALTHCHECK_PORT || 7023

const serverOptions = {
  key: fs.readFileSync(path.join(CERT_BASE, 'private/proxy.key')),
  cert: fs.readFileSync(path.join(CERT_BASE, 'certs/proxy.crt')),
  ca: fs.readFileSync(path.join(CERT_BASE, 'certs/root-ca.crt')),
  requestCert: true,
  rejectUnauthorized: true
}

const server = https.createServer(serverOptions, (req, res) => {
  const clientCert = req.socket.getPeerCertificate()
  const koskiUrl = new URL(KOSKI_BACKEND)

  // Build headers to forward
  const headers = { ...req.headers }

  // Add ALB-style mTLS headers if client cert is present
  if (clientCert && clientCert.subject) {
    const subjectDn = formatSubjectDn(clientCert.subject)
    const issuerDn = formatSubjectDn(clientCert.issuer)
    const serial = clientCert.serialNumber

    headers['x-amzn-mtls-clientcert-subject'] = subjectDn
    headers['x-amzn-mtls-clientcert-issuer'] = issuerDn
    headers['x-amzn-mtls-clientcert-serial-number'] = serial

    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url} - Client: ${subjectDn}`)
  } else {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url} - No client cert`)
  }

  headers['x-forwarded-proto'] = 'https'
  headers['x-forwarded-for'] = '0.0.0.0' // Match mock client list for local development

  // Forward request to Koski
  const proxyReq = http.request({
    hostname: koskiUrl.hostname,
    port: koskiUrl.port || 80,
    path: req.url,
    method: req.method,
    headers: headers
  }, (proxyRes) => {
    res.writeHead(proxyRes.statusCode, proxyRes.headers)
    proxyRes.pipe(res)
  })

  proxyReq.on('error', (err) => {
    console.error(`Proxy error: ${err.message}`)
    res.writeHead(502)
    res.end('Bad Gateway')
  })

  req.pipe(proxyReq)
})

/**
 * Format certificate subject as DN string (like nginx $ssl_client_s_dn)
 */
function formatSubjectDn(subject) {
  const parts = []
  if (subject.CN) parts.push(`CN=${subject.CN}`)
  if (subject.O) parts.push(`O=${subject.O}`)
  if (subject.C) parts.push(`C=${subject.C}`)
  return parts.join(',')
}

server.listen(PROXY_PORT, () => {
  console.log(`ALB proxy listening on https://localhost:${PROXY_PORT}`)
  console.log(`Forwarding to ${KOSKI_BACKEND}`)
  console.log(`Using certs from ${CERT_BASE}`)
})

// Separate HTTP server for healthcheck (no client cert required)
const healthcheckServer = http.createServer((req, res) => {
  if (req.url === '/healthcheck') {
    res.writeHead(200, { 'Content-Type': 'text/plain' })
    res.end('ok')
  } else {
    res.writeHead(404)
    res.end('Not Found')
  }
})

healthcheckServer.listen(HEALTHCHECK_PORT, () => {
  console.log(`Healthcheck listening on http://localhost:${HEALTHCHECK_PORT}/healthcheck`)
})
