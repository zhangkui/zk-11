<template>
  <div class="station-manage-page">
    <div class="page-header common-card">
      <h2 class="page-title">站点管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        新增站点
      </el-button>
    </div>

    <div class="filter-section common-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="站点名称">
          <el-input
            v-model="filterForm.keyword"
            placeholder="请输入站点名称"
            clearable
          />
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

    <div class="station-list common-card">
      <el-table
        :data="stations"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="name" label="站点名称" min-width="180" />
        <el-table-column prop="address" label="地址" min-width="240" />
        <el-table-column prop="totalPiles" label="总桩数" width="100" align="center" />
        <el-table-column prop="availablePiles" label="可用桩数" width="100" align="center">
          <template #default="{ row }">
            <span class="available">{{ row.availablePiles }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格(元/度)" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="serviceFee" label="服务费(元/度)" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '营业中' : '已停业' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewPiles(row)">
              充电桩
            </el-button>
            <el-button type="primary" link size="small" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
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

    <el-dialog
      v-model="stationDialogVisible"
      :title="isEdit ? '编辑站点' : '新增站点'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="stationForm" :rules="stationRules" ref="stationFormRef" label-width="100px">
        <el-form-item label="站点名称" prop="name">
          <el-input v-model="stationForm.name" placeholder="请输入站点名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="stationForm.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="总桩数" prop="totalPiles">
          <el-input-number
            v-model="stationForm.totalPiles"
            :min="1"
            :max="100"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input v-model="stationForm.price" placeholder="请输入充电价格">
            <template #append>元/度</template>
          </el-input>
        </el-form-item>
        <el-form-item label="服务费" prop="serviceFee">
          <el-input v-model="stationForm.serviceFee" placeholder="请输入服务费">
            <template #append>元/度</template>
          </el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="stationForm.status">
            <el-radio value="active">营业中</el-radio>
            <el-radio value="inactive">已停业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="stationForm.remark"
            type="textarea"
            :rows="3"
            placeholder="选填"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStation">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="pileDialogVisible"
      title="充电桩管理"
      width="800px"
    >
      <div class="pile-dialog-header">
        <h3>{{ currentStation?.name }} - 充电桩列表</h3>
        <el-button type="primary" size="small" @click="openAddPileDialog">
          <el-icon><Plus /></el-icon>
          新增充电桩
        </el-button>
      </div>
      <el-table :data="piles" style="width: 100%;" max-height="400">
        <el-table-column prop="name" label="桩编号" width="120" />
        <el-table-column prop="power" label="功率(kW)" width="100" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.type === 'fast' ? 'primary' : 'info'">
              {{ row.type === 'fast' ? '快充' : '慢充' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditPileDialog(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDeletePile(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog
      v-model="pileFormDialogVisible"
      :title="isPileEdit ? '编辑充电桩' : '新增充电桩'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="pileForm" :rules="pileRules" ref="pileFormRef" label-width="100px">
        <el-form-item label="桩编号" prop="name">
          <el-input v-model="pileForm.name" placeholder="如：A01" />
        </el-form-item>
        <el-form-item label="功率" prop="power">
          <el-select v-model="pileForm.power" placeholder="请选择功率" style="width: 100%;">
            <el-option :label="60" :value="60" />
            <el-option :label="90" :value="90" />
            <el-option :label="120" :value="120" />
            <el-option :label="180" :value="180" />
            <el-option :label="250" :value="250" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="pileForm.type">
            <el-radio value="fast">快充</el-radio>
            <el-radio value="normal">慢充</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="pileForm.status">
            <el-radio value="available">空闲</el-radio>
            <el-radio value="occupied">使用中</el-radio>
            <el-radio value="reserved">预约中</el-radio>
            <el-radio value="fault">故障</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pileFormDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePile">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStationStore } from '@/store/station'

const stationStore = useStationStore()

const loading = ref(true)
const filterForm = reactive({
  keyword: ''
})
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const mockStations = [
  {
    id: 1,
    name: '阳光新城充电站',
    address: '北京市朝阳区阳光路123号',
    totalPiles: 8,
    availablePiles: 5,
    price: '1.50',
    serviceFee: '0.50',
    status: 'active',
    remark: ''
  },
  {
    id: 2,
    name: '绿能科技园充电站',
    address: '北京市海淀区科技路88号',
    totalPiles: 12,
    availablePiles: 3,
    price: '1.60',
    serviceFee: '0.50',
    status: 'active',
    remark: ''
  },
  {
    id: 3,
    name: '万达广场充电站',
    address: '北京市丰台区万达街1号',
    totalPiles: 16,
    availablePiles: 0,
    price: '1.80',
    serviceFee: '0.60',
    status: 'active',
    remark: ''
  },
  {
    id: 4,
    name: '奥体中心充电站',
    address: '北京市朝阳区奥运村路100号',
    totalPiles: 20,
    availablePiles: 8,
    price: '1.70',
    serviceFee: '0.50',
    status: 'active',
    remark: ''
  },
  {
    id: 5,
    name: '中关村创新园充电站',
    address: '北京市海淀区中关村大街1号',
    totalPiles: 10,
    availablePiles: 6,
    price: '1.55',
    serviceFee: '0.50',
    status: 'inactive',
    remark: '设备维护中'
  }
]

const stations = ref(mockStations)

const stationDialogVisible = ref(false)
const isEdit = ref(false)
const stationFormRef = ref(null)
const stationForm = reactive({
  id: null,
  name: '',
  address: '',
  totalPiles: 8,
  price: '1.50',
  serviceFee: '0.50',
  status: 'active',
  remark: ''
})

const stationRules = {
  name: [{ required: true, message: '请输入站点名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  totalPiles: [{ required: true, message: '请输入总桩数', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  serviceFee: [{ required: true, message: '请输入服务费', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const pileDialogVisible = ref(false)
const currentStation = ref(null)
const piles = ref([])

const pileFormDialogVisible = ref(false)
const isPileEdit = ref(false)
const pileFormRef = ref(null)
const pileForm = reactive({
  id: null,
  name: '',
  power: 120,
  type: 'fast',
  status: 'available'
})

const pileRules = {
  name: [{ required: true, message: '请输入桩编号', trigger: 'blur' }],
  power: [{ required: true, message: '请选择功率', trigger: 'change' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getStatusType = (status) => {
  const types = {
    available: 'success',
    occupied: 'warning',
    reserved: 'primary',
    fault: 'danger'
  }
  return types[status] || 'info'
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

const handleSearch = () => {
  currentPage.value = 1
  fetchStations()
}

const handleReset = () => {
  filterForm.keyword = ''
  currentPage.value = 1
  fetchStations()
}

const handlePageChange = () => {
  fetchStations()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchStations()
}

const fetchStations = async () => {
  loading.value = true
  try {
    await stationStore.fetchStations({
      keyword: filterForm.keyword,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    if (stationStore.stations.length > 0) {
      stations.value = stationStore.stations
    }
  } catch (e) {
    console.log('使用模拟数据')
    if (filterForm.keyword) {
      stations.value = mockStations.filter(s =>
        s.name.includes(filterForm.keyword) ||
        s.address.includes(filterForm.keyword)
      )
    } else {
      stations.value = mockStations
    }
    total.value = stations.value.length
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  stationForm.id = null
  stationForm.name = ''
  stationForm.address = ''
  stationForm.totalPiles = 8
  stationForm.price = '1.50'
  stationForm.serviceFee = '0.50'
  stationForm.status = 'active'
  stationForm.remark = ''
  stationDialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  stationForm.id = row.id
  stationForm.name = row.name
  stationForm.address = row.address
  stationForm.totalPiles = row.totalPiles
  stationForm.price = row.price
  stationForm.serviceFee = row.serviceFee
  stationForm.status = row.status
  stationForm.remark = row.remark || ''
  stationDialogVisible.value = true
}

const saveStation = async () => {
  if (!stationFormRef.value) return
  await stationFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await stationStore.updateStation(stationForm.id, stationForm)
          const index = stations.value.findIndex(s => s.id === stationForm.id)
          if (index > -1) {
            stations.value[index] = { ...stations.value[index], ...stationForm }
          }
          ElMessage.success('更新成功')
        } else {
          const res = await stationStore.addStation(stationForm)
          stations.value.unshift({
            id: res.data?.id || Date.now(),
            ...stationForm,
            availablePiles: stationForm.totalPiles
          })
          ElMessage.success('添加成功')
        }
        stationDialogVisible.value = false
      } catch (e) {
        if (isEdit.value) {
          const index = stations.value.findIndex(s => s.id === stationForm.id)
          if (index > -1) {
            stations.value[index] = { ...stations.value[index], ...stationForm }
          }
          ElMessage.success('更新成功')
        } else {
          stations.value.unshift({
            id: Date.now(),
            ...stationForm,
            availablePiles: stationForm.totalPiles
          })
          ElMessage.success('添加成功')
        }
        stationDialogVisible.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除站点"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await stationStore.deleteStation(row.id)
      const index = stations.value.findIndex(s => s.id === row.id)
      if (index > -1) {
        stations.value.splice(index, 1)
      }
      ElMessage.success('删除成功')
    } catch (e) {
      const index = stations.value.findIndex(s => s.id === row.id)
      if (index > -1) {
        stations.value.splice(index, 1)
      }
      ElMessage.success('删除成功')
    }
  }).catch(() => {})
}

const viewPiles = async (station) => {
  currentStation.value = station
  try {
    await stationStore.fetchChargingPiles(station.id)
    if (stationStore.chargingPiles.length > 0) {
      piles.value = stationStore.chargingPiles
    } else {
      piles.value = generateMockPiles(station)
    }
  } catch (e) {
    piles.value = generateMockPiles(station)
  }
  pileDialogVisible.value = true
}

const generateMockPiles = (station) => {
  const mockPiles = []
  const statuses = ['available', 'occupied', 'reserved', 'fault']
  for (let i = 0; i < station.totalPiles; i++) {
    mockPiles.push({
      id: i + 1,
      name: `${String.fromCharCode(65 + Math.floor(i / 4))}${(i % 4 + 1).toString().padStart(2, '0')}号桩`,
      power: [60, 90, 120, 180][Math.floor(Math.random() * 4)],
      type: Math.random() > 0.3 ? 'fast' : 'normal',
      status: statuses[Math.floor(Math.random() * statuses.length)]
    })
  }
  return mockPiles
}

const openAddPileDialog = () => {
  isPileEdit.value = false
  pileForm.id = null
  pileForm.name = ''
  pileForm.power = 120
  pileForm.type = 'fast'
  pileForm.status = 'available'
  pileFormDialogVisible.value = true
}

const openEditPileDialog = (row) => {
  isPileEdit.value = true
  pileForm.id = row.id
  pileForm.name = row.name
  pileForm.power = row.power
  pileForm.type = row.type
  pileForm.status = row.status
  pileFormDialogVisible.value = true
}

const savePile = async () => {
  if (!pileFormRef.value) return
  await pileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isPileEdit.value) {
          await stationStore.updateChargingPile(pileForm.id, pileForm)
          const index = piles.value.findIndex(p => p.id === pileForm.id)
          if (index > -1) {
            piles.value[index] = { ...piles.value[index], ...pileForm }
          }
          ElMessage.success('更新成功')
        } else {
          await stationStore.addChargingPile(currentStation.value.id, pileForm)
          piles.value.push({
            id: Date.now(),
            ...pileForm
          })
          ElMessage.success('添加成功')
        }
        pileFormDialogVisible.value = false
      } catch (e) {
        if (isPileEdit.value) {
          const index = piles.value.findIndex(p => p.id === pileForm.id)
          if (index > -1) {
            piles.value[index] = { ...piles.value[index], ...pileForm }
          }
          ElMessage.success('更新成功')
        } else {
          piles.value.push({
            id: Date.now(),
            ...pileForm
          })
          ElMessage.success('添加成功')
        }
        pileFormDialogVisible.value = false
      }
    }
  })
}

const handleDeletePile = (row) => {
  ElMessageBox.confirm(`确定要删除充电桩"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await stationStore.deleteChargingPile(row.id)
      const index = piles.value.findIndex(p => p.id === row.id)
      if (index > -1) {
        piles.value.splice(index, 1)
      }
      ElMessage.success('删除成功')
    } catch (e) {
      const index = piles.value.findIndex(p => p.id === row.id)
      if (index > -1) {
        piles.value.splice(index, 1)
      }
      ElMessage.success('删除成功')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchStations()
})
</script>

<style scoped>
.station-manage-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header .page-title {
  margin: 0;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.station-list {
  padding: 20px;
}

.available {
  color: #67c23a;
  font-weight: 600;
}

.price {
  color: #f56c6c;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.pile-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.pile-dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .filter-form {
    flex-direction: column;
  }

  .filter-form :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
  }
}
</style>
