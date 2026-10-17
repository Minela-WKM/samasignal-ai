<script setup>
import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import { reportsApi } from '../lib/api.js'
import { categories, urgencies, hasPosition, validatePhoto, validateConfirmation } from '../lib/reports.js'
import IncidentMap from './IncidentMap.vue'
import AiConfidence from './AiConfidence.vue'
import UrgencyBadge from './UrgencyBadge.vue'

const emit = defineEmits(['confirmed', 'show-dashboard'])
const step = ref(1)
const text = ref('')
const photo = ref(null)
const preview = ref('')
const fileInput = ref(null)
const report = ref(null)
const confirmed = ref(null)
const values = ref({ categorie: '', resume: '', localisationDeclaree: '', niveauUrgence: '', latitude: '', longitude: '' })
const errors = ref({})
const message = ref('')
const busy = ref(false)
const geoBusy = ref(false)
const geoMessage = ref('')
const summary = ref(null)
const stepTitle = ref(null)
const labels = { texteBrut: 'Description', photo: 'Photo', categorie: 'Catégorie', resume: 'Résumé', localisationDeclaree: 'Lieu', niveauUrgence: 'Urgence', latitude: 'Latitude', longitude: 'Longitude' }
const position = computed(() => {
  if (values.value.latitude === '' || values.value.longitude === '') return null
  const point = { latitude: Number(values.value.latitude), longitude: Number(values.value.longitude) }
  return hasPosition(point) ? point : null
})
let controller, geoVersion = 0
async function focus(refValue) { await nextTick(); if (refValue.value?.getClientRects().length) refValue.value.focus() }
async function changeStep(value) { step.value = value; await focus(stepTitle) }
function choosePhoto(file) {
  if (busy.value) return
  if (preview.value) URL.revokeObjectURL(preview.value)
  preview.value = ''
  photo.value = null
  const error = validatePhoto(file)
  errors.value = { ...errors.value, photo: error }
  if (!error && file) { photo.value = file; preview.value = URL.createObjectURL(file) }
  if (fileInput.value && !file) fileInput.value.value = ''
}
function drop(event) {
  event.preventDefault()
  choosePhoto(event.dataTransfer.files[0])
  if (fileInput.value) fileInput.value.value = ''
}
async function showError(error) {
  message.value = error.message
  const known = Object.entries(error.fieldErrors || {}).filter(([key, value]) => key in labels && typeof value === 'string')
  errors.value = Object.fromEntries(known)
  await focus(summary)
}
async function analyze() {
  if (busy.value) return
  errors.value = {}
  message.value = ''
  if (!text.value.trim() || text.value.length > 5000) errors.value.texteBrut = 'Décrivez le problème (1 à 5 000 caractères).'
  const photoError = validatePhoto(photo.value)
  if (photoError) errors.value.photo = photoError
  if (Object.keys(errors.value).length) { message.value = 'Vérifiez votre signalement.'; await focus(summary); return }
  report.value = null
  confirmed.value = null
  busy.value = true
  controller = new AbortController()
  const data = new FormData()
  data.append('texteBrut', text.value.trim())
  if (photo.value) data.append('photo', photo.value)
  try {
    const result = await reportsApi.analyze(data, controller.signal)
    report.value = result
    values.value = { categorie: result.categorie, resume: result.resume, localisationDeclaree: result.localisationDeclaree || '', niveauUrgence: result.niveauUrgence,
      latitude: hasPosition(result) ? result.latitude : '', longitude: hasPosition(result) ? result.longitude : '' }
    await changeStep(2)
  } catch (error) { if (error.code !== 'ABORTED') await showError(error) }
  finally { busy.value = false }
}
async function review() {
  if (busy.value) return
  geoVersion++; geoBusy.value = false
  errors.value = validateConfirmation(values.value)
  message.value = Object.keys(errors.value).length ? 'Complétez les champs avant de continuer.' : ''
  if (message.value) { await focus(summary); return }
  await changeStep(3)
}
async function confirm() {
  if (busy.value) return
  errors.value = validateConfirmation(values.value)
  if (Object.keys(errors.value).length) { step.value = 2; message.value = 'Vérifiez les champs requis.'; await focus(summary); return }
  busy.value = true
  message.value = ''
  controller = new AbortController()
  try {
    const body = { ...values.value, resume: values.value.resume.trim(), localisationDeclaree: values.value.localisationDeclaree.trim(), latitude: Number(values.value.latitude), longitude: Number(values.value.longitude) }
    const result = await reportsApi.confirm(report.value.id, body, controller.signal)
    confirmed.value = result
    emit('confirmed', result)
    await changeStep(4)
  } catch (error) {
    if (error.code !== 'ABORTED') {
      if (error.status === 400) step.value = 2
      await showError(error)
    }
  } finally { busy.value = false }
}
function locate() {
  if (geoBusy.value) return
  geoMessage.value = ''
  if (!navigator.geolocation) { geoMessage.value = 'Géolocalisation indisponible. Choisissez un point ou saisissez les coordonnées.'; return }
  geoBusy.value = true
  const version = ++geoVersion
  navigator.geolocation.getCurrentPosition(({ coords }) => {
    if (version !== geoVersion) return
    values.value.latitude = Number(coords.latitude.toFixed(6)); values.value.longitude = Number(coords.longitude.toFixed(6))
    geoBusy.value = false; geoMessage.value = 'Position renseignée. Vérifiez qu’elle correspond au lieu du problème.'
  }, () => {
    if (version !== geoVersion) return
    geoBusy.value = false; geoMessage.value = 'Position non obtenue. Vous pouvez choisir un point sur la carte ou saisir les coordonnées.'
  }, { timeout: 10000, maximumAge: 60000 })
}
function pick(point) { geoVersion++; geoBusy.value = false; Object.assign(values.value, point) }
async function reset() {
  choosePhoto(null); text.value = ''; report.value = null; confirmed.value = null; errors.value = {}; message.value = ''; geoMessage.value = ''; geoVersion++; geoBusy.value = false
  await changeStep(1)
}
async function editDescription() {
  if (busy.value) return
  busy.value = true
  message.value = ''
  controller = new AbortController()
  geoVersion++; geoBusy.value = false
  try {
    await reportsApi.discard(report.value.id, controller.signal)
    report.value = null
    errors.value = {}
    await changeStep(1)
  } catch (error) { if (error.code !== 'ABORTED') await showError(error) }
  finally { busy.value = false }
}
onBeforeUnmount(() => { controller?.abort(); geoVersion++; if (preview.value) URL.revokeObjectURL(preview.value) })
</script>

