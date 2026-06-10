<template>
  <div class="container">
    <div class="page-header">
      <h2 class="page-title">排队叫号</h2>
      <div v-if="isAdmin">
        <el-select
          v-model="selectedStationId"
          placeholder="选择站点"
          style="width: 240px; margin-right: 12px"
          @change="loadCurrentQueue"
        >
          <el-option
            v-for="station in stationList"
            :key="station.id"
            :label="station.name"
            :value="station.id"
          />
        </el-select>
        <el-button type="primary" @click="handleCallNext" :icon="Bell" :disabled="!selectedStationId">
          叫号
        </el-button>
      </div>
    </div>

    <div v-if="isAdmin && currentQueue" class="queue-display">
      <div class="queue-label">当前叫号</div>
      <div class="queue-number">{{ String(currentQueue.queueNumber).padStart(3, '0') }}</div>
      <div class="queue-label">{{ currentQueue.stationName }}</div>
      <div class="queue-info">
        <div class="info-item">
          <div class="info-value">{{ currentQueue.pileTypeName }}</div>
          <div class="info-label">充电类型</div>
        </div>
        <div class="info-item">
          <div class="info-value">{{ formatDateTime(currentQueue.calledTime) }}</div>
          <div class="info-label">叫号时间</div>
        </div>
        <div class="info-item">
          <div class="info-value">{{ formatDateTime(currentQueue.expireTime) }}</div>
          <div class="info-label">过期时间</div>
        </div>
      </div>
    </div>

    <div class="search-bar">
      <el-select
        v-model="statusFilter"
        placeholder="排队状态"
        clearable
        style="width: 160px"
      >
        <el-option label="排队中" :value="0" />
        <el-option label="叫号中" :value="1" />
        <el-option label="已使用" :value="2" />
        <el-option label="已取消" :value="3" />
        <el-option label="已超时" :value="4" />
      </el-select>
      <el-button type="primary" @click="loadData" :icon="Search">
        搜索
      </el-button>
      <el-button @click="resetSearch" :icon="RefreshRight">
        重置
      </el-button>
    </div>

    <el-card class="card" shadow="hover">
      <el-table v-loading="loading" :data="queueList" stripe>
        <el-table-column prop="queueNo" label="排队单号" width="200" />
        <el-table-column label="排队号" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="large">
              {{ String(row.queueNumber).padStart(3, '0') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stationName" label="站点名称" min-width="180" />
        <el-table-column label="期望类型" width="100" align="center">
          <template #default="{ row }">
            {{ row.pileTypeName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="前面等待" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 0" class="text-warning">
              {{ row.aheadCount }} 人
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="预计等待" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 0">
              {{ row.estimatedWaitTime }} 分钟
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass('queue', row.status)]">
              {{ getStatusName('queue', row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <template v-if="!isAdmin">
              <el-button
                v-if="row.status === 0 || row.status === 1"
                type="danger"
                link
                @click="handleCancel(row.id)"
              >
                取消排队
              </el-button>
            </template>
            <span v-else>-</span>
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Bell } from '@element-plus/icons-vue'
import { queueApi, stationApi } from '@/api'
import { formatDateTime, getStatusName, getStatusClass } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const isAdmin = computed(() => userStore.isAdmin)

const loading = ref(false)
const queueList = ref([])
const stationList = ref([])
const selectedStationId = ref(null)
const currentQueue = ref(null)
const statusFilter = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const loadStations = async () => {
  try {
    const res = await stationApi.page({ pageNum: 1, pageSize: 100 })
    stationList.value = res.records || []
    if (stationList.value.length > 0) {
      selectedStationId.value = stationList.value[0].id
      loadCurrentQueue()
    }
  } catch (e) {
    console.error(e)
  }
}

const loadCurrentQueue = async () => {
  if (!selectedStationId.value) return
  try {
    currentQueue.value = null
  } catch (e) {
    console.error(e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await queueApi.pageByUser(
      userStore.userId,
      pagination.pageNum,
      pagination.pageSize
    )
    queueList.value = res.records || []
    if (statusFilter.value !== null) {
      queueList.value = queueList.value.filter(q => q.status === statusFilter.value)
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

const handleCallNext = async () => {
  if (!selectedStationId.value) return
  ElMessageBox.confirm('确定要叫下一位吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      const res = await queueApi.callNext(selectedStationId.value)
      currentQueue.value = res
      ElMessage.success(`已叫号: ${String(res.queueNumber).padStart(3, '0')}`)
    } catch (e) {
      console.error(e)
    }
  })
}

const handleCancel = (id) => {
  ElMessageBox.confirm('确定要取消排队吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await queueApi.cancel(id)
      ElMessage.success('取消排队成功')
      loadData()
    } catch (e) {
      console.error(e)
    }
  })
}

onMounted(() => {
  loadStations()
  loadData()
})
</script>

<style lang="scss" scoped>
.text-warning {
  color: #e6a23c;
  font-weight: 600;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
