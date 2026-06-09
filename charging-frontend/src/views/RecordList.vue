<template>
  <div class="container">
    <div class="page-header">
      <h2 class="page-title">充电记录</h2>
      <div>
        <el-button type="success" @click="openStartDialog" :icon="VideoPlay" :disabled="hasCurrentCharging">
          开始充电
        </el-button>
        <el-button
          v-if="hasCurrentCharging"
          type="warning"
          @click="openEndDialog"
          :icon="VideoPause"
        >
          结束充电
        </el-button>
      </div>
    </div>

    <div v-if="currentCharging" class="card current-charging-card">
      <div class="charging-header">
        <div class="charging-status">
          <el-icon :size="24" color="#67c23a" class="pulse"><Loading /></el-icon>
          <span class="charging-text">正在充电中</span>
        </div>
        <el-tag type="success" size="large">{{ currentCharging.recordNo }}</el-tag>
      </div>
      <el-row :gutter="24" style="margin-top: 20px">
        <el-col :span="6">
          <div class="info-item">
            <div class="info-label">站点名称</div>
            <div class="info-value">{{ getStationName(currentCharging.stationId) }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="info-item">
            <div class="info-label">充电桩</div>
            <div class="info-value">{{ getPileNo(currentCharging.pileId) }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="info-item">
            <div class="info-label">开始时间</div>
            <div class="info-value">{{ formatDateTime(currentCharging.startTime) }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="info-item">
            <div class="info-label">已充电量</div>
            <div class="info-value highlight">{{ currentCharging.chargedKwh || 0 }} kWh</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="search-bar">
      <el-select
        v-model="statusFilter"
        placeholder="充电状态"
        clearable
        style="width: 160px"
      >
        <el-option label="充电中" :value="0" />
        <el-option label="已完成" :value="1" />
        <el-option label="异常终止" :value="2" />
      </el-select>
      <el-button type="primary" @click="loadData" :icon="Search">
        搜索
      </el-button>
      <el-button @click="resetSearch" :icon="RefreshRight">
        重置
      </el-button>
    </div>

    <el-card class="card" shadow="hover">
      <el-table v-loading="loading" :data="recordList" stripe>
        <el-table-column prop="recordNo" label="记录编号" width="200" />
        <el-table-column label="站点名称" min-width="180">
          <template #default="{ row }">
            {{ getStationName(row.stationId) }}
          </template>
        </el-table-column>
        <el-table-column label="充电桩" width="120">
          <template #default="{ row }">
            {{ getPileNo(row.pileId) }}
          </template>
        </el-table-column>
        <el-table-column label="充电时长" width="120" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>
        <el-table-column label="充电量" width="120" align="center">
          <template #default="{ row }">
            {{ row.chargedKwh }} kWh
          </template>
        </el-table-column>
        <el-table-column label="SOC" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.startSoc !== null && row.endSoc !== null">
              {{ row.startSoc }}% → {{ row.endSoc }}%
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass('charging', row.status)]">
              {{ getStatusName('charging', row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewFee(row.id)">
              查看费用
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="startDialogVisible"
      title="开始充电"
      width="560px"
    >
      <el-form :model="startForm" :rules="startRules" ref="startFormRef" label-width="100px">
        <el-form-item label="选择站点" prop="stationId">
          <el-select
            v-model="startForm.stationId"
            placeholder="请选择站点"
            style="width: 100%"
            @change="onStationChange"
          >
            <el-option
              v-for="station in stationList"
              :key="station.id"
              :label="station.name"
              :value="station.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择充电桩" prop="pileId">
          <el-select v-model="startForm.pileId" placeholder="请选择充电桩" style="width: 100%">
            <el-option
              v-for="pile in availablePiles"
              :key="pile.id"
              :label="`${pile.pileNo} - ${getStatusName('pileType', pile.pileType)}`"
              :value="pile.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="起始SOC">
          <el-input-number v-model="startForm.startSoc" :min="0" :max="100" :step="1" suffix="%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStartCharging" :loading="submitting">
          开始充电
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="endDialogVisible"
      title="结束充电"
      width="560px"
    >
      <el-form :model="endForm" :rules="endRules" ref="endFormRef" label-width="100px">
        <el-form-item label="当前电量">
          <el-tag type="primary" size="large">
            记录编号: {{ currentCharging?.recordNo }}
          </el-tag>
        </el-form-item>
        <el-form-item label="结束SOC" prop="endSoc">
          <el-input-number v-model="endForm.endSoc" :min="0" :max="100" :step="1" suffix="%" />
        </el-form-item>
        <el-form-item label="充电量" prop="chargedKwh">
          <el-input-number v-model="endForm.chargedKwh" :min="0" :precision="2" :step="0.1" suffix="kWh" />
        </el-form-item>
        <el-form-item label="停止原因">
          <el-select v-model="endForm.status" style="width: 100%">
            <el-option label="正常完成" :value="1" />
            <el-option label="用户主动停止" :value="1" />
            <el-option label="异常终止" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="endDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEndCharging" :loading="submitting">
          确认结束
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, RefreshRight, VideoPlay, VideoPause, Loading } from '@element-plus/icons-vue'
import { recordApi, stationApi, feeApi } from '@/api'
import { formatDateTime, formatDuration, getStatusName, getStatusClass } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const startDialogVisible = ref(false)
const endDialogVisible = ref(false)
const startFormRef = ref(null)
const endFormRef = ref(null)
const recordList = ref([])
const currentCharging = ref(null)
const stationList = ref([])
const stationMap = ref({})
const statusFilter = ref(null)

const hasCurrentCharging = computed(() => !!currentCharging.value)

const availablePiles = computed(() => {
  const station = stationMap.value[startForm.value.stationId]
  return station?.piles?.filter(p => p.status === 0 || p.status === 2) || []
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const startForm = reactive({
  userId: userStore.userId,
  stationId: null,
  pileId: null,
  reservationId: null,
  queueId: null,
  startSoc: null
})

const endForm = reactive({
  recordId: null,
  endSoc: null,
  chargedKwh: null,
  stopReason: '',
  status: 1
})

const startRules = {
  stationId: [{ required: true, message: '请选择站点', trigger: 'change' }],
  pileId: [{ required: true, message: '请选择充电桩', trigger: 'change' }]
}

const endRules = {
  endSoc: [{ required: true, message: '请输入结束SOC', trigger: 'blur' }],
  chargedKwh: [{ required: true, message: '请输入充电量', trigger: 'blur' }]
}

const loadStations = async () => {
  try {
    const res = await stationApi.page({ pageNum: 1, pageSize: 100 })
    stationList.value = res.records || []
    const stationIds = stationList.value.map(s => s.id)
    for (const id of stationIds) {
      if (!stationMap.value[id]) {
        try {
          const station = await stationApi.getDetail(id)
          stationMap.value[id] = station
        } catch (e) {}
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const loadCurrentCharging = async () => {
  try {
    currentCharging.value = await recordApi.getCurrent(userStore.userId)
  } catch (e) {
    currentCharging.value = null
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await recordApi.pageByUser(
      userStore.userId,
      pagination.pageNum,
      pagination.pageSize
    )
    recordList.value = res.records || []
    if (statusFilter.value !== null) {
      recordList.value = recordList.value.filter(r => r.status === statusFilter.value)
    }
    pagination.total = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  statusFilter.value = null
  pagination.pageNum = 1
  loadData()
}

const getStationName = (id) => {
  return stationMap.value[id]?.name || '-'
}

const getPileNo = (pileId) => {
  if (!pileId) return '-'
  for (const station of Object.values(stationMap.value)) {
    const pile = station.piles?.find(p => p.id === pileId)
    if (pile) return pile.pileNo
  }
  return '-'
}

const onStationChange = () => {
  startForm.value.pileId = null
}

const openStartDialog = () => {
  startForm.value.stationId = null
  startForm.value.pileId = null
  startForm.value.startSoc = null
  startDialogVisible.value = true
}

const handleStartCharging = async () => {
  if (!startFormRef.value) return
  await startFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await recordApi.start(startForm.value)
        ElMessage.success('开始充电成功')
        startDialogVisible.value = false
        await loadCurrentCharging()
        loadData()
      } finally {
        submitting.value = false
      }
    }
  })
}

const openEndDialog = () => {
  if (!currentCharging.value) return
  endForm.value.recordId = currentCharging.value.id
  endForm.value.endSoc = 100
  endForm.value.chargedKwh = null
  endForm.value.status = 1
  endDialogVisible.value = true
}

const handleEndCharging = async () => {
  if (!endFormRef.value) return
  await endFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await recordApi.end(endForm.value)
        ElMessage.success('结束充电成功，已生成费用账单')
        endDialogVisible.value = false
        await loadCurrentCharging()
        loadData()
      } finally {
        submitting.value = false
      }
    }
  })
}

const viewFee = async (recordId) => {
  try {
    await feeApi.getByRecordId(recordId)
    router.push('/fee')
  } catch (e) {
    ElMessage.warning('暂无费用记录')
  }
}

onMounted(() => {
  loadStations()
  loadCurrentCharging()
  loadData()
})
</script>

<style lang="scss" scoped>
.current-charging-card {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  border: 1px solid #67c23a;

  .charging-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .charging-status {
    display: flex;
    align-items: center;
    gap: 10px;

    .charging-text {
      font-size: 18px;
      font-weight: 600;
      color: #67c23a;
    }
  }

  .pulse {
    animation: pulse 2s infinite;
  }

  .info-item {
    .info-label {
      font-size: 12px;
      color: #909399;
      margin-bottom: 4px;
    }

    .info-value {
      font-size: 16px;
      font-weight: 600;
      color: #303133;

      &.highlight {
        color: #67c23a;
        font-size: 20px;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
