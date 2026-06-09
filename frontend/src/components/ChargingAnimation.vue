<template>
  <div class="charging-animation">
    <div class="battery-container">
      <div class="battery">
        <div class="battery-cap" :style="{ height: `${batteryLevel}%` }">
          <div class="liquid"></div>
        </div>
        <div class="battery-level-text">{{ batteryLevel }}%</div>
      </div>
      <div class="battery-head"></div>
    </div>
    <div class="charging-info">
      <div class="power-info">
        <span class="power-value">{{ power }}kW</span>
        <span class="power-label">充电功率</span>
      </div>
      <div class="voltage-info">
        <span class="voltage-value">{{ voltage }}V</span>
        <span class="voltage-label">电压</span>
      </div>
      <div class="current-info">
        <span class="current-value">{{ current }}A</span>
        <span class="current-label">电流</span>
      </div>
    </div>
    <div class="electric-wave" v-if="isCharging">
      <div class="wave wave-1"></div>
      <div class="wave wave-2"></div>
      <div class="wave wave-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  isCharging: {
    type: Boolean,
    default: false
  },
  initialLevel: {
    type: Number,
    default: 30
  },
  targetLevel: {
    type: Number,
    default: 100
  }
})

const batteryLevel = ref(props.initialLevel)
const power = ref(0)
const voltage = ref(0)
const current = ref(0)

let chargingTimer = null
let dataTimer = null

const startCharging = () => {
  chargingTimer = setInterval(() => {
    if (batteryLevel.value < props.targetLevel) {
      batteryLevel.value += 0.5
      if (batteryLevel.value > props.targetLevel) {
        batteryLevel.value = props.targetLevel
      }
    }
  }, 500)

  dataTimer = setInterval(() => {
    power.value = (60 + Math.random() * 20).toFixed(1)
    voltage.value = (380 + Math.random() * 20).toFixed(0)
    current.value = (150 + Math.random() * 30).toFixed(0)
  }, 1000)
}

const stopCharging = () => {
  if (chargingTimer) {
    clearInterval(chargingTimer)
    chargingTimer = null
  }
  if (dataTimer) {
    clearInterval(dataTimer)
    dataTimer = null
  }
  power.value = 0
  voltage.value = 0
  current.value = 0
}

watch(() => props.isCharging, (val) => {
  if (val) {
    startCharging()
  } else {
    stopCharging()
  }
}, { immediate: true })

onMounted(() => {
  if (props.isCharging) {
    startCharging()
  }
})

onUnmounted(() => {
  stopCharging()
})
</script>

<style scoped>
.charging-animation {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 16px;
  overflow: hidden;
}

.battery-container {
  position: relative;
  margin-bottom: 30px;
}

.battery {
  width: 120px;
  height: 200px;
  border: 4px solid #fff;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
}

.battery-cap {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(180deg, #67c23a 0%, #4caf50 100%);
  transition: height 0.5s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.liquid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.3) 50%,
    transparent 100%);
  animation: liquidFlow 2s infinite linear;
}

@keyframes liquidFlow {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.battery-level-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  z-index: 10;
}

.battery-head {
  width: 40px;
  height: 16px;
  background: #fff;
  margin: 0 auto;
  border-radius: 4px 4px 0 0;
  margin-top: -4px;
}

.charging-info {
  display: flex;
  gap: 40px;
  color: #fff;
}

.power-info,
.voltage-info,
.current-info {
  text-align: center;
}

.power-value,
.voltage-value,
.current-value {
  display: block;
  font-size: 28px;
  font-weight: 600;
  color: #67c23a;
  margin-bottom: 4px;
}

.power-label,
.voltage-label,
.current-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.electric-wave {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.wave {
  position: absolute;
  width: 200px;
  height: 200px;
  border: 2px solid #67c23a;
  border-radius: 50%;
  opacity: 0;
  animation: waveExpand 2s infinite ease-out;
}

.wave-2 {
  animation-delay: 0.5s;
}

.wave-3 {
  animation-delay: 1s;
}

@keyframes waveExpand {
  0% {
    transform: scale(0.5);
    opacity: 0.8;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}
</style>
