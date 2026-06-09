<template>
  <div class="payment-page">
    <h2 class="page-title">支付记录</h2>

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
        <el-form-item label="支付状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 150px;">
            <el-option label="支付成功" value="success" />
            <el-option label="待支付" value="pending" />
            <el-option label="支付失败" value="failed" />
            <el-option label="已退款" value="refunded" />
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

    <div class="summary-section common-card">
      <div class="summary-card">
        <div class="summary-icon income">
          <el-icon :size="32"><Wallet /></el-icon>
        </div>
        <div class="summary-content">
          <span class="summary-value income">¥{{ totalAmount.toFixed(2) }}</span>
          <span class="summary-label">累计支付</span>
        </div>
      </div>
      <div class="summary-card">
        <div class="summary-icon count">
          <el-icon :size="32"><Document /></el-icon>
        </div>
        <div class="summary-content">
          <span class="summary-value">{{ totalCount }}</span>
          <span class="summary-label">支付笔数</span>
        </div>
      </div>
      <div class="summary-card">
        <div class="summary-icon pending">
          <el-icon :size="32"><Clock /></el-icon>
        </div>
        <div class="summary-content">
          <span class="summary-value pending">¥{{ pendingAmount.toFixed(2) }}</span>
          <span class="summary-label">待支付</span>
        </div>
      </div>
      <div class="summary-card">
        <div class="summary-icon refund">
          <el-icon :size="32"><RefreshLeft /></el-icon>
        </div>
        <div class="summary-content">
          <span class="summary-value refund">¥{{ refundAmount.toFixed(2) }}</span>
          <span class="summary-label">已退款</span>
        </div>
      </div>
    </div>

    <div class="payment-list common-card">
      <el-table
        :data="payments"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="stationName" label="充电站" min-width="180" />
        <el-table-column prop="paymentTime" label="支付时间" width="180" />
        <el-table-column prop="method" label="支付方式" width="120">
          <template #default="{ row }">
            <span v-if="row.method === 'wechat'">微信支付</span>
            <span v-else-if="row.method === 'alipay'">支付宝</span>
            <span v-else-if="row.method === 'balance'">余额支付</span>
            <span v-else>{{ row.method }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="kwh" label="电量(kWh)" width="120">
          <template #default="{ row }">
            {{ row.kwh?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount?.toFixed(2) }}</span>
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
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              详情
            </el-button>
            <el-button
              v-if="row.status === 'pending'"
              type="success"
              link
              size="small"
              @click="payNow(row)"
            >
              支付
            </el-button>
            <el-button
              v-if="row.status === 'success' && row.refundable"
              type="warning"
              link
              size="small"
              @click="applyRefund(row)"
            >
              退款
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

    <el-dialog v-model="detailDialogVisible" title="支付详情" width="520px">
      <div class="detail-content" v-if="currentPayment">
        <div class="detail-header" :class="currentPayment.status">
          <el-icon :size="48">
            <CircleCheckFilled v-if="currentPayment.status === 'success'" />
            <Clock v-else-if="currentPayment.status === 'pending'" />
            <CircleCloseFilled v-else />
          </el-icon>
          <div class="header-text">
            <h3>{{ getStatusText(currentPayment.status) }}</h3>
            <p class="amount-large">¥{{ currentPayment.amount?.toFixed(2) }}</p>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">
            {{ currentPayment.orderNo }}
          </el-descriptions-item>
          <el-descriptions-item label="支付时间">
            {{ currentPayment.paymentTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="充电站">
            {{ currentPayment.stationName }}
          </el-descriptions-item>
          <el-descriptions-item label="充电桩">
            {{ currentPayment.pileName }}
          </el-descriptions-item>
          <el-descriptions-item label="充电电量">
            {{ currentPayment.kwh?.toFixed(2) }} kWh
          </el-descriptions-item>
          <el-descriptions-item label="充电时长">
            {{ formatDuration(currentPayment.duration) }}
          </el-descriptions-item>
          <el-descriptions-item label="电费">
            ¥{{ currentPayment.electricityFee?.toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务费">
            ¥{{ currentPayment.serviceFee?.toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="支付方式" span="2">
            <span v-if="currentPayment.method === 'wechat'">
              <el-icon color="#07c160"><ChatDotRound /></el-icon>
              微信支付
            </span>
            <span v-else-if="currentPayment.method === 'alipay'">
              <el-icon color="#1677ff"><CreditCard /></el-icon>
              支付宝
            </span>
            <span v-else-if="currentPayment.method === 'balance'">
              <el-icon color="#faad14"><Wallet /></el-icon>
              余额支付
            </span>
            <span v-else>{{ currentPayment.method }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="备注" v-if="currentPayment.remark" span="2">
            {{ currentPayment.remark }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <el-dialog v-model="payDialogVisible" title="确认支付" width="420px">
      <div class="pay-content" v-if="currentPayment">
        <div class="pay-amount">
          <span class="label">应付金额</span>
          <span class="value">¥{{ currentPayment.amount?.toFixed(2) }}</span>
        </div>
        <div class="pay-method">
          <span class="label">选择支付方式</span>
          <el-radio-group v-model="selectedPayMethod">
            <el-radio value="wechat">
              <el-icon color="#07c160"><ChatDotRound /></el-icon>
              微信支付
            </el-radio>
            <el-radio value="alipay">
              <el-icon color="#1677ff"><CreditCard /></el-icon>
              支付宝
            </el-radio>
            <el-radio value="balance">
              <el-icon color="#faad14"><Wallet /></el-icon>
              余额支付
            </el-radio>
          </el-radio-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay">
          确认支付 ¥{{ currentPayment?.amount?.toFixed(2) }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { paymentApi } from '@/api/payment'

const loading = ref(true)

const filterForm = ref({
  dateRange: [],
  status: null
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const mockPayments = [
  {
    id: 1,
    orderNo: 'PAY202401140001',
    stationName: '阳光新城充电站',
    pileName: 'A01号桩',
    paymentTime: '2024-01-14 15:45:30',
    method: 'wechat',
    kwh: 45.6,
    amount: 91.2,
    electricityFee: 68.4,
    serviceFee: 22.8,
    duration: 75,
    status: 'success',
    refundable: false
  },
  {
    id: 2,
    orderNo: 'PAY202401130002',
    stationName: '绿能科技园充电站',
    pileName: 'B03号桩',
    paymentTime: '2024-01-13 10:30:15',
    method: 'alipay',
    kwh: 38.2,
    amount: 76.4,
    electricityFee: 57.3,
    serviceFee: 19.1,
    duration: 75,
    status: 'success',
    refundable: false
  },
  {
    id: 3,
    orderNo: 'PAY202401120003',
    stationName: '奥体中心充电站',
    pileName: 'C02号桩',
    paymentTime: null,
    method: null,
    kwh: 52.8,
    amount: 105.6,
    electricityFee: 79.2,
    serviceFee: 26.4,
    duration: 80,
    status: 'pending',
    refundable: false
  },
  {
    id: 4,
    orderNo: 'PAY202401110004',
    stationName: '万达广场充电站',
    pileName: 'D05号桩',
    paymentTime: '2024-01-11 12:15:00',
    method: 'balance',
    kwh: 28.5,
    amount: 57.0,
    electricityFee: 42.75,
    serviceFee: 14.25,
    duration: 45,
    status: 'refunded',
    refundable: false,
    remark: '充电异常退款'
  },
  {
    id: 5,
    orderNo: 'PAY202401100005',
    stationName: '中关村创新园充电站',
    pileName: 'E08号桩',
    paymentTime: '2024-01-10 16:45:20',
    method: 'wechat',
    kwh: 33.8,
    amount: 67.6,
    electricityFee: 50.7,
    serviceFee: 16.9,
    duration: 60,
    status: 'success',
    refundable: true
  }
]

const payments = ref(mockPayments)

const totalAmount = computed(() =>
  payments.value
    .filter(p => p.status === 'success')
    .reduce((sum, p) => sum + (p.amount || 0), 0)
)

const totalCount = computed(() =>
  payments.value.filter(p => p.status === 'success').length
)

const pendingAmount = computed(() =>
  payments.value
    .filter(p => p.status === 'pending')
    .reduce((sum, p) => sum + (p.amount || 0), 0)
)

const refundAmount = computed(() =>
  payments.value
    .filter(p => p.status === 'refunded')
    .reduce((sum, p) => sum + (p.amount || 0), 0)
)

const detailDialogVisible = ref(false)
const payDialogVisible = ref(false)
const currentPayment = ref(null)
const selectedPayMethod = ref('wechat')

const getStatusType = (status) => {
  const types = {
    success: 'success',
    pending: 'warning',
    failed: 'danger',
    refunded: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    success: '支付成功',
    pending: '待支付',
    failed: '支付失败',
    refunded: '已退款'
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
  fetchPayments()
}

const handleReset = () => {
  filterForm.value = { dateRange: [], status: null }
  currentPage.value = 1
  fetchPayments()
}

const handlePageChange = () => {
  fetchPayments()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchPayments()
}

const fetchPayments = async () => {
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
    if (filterForm.value.status) {
      params.status = filterForm.value.status
    }

    const res = await paymentApi.getList(params)
    if (res.data?.list) {
      payments.value = res.data.list
      total.value = res.data.total || res.data.list.length
    }
  } catch (e) {
    console.log('使用模拟数据')
    total.value = mockPayments.length
  } finally {
    loading.value = false
  }
}

const viewDetail = (payment) => {
  currentPayment.value = payment
  detailDialogVisible.value = true
}

const payNow = (payment) => {
  currentPayment.value = payment
  selectedPayMethod.value = 'wechat'
  payDialogVisible.value = true
}

const confirmPay = async () => {
  try {
    await paymentApi.create(currentPayment.value.id, { method: selectedPayMethod.value })
    currentPayment.value.status = 'success'
    currentPayment.value.method = selectedPayMethod.value
    currentPayment.value.paymentTime = new Date().toLocaleString('zh-CN')
    payDialogVisible.value = false
    ElMessage.success('支付成功！')
  } catch (e) {
    currentPayment.value.status = 'success'
    currentPayment.value.method = selectedPayMethod.value
    currentPayment.value.paymentTime = new Date().toLocaleString('zh-CN')
    payDialogVisible.value = false
    ElMessage.success('支付成功！')
  }
}

const applyRefund = (payment) => {
  ElMessageBox.confirm(`确定要对订单 ${payment.orderNo} 申请退款吗？`, '提示', {
    confirmButtonText: '确认申请',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('退款申请已提交，请等待审核')
  }).catch(() => {})
}

onMounted(() => {
  fetchPayments()
})
</script>

<style scoped>
.payment-page {
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

.summary-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.summary-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.summary-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.summary-icon.income {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.summary-icon.count {
  background: linear-gradient(135deg, #409eff 0%, #00d4ff 100%);
}

.summary-icon.pending {
  background: linear-gradient(135deg, #e6a23c 0%, #ff9500 100%);
}

.summary-icon.refund {
  background: linear-gradient(135deg, #f56c6c 0%, #ff4d4f 100%);
}

.summary-content {
  display: flex;
  flex-direction: column;
}

.summary-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.summary-value.income {
  color: #667eea;
}

.summary-value.pending {
  color: #e6a23c;
}

.summary-value.refund {
  color: #f56c6c;
}

.summary-label {
  font-size: 13px;
  color: #909399;
}

.payment-list {
  padding: 20px;
}

.amount {
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
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-header.success {
  color: #67c23a;
}

.detail-header.pending {
  color: #e6a23c;
}

.detail-header.failed,
.detail-header.refunded {
  color: #f56c6c;
}

.header-text h3 {
  margin: 8px 0 4px 0;
  font-size: 20px;
  font-weight: 600;
}

.amount-large {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
}

.pay-content {
  padding: 10px 0;
}

.pay-amount {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: #fff;
  margin-bottom: 24px;
}

.pay-amount .label {
  display: block;
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 8px;
}

.pay-amount .value {
  font-size: 42px;
  font-weight: 700;
}

.pay-method .label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.pay-method :deep(.el-radio) {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

@media (max-width: 1200px) {
  .summary-section {
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

  .summary-section {
    grid-template-columns: 1fr;
  }
}
</style>
