import { test, expect } from '@playwright/test'
import axe from 'axe-core'
import { mockApi, enterCorrection, completeCorrection } from './fixtures.js'

async function audit(page) {
  await page.addScriptTag({ content: axe.source })
  const results = await page.evaluate(() => window.axe.run(document, { runOnly: { type: 'tag', values: ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'] } }))
  expect(results.violations.map(v => ({ id: v.id, nodes: v.nodes.map(n => n.target) }))).toEqual([])
}
test('WCAG automated checks on input, validation errors, correction, review and dashboard', async ({ page }) => {
  await mockApi(page); await page.goto('/'); await audit(page)
  await page.getByRole('button', { name: 'Analyser mon signalement' }).click(); await audit(page)
  await enterCorrection(page); await audit(page)
  await completeCorrection(page); await audit(page)
  await page.getByRole('button', { name: 'Supervision Mairie', exact: true }).click()
  await expect(page.locator('tbody tr')).toHaveCount(5)
  await audit(page)
})

test('computed badge colors exceed AAA and primary action exceeds AA', async ({ page }) => {
  await mockApi(page); await page.goto('/')
  async function contrast(locator) {
    return locator.evaluate(element => {
      const style = getComputedStyle(element)
      // Canvas resolves CSS Color 4 (Tailwind oklch) to sRGB pixels.
      const ctx = document.createElement('canvas').getContext('2d', { willReadFrequently: true })
      const luminance = color => {
        ctx.clearRect(0, 0, 1, 1); ctx.fillStyle = color; ctx.fillRect(0, 0, 1, 1)
        const rgb = [...ctx.getImageData(0, 0, 1, 1).data].slice(0, 3).map(v => { v /= 255; return v <= .04045 ? v / 12.92 : ((v + .055) / 1.055) ** 2.4 })
        return .2126 * rgb[0] + .7152 * rgb[1] + .0722 * rgb[2]
      }
      const a = luminance(style.color), b = luminance(style.backgroundColor)
      return (Math.max(a, b) + .05) / (Math.min(a, b) + .05)
    })
  }
  expect(await contrast(page.getByRole('button', { name: 'Analyser mon signalement' }))).toBeGreaterThan(4.5)
  await page.getByRole('button', { name: 'Supervision Mairie', exact: true }).click()
  for (const label of ['CRITIQUE', 'MOYEN', 'FAIBLE']) expect(await contrast(page.locator('tbody').getByText(label, { exact: true }).first())).toBeGreaterThanOrEqual(7)
})
