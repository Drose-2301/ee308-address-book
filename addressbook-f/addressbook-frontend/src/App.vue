<template>
  <div class="app">
    <header class="hero">
      <div class="hero-inner">
        <div class="brand">
          <div class="logo">☎</div>
          <div>
            <h1>通讯录</h1>
            <p>Address Book · 书签 · 多联系方式 · Excel 导入导出</p>
          </div>
        </div>
        <div class="stats">
          <span class="pill">全部联系人 <b>{{ stats.total }}</b></span>
          <span class="pill star-pill">书签 <b>{{ stats.favorites }}</b></span>
        </div>
      </div>
    </header>

    <main class="container">
      <section class="toolbar">
        <div class="search">
          <span class="search-icon">🔍</span>
          <input
            v-model="keyword"
            type="search"
            placeholder="搜索姓名、公司、职位或任意联系方式…"
            aria-label="搜索联系人"
          />
        </div>

        <button class="filter" :class="{ active: favoriteOnly }" @click="favoriteOnly = !favoriteOnly">
          ★ 只看书签
        </button>

        <div class="spacer"></div>

        <button class="ghost" :disabled="busy" @click="pickImportFile">导入 Excel</button>
        <button class="ghost" :disabled="busy || contacts.length === 0" @click="handleExport">导出 Excel</button>
        <button class="primary" @click="openCreate">＋ 新建联系人</button>

        <input ref="fileInput" type="file" accept=".xlsx,.xls" class="hidden-input" @change="handleImport" />
      </section>

      <p class="hint">
        共 {{ contacts.length }} 条结果 · 书签联系人自动置顶 · 点击卡片可编辑，右上角 ★ 可切换书签
      </p>

      <div v-if="loading" class="state">正在加载联系人…</div>

      <div v-else-if="contacts.length === 0" class="empty">
        <div class="empty-icon">📇</div>
        <h2>{{ keyword || favoriteOnly ? '没有匹配的联系人' : '通讯录还是空的' }}</h2>
        <p v-if="keyword || favoriteOnly">换一个关键词，或关闭“只看书签”再试试。</p>
        <p v-else>新建第一个联系人，或者从 Excel 文件导入已有通讯录。</p>
        <div class="empty-actions">
          <button class="primary" @click="openCreate">＋ 新建联系人</button>
          <button class="ghost" @click="pickImportFile">导入 Excel</button>
        </div>
      </div>

      <div v-else class="grid">
        <ContactCard
          v-for="contact in contacts"
          :key="contact.id"
          :contact="contact"
          @edit="openEdit"
          @delete="handleDelete"
          @toggle-favorite="toggleFavorite"
        />
      </div>
    </main>

    <ContactDialog
      v-if="dialogOpen"
      :contact="editingContact"
      @close="dialogOpen = false"
      @saved="handleSaved"
    />

    <transition name="toast">
      <div v-if="toast" class="toast" :class="toast.type">{{ toast.text }}</div>
    </transition>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import ContactCard from './components/ContactCard.vue'
import ContactDialog from './components/ContactDialog.vue'
import { deleteContact, exportExcel, getStats, importExcel, listContacts, setFavorite } from './api'

const contacts = ref([])
const stats = ref({ total: 0, favorites: 0 })
const keyword = ref('')
const favoriteOnly = ref(false)
const loading = ref(false)
const busy = ref(false)
const dialogOpen = ref(false)
const editingContact = ref(null)
const toast = ref(null)
const fileInput = ref(null)

let searchTimer = null
let toastTimer = null

async function load() {
  loading.value = true
  try {
    contacts.value = await listContacts({ keyword: keyword.value, favorite: favoriteOnly.value })
    stats.value = await getStats()
  } catch (e) {
    showToast(e.message, 'error')
  } finally {
    loading.value = false
  }
}

watch(keyword, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(load, 250)
})

watch(favoriteOnly, load)

function showToast(text, type = 'success') {
  toast.value = { text, type }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toast.value = null
  }, 4000)
}

function openCreate() {
  editingContact.value = null
  dialogOpen.value = true
}

function openEdit(contact) {
  editingContact.value = contact
  dialogOpen.value = true
}

async function handleSaved({ isEdit }) {
  dialogOpen.value = false
  await load()
  showToast(isEdit ? '联系人已更新' : '联系人已创建')
}

async function toggleFavorite(contact) {
  try {
    await setFavorite(contact.id, !contact.favorite)
    await load()
    showToast(contact.favorite ? '已取消书签' : `已把「${contact.name}」加入书签`)
  } catch (e) {
    showToast(e.message, 'error')
  }
}

