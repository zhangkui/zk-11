<template>
  <div class="home-page">
    <div class="search-section common-card">
      <div class="search-header">
        <h2 class="page-title">充电站点列表</h2>
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索站点名称"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
      <div class="stats-row">
        <div class="stat-card">
          <el-icon :size="32" color="#409EFF"><Charger /></el-icon>
          <div class="stat-content">
            <span class="stat-number">{{ totalStations }}</span>
            <span class="stat-text">站点总数</span>
          </div>
        </div>
        <div class="stat-card">
          <el-icon :size="32" color="#67C23A"><CircleCheck /></el-icon>
          <div class="stat-content">
            <span class="stat-number">{{ availablePiles }}</span>
            <span class="stat-text">可用桩数</span>
          </div>
        </div>
        <div class="stat-card">
          <el-icon :size="32" color="#E6A23C"><Clock /></el-icon>
          <div class="stat-content">
            <span class="stat-number">{{ chargingCount }}</span>
            <span class="stat-text">充电中</span>
          </div>
        </div>
        <div class="stat-card">
          <el-icon :size="32" color="#F56C6C"><Warning /></el-icon>
          <div class="stat-content">
            <span class="stat-number">{{ faultCount }}</span>
            <span class="stat-text">故障桩</span>
          </div>
        </div>
      </div>
    </div>

    <div class="charts-section" v-if="showChart">
      <div class="chart-card common-card">
        <h3 class="chart-title">本周充电趋势</h3>
        <div ref="chartRef" class="chart-container"></div>
      </div>
    </div>

    <div class="stations-grid">
      <el-skeleton v-for="i in 6" :key="i" v-if="loading" :rows="4" animated />
      <StationCard
        v-for="station in filteredStations"
        :key="station.id"
        :station="station"
        @click="goToStationDetail"
      />
      <el-empty v-if="!loading && filteredStations.length === 0" description="暂无站点数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import StationCard from '@/components/StationCard.vue'
import { useStationStore } from '@/store/station'

const router = useRouter()
const stationStore = useStationStore()

const searchKeyword = ref('')
const loading = ref(true)
const showChart = ref(true)
const chartRef = ref(null)
let chartInstance = null

const mockStations = [
  {
    id: 1,
    name: '阳光新城充电站',
    address: '北京市朝阳区阳光路123号',
    totalPiles: 8,
    availablePiles: 5,
    price: '1.50',
    serviceFee: '0.50',
    distance: 1.2
  },
  {
    id: 2,
    name: '绿能科技园充电站',
    address: '北京市海淀区科技路88号',
    totalPiles: 12,
    availablePiles: 3,
    price: '1.60',
    serviceFee: '0.50',
    distance: 2.5
  },
  {
    id: 3,
    name: '万达广场充电站',
    address: '北京市丰台区万达街1号',
    totalPiles: 16,
    availablePiles: 0,
    price: '1.80',
    serviceFee: '0.60',
    distance: 3.1
  },
  {
    id: 4,
    name: '奥体中心充电站',
    address: '北京市朝阳区奥运村路100号',
    totalPiles: 20,
    availablePiles: 8,
    price: '1.70',
    serviceFee: '0.50',
    distance: 4.2
  },
  {
    id: 5,
    name: '中关村创新园充电站',
    address: '北京市海淀区中关村大街1号',
    totalPiles: 10,
    availablePiles: 6,
    price: '1.55',
    serviceFee: '0.50',
    distance: 5.0
  },
  {
    id: 6,
    name: '亦庄开发区充电站',
    address: '北京市大兴区亦庄开发区路66号',
    totalPiles: 15,
    availablePiles: 10,
    price: '1.45',
    serviceFee: '0.50',
    distance: 8.5
  }
]

const stations = ref(mockStations)

const filteredStations = computed(() => {
  if (!searchKeyword.value) return stations.value
  const keyword = searchKeyword.value.toLowerCase()
  return stations.value.filter(s =>
    s.name.toLowerCase().includes(keyword) ||
    s.address.toLowerCase().includes(keyword)
  )
})

const totalStations = computed(() => stations.value.length)
const availablePiles = computed(() =>
  stations.value.reduce((sum, s) => sum + (s.availablePiles || 0), 0)
)
const chargingCount = computed(() =>
  stations.value.reduce((sum, s) => sum + ((s.totalPiles || 0) - (s.availablePiles || 0)), 0)
)
const faultCount = computed(() => 3)

const handleSearch = () => {
  ElMessage.info(`搜索: ${searchKeyword.value || '全部'}`)
}

const goToStationDetail = (station) => {
  router.push(`/station/${station.id}`)
}

const initChart = async () => {
  await nextTick()
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['充电量', '充电次数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: [
      {
        type: 'value',
        name: '充电量(kWh)',
        position: 'left'
      },
      {
        type: 'value',
        name: '次数',
        position: 'right'
      }
    ],
    series: [
      {
        name: '充电量',
        type: 'line',
        smooth: true,
        data: [120, 132, 101, 134, 90, 230, 210],
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#667eea',
          width: 2
        },
        itemStyle: {
          color: '#667eea'
        }
      },
      {
        name: '充电次数',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: [22, 25, 18, 28, 15, 45, 38],
        lineStyle: {
          color: '#764ba2',
          width: 2
        },
        itemStyle: {
          color: '#764ba2'
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(async () => {
  try {
    await stationStore.fetchStations()
    if (stationStore.stations.length > 0) {
      stations.value = stationStore.stations
    }
  } catch (e) {
    console.log('使用模拟数据')
  } finally {
    loading.value = false
  }

  if (showChart.value) {
    await initChart()
    window.addEventListener('resize', handleResize)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.home-page {
  padding: 0;
}

.search-section {
  margin-bottom: 20px;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-title {
  margin: 0;
}

.search-box {
  width: 320px;
  max-width: 100%;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
  border-radius: 12px;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-text {
  font-size: 13px;
  color: #909399;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  padding: 24px;
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #303133;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.stations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .search-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-box {
    width: 100%;
  }

  .stats-row {
    grid-template-columns: 1fr;
  }

  .stations-grid {
    grid-template-columns: 1fr;
  }
}
</style>
