<script setup>
import { onMounted, ref } from 'vue'
import { getHealth } from './lib/api'

const status = ref('idle') // 'idle', 'loading', 'success', 'error'
const message = ref('Prêt à vérifier.')

async function checkHealth() {
  status.value = 'loading'
  message.value = 'Vérification de la connexion en cours…'

  try {
    const result = await getHealth(AbortSignal.timeout(5000))
    if (result.status === 'UP') {
      status.value = 'success'
      message.value = 'API connectée et opérationnelle.'
    } else {
      status.value = 'error'
      message.value = 'API temporairement indisponible.'
    }
  } catch {
    status.value = 'error'
    message.value = 'API indisponible — vérifiez votre réseau ou démarrez le backend.'
  }
}

onMounted(checkHealth)
</script>

<template>
  <a class="skip-link" href="#contenu">Aller au contenu</a>
  <div class="mx-auto max-w-6xl px-4 py-8 sm:px-6 lg:px-8">
    <header class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6 border-b-4 border-zinc-900 pb-6 mb-12">
      <a href="/" class="brand" aria-label="SamaSignal AI, accueil">SamaSignal<span> AI</span></a>
      <span class="badge">Dakar · Civic Tech</span>
    </header>

    <main id="contenu">
      <section aria-labelledby="hero-title">
        <p class="eyebrow mb-6">Des quartiers mieux accompagnés</p>
        <h1 id="hero-title" class="max-w-4xl text-5xl font-black leading-tight tracking-tighter sm:text-7xl lg:text-8xl">
          Un signalement.<br />
          <span class="text-emerald-500 bg-zinc-900 px-4 py-2 inline-block mt-4 border-4 border-zinc-900 shadow-[8px_8px_0px_0px_#10b981]">Une action à suivre.</span>
        </h1>
        <p class="mt-10 max-w-2xl text-xl font-bold leading-relaxed text-zinc-800 border-l-4 border-zinc-900 pl-6">
          Déchets, voirie, eau ou éclairage : SamaSignal AI aide les citoyens à transformer un texte et une photo en fiche claire pour les services municipaux.
        </p>
      </section>

      <section class="mt-16 max-w-3xl border-4 border-zinc-900 bg-white p-6 sm:p-10 shadow-[12px_12px_0px_0px_#18181b]" aria-labelledby="etat-titre">
        <h2 id="etat-titre" class="text-2xl font-black uppercase">Le projet prend forme</h2>
        <p class="mt-4 text-lg font-bold text-zinc-700">Le socle technique est disponible. La création de signalements et le tableau de bord sont en cours de développement.</p>

        <div
          class="mt-8 p-6 border-4 border-zinc-900 flex flex-col sm:flex-row items-start sm:items-center gap-6"
          :class="{
            'bg-zinc-100': status === 'idle' || status === 'loading',
            'bg-emerald-300': status === 'success',
            'bg-red-400': status === 'error'
          }"
          role="region"
          aria-live="polite"
        >
          <div class="flex-1 flex items-center gap-4">
            <span v-if="status === 'loading'" class="animate-spin h-6 w-6 border-4 border-zinc-900 border-t-transparent rounded-full" aria-hidden="true"></span>
            <span v-else-if="status === 'success'" class="text-3xl" aria-hidden="true">✅</span>
            <span v-else-if="status === 'error'" class="text-3xl" aria-hidden="true">🚨</span>
            <p
              class="text-lg font-black"
              :class="{'text-zinc-900': true}"
              :aria-busy="status === 'loading'"
            >
              {{ message }}
            </p>
          </div>
          <button
            type="button"
            class="action w-full sm:w-auto"
            :disabled="status === 'loading'"
            @click="checkHealth"
          >
            {{ status === 'loading' ? 'Vérification…' : 'Vérifier' }}
          </button>
        </div>
      </section>

      <section class="mt-24" aria-labelledby="parcours-titre">
        <h2 id="parcours-titre" class="sr-only">Le futur parcours citoyen</h2>
        <ol class="grid gap-8 sm:grid-cols-3">
          <li v-for="(step, index) in [
            ['Décrire', 'Un texte, une photo et un point sur la carte.'],
            ['Vérifier', 'Une fiche proposée par l’IA, corrigée par vous.'],
            ['Suivre', 'Un signalement confirmé, visible sur le tableau de bord.'],
          ]" :key="step[0]" class="border-4 border-zinc-900 bg-white p-6 shadow-[8px_8px_0px_0px_#18181b]">
            <span class="inline-block bg-zinc-900 text-white font-black text-xl px-3 py-1 mb-4 border-2 border-zinc-900">0{{ index + 1 }}</span>
            <h3 class="text-2xl font-black uppercase mb-3">{{ step[0] }}</h3>
            <p class="text-lg font-bold leading-relaxed text-zinc-700">{{ step[1] }}</p>
          </li>
        </ol>
      </section>
    </main>

    <footer class="mt-24 flex flex-col sm:flex-row justify-between gap-6 border-t-4 border-zinc-900 pt-8 text-base font-bold text-zinc-800">
      <p>Un projet open source · Galsen DEV 2026</p>
      <a href="/swagger-ui/index.html" class="inline-block border-b-4 border-zinc-900 hover:bg-zinc-900 hover:text-white transition-colors px-2 py-1">Documentation de l’API</a>
    </footer>
  </div>
</template>
