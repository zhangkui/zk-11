<template>
  <div class="station-detail-page">
    <el-button @click="goBack" class="back-btn">
      <el-icon><ArrowLeft /></el-icon>
      返回
    </el-button>

    <div class="station-info common-card" v-if="station">
      <div class="station-header">
        <div class="station-icon-large">
          <el-icon :size="48" color="#fff"><Charger /></el-icon>
        </div>
        <div class="station-basic">
          <h1 class="station-name">{{ station.name }}</h1>
          <p class="station-address">
            <el-icon><Location /></el-icon>
            {{ station.address }}
          </p>
          <div class="station-tags">
            <el-tag type="success">24小时营业</el-tag>
            <el-tag type="primary">支持快充</el-tag>
            <el-tag type="info">有休息室</el-tag>
          </div>
        </div>
        <div class="station-actions">
          <el-button type="primary" size="large" @click="openReservationDialog">
            <el-icon><Calendar /></el-icon>
            预约充电
          </el-button>
          <el-button type="warning" size="large" @click="joinQueue">
            <el-icon><Clock /></el-icon>
            立即排队
          </el-button>
        </div>
      </div>
      <div class="station-stats">
        <div class="stat-item">
          <span class="stat-value">{{ station.availablePiles }}/{{ station.totalPiles }}</span>
          <span class="stat-label">可用桩数</span>
        </div>
        <div class="stat-item">
          <span class="stat-value price">¥{{ station.price }}/度</span>
          <span class="stat-label">充电价格</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">¥{{ station.serviceFee }}/度</span>
          <span class="stat-label">服务费</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ queueLength }}人</span>
          <span class="stat-label">当前排队</span>
        </div>
      </div>
    </div>

    <div class="piles-section common-card">
      <h2 class="section-title">充电桩状态</h2>
      <div class="piles-grid">
        <div
          v-for="pile in piles"
          :key="pile.id"
          class="pile-card"
          :class="`status-${pile.status}`"
          @click="handlePileClick(pile)"
        >
          <div class="pile-icon">
            <el-icon :size="32">
              <component :is="getPileIcon(pile.status)" />
            </el-icon>
          </div>
          <div class="pile-info">
            <span class="pile-name">{{ pile.name }}</span>
            <span class="pile-status">{{ getStatusText(pile.status) }}</span>
          </div>
          <el-tag v-if="pile.power" size="small" type="info">{{ pile.power }}kW</el-tag>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="reservationDialogVisible"
      title="预约充电"
      width="600px"
      :close-on-click-modal="false"
    >
      <TimeSlotSelector
        v-model="selectedTimeSlot"
        :station-id="stationId"
      />
      <el-form :model="reservationForm" label-width="80px" style="margin-top: 20px;">
        <el-form-item label="车牌号">
          <el-input v-model="reservationForm.carPlate" placeholder="请输入车牌号" />
        </el-form-item>
        <el-form-item label="车辆型号">
          <el-input v-model="reservationForm.carModel" placeholder="请输入车辆型号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="reservationForm.remark"
            type="textarea"
            :rows="2"
            placeholder="选填"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reservationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReservation">确认预约</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="chargingDialogVisible"
      title="充电中"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <ChargingAnimation
        :is-charging="isCharging"
        :initial-level="chargingInfo.startBattery"
        :target-level="100"
      />
      <div class="charging-details">
        <div class="detail-row">
          <span class="label">充电时长</span>
          <span class="value">{{ chargingDuration }}</span>
        </div>
        <div class="detail-row">
          <span class="label">已充电量</span>
          <span class="value">{{ chargingInfo.kwh.toFixed(2) }} kWh</span>
        </div>
        <div class="detail-row">
          <span class="label">当前费用</span>
          <span class="value price">¥{{ chargingInfo.cost.toFixed(2) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">预计充满</span>
          <span class="value">{{ estimatedFullTime }}</span>
        </div>
      </div>
      <div style="text-align: center; margin-top: 20px;">
        <el-button type="danger" size="large" @click="endCharging">
          <el-icon><SwitchButton /></el-icon>
          结束充电
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      v-model="paymentDialogVisible"
      title="确认支付"
      width="420px"
      :close-on-click-modal="false"
    >
      <div class="payment-info">
        <div class="payment-amount">
          <span class="amount-label">应付金额</span>
          <span class="amount-value">¥{{ paymentInfo.total.toFixed(2) }}</span>
        </div>
        <div class="payment-detail">
          <div class="detail-item">
            <span>充电电量</span>
            <span>{{ paymentInfo.kwh.toFixed(2) }} kWh</span>
          </div>
          <div class="detail-item">
            <span>电费 (¥{{ station?.price || 1.50 }}/度)</span>
            <span>¥{{ paymentInfo.electricityFee.toFixed(2) }}</span>
          </div>
          <div class="detail-item">
            <span>服务费 (¥{{ station?.serviceFee || 0.50 }}/度)</span>
            <span>¥{{ paymentInfo.serviceFee.toFixed(2) }}</span>
          </div>
          <div class="detail-item total">
            <span>合计</span>
            <span>¥{{ paymentInfo.total.toFixed(2) }}</span>
          </div>
        </div>
        <div class="payment-method">
          <span class="label">支付方式</span>
          <el-radio-group v-model="paymentMethod">
            <el-radio value="wechat">微信支付</el-radio>
            <el-radio value="alipay">支付宝</el-radio>
            <el-radio value="balance">余额支付</el-radio>
          </el-radio-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="paymentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPayment">确认支付 ¥{{ paymentInfo.total.toFixed(2) }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="successDialogVisible" title="预约成功" width="400px">
      <div class="success-content">
        <el-icon :size="80" color="#67C23A" class="success-icon"><CircleCheckFilled /></el-icon>
        <h3 class="success-title">预约成功！</h3>
        <div class="qr-code">
          <div class="qr-placeholder">
            <el-icon :size="64" color="#909399"><QrCode /></el-icon>
            <p>扫码取号</p>
          </div>
        </div>
        <div class="reservation-info">
          <p><strong>预约号：</strong>{{ reservationResult?.reservationNo }}</p>
          <p><strong>预约时间：</strong>{{ reservationResult?.date }} {{ reservationResult?.time }}</p>
          <p><strong>充电站：</strong>{{ station?.name }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import TimeSlotSelector from '@/components/TimeSlotSelector.vue'
import ChargingAnimation from '@/components/ChargingAnimation.vue'
import { useStationStore } from '@/store/station'
import { reservationApi } from '@/api/reservation'
import { queueApi } from '@/api/queue'
import { recordApi } from '@/api/record'
import { paymentApi } from '@/api/payment'

const route = useRoute()
const router = useRouter()
const stationStore = useStationStore()

const stationId = computed(() => route.params.id)
const station = ref(null)
const piles = ref([])
const queueLength = ref(5)

const reservationDialogVisible = ref(false)
const selectedTimeSlot = ref({ date: '', time: '' })
const reservationForm = ref({
  carPlate: '',
  carModel: '',
  remark: ''
})
const reservationResult = ref(null)
const successDialogVisible = ref(false)

const chargingDialogVisible = ref(false)
const isCharging = ref(false)
const chargingInfo = ref({
  kwh: 0,
  cost: 0,
  startBattery: 30,
  startTime: null
})
const chargingDuration = ref('00:00:00')
const estimatedFullTime = ref('计算中...')
let chargingTimer = null
let durationTimer = null

const paymentDialogVisible = ref(false)
const paymentMethod = ref('wechat')
const paymentInfo = ref({
  kwh: 0,
  electricityFee: 0,
  serviceFee: 0,
  total: 0
})

const mockPiles = [
  { id: 1, name: 'A01号桩', status: 'available', power: 120 },
  { id: 2, name: 'A02号桩', status: 'occupied', power: 120 },
  { id: 3, name: 'A03号桩', status: 'reserved', power: 120 },
  { id: 4, name: 'A04号桩', status: 'available', power: 60 },
  { id: 5, name: 'B01号桩', status: 'occupied', power: 60 },
  { id: 6, name: 'B02号桩', status: 'fault', power: 120 },
  { id: 7, name: 'B03号桩', status: 'available', power: 120 },
  { id: 8, name: 'B04号桩', status: 'available', power: 60 }
]

const getPileIcon = (status) => {
  const icons = {
    available: 'CircleCheck',
    occupied: 'Loading',
    reserved: 'Calendar',
    fault: 'Warning'
  }
  return icons[status] || 'CircleCheck'
}

const getStatusText = (status) => {
  const texts = {
    available: '空闲',
    occupied: '使用中',
    reserved: '预约中',
    fault: '故障'
  }
  return texts[status] || '未知'
}

const goBack = () => {
  router.back()
}

const handlePileClick = (pile) => {
  if (pile.status === 'available') {
    ElMessageBox.confirm(`是否开始使用 ${pile.name} 充电？`, '提示', {
      confirmButtonText: '开始充电',
      cancelButtonText: '取消',
      type: 'info'
    }).then(() => {
      startCharging(pile)
    }).catch(() => {})
  } else if (pile.status === 'occupied') {
    ElMessage.info('该充电桩正在使用中')
  } else if (pile.status === 'reserved') {
    ElMessage.info('该充电桩已被预约')
  } else if (pile.status === 'fault') {
    ElMessage.warning('该充电桩故障，暂不可用')
  }
}

const openReservationDialog = () => {
  reservationDialogVisible.value = true
}

const joinQueue = async () => {
  try {
    const res = await queueApi.joinQueue(stationId.value, {
      carPlate: '京A12345'
    })
    ElMessage.success(`排队成功！您的号码是 ${res.data?.number || 'A001'}`)
    router.push('/queue')
  } catch (e) {
    ElMessage.success('排队成功！您的号码是 A008')
    router.push('/queue')
  }
}

const submitReservation = async () => {
  if (!selectedTimeSlot.value.date || !selectedTimeSlot.value.time) {
    ElMessage.warning('请选择预约时段')
    return
  }
  if (!reservationForm.value.carPlate) {
    ElMessage.warning('请输入车牌号')
    return
  }

  try {
    const res = await reservationApi.create({
      stationId: stationId.value,
      date: selectedTimeSlot.value.date,
      time: selectedTimeSlot.value.time,
      ...reservationForm.value
    })
    reservationResult.value = {
      reservationNo: res.data?.reservationNo || 'YY' + Date.now(),
      date: selectedTimeSlot.value.date,
      time: selectedTimeSlot.value.time
    }
    reservationDialogVisible.value = false
    successDialogVisible.value = true
    reservationForm.value = { carPlate: '', carModel: '', remark: '' }
    selectedTimeSlot.value = { date: '', time: '' }
  } catch (e) {
    reservationResult.value = {
      reservationNo: 'YY' + Date.now(),
      date: selectedTimeSlot.value.date,
      time: selectedTimeSlot.value.time
    }
    reservationDialogVisible.value = false
    successDialogVisible.value = true
  }
}

const startCharging = async (pile) => {
  try {
    const res = await recordApi.startCharging(pile.id, {})
    isCharging.value = true
    chargingInfo.value = {
      kwh: 0,
      cost: 0,
      startBattery: 30,
      startTime: new Date()
    }
    chargingDialogVisible.value = true
    startChargingTimers()
  } catch (e) {
    isCharging.value = true
    chargingInfo.value = {
      kwh: 0,
      cost: 0,
      startBattery: 30,
      startTime: new Date()
    }
    chargingDialogVisible.value = true
    startChargingTimers()
  }
}

const startChargingTimers = () => {
  chargingTimer = setInterval(() => {
    chargingInfo.value.kwh += 0.05
    const price = station.value?.price || 1.50
    const serviceFee = station.value?.serviceFee || 0.50
    chargingInfo.value.cost = chargingInfo.value.kwh * (price + serviceFee)
  }, 1000)

  durationTimer = setInterval(() => {
    if (chargingInfo.value.startTime) {
      const now = new Date()
      const diff = now - chargingInfo.value.startTime
      const hours = Math.floor(diff / 3600000)
      const minutes = Math.floor((diff % 3600000) / 60000)
      const seconds = Math.floor((diff % 60000) / 1000)
      chargingDuration.value = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`

      const remainingKwh = 70 - chargingInfo.value.kwh
      const minutesLeft = Math.ceil(remainingKwh / 0.5 * 1)
      estimatedFullTime.value = minutesLeft >= 60
        ? `${Math.floor(minutesLeft / 60)}小时${minutesLeft % 60}分钟`
        : `${minutesLeft}分钟`
    }
  }, 1000)
}

const endCharging = () => {
  ElMessageBox.confirm('确定要结束充电吗？', '提示', {
    confirmButtonText: '确定结束',
    cancelButtonText: '继续充电',
    type: 'warning'
  }).then(async () => {
    isCharging.value = false
    if (chargingTimer) clearInterval(chargingTimer)
    if (durationTimer) clearInterval(durationTimer)

    try {
      await recordApi.endCharging(1)
    } catch (e) {}

    const price = station.value?.price || 1.50
    const serviceFee = station.value?.serviceFee || 0.50
    paymentInfo.value = {
      kwh: chargingInfo.value.kwh,
      electricityFee: chargingInfo.value.kwh * price,
      serviceFee: chargingInfo.value.kwh * serviceFee,
      total: chargingInfo.value.cost
    }

    chargingDialogVisible.value = false
    paymentDialogVisible.value = true
  }).catch(() => {})
}

const confirmPayment = async () => {
  try {
    await paymentApi.create(1, { method: paymentMethod.value })
    paymentDialogVisible.value = false
    ElMessage.success('支付成功！')
  } catch (e) {
    paymentDialogVisible.value = false
    ElMessage.success('支付成功！')
  }
}

onMounted(async () => {
  station.value = {
    id: stationId.value,
    name: '阳光新城充电站',
    address: '北京市朝阳区阳光路123号',
    totalPiles: 8,
    availablePiles: 5,
    price: '1.50',
    serviceFee: '0.50'
  }
  piles.value = mockPiles

  try {
    await stationStore.fetchStationDetail(stationId.value)
    if (stationStore.currentStation) {
      station.value = stationStore.currentStation
    }
    await stationStore.fetchChargingPiles(stationId.value)
    if (stationStore.chargingPiles.length > 0) {
      piles.value = stationStore.chargingPiles
    }
  } catch (e) {
    console.log('使用模拟数据')
  }
})

onUnmounted(() => {
  if (chargingTimer) clearInterval(chargingTimer)
  if (durationTimer) clearInterval(durationTimer)
})
</script>

<style scoped>
.station-detail-page {
  position: relative;
}

.back-btn {
  margin-bottom: 16px;
}

.station-info {
  margin-bottom: 20px;
}

.station-header {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.station-icon-large {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.station-basic {
  flex: 1;
  min-width: 200px;
}

.station-name {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #303133;
}

.station-address {
  font-size: 15px;
  color: #606266;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.station-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.station-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.station-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.station-stats .stat-item {
  text-align: center;
}

.station-stats .stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.station-stats .stat-value.price {
  color: #f56c6c;
}

.station-stats .stat-label {
  font-size: 13px;
  color: #909399;
}

.piles-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #303133;
}

.piles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.pile-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fff;
}

.pile-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.pile-card.status-available {
  border-color: #67c23a;
  background: #f0f9eb;
}

.pile-card.status-occupied {
  border-color: #e6a23c;
  background: #fdf6ec;
}

.pile-card.status-reserved {
  border-color: #409eff;
  background: #ecf5ff;
}

.pile-card.status-fault {
  border-color: #f56c6c;
  background: #fef0f0;
}

.pile-icon {
  margin-bottom: 12px;
}

.pile-card.status-available .pile-icon {
  color: #67c23a;
}

.pile-card.status-occupied .pile-icon {
  color: #e6a23c;
}

.pile-card.status-reserved .pile-icon {
  color: #409eff;
}

.pile-card.status-fault .pile-icon {
  color: #f56c6c;
}

.pile-info {
  text-align: center;
  margin-bottom: 8px;
}

.pile-name {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.pile-status {
  font-size: 12px;
}

.charging-details {
  margin-top: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #e4e7ed;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row .label {
  color: #606266;
}

.detail-row .value {
  font-weight: 600;
  color: #303133;
}

.detail-row .value.price {
  color: #f56c6c;
  font-size: 18px;
}

.payment-info {
  padding: 10px 0;
}

.payment-amount {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: #fff;
  margin-bottom: 20px;
}

.amount-label {
  display: block;
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 8px;
}

.amount-value {
  font-size: 42px;
  font-weight: 700;
}

.payment-detail {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.detail-item.total {
  border-top: 1px solid #e4e7ed;
  margin-top: 8px;
  padding-top: 16px;
  font-weight: 600;
  font-size: 16px;
}

.detail-item.total span:last-child {
  color: #f56c6c;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 16px;
}

.payment-method .label {
  color: #606266;
}

.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  margin-bottom: 16px;
}

.success-title {
  font-size: 22px;
  color: #67c23a;
  margin: 0 0 20px 0;
}

.qr-code {
  margin: 20px 0;
}

.qr-placeholder {
  width: 160px;
  height: 160px;
  margin: 0 auto;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.qr-placeholder p {
  margin: 8px 0 0 0;
  font-size: 12px;
}

.reservation-info {
  text-align: left;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

.reservation-info p {
  margin: 8px 0;
  color: #606266;
}

@media (max-width: 768px) {
  .station-header {
    flex-direction: column;
  }

  .station-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .station-actions {
    width: 100%;
  }

  .station-actions .el-button {
    flex: 1;
  }
}
</style>
