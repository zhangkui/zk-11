<template>
  <div class="container">
    <div class="page-header">
      <h2 class="page-title">数据看板</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="6">
        <div class="stat-card blue">
          <div class="stat-icon">
            <el-icon :size="32"><Location /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">站点总数</div>
            <div class="stat-value">{{ stats.totalStations }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card green">
          <div class="stat-icon">
            <el-icon :size="32"><Lightning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">充电桩总数</div>
            <div class="stat-value">{{ stats.totalPiles }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card orange">
          <div class="stat-icon">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">今日排队</div>
            <div class="stat-value">{{ stats.todayQueue }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card purple">
          <div class="stat-icon">
            <el-icon :size="32"><Money /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">今日收入</div>
            <div class="stat-value">¥{{ stats.todayIncome.toFixed(2) }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="card chart-card">
          <template #header>
            <span>充电桩状态分布</span>
          </template>
          <div ref="pileChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="card chart-card">
          <template #header>
            <span>近7日充电趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="card">
          <template #header>
            <span>站点运营数据</span>
          </template>
          <el-table :data="stationStats" stripe>
            <el-table-column prop="name" label="站点名称" min-width="180" />
            <el-table-column label="充电桩总数" width="120" align="center">
              <template #default="{ row }">
                {{ row.totalPiles }}
              </template>
            </el-table-column>
            <el-table-column label="可用充电桩" width="120" align="center">
              <template #default="{ row }">
                <span :class="row.availablePiles > 0 ? 'text-success' : 'text-danger'">
                  {{ row.availablePiles }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="使用率" width="140" align="center">
              <template #default="{ row }">
                <el-progress
                  :percentage="Math.round((1 - row.availablePiles / row.totalPiles) * 100)"
                  :status="row.availablePiles === 0 ? 'exception' : 'success'"
                />
              </template>
            </el-table-column>
            <el-table-column label="今日充电量" width="140" align="center">
              <template #default="{ row }">
                {{ row.todayKwh || 0 }} kWh
              </template>
            </el-table-column>
            <el-table-column label="今日收入" width="140" align="center">
              <template #default="{ row }">
                ¥{{ (row.todayIncome || 0).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { Location, Lightning, User, Money } from '@element-plus/icons-vue'
import { dashboardApi } from '@/api'

const pileChartRef = ref(null)
const trendChartRef = ref(null)
let pileChart = null
let trendChart = null

const stats = reactive({
  totalStations: 0,
  totalPiles: 0,
  todayQueue: 0,
  todayIncome: 0
})

const stationStats = ref([])
const pileStatusData = ref([])
const weekTrendData = ref([])

const statusColors = {
  '空闲': '#67c23a',
  '使用中': '#e6a23c',
  '预约中': '#409eff',
  '故障': '#f56c6c',
  '维护中': '#909399'
}

const loadStats = async () => {
  try {
    const res = await dashboardApi.getStats()
    stats.totalStations = res.totalStations || 0
    stats.totalPiles = res.totalPiles || 0
    stats.todayQueue = res.todayQueue || 0
    stats.todayIncome = res.todayIncome || 0

    pileStatusData.value = res.pileStatusStats || []
    weekTrendData.value = res.weekTrend || []
    stationStats.value = res.stationStats || []

    updatePileChart()
    updateTrendChart()
  } catch (e) {
    console.error(e)
  }
}

const updatePileChart = () => {
  if (!pileChart) return
  const data = pileStatusData.value.map(item => ({
    value: item.count,
    name: item.statusName,
    itemStyle: { color: statusColors[item.statusName] }
  }))
  pileChart.setOption({
    series: [{ data }]
  })
}

const updateTrendChart = () => {
  if (!trendChart) return
  const dates = weekTrendData.value.map(item => item.date)
  const kwhData = weekTrendData.value.map(item => item.totalKwh || 0)
  const incomeData = weekTrendData.value.map(item => item.totalIncome || 0)

  trendChart.setOption({
    xAxis: { data: dates },
    series: [
      { data: kwhData },
      { data: incomeData }
    ]
  })
}

const initCharts = async () => {
  await nextTick()

  if (pileChartRef.value) {
    pileChart = echarts.init(pileChartRef.value)
    pileChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '5%', left: 'center' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: 20, fontWeight: 'bold' }
        },
        labelLine: { show: false },
        data: []
      }]
    })
  }

  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['充电量(kWh)', '收入(元)'] },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: [] },
      yAxis: [
        { type: 'value', name: 'kWh' },
        { type: 'value', name: '元' }
      ],
      series: [
        {
          name: '充电量(kWh)',
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.3 },
          data: [],
          itemStyle: { color: '#409eff' }
        },
        {
          name: '收入(元)',
          type: 'line',
          smooth: true,
          yAxisIndex: 1,
          areaStyle: { opacity: 0.3 },
          data: [],
          itemStyle: { color: '#67c23a' }
        }
      ]
    })
  }
}

const handleResize = () => {
  pileChart?.resize()
  trendChart?.resize()
}

onMounted(async () => {
  await initCharts()
  await loadStats()
  window.addEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  border-radius: 12px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

  &.blue {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  &.green {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  }

  &.orange {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }

  &.purple {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }

  .stat-icon {
    margin-right: 20px;
    opacity: 0.9;
  }

  .stat-content {
    .stat-label {
      font-size: 14px;
      opacity: 0.9;
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 28px;
      font-weight: bold;
    }
  }
}

.chart-card {
  .chart-container {
    height: 300px;
    width: 100%;
  }
}

.text-success {
  color: #67c23a;
  font-weight: 600;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>
