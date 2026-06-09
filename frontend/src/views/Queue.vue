<template>
  <div class="queue-page">
    <h2 class="page-title">排队叫号</h2>

    <div class="station-selector common-card">
      <label class="label">选择充电站</label>
      <el-select v-model="selectedStation" placeholder="请选择充电站" @change="handleStationChange">
        <el-option
          v-for="station in stations"
          :key="station.id"
          :label="station.name"
          :value="station.id"
        />
      </el-select>
    </div>

    <div class="queue-content">
      <QueueDisplay
        :station-name="currentStation?.name || '充电站'"
        :current-number="currentNumber"
        :waiting-count="queueList.length"
        :my-position="myPosition"
        :queue-list="queueList"
      />

      <div class="my-queue-card common-card" v-if="isInQueue">
        <div class="card-header">
          <h3 class="title">我的排队信息</h3>
          <el-tag type="primary" effect="light" size="large">排队中</el-tag>
        </div>
        <div class="card-body">
          <div class="my-number">
            <span class="label">我的号码</span>
            <span class="number">{{ myNumber }}</span>
          </div>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">排队位置</span>
              <span class="value">第 {{ myPosition }} 位</span>
            </div>
            <div class="info-item">
              <span class="label">等待人数</span>
              <span class="value">{{ myPosition - 1 }} 人</span>
            </div>
            <div class="info-item">
              <span class="label">预计等待</span>
              <span class="value">{{ estimatedWaitTime }}</span>
            </div>
            <div class="info-item">
              <span class="label">排队时间</span>
              <span class="value">{{ joinTime }}</span>
            </div>
          </div>
        </div>
        <div class="card-footer">
          <el-button type="danger" @click="leaveQueue">
            <el-icon><Close /></el-icon>
            离开队列
          </el-button>
        </div>
      </div>

      <div class="join-queue-card common-card" v-else>
        <div class="join-content">
          <el-icon :size="64" color="#909399"><Clock /></el-icon>
          <h3 class="title">当前不在队列中</h3>
          <p class="desc">选择充电站后，点击下方按钮加入排队</p>
          <el-button type="primary" size="large" :disabled="!selectedStation" @click="joinQueue">
            <el-icon><Plus /></el-icon>
            加入排队
          </el-button>
        </div>
      </div>

      <div class="queue-history common-card">
        <h3 class="section-title">排队历史</h3>
        <el-table :data="queueHistory" style="width: 100%">
          <el-table-column prop="stationName" label="充电站" />
          <el-table-column prop="number" label="号码" />
          <el-table-column prop="joinTime" label="排队时间" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="row.status === 'completed' ? 'success' : 'info'">
                {{ row.status === 'completed' ? '已完成' : '已离开' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog
      v-model="joinDialogVisible"
      title="加入排队"
      width="420px"
    >
      <el-form :model="joinForm" label-width="80px">
        <el-form-item label="充电站">
          <span>{{ currentStation?.name }}</span>
        </el-form-item>
        <el-form-item label="车牌号" required>
          <el-input v-model="joinForm.carPlate" placeholder="请输入车牌号" />
        </el-form-item>
        <el-form-item label="车辆型号">
          <el-input v-model="joinForm.carModel" placeholder="请输入车辆型号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="joinDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmJoinQueue">确认排队</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import QueueDisplay from '@/components/QueueDisplay.vue'
import { queueApi } from '@/api/queue'

const selectedStation = ref(null)
const stations = ref([
  { id: 1, name: '阳光新城充电站' },
  { id: 2, name: '绿能科技园充电站' },
  { id: 3, name: '万达广场充电站' },
  { id: 4, name: '奥体中心充电站' }
])

const currentStation = computed(() =>
  stations.value.find(s => s.id === selectedStation.value)
)

const currentNumber = ref('A005')
const isInQueue = ref(false)
const myNumber = ref('A008')
const myPosition = ref(3)
const joinTime = ref('14:30:00')

const queueList = ref([
  { id: 1, number: 'A005', carPlate: '京B67890', isMe: false },
  { id: 2, number: 'A006', carPlate: '京C12345', isMe: false },
  { id: 3, number: 'A007', carPlate: '京D67890', isMe: false },
  { id: 4, number: 'A008', carPlate: '京A12345', isMe: true },
  { id: 5, number: 'A009', carPlate: '京E12345', isMe: false },
  { id: 6, number: 'A010', carPlate: '京F67890', isMe: false }
])

const queueHistory = ref([
  {
    id: 1,
    stationName: '阳光新城充电站',
    number: 'A003',
    joinTime: '2024-01-14 10:30:00',
    status: 'completed'
  },
  {
    id: 2,
    stationName: '绿能科技园充电站',
    number: 'B012',
    joinTime: '2024-01-13 15:20:00',
    status: 'completed'
  },
  {
    id: 3,
    stationName: '奥体中心充电站',
    number: 'C008',
    joinTime: '2024-01-12 09:15:00',
    status: 'left'
  }
])

const joinDialogVisible = ref(false)
const joinForm = ref({
  carPlate: '',
  carModel: ''
})

const estimatedWaitTime = computed(() => {
  const minutes = (myPosition.value - 1) * 15
  return minutes >= 60
    ? `${Math.floor(minutes / 60)}小时${minutes % 60}分钟`
    : `${minutes}分钟`
})

const handleStationChange = () => {
  refreshQueueData()
}

const refreshQueueData = async () => {
  if (!selectedStation.value) return

  try {
    const res = await queueApi.getCurrentNumber(selectedStation.value)
    if (res.data?.currentNumber) {
      currentNumber.value = res.data.currentNumber
    }
    const queueRes = await queueApi.getQueueList(selectedStation.value)
    if (queueRes.data) {
      queueList.value = queueRes.data
    }
    const myRes = await queueApi.getMyQueueStatus(selectedStation.value)
    if (myRes.data) {
      isInQueue.value = true
      myNumber.value = myRes.data.number
      myPosition.value = myRes.data.position
    } else {
      isInQueue.value = false
    }
  } catch (e) {
    console.log('使用模拟数据')
  }
}

const joinQueue = () => {
  joinForm.value = { carPlate: '京A12345', carModel: '' }
  joinDialogVisible.value = true
}

const confirmJoinQueue = async () => {
  if (!joinForm.value.carPlate) {
    ElMessage.warning('请输入车牌号')
    return
  }

  try {
    const res = await queueApi.joinQueue(selectedStation.value, joinForm.value)
    myNumber.value = res.data?.number || 'A008'
    myPosition.value = queueList.value.length + 1
    joinTime.value = new Date().toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
    queueList.value.push({
      id: Date.now(),
      number: myNumber.value,
      carPlate: joinForm.value.carPlate,
      isMe: true
    })
    isInQueue.value = true
    joinDialogVisible.value = false
    ElMessage.success(`排队成功！您的号码是 ${myNumber.value}`)
  } catch (e) {
    myNumber.value = 'A008'
    myPosition.value = queueList.value.length + 1
    joinTime.value = new Date().toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
    queueList.value.push({
      id: Date.now(),
      number: myNumber.value,
      carPlate: joinForm.value.carPlate,
      isMe: true
    })
    isInQueue.value = true
    joinDialogVisible.value = false
    ElMessage.success(`排队成功！您的号码是 ${myNumber.value}`)
  }
}

const leaveQueue = () => {
  ElMessageBox.confirm('确定要离开排队队列吗？', '提示', {
    confirmButtonText: '确定离开',
    cancelButtonText: '继续等待',
    type: 'warning'
  }).then(async () => {
    try {
      await queueApi.leaveQueue(selectedStation.value)
    } catch (e) {}

    const myIndex = queueList.value.findIndex(item => item.isMe)
    if (myIndex > -1) {
      queueList.value.splice(myIndex, 1)
    }

    isInQueue.value = false
    ElMessage.success('已离开队列')
  }).catch(() => {})
}

let refreshTimer = null

onMounted(() => {
  if (stations.value.length > 0) {
    selectedStation.value = stations.value[0].id
    refreshQueueData()
  }

  refreshTimer = setInterval(() => {
    if (Math.random() > 0.7 && queueList.value.length > 0) {
      const calledNumber = queueList.value.shift()
      if (calledNumber) {
        currentNumber.value = calledNumber.number
        if (calledNumber.isMe) {
          myPosition.value = 1
        } else if (myPosition.value > 1) {
          myPosition.value--
        }
      }
    }
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.queue-page {
  padding: 0;
}

.station-selector {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.station-selector .label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.station-selector .el-select {
  width: 300px;
}

.queue-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.my-queue-card {
  grid-column: span 1;
}

.my-queue-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.my-queue-card .title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.my-number {
  text-align: center;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: #fff;
}

.my-number .label {
  display: block;
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 8px;
}

.my-number .number {
  font-size: 48px;
  font-weight: 700;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.info-item {
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.info-item .label {
  display: block;
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.info-item .value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
}

.join-queue-card {
  grid-column: span 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

.join-content {
  text-align: center;
}

.join-content .title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 16px 0 8px 0;
}

.join-content .desc {
  color: #909399;
  margin: 0 0 24px 0;
}

.queue-history {
  grid-column: span 2;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #303133;
}

@media (max-width: 992px) {
  .queue-content {
    grid-template-columns: 1fr;
  }

  .my-queue-card,
  .join-queue-card,
  .queue-history {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .station-selector {
    flex-direction: column;
    align-items: flex-start;
  }

  .station-selector .el-select {
    width: 100%;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
