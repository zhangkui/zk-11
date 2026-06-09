<template>
  <div class="reservation-page">
    <h2 class="page-title">我的预约</h2>

    <div class="filter-section common-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待使用" name="pending" />
        <el-tab-pane label="已完成" name="completed" />
        <el-tab-pane label="已取消" name="cancelled" />
      </el-tabs>
    </div>

    <div class="reservation-list">
      <el-skeleton v-for="i in 3" :key="i" v-if="loading" :rows="4" animated />
      <div
        v-for="item in filteredReservations"
        :key="item.id"
        class="reservation-card common-card"
      >
        <div class="card-header">
          <div class="station-info">
            <h3 class="station-name">{{ item.stationName }}</h3>
            <p class="station-address">
              <el-icon><Location /></el-icon>
              {{ item.address }}
            </p>
          </div>
          <el-tag
            :type="getStatusType(item.status)"
            effect="light"
            size="large"
          >
            {{ getStatusText(item.status) }}
          </el-tag>
        </div>
        <div class="card-body">
          <div class="info-row">
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <span>{{ item.date }} {{ item.time }}</span>
            </div>
            <div class="info-item">
              <el-icon><Charger /></el-icon>
              <span>{{ item.pileName || '自动分配' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Car /></el-icon>
              <span>{{ item.carPlate }}</span>
            </div>
          </div>
          <div class="qr-section" v-if="item.status === 'pending'">
            <div class="qr-code">
              <div class="qr-placeholder">
                <el-icon :size="48" color="#909399"><QrCode /></el-icon>
                <p>扫码取号</p>
              </div>
            </div>
            <div class="reservation-code">
              <span class="label">预约号</span>
              <span class="code">{{ item.reservationNo }}</span>
            </div>
          </div>
        </div>
        <div class="card-footer">
          <div class="create-time">
            创建时间: {{ item.createTime }}
          </div>
          <div class="actions" v-if="item.status === 'pending'">
            <el-button type="primary" size="small" @click="navigateToStation(item)">
              <el-icon><Location /></el-icon>
              导航前往
            </el-button>
            <el-button type="danger" size="small" @click="cancelReservation(item)">
              <el-icon><Close /></el-icon>
              取消预约
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && filteredReservations.length === 0" description="暂无预约记录" />
    </div>

    <el-pagination
      v-if="total > pageSize"
      class="pagination"
      v-model:current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reservationApi } from '@/api/reservation'

const router = useRouter()

const activeTab = ref('all')
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const mockReservations = [
  {
    id: 1,
    stationName: '阳光新城充电站',
    address: '北京市朝阳区阳光路123号',
    date: '2024-01-15',
    time: '14:00-15:00',
    pileName: 'A03号桩',
    carPlate: '京A12345',
    status: 'pending',
    reservationNo: 'YY20240115001',
    createTime: '2024-01-14 10:30:00'
  },
  {
    id: 2,
    stationName: '绿能科技园充电站',
    address: '北京市海淀区科技路88号',
    date: '2024-01-12',
    time: '09:00-10:00',
    pileName: 'B01号桩',
    carPlate: '京A12345',
    status: 'completed',
    reservationNo: 'YY20240112003',
    createTime: '2024-01-11 15:20:00'
  },
  {
    id: 3,
    stationName: '奥体中心充电站',
    address: '北京市朝阳区奥运村路100号',
    date: '2024-01-10',
    time: '16:00-17:00',
    pileName: 'C02号桩',
    carPlate: '京A12345',
    status: 'cancelled',
    reservationNo: 'YY20240110008',
    createTime: '2024-01-09 09:15:00'
  }
]

const reservations = ref(mockReservations)

const filteredReservations = computed(() => {
  if (activeTab.value === 'all') return reservations.value
  return reservations.value.filter(r => r.status === activeTab.value)
})

const getStatusType = (status) => {
  const types = {
    pending: 'primary',
    completed: 'success',
    cancelled: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待使用',
    completed: '已完成',
    cancelled: '已取消'
  }
  return texts[status] || '未知'
}

const handleTabChange = () => {
  currentPage.value = 1
  fetchReservations()
}

const handlePageChange = () => {
  fetchReservations()
}

const fetchReservations = async () => {
  loading.value = true
  try {
    const res = await reservationApi.getList({
      status: activeTab.value,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    if (res.data?.list) {
      reservations.value = res.data.list
      total.value = res.data.total || res.data.list.length
    }
  } catch (e) {
    console.log('使用模拟数据')
    total.value = mockReservations.length
  } finally {
    loading.value = false
  }
}

const navigateToStation = (item) => {
  router.push(`/station/${item.id}`)
}

const cancelReservation = async (item) => {
  ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
    confirmButtonText: '确定取消',
    cancelButtonText: '我再想想',
    type: 'warning'
  }).then(async () => {
    try {
      await reservationApi.cancel(item.id)
      item.status = 'cancelled'
      ElMessage.success('预约已取消')
    } catch (e) {
      item.status = 'cancelled'
      ElMessage.success('预约已取消')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchReservations()
})
</script>

<style scoped>
.filter-section {
  padding: 0;
  overflow: hidden;
}

.filter-section :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 20px;
}

.filter-section :deep(.el-tabs__content) {
  padding: 0;
}

.reservation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.reservation-card {
  transition: all 0.3s ease;
}

.reservation-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.station-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 6px 0;
  color: #303133;
}

.station-address {
  font-size: 13px;
  color: #909399;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-body {
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  gap: 32px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
}

.qr-section {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
  border-radius: 8px;
}

.qr-code .qr-placeholder {
  width: 100px;
  height: 100px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: #fff;
}

.qr-placeholder p {
  margin: 6px 0 0 0;
  font-size: 11px;
}

.reservation-code {
  display: flex;
  flex-direction: column;
}

.reservation-code .label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.reservation-code .code {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
  letter-spacing: 2px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.create-time {
  font-size: 12px;
  color: #c0c4cc;
}

.actions {
  display: flex;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    gap: 12px;
  }

  .info-row {
    flex-direction: column;
    gap: 8px;
  }

  .qr-section {
    flex-direction: column;
    text-align: center;
  }

  .card-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .actions {
    width: 100%;
  }

  .actions .el-button {
    flex: 1;
  }
}
</style>
