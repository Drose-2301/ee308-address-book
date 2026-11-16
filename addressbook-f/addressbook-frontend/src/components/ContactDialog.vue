<template>
  <div class="overlay" @click.self="close">
    <form class="dialog" @submit.prevent="save">
      <header class="dialog-head">
        <div>
          <h2>{{ isEdit ? '编辑联系人' : '新建联系人' }}</h2>
          <p>一个联系人可以保存多条联系方式，例如两个电话和一个微信。</p>
        </div>
        <button type="button" class="close" title="关闭" @click="close">✕</button>
      </header>

      <div class="body">
        <div class="grid-2">
          <label class="field">
            <span>姓名 <b>*</b></span>
            <input ref="nameInput" v-model="form.name" placeholder="例如：林晓" maxlength="100" />
          </label>
          <label class="field">
            <span>公司 / 单位</span>
            <input v-model="form.company" placeholder="例如：福大科技有限公司" maxlength="100" />
          </label>
        </div>

        <div class="grid-2">
          <label class="field">
            <span>职位</span>
            <input v-model="form.position" placeholder="例如：产品经理" maxlength="100" />
          </label>
          <label class="field">
            <span>地址</span>
            <input v-model="form.address" placeholder="例如：福州市鼓楼区工业路 523 号" maxlength="300" />
          </label>
        </div>

        <div class="field">
          <span>联系方式 <em>共 {{ form.methods.length }} 条</em></span>
          <div class="methods">
            <div v-for="(method, index) in form.methods" :key="index" class="method-row">
              <select v-model="method.type">
                <option v-for="option in METHOD_TYPES" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
              <input v-model="method.label" class="label-input" placeholder="备注名（可选）" maxlength="40" />
              <input v-model="method.value" class="value-input" placeholder="号码 / 邮箱 / 账号" maxlength="200" />
              <button type="button" class="remove" title="删除这条联系方式" @click="removeMethod(index)">✕</button>
            </div>
            <button type="button" class="add-method" @click="addMethod">＋ 添加一条联系方式</button>
          </div>
        </div>

        <div class="grid-2">
          <label class="field">
            <span>备注</span>
            <textarea v-model="form.notes" rows="3" maxlength="1000" placeholder="沟通偏好、重要提醒…"></textarea>
          </label>
          <div class="field">
            <span>书签</span>
            <button type="button" class="switch" :class="{ on: form.favorite }" @click="form.favorite = !form.favorite">
              <i></i>
            </button>
            <em class="hint">{{ form.favorite ? '已加入书签，列表会置顶显示' : '加入书签后可以在列表中快速找到' }}</em>
          </div>
        </div>

        <p v-if="error" class="error">{{ error }}</p>
      </div>

      <footer class="dialog-foot">
        <button type="button" class="ghost" @click="close">取消</button>
        <button type="submit" class="primary" :disabled="saving">{{ saving ? '保存中…' : '保存' }}</button>
      </footer>
    </form>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { createContact, updateContact } from '../api'
import { METHOD_TYPES } from '../methodTypes'

const props = defineProps({
  contact: { type: Object, default: null }
})

const emit = defineEmits(['close', 'saved'])

const isEdit = computed(() => Boolean(props.contact && props.contact.id))

const nameInput = ref(null)
const saving = ref(false)
const error = ref('')

const form = reactive({
  name: props.contact?.name || '',
  company: props.contact?.company || '',
  position: props.contact?.position || '',
  address: props.contact?.address || '',
  notes: props.contact?.notes || '',
  favorite: props.contact?.favorite || false,
  methods: props.contact
    ? props.contact.methods.map(method => ({ type: method.type, label: method.label || '', value: method.value }))
    : [{ type: 'PHONE', label: '手机', value: '' }]
})

function addMethod() {
  form.methods.push({ type: 'PHONE', label: '', value: '' })
}

function removeMethod(index) {
  form.methods.splice(index, 1)
}

function close() {
  emit('close')
}

function onKeydown(event) {
  if (event.key === 'Escape') close()
}

