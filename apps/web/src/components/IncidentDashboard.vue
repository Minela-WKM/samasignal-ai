<script setup>
import { computed, nextTick, onActivated, onBeforeUnmount, onDeactivated, ref, watch } from 'vue'
import { reportsApi } from '../lib/api.js'
import { categories, urgencies, filterReports, hasPosition, canUseDemo } from '../lib/reports.js'
import { demoReports } from '../fixtures/demoReports.js'
import IncidentMap from './IncidentMap.vue'
import UrgencyBadge from './UrgencyBadge.vue'
import AiConfidence from './AiConfidence.vue'

const props = defineProps({ revision: { type: Number, default: 0 }, confirmedReport: Object })
const reports = ref([])
const category = ref('')
const urgency = ref('')
const loading = ref(false)
const source = ref('none')
const error = ref('')
const selected = ref(null)
const map = ref(null)
const filtered = computed(() => filterReports(reports.value, category.value, urgency.value))
const mappable = computed(() => filtered.value.filter(hasPosition))
const critical = computed(() => filtered.value.filter(r => r.niveauUrgence === 'haute').length)
let controller, sequence = 0, active = false, loaded = false, lastRevision = -1, hadReal = false

async function load() {
  controller?.abort()
  const version = ++sequence
  controller = new AbortController()
  loading.value = true
  error.value = ''
  try {
    const result = await reportsApi.list({}, controller.signal)
    if (version !== sequence) return
    reports.value = result; source.value = 'live'; hadReal = true
    if (props.confirmedReport && props.revision !== lastRevision) {
      const match = result.find(r => r.id === props.confirmedReport.id)
      if (match) { category.value = ''; urgency.value = ''; selected.value = match.id }
      else error.value = 'Votre confirmation est enregistrée, mais cette fiche n’apparaît pas encore dans la liste. Actualisez dans un instant.'
    }
  } catch (failure) {
    if (version !== sequence || failure.code === 'ABORTED') return
    if (!hadReal && !props.confirmedReport && canUseDemo(failure)) {
      source.value = 'demo'; reports.value = demoReports.map(r => ({ ...r })); selected.value = null
    } else {
      if (!hadReal) { source.value = 'none'; reports.value = [] }
      error.value = `${props.confirmedReport ? 'Votre confirmation reste enregistrée. ' : ''}${failure.message}${hadReal ? ' Les dernières données réelles sont conservées et peuvent être obsolètes.' : ''}`
    }
  } finally {
    if (version === sequence) {
      loading.value = false; loaded = true; lastRevision = props.revision
      await nextTick()
      if (selected.value) map.value?.locate(selected.value, false)
    }
  }
}
function locate(report) { selected.value = report.id; map.value?.locate(report.id) }
watch(filtered, (list) => { if (!list.some(r => r.id === selected.value)) selected.value = null })
watch(() => props.revision, () => { if (active && props.revision !== lastRevision) load() })
onActivated(() => { active = true; if (!loaded || props.revision !== lastRevision) load() })
onDeactivated(() => { active = false; if (loading.value) { sequence++; controller?.abort(); loading.value = false; loaded = false } })
onBeforeUnmount(() => { sequence++; controller?.abort() })
</script>

<template>
  <div>
    <div class="flex flex-wrap items-end justify-between gap-4">
      <div><p class="eyebrow">Supervision mairie / Vue publique</p><h1 class="page-title" tabindex="-1">Les signalements, en un regard.</h1><p class="muted">Repérez les priorités et localisez les incidents confirmés.</p></div>
      <button class="btn" type="button" :disabled="loading" @click="load">{{ loading ? 'Chargement…' : 'Actualiser les signalements' }}</button>
    </div>
    <div v-if="source === 'demo'" class="notice mt-6" role="status"><p class="font-bold">Mode démonstration — données fictives, API indisponible</p><p class="text-sm mt-1">Ces cinq exemples illustrent la supervision. Ils ne sont pas des signalements réels.</p><button class="btn btn-small mt-3" type="button" :disabled="loading" @click="load">Réessayer l’API</button></div>
    <p v-if="error" class="error-box" role="alert">{{ error }}</p>
    <div class="filters panel">
      <div class="field"><label for="filter-category">Catégorie</label><select id="filter-category" v-model="category"><option value="">TOUTES</option><option v-for="(label, key) in categories" :key="key" :value="key">{{ label }}</option></select></div>
      <div class="field"><label for="filter-urgency">Urgence</label><select id="filter-urgency" v-model="urgency"><option value="">TOUS</option><option v-for="(label, key) in urgencies" :key="key" :value="key">{{ label }}</option></select></div>
      <div class="text-sm pb-2" role="status" aria-live="polite" aria-atomic="true">{{ loading ? 'Chargement des signalements…' : `${filtered.length} signalement${filtered.length > 1 ? 's' : ''} affiché${filtered.length > 1 ? 's' : ''}` }}<span v-if="!loading && source !== 'none'" class="block text-xs muted mt-1">{{ critical }} critique{{ critical > 1 ? 's' : '' }} · {{ mappable.length }} sur la carte</span></div>
    </div>
    <div class="dashboard-grid" :aria-busy="loading">
      <div class="panel min-w-0">
        <div class="table-scroll" role="region" aria-label="Tableau des signalements, défilement horizontal disponible" tabindex="0">
          <table><caption>Tableau de triage <span class="font-normal text-xs muted">/ {{ source === 'demo' ? 'Exemples fictifs' : 'Fiches confirmées' }}</span></caption>
            <thead><tr><th scope="col">Urgence</th><th scope="col">Catégorie</th><th scope="col">Résumé IA &amp; Localisation</th><th scope="col">Confiance Gemma 4</th><th scope="col">Action</th></tr></thead>
            <tbody><tr v-for="report in filtered" :key="report.id" :data-selected="selected === report.id">
              <td><UrgencyBadge :urgency="report.niveauUrgence" /></td><td class="font-semibold text-xs">{{ categories[report.categorie] }}</td>
              <td><p class="font-semibold">{{ report.resume }}</p><p class="muted text-xs mt-2">{{ report.localisationDeclaree || 'Lieu non précisé' }}</p></td>
              <td class="bg-violet-50"><AiConfidence :score="report.scoreConfiance" /></td>
              <td><button v-if="hasPosition(report)" type="button" class="btn btn-small" :aria-label="`Localiser sur la carte : ${report.resume}`" @click="locate(report)">Localiser sur la carte</button><span v-else class="muted text-xs">Position indisponible</span></td>
            </tr><tr v-if="!filtered.length"><td colspan="5" class="text-center py-10 muted">{{ loading ? 'Chargement en cours…' : source === 'none' ? 'Les signalements ne sont pas disponibles.' : 'Aucun signalement ne correspond à cette sélection.' }}</td></tr></tbody>
          </table>
        </div>
      </div>
      <section class="panel panel-pad min-w-0" aria-labelledby="map-title"><div class="flex flex-wrap justify-between gap-2 items-center mb-4"><h2 id="map-title" class="section-title">Sur le terrain</h2><span class="eyebrow">Dakar, Sénégal</span></div><IncidentMap ref="map" :reports="filtered" /><p class="hint mt-3">Le tableau reste consultable sans carte. La confiance IA est une estimation, pas une garantie d’exactitude.</p></section>
    </div>
  </div>
</template>

