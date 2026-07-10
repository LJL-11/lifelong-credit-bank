<script setup>
defineProps({
  toast: { type: Object, default: () => ({ text: "", type: "ok" }) },
});
</script>

<template>
  <div v-if="toast.text" :class="['toast', toast.type]">{{ toast.text }}</div>
</template>
