<template>
  <div class="container">
    <div class="page-header">
      <h2 class="page-title">费用管理</h2>
      <el-statistic title="待支付金额" :value="unpaidTotal" suffix="元" style="margin-right: 40px">
        <template #formatter>
          <span style="color: #e6a23c; font-size: 24px">¥{{ unpaidTotal.toFixed(2) }}</span>
        </template>
      </el-statistic>
    </div>

    <div class="search-bar">
      <el-select
        v-model="statusFilter"
        placeholder="支付状态"
        clearable
        style="width: 160px"
      >
        <el-option label="待支付" :value="0" />
        <el-option label="已支付" :value="1" />
        <el-option label="已退款" :value="2" />
      </el-select>
      <el-button type="primary" @click="loadData" :icon="Search">
        搜索
      </el-button>
      <el-button @click="resetSearch" :icon="RefreshRight">
        重置
      </el-button>
    </div>

    <el-card class="card" shadow="hover">
      <el-table v-loading="loading" :data="feeList" stripe>
        <el-table-column prop="feeNo" label="费用单号" width="200" />
        <el-table-column label="充电量" width="120" align="center">
          <template #default="{ row }">
            {{ row.chargedKwh }} kWh
          </template>
        </el-table-column>
        <el-table-column label="电费" width="100" align="center">
          <template #default="{ row }">
            ¥{{ row.electricityFee?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="服务费" width="100" align="center">
          <template #default="{ row }">
            ¥{{ row.serviceFee?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="总金额" width="120" align="center">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="优惠" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.discountAmount > 0" class="discount">
              -¥{{ row.discountAmount?.toFixed(2) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="实付金额" width="120" align="center">
          <template #default="{ row }">
            <span class="pay-amount">¥{{ row.payAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass('pay', row.payStatus)]">
              {{ getStatusName('pay', row.payStatus) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="100" align="center">
          <template #default="{ row }">
            {{ row.payMethod ? getStatusName('payMethod', row.payMethod) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="支付时间" width="180">
          <template #default="{ row }">
            {{ row.payTime ? formatDateTime(row.payTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <template v-if="!isAdmin">
              <el-button
                v-if="row.payStatus === 0"
                type="success"
                link
                @click="handlePay(row)"
              >
                去支付
              </el-button>
            </template>
            <el-button type="primary" link @click="viewDetail(row)">
              详情
            </el-button>
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
      v-model="payDialogVisible"
      title="支付费用"
      width="480px"
    >
      <div v-if="currentFee" class="pay-summary">
        <div class="pay-title">待支付金额</div>
        <div class="pay-amount-large">¥{{ currentFee.payAmount?.toFixed(2) }}</div>
        <el-descriptions :column="1" border size="small" style="margin-top: 20px">
          <el-descriptions-item label="费用单号">
            {{ currentFee.feeNo }}
          </el-descriptions-item>
          <el-descriptions-item label="充电量">
            {{ currentFee.chargedKwh }} kWh × ¥{{ currentFee.pricePerKwh }}/kWh
          </el-descriptions-item>
          <el-descriptions-item label="电费">
            ¥{{ currentFee.electricityFee?.toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务费">
            {{ currentFee.chargedKwh }} kWh × ¥{{ currentFee.serviceFeePerKwh }}/kWh
          </el-descriptions-item>
          <el-descriptions-item label="服务费金额">
            ¥{{ currentFee.serviceFee?.toFixed(2) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form label-width="100px" style="margin-top: 20px">
        <el-form-item label="支付方式">
          <el-radio-group v-model="payMethod">
            <el-radio :value="1">
              <el-icon><Wallet /></el-icon> 余额支付
            </el-radio>
            <el-radio :value="2">
              <el-icon><ChatDotRound /></el-icon> 微信支付
            </el-radio>
            <el-radio :value="3">
              <el-icon><CreditCard /></el-icon> 支付宝
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay" :loading="submitting">
          确认支付 ¥{{ currentFee?.payAmount?.toFixed(2) }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailDialogVisible"
      title="费用详情"
      width="560px"
    >
      <el-descriptions v-if="currentFee" :column="2" border>
        <el-descriptions-item label="费用单号">
          {{ currentFee.feeNo }}
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <span :class="['status-tag', getStatusClass('pay', currentFee.payStatus)]">
            {{ getStatusName('pay', currentFee.payStatus) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="充电量">
          {{ currentFee.chargedKwh }} kWh
        </el-descriptions-item>
        <el-descriptions-item label="电价">
          ¥{{ currentFee.pricePerKwh }}/kWh
        </el-descriptions-item>
        <el-descriptions-item label="服务费单价">
          ¥{{ currentFee.serviceFeePerKwh }}/kWh
        </el-descriptions-item>
        <el-descriptions-item label="电费">
          ¥{{ currentFee.electricityFee?.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="服务费">
          ¥{{ currentFee.serviceFee?.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="违约金">
          ¥{{ currentFee.penaltyFee?.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="优惠金额">
          ¥{{ currentFee.discountAmount?.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="总金额">
          ¥{{ currentFee.totalAmount?.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="实付金额">
          <span class="pay-amount">¥{{ currentFee.payAmount?.toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          {{ currentFee.payMethod ? getStatusName('payMethod', currentFee.payMethod) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="支付时间">
          {{ currentFee.payTime ? formatDateTime(currentFee.payTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(currentFee.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentFee.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshRight, Wallet, ChatDotRound, CreditCard } from '@element-plus/icons-vue'
import { feeApi } from '@/api'
import { formatDateTime, getStatusName, getStatusClass } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const isAdmin = computed(() => userStore.isAdmin)

const loading = ref(false)
const submitting = ref(false)
const payDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const payMethod = ref(1)
const currentFee = ref(null)
const feeList = ref([])
const statusFilter = ref(null)

const unpaidTotal = computed(() => {
  return feeList.value
    .filter(f => f.payStatus === 0)
    .reduce((sum, f) => sum + (f.payAmount || 0), 0)
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await feeApi.pageByUser(
      userStore.userId,
      pagination.pageNum,
      pagination.pageSize
    )
    feeList.value = res.records || []
    if (statusFilter.value !== null) {
      feeList.value = feeList.value.filter(f => f.payStatus === statusFilter.value)
    }
    pagination.total = res.total || 0
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

const handlePay = (row) => {
  currentFee.value = row
  payMethod.value = 1
  payDialogVisible.value = true
}

const confirmPay = async () => {
  if (!currentFee.value) return
  submitting.value = true
  try {
    await feeApi.pay({
      feeId: currentFee.value.id,
      payMethod: payMethod.value
    })
    ElMessage.success('支付成功')
    payDialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const viewDetail = (row) => {
  currentFee.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.amount {
  font-weight: 600;
  color: #303133;
}

.discount {
  color: #67c23a;
  font-weight: 500;
}

.pay-amount {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.pay-amount-large {
  font-size: 48px;
  font-weight: bold;
  color: #f56c6c;
  text-align: center;
  margin: 20px 0;
}

.pay-summary {
  text-align: center;

  .pay-title {
    font-size: 14px;
    color: #606266;
  }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