async function handleDelete(contact) {
  if (!window.confirm(`确定要删除「${contact.name}」吗？该联系人的所有联系方式也会一起删除。`)) {
    return
  }
  try {
    await deleteContact(contact.id)
    await load()
    showToast('联系人已删除')
  } catch (e) {
    showToast(e.message, 'error')
  }
}

function pickImportFile() {
  fileInput.value?.click()
}

async function handleImport(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  busy.value = true
  try {
    const result = await importExcel(file)
    await load()
    const skipped = result.skipped > 0 ? `，跳过 ${result.skipped} 行` : ''
    showToast(`导入完成：共 ${result.totalRows} 行，成功 ${result.imported} 条${skipped}`)
    if (result.messages?.length) {
      console.info('导入提示：', result.messages)
    }
  } catch (e) {
    showToast(e.message, 'error')
  } finally {
    busy.value = false
  }
}

async function handleExport() {
  busy.value = true
  try {
    await exportExcel()
    showToast('已导出 Excel 文件')
  } catch (e) {
    showToast(e.message, 'error')
  } finally {
    busy.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.app {
  min-height: 100%;
  padding-bottom: 60px;
}

.hero {
  background: linear-gradient(120deg, #4f46e5 0%, #6d5ef0 45%, #8b5cf6 100%);
  color: #fff;
  padding: 30px 24px 34px;
  position: relative;
  overflow: hidden;
}

.hero::after {
  content: '';
  position: absolute;
  width: 420px;
  height: 420px;
  right: -120px;
  top: -220px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}

.hero-inner {
  max-width: 1180px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-size: 24px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.35);
}

.brand h1 {
  margin: 0;
  font-size: 24px;
  letter-spacing: 0.02em;
}

.brand p {
  margin: 4px 0 0;
  font-size: 12.5px;
  opacity: 0.85;
}

.stats {
  display: flex;
  gap: 10px;
}

.pill {
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.28);
  font-size: 13px;
}

.pill b {
  font-size: 15px;
  margin-left: 4px;
}

.star-pill {
  background: rgba(245, 158, 11, 0.25);
  border-color: rgba(253, 230, 138, 0.6);
}

.container {
  max-width: 1180px;
  margin: -18px auto 0;
  padding: 0 24px;
  position: relative;
  z-index: 2;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 14px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.search {
  position: relative;
  flex: 1 1 300px;
  min-width: 240px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 13px;
  opacity: 0.6;
}

.search input {
  width: 100%;
  padding: 10px 12px 10px 34px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fbfcfe;
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.search input:focus {
  border-color: #a5b4fc;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.12);
  background: #fff;
}

.filter,
.ghost,
.primary {
  padding: 9px 15px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 600;
  border: 1px solid var(--line);
  background: #fff;
  color: #475569;
  transition: all 0.15s ease;
}

.filter:hover,
.ghost:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.filter.active {
  background: #fffbeb;
  border-color: #fcd34d;
  color: #b45309;
}

.primary {
  border: none;
  background: var(--primary);
  color: #fff;
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.28);
}

.primary:hover {
  background: var(--primary-dark);
  transform: translateY(-1px);
}

.ghost:disabled,
.primary:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
}

.spacer {
  flex: 1;
}

.hidden-input {
  display: none;
}

.hint {
  margin: 14px 4px 16px;
  font-size: 12.5px;
  color: var(--muted);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  align-items: start;
  gap: 16px;
}

.state {
  padding: 60px 0;
  text-align: center;
  color: var(--muted);
}

.empty {
  padding: 60px 20px;
  text-align: center;
  background: var(--surface);
  border: 1px dashed #d7dced;
  border-radius: var(--radius);
}

.empty-icon {
  font-size: 40px;
}

.empty h2 {
  margin: 10px 0 6px;
  font-size: 18px;
}

.empty p {
  margin: 0 0 18px;
  color: var(--muted);
  font-size: 13.5px;
}

.empty-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.toast {
  position: fixed;
  left: 50%;
  bottom: 28px;
  transform: translateX(-50%);
  padding: 11px 20px;
  border-radius: 999px;
  font-size: 13.5px;
  font-weight: 600;
  color: #fff;
  background: #16a34a;
  box-shadow: 0 10px 30px rgba(22, 101, 52, 0.3);
  z-index: 60;
}

.toast.error {
  background: var(--danger);
  box-shadow: 0 10px 30px rgba(220, 38, 38, 0.3);
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, 10px);
}

@media (max-width: 720px) {
  .hero-inner {
    align-items: flex-start;
  }

  .stats {
    width: 100%;
  }
}
</style>
