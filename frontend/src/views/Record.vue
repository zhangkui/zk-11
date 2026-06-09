<template>
  <div class="record-page">
    <h2 class="page-title">充电记录</h2>

    <div class="filter-section common-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="充电站">
          <el-select v-model="filterForm.stationId" placeholder="全部" clearable style="width: 200px;">
            <el-option
              v-for="station in stations"
              :key="station.id"
              :label="station.name"
              :value="station.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="statistics-section" v-if="showStats">
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="28" color="#409EFF"><Lightning /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ totalKwh.toFixed(2) }}</span>
          <span class="stat-label">总充电量 (kWh)</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="28" color="#67C23A"><Money /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">¥{{ totalCost.toFixed(2) }}</span>
          <span class="stat-label">总费用</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="28" color="#E6A23C"><Timer /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ totalMinutes }}</span>
          <span class="stat-label">总时长 (分钟)</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="28" color="#F56C6C"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ totalCount }}</span>
          <span class="stat-label">充电次数</span>
        </div>
      </div>
    </div>

    <div class="chart-section common-card" v-if="showChart">
      <h3 class="section-title">充电趋势</h3>
      <div ref="chartRef" class="chart-container"></div>
    </div>

    <div class="record-list common-card">
      <el-table
        :data="records"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="stationName" label="充电站" min-width="180" />
        <el-table-column prop="pileName" label="充电桩" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="duration" label="时长" width="100">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>
        <el-table-column prop="kwh" label="电量(kWh)" width="100">
          <template #default="{ row }">
            {{ row.kwh?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="cost" label="费用(元)" width="100">
          <template #default="{ row }">
            <span class="cost">¥{{ row.cost?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'completed' ? 'success' : row.status === 'charging' ? 'warning' : 'info'"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > pageSize"
        class="pagination"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, sizes"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="detailDialogVisible" title="充电详情" width="520px">
      <div class="detail-content" v-if="currentRecord">
        <div class="detail-header">
          <el-icon :size="48" color="#409EFF"><Charger /></el-icon>
          <div class="header-info">
            <h3>{{ currentRecord.stationName }}</h3>
            <p>{{ currentRecord.pileName }}</p>
          </div>
          <el-tag
            :type="currentRecord.status === 'completed' ? 'success' : 'warning'"
            size="large"
          >
            {{ getStatusText(currentRecord.status) }}
          </el-tag>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="开始时间">
            {{ currentRecord.startTime }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            {{ currentRecord.endTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="充电时长">
            {{ formatDuration(currentRecord.duration) }}
          </el-descriptions-item>
          <el-descriptions-item label="充电电量">
            {{ currentRecord.kwh?.toFixed(2) }} kWh
          </el-descriptions-item>
          <el-descriptions-item label="起始电量">
            {{ currentRecord.startBattery }}%
          </el-descriptions-item>
          <el-descriptions-item label="结束电量">
            {{ currentRecord.endBattery || '-' }}%
          </el-descriptions-item>
          <el-descriptions-item label="电费">
            ¥{{ currentRecord.electricityFee?.toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务费">
            ¥{{ currentRecord.serviceFee?.toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="总费用" span="2">
            <span class="total-cost">¥{{ currentRecord.cost?.toFixed(2) }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <div class="detail-footer" v-if="currentRecord.status === 'completed' && !currentRecord.paid">
          <el-button type="primary" size="large" @click="goToPay(currentRecord)">
            去支付 ¥{{ currentRecord.cost?.toFixed(2) }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { recordApi } from '@/api/record'

const router = useRouter()

const loading = ref(true)
const showStats = ref(true)
const showChart = ref(true)
const chartRef = ref(null)
let chartInstance = null

const filterForm = ref({
  dateRange: [],
  stationId: null
})

const stations = ref([
  { id: 1, name: '阳光新城充电站' },
  { id: 2, name: '绿能科技园充电站' },
  { id: 3, name: '万达广场充电站' },
  { id: 4, name: '奥体中心充电站' }
])

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const mockRecords = [
  {
    id: 1,
    stationName: '阳光新城充电站',
    pileName: 'A01号桩',
    startTime: '2024-01-14 14:30:00',
    endTime: '2024-01-14 15:45:00',
    duration: 75,
    kwh: 45.6,
    cost: 91.2,
    electricityFee: 68.4,
    serviceFee: 22.8,
    startBattery: 30,
    endBattery: 85,
    status: 'completed',
    paid: true
  },
  {
    id: 2,
    stationName: '绿能科技园充电站',
    pileName: 'B03号桩',
    startTime: '2024-01-13 09:15:00',
    endTime: '2024-01-13 10:30:00',
    duration: 75,
    kwh: 38.2,
    cost: 76.4,
    electricityFee: 57.3,
    serviceFee: 19.1,
    startBattery: 25,
    endBattery: 78,
    status: 'completed',
    paid: true
  },
  {
    id: 3,
    stationName: '奥体中心充电站',
    pileName: 'C02号桩',
    startTime: '2024-01-12 16:00:00',
    endTime: '2024-01-12 17:20:00',
    duration: 80,
    kwh: 52.8,
    cost: 105.6,
    electricityFee: 79.2,
    serviceFee: 26.4,
    startBattery: 20,
    endBattery: 90,
    status: 'completed',
    paid: false
  },
  {
    id: 4,
    stationName: '万达广场充电站',
    pileName: 'D05号桩',
    startTime: '2024-01-11 11:30:00',
    endTime: '2024-01-11 12:15:00',
    duration: 45,
    kwh: 28.5,
    cost: 57.0,
    electricityFee: 42.75,
    serviceFee: 14.25,
    startBattery: 45,
    endBattery: 75,
    status: 'completed',
    paid: true
  },
  {
    id: 5,
    stationName: '阳光新城充电站',
    pileName: 'A03号桩',
    startTime: '2024-01-15 10:00:00',
    endTime: null,
    duration: 30,
    kwh: 18.5,
    cost: 37.0,
    electricityFee: 27.75,
    serviceFee: 9.25,
    startBattery: 35,
    endBattery: null,
    status: 'charging',
    paid: false
  }
]

const records = ref(mockRecords)

const totalKwh = computed(() =>
  records.value.reduce((sum, r) => sum + (r.kwh || 0), 0)
)

const totalCost = computed(() =>
  records.value.reduce((sum, r) => sum + (r.cost || 0), 0)
)

const totalMinutes = computed(() =>
  records.value.reduce((sum, r) => sum + (r.duration || 0), 0)
)

const totalCount = computed(() => records.value.length)

const detailDialogVisible = ref(false)
const currentRecord = ref(null)

const getStatusText = (status) => {
  const texts = {
    charging: '充电中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return texts[status] || '未知'
}

const formatDuration = (minutes) => {
  if (!minutes) return '-'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) {
    return `${hours}小时${mins}分钟`
  }
  return `${mins}分钟`
}

const handleSearch = () => {
  currentPage.value = 1
  fetchRecords()
}

const handleReset = () => {
  filterForm.value = { dateRange: [], stationId: null }
  currentPage.value = 1
  fetchRecords()
}

const handlePageChange = () => {
  fetchRecords()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchRecords()
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterForm.value.dateRange?.length === 2) {
      params.startDate = filterForm.value.dateRange[0]
      params.endDate = filterForm.value.dateRange[1]
    }
    if (filterForm.value.stationId) {
      params.stationId = filterForm.value.stationId
    }

    const res = await recordApi.getList(params)
    if (res.data?.list) {
      records.value = res.data.list
      total.value = res.data.total || res.data.list.length
    }
  } catch (e) {
    console.log('使用模拟数据')
    total.value = mockRecords.length
  } finally {
    loading.value = false
  }
}

const viewDetail = (record) => {
  currentRecord.value = record
  detailDialogVisible.value = true
}

const goToPay = (record) => {
  detailDialogVisible.value = false
  router.push('/payment')
}

const initChart = async () => {
  await nextTick()
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['充电量', '费用']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月10日', '1月11日', '1月12日', '1月13日', '1月14日', '1月15日', '今天']
    },
    yAxis: [
      {
        type: 'value',
        name: '充电量(kWh)',
        position: 'left'
      },
      {
        type: 'value',
        name: '费用(元)',
        position: 'right'
      }
    ],
    series: [
      {
        name: '充电量',
        type: 'bar',
        data: [38, 28.5, 52.8, 38.2, 45.6, 0, 18.5],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '费用',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: [76, 57, 105.6, 76.4, 91.2, 0, 37],
        lineStyle: {
          color: '#f56c6c',
          width: 2
        },
        itemStyle: {
          color: '#f56c6c'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.2)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.02)' }
          ])
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

const handleResize = () => {
  chartInstance?.resize()
}

watch(showChart, async (val) => {
  if (val) {
    await nextTick()
    initChart()
  }
})

onMounted(async () => {
  await fetchRecords()
  if (showChart.value) {
    await initChart()
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.record-page {
  padding: 0;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.statistics-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.chart-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #303133;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.record-list {
  padding: 20px;
}

.cost {
  color: #f56c6c;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.detail-content {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.header-info h3 {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-info p {
  margin: 0;
  color: #909399;
}

.total-cost {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}

.detail-footer {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 1200px) {
  .statistics-section {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .filter-form {
    flex-direction: column;
  }

  .filter-form :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
  }

  .statistics-section {
    grid-template-columns: 1fr;
  }
}
</style>
