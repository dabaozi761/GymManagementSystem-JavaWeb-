<script setup lang="ts">
import type { Field } from '../types'
import RelationPicker from './RelationPicker.vue'
defineProps<{ field: Field; modelValue?: any; prefix?: string; filter?: boolean; noEmpty?: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [value: any] }>()
function update(event: Event, numeric: boolean) {
  const value = (event.target as HTMLInputElement).value
  emit('update:modelValue', numeric && value !== '' ? Number(value) : value)
}
</script>
<template>
  <div class="form-field" :class="{ 'field-wide': field.type === 'textarea' }">
    <label :for="`${prefix || 'field'}-${field.key}`">{{ field.label }}<span v-if="field.required && !filter" class="required"> *</span></label>
    <RelationPicker v-if="field.type === 'relation'" :id="`${prefix || 'field'}-${field.key}`" :model-value="modelValue" :resource="field.resource!" :search-key="field.searchKey" :label-key="field.labelKey" :label="field.label" :required="field.required && !filter" @update:model-value="emit('update:modelValue', $event)" />
    <select v-else-if="field.type === 'select'" :id="`${prefix || 'field'}-${field.key}`" :value="modelValue ?? ''" :required="field.required && !filter" @change="update($event, true)">
      <option v-if="!noEmpty" value="">{{ filter ? `全部${field.label}` : '请选择' }}</option><option v-for="option in field.options" :key="option.value" :value="option.value">{{ option.label }}</option>
    </select>
    <textarea v-else-if="field.type === 'textarea'" :id="`${prefix || 'field'}-${field.key}`" :value="modelValue" rows="3" :required="field.required && !filter" :maxlength="field.maxLength" :placeholder="field.placeholder" @input="update($event, false)" />
    <input v-else :id="`${prefix || 'field'}-${field.key}`" :type="field.type || 'text'" :value="modelValue" :required="field.required && !filter" :min="field.min" :max="field.max" :step="field.step || '1'" :maxlength="field.maxLength || 255" :placeholder="field.placeholder || (filter ? field.label : `请输入${field.label}`)" :autocomplete="field.type === 'password' ? 'new-password' : 'off'" @input="update($event, field.type === 'number')" />
    <small v-if="field.hint && !filter" class="field-hint">{{ field.hint }}</small>
  </div>
</template>
