<template>
  <el-card class="station-card" shadow="hover" @click="handleClick">
    <div class="card-header">
      <div class="station-icon">
        <el-icon :size="32" color="#409EFF"><Charger /></el-icon>
      </div>
      <div class="station-info">
        <h3 class="station-name">{{ station.name }}</h3>
        <p class="station-address">
          <el-icon><Location /></el-icon>
          {{ station.address }}
        </p>
      </div>
    </div>
    <div class="card-body">
      <div class="stat-item">
        <span class="stat-label">可用桩数</span>
        <span class="stat-value available">{{ station.availablePiles || 0 }}/{{ station.totalPiles || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">充电价格</span>
        <span class="stat-value price">¥{{ station.price || '1.50' }}/度</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">服务费率</span>
        <span class="stat-value">¥{{ station.serviceFee || '0.50' }}/度</span>
      </div>
    </div>
    <div class="card-footer">
      <el-tag v-if="station.availablePiles > 0" type="success" effect="light">
        有空闲
      </el-tag>
      <el-tag v-else type="warning" effect="light">
        已满
      </el-tag>
      <span class="distance" v-if="station.distance">
        {{ station.distance }}km
      </span>
    </div>
  </el-card>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  station: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click', props.station)
}
</script>

<style scoped>
.station-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
}

.station-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.station-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.station-icon :deep(.el-icon) {
  color: #fff !important;
}

.station-info {
  flex: 1;
  min-width: 0;
}

.station-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.station-address {
  font-size: 13px;
  color: #909399;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-body {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.stat-value.available {
  color: #67c23a;
}

.stat-value.price {
  color: #f56c6c;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.distance {
  font-size: 13px;
  color: #909399;
}
</style>
