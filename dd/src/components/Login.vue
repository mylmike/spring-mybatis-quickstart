<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">亿林综合管理系统</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width:100%;height:44px;font-size:16px">登 录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/pendingOrders.js'

const emit = defineEmits(['login-success'])

const formRef = ref(null)
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const payload = {
      username: form.username,
      password: form.password
    }
    console.log('=== 登录请求 ===')
    console.log('URL: http://192.168.0.85:85/snapshot/auth')
    console.log('请求体:', JSON.stringify(payload))
    console.log('密码长度:', form.password.length, '字符:', JSON.stringify(form.password))
    const res = await login(payload)
    console.log('响应:', res.data)
    if (res.data && String(res.data.flag) === '1') {
      ElMessage.success('登录成功')
      const data = res.data || {}
      const userName = data.userName || data.username || data.name || data.realName || data.realname || data.nickName || data.nickname || ''
      console.log('auth 返回字段:', Object.keys(data))
      console.log('提取用户名:', JSON.stringify(userName))
      emit('login-success', {
        userName,
        deptNo: data.deptNo || '',
        deptName: data.deptName || ''
      })
    } else {
      ElMessage.error('账号或密码错误')
    }
  } catch (err) {
    ElMessage.error('登录请求失败: ' + (err.message || '网络错误'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a3a5c 0%, #2d6aa0 100%);
}
.login-card {
  width: 400px;
  padding: 40px 36px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.2);
}
.login-title {
  text-align: center;
  margin-bottom: 32px;
  color: #1a3a5c;
  font-size: 22px;
  letter-spacing: 2px;
}
</style>
