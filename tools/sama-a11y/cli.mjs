#!/usr/bin/env node
import { chromium } from 'playwright'
import axe from 'axe-core'
import { mkdir, writeFile } from 'node:fs/promises'
import { resolve } from 'node:path'
import { pathToFileURL } from 'node:url'

// --prepare is an optional, trusted local JS module: setup(page), states[{name, enter}].
const args = process.argv.slice(2)
const value = (flag, fallback) => args.includes(flag) ? args[args.indexOf(flag) + 1] : fallback
const url = args[0]
const output = resolve(value('--output', 'reports/sama-a11y'))
const prepared = value('--prepare', null)
const result = { url, generatedAt: new Date().toISOString(), preparedScenario: Boolean(prepared), standard: 'WCAG 2.1 AA (automated subset)', states: [], errors: [] }
let browser
try {
  if (!url || !/^https?:\/\//.test(url)) throw new Error('Usage: node cli.mjs URL [--output directory] [--prepare trusted-local-module.mjs]')
  const scenario = prepared ? await import(pathToFileURL(resolve(prepared)).href) : {}
  browser = await chromium.launch()
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 }, reducedMotion: 'reduce' })
  await scenario.setup?.(page)
  await page.goto(url, { waitUntil: 'networkidle' })
  const states = scenario.states || [{ name: 'page', enter: async () => {} }]
  for (const state of states) {
    await state.enter(page)
    await page.addScriptTag({ content: axe.source })
    const audit = await page.evaluate(async () => window.axe.run(document, { runOnly: { type: 'tag', values: ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'] } }))
    result.states.push({ name: state.name, url: page.url(), violations: audit.violations, incomplete: audit.incomplete, passes: audit.passes.length })
    await mkdir(output, { recursive: true })
    await page.screenshot({ path: resolve(output, `${state.name.replace(/[^a-z0-9-]/gi, '-')}.png`), fullPage: true })
  }
} catch (error) {
  result.errors.push(error.message)
} finally {
  await browser?.close()
  await mkdir(output, { recursive: true })
  await writeFile(resolve(output, 'report.json'), JSON.stringify(result, null, 2))
  const lines = ['# SamaA11y — rapport automatisé', '', `Cible : ${url}`, `Date : ${result.generatedAt}`, `Scénario préparé : ${Boolean(prepared)}`, '', 'Périmètre : règles automatisées WCAG 2.1 AA. Ne constitue pas un audit RGAA ni une certification de conformité.', 'Les résultats « incomplete » demandent une vérification humaine.', '']
  for (const state of result.states) {
    lines.push(`## ${state.name}`, '', `${state.violations.length} violation(s), ${state.incomplete.length} contrôle(s) à revoir, ${state.passes} règle(s) réussie(s).`, '')
    for (const issue of state.violations) {
      lines.push(`- **${issue.id}** (${issue.impact}) : ${issue.help} — ${issue.helpUrl}`)
      for (const node of issue.nodes) lines.push(`  - ${node.target.join(', ')} : ${(node.failureSummary || '').replace(/\n/g, ' ')}`)
    }
  }
  if (result.errors.length) lines.push('## Erreurs techniques', '', ...result.errors.map(error => `- ${error}`))
  await writeFile(resolve(output, 'report.md'), lines.join('\n') + '\n')
  const count = result.states.reduce((sum, state) => sum + state.violations.length, 0)
  console.log(`SamaA11y: ${result.states.length} état(s), ${count} violation(s), ${result.errors.length} erreur(s). Rapports: ${output}`)
  process.exitCode = result.errors.length ? 2 : count ? 1 : 0
}
