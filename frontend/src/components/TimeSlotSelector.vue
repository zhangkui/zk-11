<template>
  <div class="time-slot-selector">
    <div class="date-selector">
      <label class="label">选择日期</label>
      <el-date-picker
        v-model="selectedDate"
        type="date"
        :disabled-date="disabledDate"
        placeholder="选择日期"
        value-format="YYYY-MM-DD"
        @change="handleDateChange"
      />
    </div>
    <div class="time-slots">
      <label class="label">选择时段</label>
      <div class="slots-grid">
        <div
          v-for="slot in timeSlots"
          :key="slot.time"
          class="slot-item"
          :class="{
            selected: selectedSlot === slot.time,
            disabled: slot.disabled,
            booked: slot.booked
          }"
          @click="selectSlot(slot)"
        >
          <span class="slot-time">{{ slot.time }}</span>
          <span class="slot-status">
            {{ slot.disabled ? '不可用' : slot.booked ? '已预约' : '可预约' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  stationId: {
    type: [String, Number],
    required: true
  },
  modelValue: {
    type: Object,
    default: () => ({ date: '', time: '' })
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedDate = ref(props.modelValue.date || '')
const selectedSlot = ref(props.modelValue.time || '')

const timeSlots = computed(() => {
  const slots = []
  for (let i = 0; i < 24; i++) {
    const hour = i.toString().padStart(2, '0')
    const nextHour = ((i + 1) % 24).toString().padStart(2, '0')
    slots.push({
      time: `${hour}:00-${nextHour}:00`,
      disabled: i < 6 || i >= 22,
      booked: Math.random() > 0.7
    })
  }
  return slots
})

const disabledDate = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const maxDate = new Date()
  maxDate.setDate(maxDate.getDate() + 7)
  return time.getTime() < today.getTime() || time.getTime() > maxDate.getTime()
}

const handleDateChange = () => {
  selectedSlot.value = ''
  emitChange()
}

const selectSlot = (slot) => {
  if (slot.disabled || slot.booked) {
    ElMessage.warning('该时段不可选择')
    return
  }
  selectedSlot.value = slot.time
  emitChange()
}

const emitChange = () => {
  const value = {
    date: selectedDate.value,
    time: selectedSlot.value
  }
  emit('update:modelValue', value)
  emit('change', value)
}

onMounted(() => {
  if (!selectedDate.value) {
    const today = new Date()
    selectedDate.value = today.toISOString().split('T')[0]
  }
})
</script>

<style scoped>
.time-slot-selector {
  width: 100%;
}

.date-selector {
  margin-bottom: 20px;
}

.label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.slots-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.slot-item {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 12px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fff;
}

.slot-item:hover:not(.disabled):not(.booked) {
  border-color: #409eff;
  background: #ecf5ff;
}

.slot-item.selected {
  border-color: #409eff;
  background: #409eff;
  color: #fff;
}

.slot-item.disabled {
  background: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
}

.slot-item.booked {
  background: #fef0f0;
  color: #f56c6c;
  cursor: not-allowed;
}

.slot-time {
  display: block;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.slot-status {
  display: block;
  font-size: 11px;
  opacity: 0.8;
}

@media (max-width: 768px) {
  .slots-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
