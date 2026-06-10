<template>
  <div class="container">
    <div class="page-header">
      <h2 class="page-title">充电站点</h2>
      <el-button v-if="isAdmin" type="primary" @click="openAddDialog" :icon="Plus">
        新增站点
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchForm.name"
        placeholder="搜索站点名称"
        clearable
        style="width: 240px"
        @keyup.enter="loadData"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select
        v-model="searchForm.status"
        placeholder="站点状态"
        clearable
        style="width: 160px"
      >
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadData" :icon="Search">
        搜索
      </el-button>
      <el-button @click="resetSearch" :icon="RefreshRight">
        重置
      </el-button>
    </div>

    <el-card class="card" shadow="hover">
      <el-table v-loading="loading" :data="stationList" stripe>
        <el-table-column prop="name" label="站点名称" min-width="180" />
        <el-table-column prop="address" label="地址" min-width="240" />
        <el-table-column prop="totalPiles" label="充电桩总数" width="120" align="center" />
        <el-table-column label="可用充电桩" width="140" align="center">
          <template #default="{ row }">
            <span :class="row.availablePiles > 0 ? 'text-success' : 'text-danger'">
              {{ row.availablePiles }} / {{ row.totalPiles }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass('station', row.status)]">
              {{ getStatusName('station', row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="openingHours" label="营业时间" width="140" />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="isAdmin ? 240 : 100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.id)" :icon="View">
              详情
            </el-button>
            <template v-if="isAdmin">
              <el-button type="primary" link @click="openEditDialog(row)" :icon="Edit">
                编辑
              </el-button>
              <el-button
                :type="row.status === 1 ? 'warning' : 'success'"
                link
                @click="toggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑站点' : '新增站点'"
      width="600px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="站点名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入站点名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入站点地址" />
        </el-form-item>
        <el-form-item label="充电桩总数" prop="totalPiles">
          <el-input-number v-model="form.totalPiles" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="form.openingHours" placeholder="例如：00:00-24:00" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入站点描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, RefreshRight, View, Edit } from '@element-plus/icons-vue'
import { stationApi } from '@/api'
import { formatDateTime, getStatusName, getStatusClass } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isAdmin = computed(() => userStore.isAdmin)

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const stationList = ref([])

const searchForm = reactive({
  name: '',
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null,
  name: '',
  address: '',
  longitude: null,
  latitude: null,
  totalPiles: 1,
  status: 1,
  openingHours: '',
  phone: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入站点名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入站点地址', trigger: 'blur' }],
  totalPiles: [{ required: true, message: '请输入充电桩总数', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await stationApi.page(params)
    stationList.value = res.records
    pagination.total = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.status = null
  pagination.pageNum = 1
  loadData()
}

const goDetail = (id) => {
  router.push(`/station/${id}`)
}

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    address: '',
    longitude: null,
    latitude: null,
    totalPiles: 1,
    status: 1,
    openingHours: '',
    phone: '',
    description: ''
  })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        await stationApi.save(form)
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        loadData()
      } finally {
        saving.value = false
      }
    }
  })
}

const toggleStatus = (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}该站点吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await stationApi.updateStatus(row.id, row.status === 1 ? 0 : 1)
      ElMessage.success(`${action}成功`)
      loadData()
    } catch (e) {
      console.error(e)
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.text-success {
  color: #67c23a;
  font-weight: 600;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
