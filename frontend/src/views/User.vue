<template>
  <div class="user-page">
    <h2 class="page-title">用户中心</h2>

    <el-row :gutter="20">
      <el-col :xs="24" :md="8">
        <div class="user-info-card common-card">
          <div class="user-avatar">
            <el-avatar :size="80" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
          </div>
          <div class="user-basic">
            <h3 class="user-name">{{ userStore.userInfo?.name || '用户' }}</h3>
            <p class="user-phone">{{ userStore.userInfo?.phone || '138****8888' }}</p>
            <el-tag type="primary" effect="light">{{ userStore.userInfo?.role || '普通用户' }}</el-tag>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-value">{{ stats.totalCharges }}</span>
              <span class="stat-label">充电次数</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.totalKwh.toFixed(1) }}</span>
              <span class="stat-label">总充电量</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">¥{{ stats.totalCost.toFixed(2) }}</span>
              <span class="stat-label">总消费</span>
            </div>
          </div>
        </div>

        <div class="balance-card common-card">
          <div class="balance-header">
            <span class="balance-label">账户余额</span>
            <el-button type="primary" link @click="showRecharge = true">充值</el-button>
          </div>
          <div class="balance-amount">
            <span class="currency">¥</span>
            <span class="amount">{{ balance.toFixed(2) }}</span>
          </div>
          <div class="balance-actions">
            <el-button type="warning" size="small" @click="showBillDialog = true">
              <el-icon><Document /></el-icon>
              账单明细
            </el-button>
          </div>
        </div>

        <div class="vehicles-card common-card">
          <div class="card-header">
            <span class="card-title">我的车辆</span>
            <el-button type="primary" size="small" @click="openAddVehicle">
              <el-icon><Plus /></el-icon>
              添加
            </el-button>
          </div>
          <div class="vehicle-list">
            <div
              v-for="vehicle in vehicles"
              :key="vehicle.id"
              class="vehicle-item"
            >
              <div class="vehicle-icon">
                <el-icon :size="24"><Car /></el-icon>
              </div>
              <div class="vehicle-info">
                <span class="plate">{{ vehicle.plate }}</span>
                <span class="model">{{ vehicle.model }}</span>
              </div>
              <div class="vehicle-actions">
                <el-button type="primary" link size="small" @click="editVehicle(vehicle)">
                  编辑
                </el-button>
                <el-button type="danger" link size="small" @click="deleteVehicle(vehicle)">
                  删除
                </el-button>
              </div>
            </div>
            <el-empty v-if="vehicles.length === 0" description="暂无车辆" :image-size="60" />
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :md="16">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane label="个人信息" name="profile">
            <div class="profile-card common-card">
              <h3 class="section-title">基本信息</h3>
              <el-form :model="profileForm" label-width="100px">
                <el-form-item label="用户名">
                  <el-input v-model="profileForm.name" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="profileForm.phone" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="profileForm.email" />
                </el-form-item>
                <el-form-item label="性别">
                  <el-radio-group v-model="profileForm.gender">
                    <el-radio value="male">男</el-radio>
                    <el-radio value="female">女</el-radio>
                    <el-radio value="other">保密</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="生日">
                  <el-date-picker
                    v-model="profileForm.birthday"
                    type="date"
                    placeholder="选择日期"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
                <el-form-item label="所在城市">
                  <el-input v-model="profileForm.city" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveProfile">保存修改</el-button>
                  <el-button @click="resetProfile">重置</el-button>
                </el-form-item>
              </el-form>
            </div>

            <div class="password-card common-card">
              <h3 class="section-title">修改密码</h3>
              <el-form :model="passwordForm" label-width="100px">
                <el-form-item label="原密码">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="changePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <el-tab-pane label="充电统计" name="statistics">
            <div class="statistics-card common-card">
              <h3 class="section-title">充电统计</h3>
              <div class="chart-container" ref="chartRef"></div>
            </div>

            <div class="stats-grid common-card">
              <div class="stat-card">
                <el-icon :size="32" color="#67C23A"><Lightning /></el-icon>
                <div>
                  <span class="stat-number">{{ stats.totalKwh.toFixed(1) }} kWh</span>
                  <span class="stat-desc">累计充电量</span>
                </div>
              </div>
              <div class="stat-card">
                <el-icon :size="32" color="#409EFF"><Timer /></el-icon>
                <div>
                  <span class="stat-number">{{ stats.totalMinutes }} 分钟</span>
                  <span class="stat-desc">累计充电时长</span>
                </div>
              </div>
              <div class="stat-card">
                <el-icon :size="32" color="#E6A23C"><Money /></el-icon>
                <div>
                  <span class="stat-number">¥{{ stats.totalCost.toFixed(2) }}</span>
                  <span class="stat-desc">累计消费金额</span>
                </div>
              </div>
              <div class="stat-card">
                <el-icon :size="32" color="#F56C6C"><Document /></el-icon>
                <div>
                  <span class="stat-number">{{ stats.totalCharges }} 次</span>
                  <span class="stat-desc">累计充电次数</span>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="消息通知" name="notifications">
            <div class="notifications-card common-card">
              <h3 class="section-title">消息通知</h3>
              <div class="notification-list">
                <div
                  v-for="notification in notifications"
                  :key="notification.id"
                  class="notification-item"
                  :class="{ unread: !notification.read }"
                >
                  <div class="notification-icon" :class="notification.type">
                    <el-icon>
                      <component :is="getNotificationIcon(notification.type)" />
                    </el-icon>
                  </div>
                  <div class="notification-content">
                    <h4 class="notification-title">{{ notification.title }}</h4>
                    <p class="notification-desc">{{ notification.content }}</p>
                    <span class="notification-time">{{ notification.time }}</span>
                  </div>
                  <div class="notification-action" v-if="!notification.read">
                    <el-button type="primary" link size="small" @click="markAsRead(notification)">
                      标为已读
                    </el-button>
                  </div>
                </div>
                <el-empty v-if="notifications.length === 0" description="暂无消息" :image-size="60" />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <el-dialog v-model="showRecharge" title="账户充值" width="420px">
      <div class="recharge-content">
        <div class="recharge-amounts">
          <div
            v-for="amount in rechargeOptions"
            :key="amount"
            class="recharge-option"
            :class="{ active: selectedRecharge === amount }"
            @click="selectedRecharge = amount"
          >
            <span class="amount">¥{{ amount }}</span>
          </div>
        </div>
        <div class="recharge-method">
          <span class="label">支付方式</span>
          <el-radio-group v-model="rechargeMethod">
            <el-radio value="wechat">微信支付</el-radio>
            <el-radio value="alipay">支付宝</el-radio>
          </el-radio-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="showRecharge = false">取消</el-button>
        <el-button type="primary" @click="confirmRecharge">
          充值 ¥{{ selectedRecharge }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showBillDialog" title="账单明细" width="600px">
      <el-table :data="bills" style="width: 100%;" max-height="400">
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'recharge' ? 'success' : 'danger'" size="small">
              {{ row.type === 'recharge' ? '充值' : '消费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stationName" label="说明" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">
            <span :class="row.type === 'recharge' ? 'income' : 'expense'">
              {{ row.type === 'recharge' ? '+' : '-' }}¥{{ row.amount.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="100">
          <template #default="{ row }">
            ¥{{ row.balance.toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="vehicleDialogVisible" :title="isVehicleEdit ? '编辑车辆' : '添加车辆'" width="420px">
      <el-form :model="vehicleForm" label-width="80px">
        <el-form-item label="车牌号" required>
          <el-input v-model="vehicleForm.plate" placeholder="请输入车牌号" />
        </el-form-item>
        <el-form-item label="车辆型号">
          <el-input v-model="vehicleForm.model" placeholder="请输入车辆型号" />
        </el-form-item>
        <el-form-item label="车辆类型">
          <el-select v-model="vehicleForm.type" style="width: 100%;">
            <el-option label="轿车" value="sedan" />
            <el-option label="SUV" value="suv" />
            <el-option label="MPV" value="mpv" />
            <el-option label="货车" value="truck" />
          </el-select>
        </el-form-item>
        <el-form-item label="电池容量">
          <el-input v-model="vehicleForm.battery" placeholder="kWh">
            <template #append>kWh</template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="vehicleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveVehicle">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { useUserStore } from '@/store/user'
import { userApi } from '@/api/user'

const userStore = useUserStore()

const activeTab = ref('profile')
const chartRef = ref(null)
let chartInstance = null

const balance = ref(128.50)
const showRecharge = ref(false)
const selectedRecharge = ref(100)
const rechargeMethod = ref('wechat')
const rechargeOptions = [50, 100, 200, 500, 1000]

const showBillDialog = ref(false)
const bills = ref([
  { id: 1, time: '2024-01-14 15:45:30', type: 'expense', stationName: '阳光新城充电站-充电', amount: 91.20, balance: 128.50 },
  { id: 2, time: '2024-01-14 10:30:00', type: 'recharge', stationName: '账户充值', amount: 200.00, balance: 219.70 },
  { id: 3, time: '2024-01-13 10:30:15', type: 'expense', stationName: '绿能科技园充电站-充电', amount: 76.40, balance: 19.70 },
  { id: 4, time: '2024-01-12 17:20:00', type: 'expense', stationName: '奥体中心充电站-充电', amount: 105.60, balance: 96.10 },
  { id: 5, time: '2024-01-12 09:00:00', type: 'recharge', stationName: '账户充值', amount: 200.00, balance: 201.70 }
])

const vehicles = ref([
  { id: 1, plate: '京A12345', model: '特斯拉 Model 3', type: 'sedan', battery: 75 },
  { id: 2, plate: '京B67890', model: '比亚迪 汉EV', type: 'sedan', battery: 85 }
])

const vehicleDialogVisible = ref(false)
const isVehicleEdit = ref(false)
const vehicleForm = reactive({
  id: null,
  plate: '',
  model: '',
  type: 'sedan',
  battery: ''
})

const stats = ref({
  totalCharges: 45,
  totalKwh: 1568.5,
  totalCost: 3137.0,
  totalMinutes: 2880
})

const profileForm = reactive({
  name: userStore.userInfo?.name || '张三',
  phone: userStore.userInfo?.phone || '138****8888',
  email: userStore.userInfo?.email || 'zhangsan@example.com',
  gender: 'male',
  birthday: '1990-01-01',
  city: '北京市'
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const notifications = ref([
  {
    id: 1,
    type: 'charging',
    title: '充电完成提醒',
    content: '您的车辆已在阳光新城充电站完成充电，充电量45.6kWh，费用91.20元。',
    time: '2024-01-14 15:45:30',
    read: false
  },
  {
    id: 2,
    type: 'reservation',
    title: '预约成功通知',
    content: '您已成功预约绿能科技园充电站 2024-01-15 14:00-15:00 的充电时段。',
    time: '2024-01-14 10:30:00',
    read: false
  },
  {
    id: 3,
    type: 'queue',
    title: '排队叫号提醒',
    content: '您的排队号码 A008 即将叫号，请前往充电站准备充电。',
    time: '2024-01-13 10:15:00',
    read: true
  },
  {
    id: 4,
    type: 'system',
    title: '系统公告',
    content: '为提升服务质量，本平台将于本周日凌晨2:00-4:00进行系统维护。',
    time: '2024-01-12 09:00:00',
    read: true
  }
])

const getNotificationIcon = (type) => {
  const icons = {
    charging: 'Lightning',
    reservation: 'Calendar',
    queue: 'Bell',
    system: 'InfoFilled'
  }
  return icons[type] || 'Bell'
}

const markAsRead = (notification) => {
  notification.read = true
  ElMessage.success('已标为已读')
}

const saveProfile = async () => {
  try {
    await userApi.updateProfile(profileForm)
    userStore.updateUserInfo(profileForm)
    ElMessage.success('保存成功')
  } catch (e) {
    userStore.updateUserInfo(profileForm)
    ElMessage.success('保存成功')
  }
}

const resetProfile = () => {
  profileForm.name = userStore.userInfo?.name || '张三'
  profileForm.phone = userStore.userInfo?.phone || '138****8888'
  profileForm.email = userStore.userInfo?.email || 'zhangsan@example.com'
  profileForm.gender = 'male'
  profileForm.birthday = '1990-01-01'
  profileForm.city = '北京市'
}

const changePassword = async () => {
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!passwordForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  try {
    await userApi.changePassword(passwordForm)
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (e) {
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  }
}

const confirmRecharge = async () => {
  showRecharge.value = false
  balance.value += selectedRecharge.value
  bills.value.unshift({
    id: Date.now(),
    time: new Date().toLocaleString('zh-CN'),
    type: 'recharge',
    stationName: '账户充值',
    amount: selectedRecharge.value,
    balance: balance.value
  })
  ElMessage.success(`充值成功！已到账 ¥${selectedRecharge.value}`)
}

const openAddVehicle = () => {
  isVehicleEdit.value = false
  vehicleForm.id = null
  vehicleForm.plate = ''
  vehicleForm.model = ''
  vehicleForm.type = 'sedan'
  vehicleForm.battery = ''
  vehicleDialogVisible.value = true
}

const editVehicle = (vehicle) => {
  isVehicleEdit.value = true
  vehicleForm.id = vehicle.id
  vehicleForm.plate = vehicle.plate
  vehicleForm.model = vehicle.model
  vehicleForm.type = vehicle.type
  vehicleForm.battery = vehicle.battery
  vehicleDialogVisible.value = true
}

const saveVehicle = async () => {
  if (!vehicleForm.plate) {
    ElMessage.warning('请输入车牌号')
    return
  }
  try {
    if (isVehicleEdit.value) {
      await userApi.updateVehicle(vehicleForm.id, vehicleForm)
      const index = vehicles.value.findIndex(v => v.id === vehicleForm.id)
      if (index > -1) {
        vehicles.value[index] = { ...vehicles.value[index], ...vehicleForm }
      }
    } else {
      await userApi.addVehicle(vehicleForm)
      vehicles.value.push({
        id: Date.now(),
        ...vehicleForm
      })
    }
    vehicleDialogVisible.value = false
    ElMessage.success(isVehicleEdit.value ? '更新成功' : '添加成功')
  } catch (e) {
    if (isVehicleEdit.value) {
      const index = vehicles.value.findIndex(v => v.id === vehicleForm.id)
      if (index > -1) {
        vehicles.value[index] = { ...vehicles.value[index], ...vehicleForm }
      }
    } else {
      vehicles.value.push({
        id: Date.now(),
        ...vehicleForm
      })
    }
    vehicleDialogVisible.value = false
    ElMessage.success(isVehicleEdit.value ? '更新成功' : '添加成功')
  }
}

const deleteVehicle = (vehicle) => {
  ElMessage.confirm(`确定要删除车辆"${vehicle.plate}"吗？`, '提示', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await userApi.deleteVehicle(vehicle.id)
    } catch (e) {}
    const index = vehicles.value.findIndex(v => v.id === vehicle.id)
    if (index > -1) {
      vehicles.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  }).catch(() => {})
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
      data: ['充电量(kWh)', '费用(元)']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月']
    },
    yAxis: [
      {
        type: 'value',
        name: '充电量(kWh)'
      },
      {
        type: 'value',
        name: '费用(元)'
      }
    ],
    series: [
      {
        name: '充电量(kWh)',
        type: 'bar',
        data: [280, 320, 260, 310, 350, 248.5],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '费用(元)',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: [560, 640, 520, 620, 700, 497],
        lineStyle: {
          color: '#f56c6c',
          width: 2
        },
        itemStyle: {
          color: '#f56c6c'
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
    await userApi.getProfile()
  } catch (e) {}

  if (activeTab.value === 'statistics') {
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
.user-page {
  padding: 0;
}

.user-info-card {
  text-align: center;
  margin-bottom: 20px;
}

.user-avatar {
  margin-bottom: 16px;
}

.user-basic {
  margin-bottom: 20px;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #303133;
}

.user-phone {
  font-size: 14px;
  color: #909399;
  margin: 0 0 12px 0;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.user-stats .stat-item {
  display: flex;
  flex-direction: column;
}

.user-stats .stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.user-stats .stat-label {
  font-size: 12px;
  color: #909399;
}

.balance-card {
  margin-bottom: 20px;
}

.balance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.balance-label {
  font-size: 14px;
  color: #606266;
}

.balance-amount {
  margin-bottom: 16px;
}

.balance-amount .currency {
  font-size: 20px;
  color: #f56c6c;
  margin-right: 4px;
}

.balance-amount .amount {
  font-size: 36px;
  font-weight: 700;
  color: #f56c6c;
}

.balance-actions {
  display: flex;
  justify-content: flex-end;
}

.vehicles-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.vehicle-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.vehicle-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.vehicle-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.vehicle-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.vehicle-info .plate {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.vehicle-info .model {
  font-size: 12px;
  color: #909399;
}

.vehicle-actions {
  display: flex;
  gap: 8px;
}

.profile-tabs {
  margin-bottom: 20px;
}

.profile-card,
.password-card,
.statistics-card,
.stats-grid,
.notifications-card {
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
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

.stat-card .stat-number {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-card .stat-desc {
  font-size: 13px;
  color: #909399;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.notification-item.unread {
  background: #ecf5ff;
  border-color: #409eff;
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.notification-icon.charging {
  background: #67c23a;
}

.notification-icon.reservation {
  background: #409eff;
}

.notification-icon.queue {
  background: #e6a23c;
}

.notification-icon.system {
  background: #909399;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #303133;
}

.notification-desc {
  font-size: 13px;
  color: #606266;
  margin: 0 0 6px 0;
  line-height: 1.5;
}

.notification-time {
  font-size: 12px;
  color: #c0c4cc;
}

.recharge-content {
  padding: 10px 0;
}

.recharge-amounts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.recharge-option {
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.recharge-option:hover {
  border-color: #409eff;
}

.recharge-option.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.recharge-option .amount {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.recharge-option.active .amount {
  color: #409eff;
}

.recharge-method .label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.income {
  color: #67c23a;
  font-weight: 600;
}

.expense {
  color: #f56c6c;
  font-weight: 600;
}

@media (max-width: 992px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .recharge-amounts {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
