<template>
  <div class="queue-display">
    <div class="display-header">
      <h2 class="station-name">{{ stationName }}</h2>
      <p class="subtitle">实时叫号显示屏</p>
    </div>
    <div class="current-number">
      <div class="number-label">当前叫号</div>
      <div class="number-value">{{ currentNumber }}</div>
      <div class="number-time">{{ currentTime }}</div>
    </div>
    <div class="queue-info">
      <div class="info-item">
        <div class="info-value">{{ waitingCount }}</div>
        <div class="info-label">等待人数</div>
      </div>
      <div class="info-item">
        <div class="info-value">{{ myPosition > 0 ? myPosition : '-' }}</div>
        <div class="info-label">我的位置</div>
      </div>
      <div class="info-item">
        <div class="info-value">{{ estimatedTime }}</div>
        <div class="info-label">预计等待</div>
      </div>
    </div>
    <div class="queue-list" v-if="queueList.length > 0">
      <div class="list-title">等待队列</div>
      <div class="list-content">
        <div
          v-for="(item, index) in queueList.slice(0, 5)"
          :key="item.id"
          class="queue-item"
          :class="{ active: index === 0, 'is-me': item.isMe }"
        >
          <span class="queue-number">{{ item.number }}</span>
          <span class="queue-car">{{ item.carPlate }}</span>
          <el-tag v-if="item.isMe" size="small" type="primary">我</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  stationName: {
    type: String,
    default: '充电站'
  },
  currentNumber: {
    type: String,
    default: 'A001'
  },
  waitingCount: {
    type: Number,
    default: 0
  },
  myPosition: {
    type: Number,
    default: 0
  },
  queueList: {
    type: Array,
    default: () => []
  }
})

const currentTime = ref('')
const estimatedTime = ref('0分钟')

let timer = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const updateEstimatedTime = () => {
  if (props.myPosition > 0) {
    const minutes = props.myPosition * 15
    estimatedTime.value = minutes >= 60
      ? `${Math.floor(minutes / 60)}小时${minutes % 60}分钟`
      : `${minutes}分钟`
  } else {
    estimatedTime.value = '0分钟'
  }
}

watch(() => props.myPosition, updateEstimatedTime)

onMounted(() => {
  updateTime()
  updateEstimatedTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.queue-display {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 16px;
  padding: 30px;
  text-align: center;
}

.display-header {
  margin-bottom: 20px;
}

.station-name {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.subtitle {
  font-size: 14px;
  opacity: 0.8;
  margin: 0;
}

.current-number {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 30px 20px;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
}

.number-label {
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 8px;
}

.number-value {
  font-size: 72px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 8px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.number-time {
  font-size: 14px;
  opacity: 0.8;
}

.queue-info {
  display: flex;
  justify-content: space-around;
  margin-bottom: 24px;
}

.info-item {
  text-align: center;
}

.info-value {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 4px;
}

.info-label {
  font-size: 12px;
  opacity: 0.8;
}

.queue-list {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 16px;
  backdrop-filter: blur(10px);
}

.list-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
  text-align: left;
  opacity: 0.9;
}

.list-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.queue-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.queue-item.active {
  background: rgba(103, 194, 58, 0.3);
  border: 1px solid rgba(103, 194, 58, 0.5);
}

.queue-item.is-me {
  background: rgba(64, 158, 255, 0.3);
  border: 1px solid rgba(64, 158, 255, 0.5);
}

.queue-number {
  font-size: 16px;
  font-weight: 600;
}

.queue-car {
  font-size: 13px;
  opacity: 0.8;
}
</style>
