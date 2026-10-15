<script setup>
import { onMounted, ref } from 'vue'
import { getHealth } from './lib/api'

const health = ref('Vérification de la connexion…')
const checking = ref(false)
async function checkHealth() {
  checking.value = true
  try {
    const result = await getHealth(AbortSignal.timeout(5000))
    health.value = result.status === 'UP' ? 'API connectée' : 'API temporairement indisponible'
  } catch {
    health.value = 'API indisponible — démarrez le backend puis réessayez.'
  } finally {
    checking.value = false
  }
}
onMounted(checkHealth)
</script>

<template>
  <a class="skip-link" href="#contenu">Aller au contenu</a>
  <div class="mx-auto max-w-6xl px-6 py-8 sm:px-10">
    <header class="flex flex-wrap items-center justify-between gap-4 border-b border-emerald-900/15 pb-6">
      <a href="/" class="brand" aria-label="SamaSignal AI, accueil">SamaSignal<span> AI</span></a>
      <span class="badge">Dakar · Civic Tech</span>
    </header>
    <main id="contenu" class="py-16 sm:py-24">
      <p class="eyebrow">Des quartiers mieux accompagnés</p>
      <h1 class="mt-5 max-w-3xl text-5xl font-semibold leading-tight tracking-tight sm:text-7xl">
        Un signalement.<br /><span class="text-emerald-800">Une action à suivre.</span>
      </h1>
      <p class="mt-7 max-w-xl text-lg leading-relaxed text-stone-700">
        Déchets, voirie, eau ou éclairage : SamaSignal AI aidera les citoyens à
        transformer un texte et une photo en fiche claire pour les services municipaux.
      </p>
      <section class="mt-10 max-w-2xl rounded-2xl border border-emerald-900/20 bg-white p-6" aria-labelledby="etat-titre">
        <h2 id="etat-titre" class="text-lg font-semibold">Le projet prend forme</h2>
        <p class="mt-2 text-stone-700">Le socle technique est disponible. La création de signalements et le tableau de bord sont en cours de développement.</p>
        <div class="mt-5 flex flex-wrap items-center gap-4">
          <p role="status" class="text-sm font-medium">{{ health }}</p>
          <button type="button" class="action" :disabled="checking" @click="checkHealth">
            {{ checking ? 'Vérification…' : 'Vérifier la connexion' }}
          </button>
        </div>
      </section>
      <ol class="mt-14 grid gap-6 sm:grid-cols-3" aria-label="Le futur parcours citoyen">
        <li v-for="(step, index) in [
          ['Décrire', 'Un texte, une photo et un point sur la carte.'],
          ['Vérifier', 'Une fiche proposée par l’IA, corrigée par vous.'],
          ['Suivre', 'Un signalement confirmé, visible sur le tableau de bord.'],
        ]" :key="step[0]" class="border-t border-emerald-900/20 pt-5">
          <span class="eyebrow">0{{ index + 1 }}</span>
          <h2 class="mt-3 text-xl font-semibold">{{ step[0] }}</h2>
          <p class="mt-2 leading-relaxed text-stone-700">{{ step[1] }}</p>
        </li>
      </ol>
    </main>
    <footer class="flex flex-wrap justify-between gap-4 border-t border-emerald-900/15 pt-6 text-sm text-stone-700">
      <p>Un projet open source · Galsen DEV 2026</p>
      <a href="/swagger-ui/index.html">Documentation de l’API</a>
    </footer>
  </div>
</template>
