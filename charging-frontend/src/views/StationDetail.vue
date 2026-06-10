<template>
  <div class="container">
    <div class="page-header">
      <div>
        <el-button @click="goBack" :icon="ArrowLeft" style="margin-right: 12px">
          返回
        </el-button>
        <h2 class="page-title" style="display: inline">站点详情</h2>
      </div>
      <div v-if="!isAdmin">
        <el-tooltip
          v-if="station?.status !== 1"
          content="站点已停用，无法使用"
          placement="bottom"
        >
          <el-button type="primary" disabled :icon="List">
            加入排队
          </el-button>
        </el-tooltip>
        <el-tooltip
          v-if="station?.status !== 1"
          content="站点已停用，无法预约"
          placement="bottom"
        >
          <el-button type="success" disabled :icon="Calendar">
            立即预约
          </el-button>
        </el-tooltip>
        <template v-else>
          <el-button type="primary" @click="openQueueDialog" :icon="List">
            加入排队
          </el-button>
          <el-tooltip
            v-if="availablePiles.length === 0"
            content="暂无空闲充电桩"
            placement="bottom"
          >
            <el-button type="success" disabled :icon="Calendar">
              立即预约
            </el-button>
          </el-tooltip>
          <el-button
            v-else
            type="success"
            @click="openReservationDialog"
            :icon="Calendar"
          >
            立即预约
          </el-button>
        </template>
      </div>
    </div>

    <div v-loading="loading">
      <el-card class="card">
        <div class="station-header">
          <div>
            <h3>{{ station?.name }}</h3>
            <p class="station-address">
              <el-icon><Location /></el-icon>
              {{ station?.address }}
            </p>
          </div>
          <div class="station-stats">
            <div class="stat-item">
              <div class="stat-value">{{ station?.availablePiles }}</div>
              <div class="stat-label">可用充电桩</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ station?.totalPiles }}</div>
              <div class="stat-label">充电桩总数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ queueCount }}</div>
              <div class="stat-label">当前排队</div>
            </div>
          </div>
        </div>

        <el-divider />

        <div class="station-info">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="营业时间">
              {{ station?.openingHours || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ station?.phone || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <span :class="['status-tag', getStatusClass('station', station?.status)]">
                {{ getStatusName('station', station?.status) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="3">
              {{ station?.description || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <el-card class="card">
        <template #header>
          <div class="card-header">
            <span>充电桩状态</span>
            <div class="pile-legend">
              <span class="legend-item"><span class="legend-dot idle"></span>空闲</span>
              <span class="legend-item"><span class="legend-dot in-use"></span>使用中</span>
              <span class="legend-item"><span class="legend-dot reserved"></span>预约中</span>
              <span class="legend-item"><span class="legend-dot fault"></span>故障</span>
              <span class="legend-item"><span class="legend-dot maintenance"></span>维护中</span>
            </div>
          </div>
        </template>
        <div class="pile-grid">
          <div
            v-for="pile in station?.piles"
            :key="pile.id"
            :class="[
              'pile-item',
              getPileStatusClass(pile.status),
              { selected: selectedPile?.id === pile.id, disabled: !canReservePile(pile) }
            ]"
            @click="selectPile(pile)"
          >
            <el-tooltip
              v-if="!canReservePile(pile)"
              :content="getPileReserveTip(pile)"
              placement="top"
            >
              <div class="pile-content">
                <div class="pile-no">{{ pile.pileNo }}</div>
                <div class="pile-type">
                  {{ getStatusName('pileType', pile.pileType) }} · {{ pile.powerRating }}kW
                </div>
                <div :class="['pile-status', getPileStatusClass(pile.status)]">
                  {{ getStatusName('pile', pile.status) }}
                </div>
                <div v-if="station?.status !== 1" class="pile-disable-tip">
                  站点停用
                </div>
                <div v-else-if="pile.status === 1" class="pile-disable-tip">
                  已占用
                </div>
                <div v-else-if="pile.status === 2" class="pile-disable-tip">
                  已预约
                </div>
                <div v-else-if="pile.status === 3" class="pile-disable-tip">
                  故障
                </div>
                <div v-else-if="pile.status === 4" class="pile-disable-tip">
                  维护中
                </div>
              </div>
            </el-tooltip>
            <div v-else class="pile-content">
              <div class="pile-no">{{ pile.pileNo }}</div>
              <div class="pile-type">
                {{ getStatusName('pileType', pile.pileType) }} · {{ pile.powerRating }}kW
              </div>
              <div :class="['pile-status', getPileStatusClass(pile.status)]">
                {{ getStatusName('pile', pile.status) }}
              </div>
              <div class="pile-enable-tip">可预约</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog
      v-model="queueDialogVisible"
      title="加入排队"
      width="480px"
    >
      <el-form :model="queueForm" label-width="100px">
        <el-form-item label="站点名称">
          <span>{{ station?.name }}</span>
        </el-form-item>
        <el-form-item label="充电桩类型">
          <el-radio-group v-model="queueForm.pileType">
            <el-radio :value="1">快充</el-radio>
            <el-radio :value="2">慢充</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="queueForm.remark"
            type="textarea"
            :rows="2"
            placeholder="可选填备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="queueDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleJoinQueue" :loading="submitting">
          确认排队
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="reservationDialogVisible"
      title="预约充电"
      width="560px"
    >
      <el-form :model="reservationForm" label-width="100px">
        <el-form-item label="站点名称">
          <span>{{ station?.name }}</span>
        </el-form-item>
        <el-form-item label="选择充电桩">
          <el-select v-model="reservationForm.pileId" placeholder="请选择充电桩" style="width: 100%">
            <el-option
              v-for="pile in availablePiles"
              :key="pile.id"
              :label="`${pile.pileNo} - ${getStatusName('pileType', pile.pileType)} - ${pile.powerRating}kW`"
              :value="pile.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预约时段">
          <el-date-picker
            v-model="reservationTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="reservationForm.remark"
            type="textarea"
            :rows="2"
            placeholder="可选填备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reservationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateReservation" :loading="submitting">
          确认预约
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, List, Calendar, Location } from '@element-plus/icons-vue'
import { stationApi, queueApi, reservationApi } from '@/api'
import { formatDateTime, getStatusName, getStatusClass } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const station = ref(null)
const selectedPile = ref(null)
const queueDialogVisible = ref(false)
const reservationDialogVisible = ref(false)
const reservationTimeRange = ref([])

const queueForm = reactive({
  userId: userStore.userId,
  stationId: route.params.id,
  pileType: 1,
  remark: ''
})

const reservationForm = reactive({
  userId: userStore.userId,
  stationId: route.params.id,
  pileId: null,
  reserveStartTime: null,
  reserveEndTime: null,
  remark: ''
})

const isAdmin = computed(() => userStore.isAdmin)

const queueCount = computed(() => station.value?.queueCount || 0)

const availablePiles = computed(() => {
  return station.value?.piles?.filter(p => p.status === 0) || []
})

const getPileStatusClass = (status) => {
  const map = { 0: 'idle', 1: 'in-use', 2: 'reserved', 3: 'fault', 4: 'maintenance' }
  return map[status] || ''
}

const canReservePile = (pile) => {
  if (!pile) return false
  if (station.value?.status !== 1) return false
  return pile.status === 0
}

const getPileReserveTip = (pile) => {
  if (!pile) return ''
  if (station.value?.status !== 1) return '站点已停用，无法预约'
  if (pile.status === 1) return '充电桩已占用，无法预约'
  if (pile.status === 2) return '充电桩已被预约，无法预约'
  if (pile.status === 3) return '充电桩故障，无法预约'
  if (pile.status === 4) return '充电桩维护中，无法预约'
  return ''
}

const selectPile = (pile) => {
  if (!canReservePile(pile)) {
    return
  }
  selectedPile.value = pile
  if (reservationDialogVisible.value) {
    reservationForm.pileId = pile.id
  }
}

const goBack = () => {
  router.back()
}

const loadDetail = async () => {
  loading.value = true
  try {
    station.value = await stationApi.getDetail(route.params.id)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openQueueDialog = () => {
  queueForm.pileType = 1
  queueForm.remark = ''
  queueDialogVisible.value = true
}

const handleJoinQueue = async () => {
  submitting.value = true
  try {
    const res = await queueApi.join(queueForm)
    ElMessage.success(`排队成功，您的排队号：${res.queueNumber}`)
    queueDialogVisible.value = false
    router.push('/queue')
  } finally {
    submitting.value = false
  }
}

const openReservationDialog = () => {
  if (station.value?.status !== 1) {
    ElMessage.warning('站点已停用，无法预约')
    return
  }
  if (availablePiles.value.length === 0) {
    ElMessage.warning('暂无空闲充电桩')
    return
  }
  reservationForm.pileId = selectedPile.value?.id || null
  reservationTimeRange.value = []
  reservationForm.remark = ''
  reservationDialogVisible.value = true
}

const handleCreateReservation = async () => {
  if (!reservationForm.pileId) {
    ElMessage.warning('请选择充电桩')
    return
  }
  if (!reservationTimeRange.value || reservationTimeRange.value.length < 2) {
    ElMessage.warning('请选择预约时段')
    return
  }
  submitting.value = true
  try {
    reservationForm.reserveStartTime = reservationTimeRange.value[0]
    reservationForm.reserveEndTime = reservationTimeRange.value[1]
    const res = await reservationApi.create(reservationForm)
    ElMessage.success(`预约成功，预约单号：${res.reservationNo}`)
    reservationDialogVisible.value = false
    router.push('/reservation')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.station-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;

  h3 {
    font-size: 24px;
    margin-bottom: 8px;
    color: #303133;
  }

  .station-address {
    color: #606266;
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.station-stats {
  display: flex;
  gap: 40px;

  .stat-item {
    text-align: center;

    .stat-value {
      font-size: 32px;
      font-weight: bold;
      color: #409eff;
    }

    .stat-label {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pile-legend {
  display: flex;
  gap: 16px;

  .legend-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #606266;

    .legend-dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      display: inline-block;

      &.idle { background: #67c23a; }
      &.in-use { background: #e6a23c; }
      &.reserved { background: #409eff; }
      &.fault { background: #f56c6c; }
      &.maintenance { background: #909399; }
    }
  }
}

.pile-no {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.pile-type {
  font-size: 12px;
  color: #606266;
  margin-bottom: 8px;
}

.pile-status {
  font-size: 12px;
  font-weight: 500;

  &.idle { color: #67c23a; }
  &.in-use { color: #e6a23c; }
  &.reserved { color: #409eff; }
  &.fault { color: #f56c6c; }
  &.maintenance { color: #909399; }
}

.pile-item {
  position: relative;
  padding: 16px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;

  &:hover {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

  &.selected {
    border-color: #409eff;
    background: #ecf5ff;
  }

  &.disabled {
    cursor: not-allowed;
    opacity: 0.7;
    background: #f5f7fa;

    &:hover {
      box-shadow: none;
    }
  }

  &.in-use {
    border-color: #faecd8;
  }

  &.reserved {
    border-color: #d9ecff;
  }

  &.fault {
    border-color: #fde2e2;
  }

  &.maintenance {
    border-color: #e9e9eb;
  }
}

.pile-content {
  width: 100%;
  height: 100%;
}

.pile-disable-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #f56c6c;
  font-weight: 500;
}

.pile-enable-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #67c23a;
  font-weight: 500;
}
</style>
