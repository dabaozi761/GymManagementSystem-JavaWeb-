<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'
import AppIcon from './AppIcon.vue'
const props = defineProps<{ title: string; description?: string; busy?: boolean; small?: boolean }>()
const emit = defineEmits<{ close: [] }>()
const dialog = ref<HTMLDialogElement>()
function close() { if (!props.busy) emit('close') }
onMounted(() => { dialog.value?.showModal(); document.body.classList.add('modal-open') })
onBeforeUnmount(() => { dialog.value?.close(); document.body.classList.remove('modal-open') })
</script>
<template>
  <dialog ref="dialog" class="modal" :class="{ 'modal-small': small }" aria-labelledby="modal-title" @cancel.prevent="close" @click="($event.target === dialog) && close()">
    <header class="modal-header"><div><h2 id="modal-title">{{ title }}</h2><p v-if="description">{{ description }}</p></div><button type="button" class="icon-button" aria-label="关闭弹窗" :disabled="busy" @click="close"><AppIcon name="close" /></button></header>
    <slot />
  </dialog>
</template>