<template>
  <div class="mx-auto max-w-2xl">
    <p class="eyebrow">Signalement citoyen / Dakar</p>
    <h1 class="page-title" tabindex="-1">Votre quartier mérite mieux.</h1>
    <p class="muted">Décrivez le problème. L’IA structure, vous vérifiez.</p>
    <ol class="steps" aria-label="Étapes du signalement"><li v-for="(name, i) in ['Décrire', 'Compléter', 'Confirmer', 'Consulter']" :key="name" :aria-current="step === i + 1 ? 'step' : undefined">{{ i + 1 }}. {{ name }}</li></ol>
    <div class="panel panel-pad">
      <h2 ref="stepTitle" tabindex="-1" class="section-title">{{ ['Décrivez le problème', 'Vérifiez la fiche proposée', 'Prêt à envoyer ?', 'Signalement confirmé'][step - 1] }}</h2>
      <div v-if="message" ref="summary" tabindex="-1" class="error-box" role="alert">
        <p class="font-bold">{{ message }}</p>
        <ul v-if="Object.values(errors).some(Boolean)" class="mt-2 list-disc pl-5"><template v-for="(error, key) in errors" :key="key"><li v-if="error"><a :href="`#${key}`">{{ labels[key] }} : {{ error }}</a></li></template></ul>
      </div>
      <p class="sr-only" role="status">{{ busy ? (step === 1 ? 'Analyse en cours. Merci de patienter.' : step === 2 ? 'Suppression du brouillon en cours.' : 'Confirmation en cours.') : '' }}</p>

      <form v-if="step === 1" novalidate @submit.prevent="analyze">
        <fieldset :disabled="busy">
          <legend class="sr-only">Description du signalement</legend>
          <div class="field">
            <label for="texteBrut">Que se passe-t-il ? <span class="hint">(obligatoire)</span></label>
            <textarea id="texteBrut" v-model="text" maxlength="5000" required aria-describedby="texteBrut-help texteBrut-error" :aria-invalid="!!errors.texteBrut" placeholder="Ex. : des déchets bloquent le trottoir près du marché de Médina…"></textarea>
            <p id="texteBrut-help" class="hint">Précisez le lieu et ce que vous observez. {{ text.length }} / 5 000 caractères.</p>
            <p id="texteBrut-error" class="field-error">{{ errors.texteBrut }}</p>
          </div>
          <div class="field">
            <label for="photo">Ajouter une photo <span class="hint">(facultatif)</span></label>
            <div class="upload-zone" @dragover.prevent @drop="drop">
              <p id="photo-help" class="hint mb-3">Choisissez ou déposez une photo. JPEG, PNG ou WebP, 5 Mo maximum.</p>
              <input id="photo" ref="fileInput" type="file" accept="image/jpeg,image/png,image/webp" aria-describedby="photo-help photo-error" :aria-invalid="!!errors.photo" @change="choosePhoto($event.target.files[0])" />
              <p id="photo-error" class="field-error">{{ errors.photo }}</p>
              <template v-if="photo"><p class="hint mt-2 break-all">{{ photo.name }}</p><img :src="preview" alt="Aperçu de la photo jointe au signalement" class="preview-image" /><button class="btn btn-small mt-3" type="button" @click="choosePhoto(null)">Retirer la photo</button></template>
            </div>
          </div>
          <div class="ai-panel mt-6"><p class="font-bold text-sm">Vous gardez le dernier mot.</p><p class="text-sm mt-1">Gemma 4 proposera une fiche. Rien n’est publié avant votre confirmation.</p></div>
          <div class="actions"><button class="btn btn-primary" type="submit">{{ busy ? 'Analyse en cours…' : 'Analyser mon signalement' }}</button></div>
        </fieldset>
      </form>

      <form v-else-if="step === 2" novalidate @submit.prevent="review">
        <div class="ai-panel mt-5">
          <p class="font-bold">Proposition Gemma 4 · <AiConfidence :score="report.scoreConfiance" /></p>
          <p class="text-sm mt-2">{{ report.resume }}</p>
          <p class="text-xs mt-2">Score estimatif du modèle, pas une garantie d’exactitude.</p>
          <p v-if="report.elementsVisuelsDetectes.length" class="text-sm mt-2">Éléments visuels : {{ report.elementsVisuelsDetectes.join(', ') }}.</p>
          <p v-if="report.champsManquants.length" class="text-sm mt-2 font-semibold">À compléter : {{ report.champsManquants.map(key => labels[key] || key).join(', ') }}.</p>
        </div>
        <p class="hint mt-4">Tous les champs suivants sont obligatoires. Corrigez la proposition si nécessaire.</p>
        <div class="two-fields">
          <div class="field"><label for="categorie">Catégorie</label><select id="categorie" v-model="values.categorie" :aria-invalid="!!errors.categorie" aria-describedby="categorie-error"><option v-for="(label, key) in categories" :key="key" :value="key">{{ label }}</option></select><p id="categorie-error" class="field-error">{{ errors.categorie }}</p></div>
          <div class="field"><label for="niveauUrgence">Urgence</label><select id="niveauUrgence" v-model="values.niveauUrgence" :aria-invalid="!!errors.niveauUrgence" aria-describedby="niveauUrgence-error"><option v-for="(label, key) in urgencies" :key="key" :value="key">{{ label }}</option></select><p id="niveauUrgence-error" class="field-error">{{ errors.niveauUrgence }}</p></div>
        </div>
        <div class="field"><label for="resume">Résumé</label><textarea id="resume" v-model="values.resume" maxlength="1000" :aria-invalid="!!errors.resume" aria-describedby="resume-error"></textarea><p id="resume-error" class="field-error">{{ errors.resume }}</p></div>
        <div class="field"><label for="localisationDeclaree">Lieu du signalement</label><input id="localisationDeclaree" v-model="values.localisationDeclaree" maxlength="500" :aria-invalid="!!errors.localisationDeclaree" aria-describedby="localisationDeclaree-error" /><p id="localisationDeclaree-error" class="field-error">{{ errors.localisationDeclaree }}</p></div>
        <div class="mt-6"><h3 class="font-bold mb-3">Position précise</h3><IncidentMap picker :position="position" @pick="pick" /></div>
        <button type="button" class="btn btn-small mt-3" :disabled="geoBusy" @click="locate">{{ geoBusy ? 'Recherche de la position…' : 'Utiliser ma position' }}</button>
        <p role="status" class="hint mt-2">{{ geoMessage }}</p>
        <div class="two-fields"><div v-for="key in ['latitude', 'longitude']" :key="key" class="field"><label :for="key">{{ labels[key] }}</label><input :id="key" v-model="values[key]" type="number" step="any" :min="key === 'latitude' ? -90 : -180" :max="key === 'latitude' ? 90 : 180" :aria-invalid="!!errors[key]" :aria-describedby="`${key}-error`" @input="geoVersion++; geoBusy = false" /><p :id="`${key}-error`" class="field-error">{{ errors[key] }}</p></div></div>
        <div class="actions"><button type="submit" class="btn btn-primary" :disabled="busy">Vérifier avant confirmation</button><button type="button" class="btn" :disabled="busy" @click="editDescription">Modifier le signalement</button></div>
      </form>

      <div v-else-if="step === 3">
        <p class="muted mt-3">Ces informations seront visibles dans la supervision publique.</p>
        <dl class="mt-5 grid gap-3 text-sm"><div><dt class="font-bold">Résumé</dt><dd>{{ values.resume }}</dd></div><div><dt class="font-bold">Catégorie</dt><dd>{{ categories[values.categorie] }}</dd></div><div><dt class="font-bold">Urgence</dt><dd class="mt-1"><UrgencyBadge :urgency="values.niveauUrgence" /></dd></div><div><dt class="font-bold">Localisation</dt><dd>{{ values.localisationDeclaree }} · {{ values.latitude }}, {{ values.longitude }}</dd></div></dl>
        <div class="actions"><button type="button" class="btn btn-primary" :disabled="busy" @click="confirm">{{ busy ? 'Confirmation en cours…' : 'Confirmer le signalement' }}</button><button type="button" class="btn" :disabled="busy" @click="changeStep(2)">Corriger la fiche</button></div>
      </div>
      <div v-else>
        <p class="mt-4 font-semibold" role="status">Votre signalement a bien été enregistré par le serveur.</p>
        <p class="muted mt-2">{{ confirmed.resume }}</p><p class="hint mt-3 break-all">Référence : {{ confirmed.id }}</p>
        <div class="actions"><button class="btn btn-primary" type="button" @click="emit('show-dashboard')">Voir dans la supervision</button><button class="btn" type="button" @click="reset">Nouveau signalement</button></div>
      </div>
    </div>
  </div>
</template>