async function save() {
  error.value = ''
  if (!form.name.trim()) {
    error.value = '请先填写姓名'
    nameInput.value?.focus()
    return
  }
  const payload = {
    name: form.name,
    company: form.company,
    position: form.position,
    address: form.address,
    notes: form.notes,
    favorite: form.favorite,
    methods: form.methods
      .filter(method => method.value && method.value.trim())
      .map(method => ({ type: method.type, label: method.label, value: method.value }))
  }
  saving.value = true
  try {
    const saved = isEdit.value
      ? await updateContact(props.contact.id, payload)
      : await createContact(payload)
    emit('saved', { contact: saved, isEdit: isEdit.value })
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  window.addEventListener('keydown', onKeydown)
  nameInput.value?.focus()
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
})
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 40;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(2px);
}

.dialog {
  width: min(760px, 100%);
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border-radius: 18px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.25);
  overflow: hidden;
}

.dialog-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 24px 14px;
  border-bottom: 1px solid var(--line);
}

.dialog-head h2 {
  margin: 0 0 4px;
  font-size: 18px;
}

.dialog-head p {
  margin: 0;
  font-size: 12.5px;
  color: var(--muted);
}

.close {
  border: none;
  background: transparent;
  color: #94a3b8;
  font-size: 16px;
  padding: 4px 8px;
  border-radius: 8px;
}

.close:hover {
  background: #f1f5f9;
  color: #334155;
}

.body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 24px;
  overflow-y: auto;
}

.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #374151;
}

.field > span {
  font-weight: 600;
}

.field b {
  color: var(--danger);
}

.field em {
  font-style: normal;
  font-weight: 400;
  color: var(--muted);
  font-size: 12px;
}

.field input,
.field textarea,
.method-row input,
.method-row select {
  padding: 9px 11px;
  border: 1px solid var(--line);
  border-radius: 9px;
  background: #fbfcfe;
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.field input:focus,
.field textarea:focus,
.method-row input:focus,
.method-row select:focus {
  border-color: #a5b4fc;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.12);
  background: #fff;
}

.field textarea {
  resize: vertical;
}

.methods {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 2px;
}

.method-row {
  display: grid;
  grid-template-columns: 92px 170px 1fr 34px;
  gap: 8px;
  align-items: center;
}

.remove {
  width: 30px;
  height: 30px;
  padding: 0;
  border: none;
  border-radius: 8px;
  background: #f8fafc;
  color: #94a3b8;
  font-size: 12px;
}

.remove:hover {
  background: #fef2f2;
  color: var(--danger);
}

.add-method {
  align-self: flex-start;
  padding: 7px 12px;
  border: 1px dashed #c7d2fe;
  border-radius: 9px;
  background: var(--primary-soft);
  color: var(--primary);
  font-size: 12.5px;
  font-weight: 600;
}

.add-method:hover {
  background: #e0e7ff;
}

.switch {
  position: relative;
  width: 52px;
  height: 28px;
  padding: 0;
  border: none;
  border-radius: 999px;
  background: #e2e8f0;
  transition: background 0.2s ease;
}

.switch i {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.25);
  transition: transform 0.2s ease;
}

.switch.on {
  background: var(--star);
}

.switch.on i {
  transform: translateX(24px);
}

.hint {
  font-size: 12px;
  color: var(--muted);
}

.error {
  margin: 0;
  padding: 9px 12px;
  border-radius: 9px;
  background: #fef2f2;
  color: var(--danger);
  font-size: 13px;
}

.dialog-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--line);
  background: #fafbff;
}

.ghost,
.primary {
  padding: 9px 18px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
}

.ghost {
  border: 1px solid var(--line);
  background: #fff;
  color: #475569;
}

.ghost:hover {
  background: #f8fafc;
}

.primary {
  border: none;
  background: var(--primary);
  color: #fff;
}

.primary:hover {
  background: var(--primary-dark);
}

.primary:disabled {
  opacity: 0.7;
  cursor: default;
}

@media (max-width: 640px) {
  .grid-2 {
    grid-template-columns: 1fr;
  }

  .method-row {
    grid-template-columns: 1fr 1fr;
  }

  .method-row .value-input {
    grid-column: 1 / -1;
  }
}
</style>
