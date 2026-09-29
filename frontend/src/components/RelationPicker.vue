<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { getPage } from '../api'
import { errorMessage } from '../format'
import type { Row } from '../types'
import AppIcon from './AppIcon.vue'
const props = defineProps<{ modelValue?: number | string; resource: string; searchKey?: string; labelKey?: string; label: string; required?: boolean; id: string }>()
const emit = defineEmits<{ 'update:modelValue': [value: number | string] }>()
const rows = ref<Row[]>([]), query = ref(''), current = ref(1), total = ref(0), loading = ref(false), error = ref(''), searching = ref(false)
let controller: AbortController | undefined, timer: ReturnType<typeof setTimeout>
async function load() {
  controller?.abort(); const active = new AbortController(); controller = active
  loading.value = true; error.value = ''
  try {
    const page = await getPage(props.resource, { current: current.value, size: 20, [props.searchKey || 'name']: query.value }, active.signal)
    rows.value = page.records; total.value = page.total
  } catch (e) { if (!active.signal.aborted) error.value = errorMessage(e) }
  finally { if (!active.signal.aborted) loading.value = false }
}
function search() { clearTimeout(timer); timer = setTimeout(() => { current.value = 1; void load() }, 300) }
function move(step: number) { current.value += step; void load() }
onMounted(load)
onBeforeUnmount(() => { clearTimeout(timer); controller?.abort() })
</script>
<template>
  <div class="relation-picker">
    <div class="relation-main"><select :id="id" :value="modelValue || ''" :aria-label="label" :required="required" @change="emit('update:modelValue', ($event.target as HTMLSelectElement).value ? Number(($event.target as HTMLSelectElement).value) : '')">
      <option value="">{{ loading ? '正在加载…' : `请选择${label}` }}</option>
      <option v-if="modelValue && !rows.some(row => row.id === Number(modelValue))" :value="modelValue">已选择 #{{ modelValue }}</option>
      <option v-for="row in rows" :key="row.id" :value="row.id">{{ row[labelKey || 'name'] || row.username || row.title || '未命名' }} · #{{ row.id }}</option>
    </select><button type="button" class="icon-button" :aria-label="`搜索${label}`" :aria-expanded="searching" @click="searching = !searching"><AppIcon name="search" :size="17" /></button></div>
    <div v-if="searching" class="relation-search"><input v-model="query" :aria-label="`${label}搜索关键词`" placeholder="输入关键词筛选" @input="search" /><div class="relation-pagination"><button type="button" class="icon-button" :disabled="current <= 1 || loading" aria-label="上一组选项" @click="move(-1)"><AppIcon name="left" :size="15" /></button><small>{{ current }} / {{ Math.max(1, Math.ceil(total / 20)) }}</small><button type="button" class="icon-button" :disabled="current * 20 >= total || loading" aria-label="下一组选项" @click="move(1)"><AppIcon name="right" :size="15" /></button></div></div>
    <div v-if="error" class="field-error" role="alert">{{ error }} <button type="button" class="text-button" @click="load">重试</button></div>
  </div>
</template>
