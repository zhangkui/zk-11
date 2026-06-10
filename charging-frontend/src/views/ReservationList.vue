<template>
  <div class="container">
    <div class="page-header">
      <h2 class="page-title">预约管理</h2>
    </div>

    <div class="search-bar">
      <el-select
        v-model="statusFilter"
        placeholder="预约状态"
        clearable
        style="width: 160px"
      >
        <el-option label="待确认" :value="0" />
        <el-option label="已确认" :value="1" />
        <el-option label="进行中" :value="2" />
        <el-option label="已完成" :value="3" />
        <el-option label="已取消" :value="4" />
        <el-option label="已超时" :value="5" />
      </el-select>
      <el-button type="primary" @click="loadData" :icon="Search">
        搜索
      </el-button>
      <el-button @click="resetSearch" :icon="RefreshRight">
        重置
      </el-button>
    </div>

    <el-card class="card" shadow="hover">
      <el-table v-loading="loading" :data="reservationList" stripe>
        <el-table-column prop="reservationNo" label="预约单号" width="200" />
        <el-table-column label="站点名称" min-width="180">
          <template #default="{ row }">
            {{ getStationName(row.stationId) }}
          </template>
        </el-table-column>
        <el-table-column label="充电桩" width="160">
          <template #default="{ row }">
            {{ getPileNo(row.pileId) }}
          </template>
        </el-table-column>
        <el-table-column label="预约时段" min-width="320">
          <template #default="{ row }">
            <div>
              <div>开始: {{ formatDateTime(row.reserveStartTime) }}</div>
              <div>结束: {{ formatDateTime(row.reserveEndTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass('reservation', row.status)]">
              {{ getStatusName('reservation', row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleViewDetail(row)"
            >
              查看详情
            </el-button>
            <template v-if="!isAdmin">
              <el-button
                v-if="row.status === 1"
                type="success"
                link
                @click="handleConfirmArrive(row.id)"
              >
                确认到店
              </el-button>
              <el-button
                v-if="row.status === 0 || row.status === 1"
                type="danger"
                link
                @click="handleCancel(row)"
              >
                取消预约
              </el-button>
            </template>
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
      v-model="cancelDialogVisible"
      title="取消预约"
      width="480px"
    >
      <el-form label-width="100px">
        <el-form-item label="取消原因">
          <el-input
            v-model="cancelReason"
            type="textarea"
            :rows="3"
            placeholder="请输入取消原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmCancel" :loading="submitting">
          确认取消
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailDialogVisible"
      title="预约详情"
      width="600px"
    >
      <el-descriptions v-if="currentDetail" :column="2" border>
        <el-descriptions-item label="预约单号" :span="2">
          {{ currentDetail.reservationNo }}
        </el-descriptions-item>
        <el-descriptions-item label="站点名称">
          {{ getStationName(currentDetail.stationId) }}
        </el-descriptions-item>
        <el-descriptions-item label="充电桩">
          {{ getPileNo(currentDetail.pileId) }}
        </el-descriptions-item>
        <el-descriptions-item label="预约状态">
          <span :class="['status-tag', getStatusClass('reservation', currentDetail.status)]">
            {{ getStatusName('reservation', currentDetail.status) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(currentDetail.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="预约开始时间" :span="2">
          {{ formatDateTime(currentDetail.reserveStartTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="预约结束时间" :span="2">
          {{ formatDateTime(currentDetail.reserveEndTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="到店时间" :span="2">
          {{ formatDateTime(currentDetail.arriveTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="取消时间" :span="2">
          {{ formatDateTime(currentDetail.cancelTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="取消原因" :span="2">
          {{ currentDetail.cancelReason || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentDetail.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailDialogVisible = false">
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { reservationApi, stationApi } from '@/api'
import { formatDateTime, getStatusName, getStatusClass } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const isAdmin = computed(() => userStore.isAdmin)

const loading = ref(false)
const submitting = ref(false)
const cancelDialogVisible = ref(false)
const cancelReason = ref('')
const currentCancelId = ref(null)
const reservationList = ref([])
const stationMap = ref({})
const detailDialogVisible = ref(false)
const currentDetail = ref(null)

const statusFilter = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await reservationApi.pageByUser(
      userStore.userId,
      pagination.pageNum,
      pagination.pageSize
    )
    reservationList.value = res.records || []
    if (statusFilter.value !== null) {
      reservationList.value = reservationList.value.filter(r => r.status === statusFilter.value)
    }
    pagination.total = res.total || 0

    const stationIds = [...new Set(reservationList.value.map(r => r.stationId))]
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

const handleConfirmArrive = async (id) => {
  try {
    await reservationApi.confirmArrive(id)
    ElMessage.success('确认到店成功')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleViewDetail = async (row) => {
  try {
    const detail = await reservationApi.getDetail(row.id)
    currentDetail.value = detail
    detailDialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const handleCancel = (row) => {
  currentCancelId.value = row.id
  cancelReason.value = ''
  cancelDialogVisible.value = true
}

const confirmCancel = async () => {
  if (!cancelReason.value) {
    ElMessage.warning('请输入取消原因')
    return
  }
  submitting.value = true
  try {
    await reservationApi.cancel({
      id: currentCancelId.value,
      cancelReason: cancelReason.value
    })
    ElMessage.success('取消预约成功')
    cancelDialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
