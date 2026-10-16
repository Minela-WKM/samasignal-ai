<script setup>
import { confidence } from '../lib/reports.js'
defineProps({ score: Number })
</script>
<template><span class="font-mono font-bold text-violet-950"><span class="sr-only">Confiance estimée par Gemma 4 : </span>{{ confidence(score) }}</span></template>
