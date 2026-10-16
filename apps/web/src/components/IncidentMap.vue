<script setup>
import { nextTick, onActivated, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { hasPosition, urgencies } from '../lib/reports.js'

const props = defineProps({ reports: { type: Array, default: () => [] }, picker: Boolean, position: Object })
const emit = defineEmits(['pick'])
const canvas = ref(null)
const tileError = ref(false)
let map, layer, observer, selectedMarker
const markers = new Map()
const reduced = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches
function icon(text) { return L.divIcon({ className: 'map-pin', html: String(text), iconSize: [30, 30], iconAnchor: [15, 15] }) }
function render() {
  if (!map) return
  layer.clearLayers()
  markers.clear()
  selectedMarker = null
  if (props.picker) {
    if (props.position && hasPosition(props.position)) {
      selectedMarker = L.marker([props.position.latitude, props.position.longitude], { icon: icon('+'), title: 'Position choisie', keyboard: true }).addTo(layer)
      selectedMarker.getElement()?.setAttribute('aria-label', '+ Position choisie')
      map.panTo(selectedMarker.getLatLng(), { animate: false })
    }
    return
  }
  props.reports.filter(hasPosition).forEach((report, index) => {
    const content = document.createElement('div')
    const title = document.createElement('strong')
    title.textContent = report.resume
    const urgency = document.createElement('p')
    urgency.textContent = `Urgence : ${urgencies[report.niveauUrgence]}`
    content.append(title, urgency)
    const marker = L.marker([report.latitude, report.longitude], { icon: icon(index + 1), title: report.resume, keyboard: true })
      .bindPopup(content).addTo(layer)
    marker.getElement()?.setAttribute('aria-label', `${index + 1}. Signalement : ${report.resume}`)
    markers.set(report.id, marker)
  })
}
function locate(id, focus = true) {
  const marker = markers.get(id)
  if (!marker || !map) return
  map.setView(marker.getLatLng(), 16, { animate: !reduced() })
  marker.openPopup()
  if (focus) { canvas.value.scrollIntoView({ block: 'center', behavior: 'instant' }); canvas.value.focus({ preventScroll: true }) }
}
defineExpose({ locate })
onMounted(() => {
  map = L.map(canvas.value, { scrollWheelZoom: false, zoomAnimation: !reduced(), fadeAnimation: !reduced(), markerZoomAnimation: !reduced() }).setView([14.7167, -17.4677], 12)
  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>', maxZoom: 19,
  }).on('tileerror', () => { tileError.value = true }).addTo(map)
  map.zoomControl.setPosition('topright')
  map.attributionControl.setPrefix('<a href="https://leafletjs.com">Leaflet</a>')
  const zoomIn = canvas.value.querySelector('.leaflet-control-zoom-in')
  const zoomOut = canvas.value.querySelector('.leaflet-control-zoom-out')
  zoomIn?.setAttribute('aria-label', '+ Zoom avant')
  zoomOut?.setAttribute('aria-label', '− Zoom arrière')
  layer = L.layerGroup().addTo(map)
  if (props.picker) map.on('click', ({ latlng }) => emit('pick', { latitude: Number(latlng.lat.toFixed(6)), longitude: Number(latlng.lng.toFixed(6)) }))
  observer = new ResizeObserver(() => map?.invalidateSize({ animate: false }))
  observer.observe(canvas.value)
  render()
})
watch(() => [props.reports, props.position], render, { deep: true })
onActivated(async () => { await nextTick(); map?.invalidateSize({ animate: false }) })
onBeforeUnmount(() => { observer?.disconnect(); map?.remove(); map = null })
</script>
<template>
  <div :class="{ 'map-picker': picker }">
    <div ref="canvas" class="map-canvas map-region" role="region" tabindex="0"
      :aria-label="picker ? 'Choisir la position du signalement à Dakar' : 'Carte interactive des signalements citoyens à Dakar'"></div>
    <p class="hint mt-2">{{ picker ? 'Cliquez sur la carte ou renseignez les coordonnées ci-dessous.' : 'Les numéros repèrent les signalements visibles dans le tableau.' }} Flèches pour déplacer la carte, + et − pour zoomer.</p>
    <p v-if="tileError" class="notice mt-3 text-sm" role="status">Le fond de carte est indisponible. Les coordonnées et les informations textuelles restent utilisables.</p>
  </div>
</template>
