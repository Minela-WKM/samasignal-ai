<script setup>
import { confidence } from '../lib/reports.js'
defineProps({ score: Number })
</script>
<template><span class="font-mono font-bold text-violet-950" :aria-label="`Confiance estimée par Gemma 4 : ${confidence(score)}`">{{ confidence(score) }}</span></template>
