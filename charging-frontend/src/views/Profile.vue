<template>
  <div class="container">
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="user-card">
          <div class="user-avatar-section">
            <el-avatar :size="80" class="user-avatar-large">
              {{ currentUser?.username?.charAt(0) }}
            </el-avatar>
            <div class="user-basic-info">
              <h3 class="user-name">{{ currentUser?.username }}</h3>
              <p class="user-phone">{{ currentUser?.phone }}</p>
              <div class="user-balance-large">
                <span class="balance-label">账户余额</span>
                <span class="balance-value">¥{{ currentUser?.balance?.toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <el-divider />

          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-value">{{ stats.reservations }}</div>
              <div class="stat-label">总预约数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.chargings }}</div>
              <div class="stat-label">充电次数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.totalKwh }} kWh</div>
              <div class="stat-label">总充电量</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card class="edit-card">
          <template #header>
            <span>修改个人信息</span>
          </template>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="100px"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item label="车牌号" prop="licensePlate">
              <el-input v-model="form.licensePlate" placeholder="请输入车牌号" />
            </el-form-item>

            <el-form-item label="新密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="不修改请留空" show-password />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleSubmit">
                保存修改
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="danger-card" style="margin-top: 20px">
          <template #header>
            <span class="danger-title">账号安全</span>
          </template>
          <div class="danger-actions">
            <el-button type="danger" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { SwitchButton } from '@element-plus/icons-vue'
import { userApi, reservationApi, recordApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const currentUser = computed(() => userStore.currentUser)

const form = reactive({
  username: '',
  phone: '',
  licensePlate: '',
  password: ''
})

const stats = reactive({
  reservations: 0,
  chargings: 0,
  totalKwh: 0
})

const rules = {
  username: [
    { min: 2, max: 20, message: '用户名长度在2到20个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ]
}

const loadUserInfo = async () => {
  if (!currentUser.value?.id) return
  try {
    const info = await userApi.getInfo(currentUser.value.id)
    userStore.setCurrentUser(info)
    form.username = info.username
    form.phone = info.phone
    form.licensePlate = info.licensePlate || ''
  } catch (e) {
    console.error(e)
  }
}

const loadStats = async () => {
  if (!currentUser.value?.id) return
  try {
    const [resRes, recordRes] = await Promise.all([
      reservationApi.pageByUser(currentUser.value.id, 1, 1),
      recordApi.pageByUser(currentUser.value.id, 1, 1000)
    ])
    stats.reservations = resRes.total || 0
    const records = recordRes.records || []
    stats.chargings = records.length
    stats.totalKwh = records.reduce((sum, r) => sum + (r.chargedKwh || 0), 0).toFixed(2)
  } catch (e) {
    console.error(e)
  }
}

const handleSubmit = async () => {
  if (!formRef.value || !currentUser.value?.id) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const data = { ...form }
        if (!data.password) delete data.password
        await userApi.updateInfo(currentUser.value.id, data)
        ElMessage.success('修改成功')
        await loadUserInfo()
      } catch (e) {
        console.error(e)
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  if (currentUser.value) {
    form.username = currentUser.value.username
    form.phone = currentUser.value.phone
    form.licensePlate = currentUser.value.licensePlate || ''
    form.password = ''
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    if (currentUser.value?.id) {
      await userApi.logout(currentUser.value.id)
    }
    userStore.setCurrentUser(null)
    localStorage.removeItem('userId')
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  loadUserInfo()
  loadStats()
})
</script>

<style lang="scss" scoped>
.user-card {
  .user-avatar-section {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .user-avatar-large {
    width: 80px;
    height: 80px;
    font-size: 32px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  .user-basic-info {
    flex: 1;

    .user-name {
      font-size: 20px;
      font-weight: 600;
      margin: 0 0 4px;
      color: #303133;
    }

    .user-phone {
      font-size: 14px;
      color: #909399;
      margin: 0 0 12px;
    }

    .user-balance-large {
      display: flex;
      align-items: baseline;
      gap: 8px;

      .balance-label {
        font-size: 14px;
        color: #606266;
      }

      .balance-value {
        font-size: 24px;
        font-weight: 600;
        color: #67c23a;
      }
    }
  }

  .user-stats {
    display: flex;
    justify-content: space-around;
    padding-top: 10px;

    .stat-item {
      text-align: center;

      .stat-value {
        font-size: 20px;
        font-weight: 600;
        color: #409eff;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.edit-card {
  :deep(.el-card__header) {
    font-weight: 600;
    font-size: 16px;
  }
}

.danger-card {
  :deep(.el-card__header) {
    font-weight: 600;
    font-size: 16px;
  }

  .danger-title {
    color: #f56c6c;
  }

  .danger-actions {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
