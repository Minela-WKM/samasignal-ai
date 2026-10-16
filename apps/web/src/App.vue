<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import IncidentReportFlow from './components/IncidentReportFlow.vue'
import IncidentDashboard from './components/IncidentDashboard.vue'
const view = ref('citizen')
const header = ref(null)
const confirmed = ref(null)
const revision = ref(0)
const component = computed(() => view.value === 'citizen' ? IncidentReportFlow : IncidentDashboard)
let observer
async function navigate(target) {
  if (view.value === target) return
  view.value = target
  await nextTick()
  document.querySelector('main h1')?.focus()
}
function onConfirmed(report) { confirmed.value = report; revision.value++ }
onMounted(() => {
  observer = new ResizeObserver(() => document.documentElement.style.setProperty('--header-height', `${header.value.offsetHeight}px`))
  observer.observe(header.value)
})
onBeforeUnmount(() => observer?.disconnect())
</script>
<template>
  <a class="skip-link" href="#contenu">Aller au contenu</a>
  <header ref="header" role="banner" class="app-header">
    <div class="header-inner">
      <div class="brand-row">
        <span class="logo-mark" aria-hidden="true">S<span class="logo-dot">.</span></span>
        <div><p class="brand-name">SamaSignal AI</p><p class="eyebrow">Dakar · Votre quartier, votre voix</p></div>
        <span class="ai-badge">Propulsé par Gemma 4</span>
      </div>
      <div role="group" aria-label="Choisir une vue" class="view-switch">
        <button type="button" :aria-pressed="view === 'citizen'" @click="navigate('citizen')">Signalement Citoyen</button>
        <button type="button" :aria-pressed="view === 'dashboard'" @click="navigate('dashboard')">Supervision Mairie</button>
      </div>
    </div>
  </header>
  <main id="contenu" tabindex="-1" class="app-main">
    <KeepAlive><component :is="component" :revision="revision" :confirmed-report="confirmed" @confirmed="onConfirmed" @show-dashboard="navigate('dashboard')" /></KeepAlive>
  </main>
  <footer class="app-footer"><span>SamaSignal AI · Un projet citoyen open source</span><span>Prototype public · Ne partagez pas de données sensibles.</span></footer>
</template>
