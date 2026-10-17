<script setup>
import { urgencies } from '../lib/reports.js'
defineProps({ urgency: { type: String, required: true } })
const styles = {
  haute: 'bg-red-100 text-red-950 border-red-900 font-bold',
  moyenne: 'bg-amber-100 text-amber-950 border-amber-900 font-bold',
  basse: 'bg-slate-200 text-slate-900 border-slate-800 font-semibold',
}
</script>
<template><span :class="styles[urgency]" class="inline-block rounded border px-2 py-1 text-xs whitespace-nowrap">{{ urgencies[urgency] }}</span></template>
