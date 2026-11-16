<template>
  <article class="card">
    <button
      class="star"
      :class="{ on: contact.favorite }"
      :title="contact.favorite ? '取消书签' : '加入书签'"
      @click.stop="emit('toggle-favorite', contact)"
    >
      ★
    </button>

    <header class="head" @click="emit('edit', contact)">
      <div class="avatar" :style="{ background: avatarColor }">{{ initial }}</div>
      <div class="who">
        <h3>{{ contact.name }}</h3>
        <p>{{ subtitle }}</p>
      </div>
    </header>

    <div class="methods" @click="emit('edit', contact)">
      <div v-for="method in visibleMethods" :key="method.id ?? method.value" class="method">
        <span class="tag" :style="{ color: typeMeta(method.type).color, background: typeMeta(method.type).bg }">
          {{ method.label || typeMeta(method.type).label }}
        </span>
        <span class="value" :title="method.value">{{ method.value }}</span>
      </div>
      <p v-if="contact.methods.length === 0" class="none">暂无联系方式</p>
      <p v-else-if="contact.methods.length > visibleMethods.length" class="more">
        还有 {{ contact.methods.length - visibleMethods.length }} 项联系方式…
      </p>
    </div>

    <footer class="foot">
      <span v-if="contact.address" class="addr" :title="contact.address">📍 {{ contact.address }}</span>
      <span v-else class="addr muted">未填写地址</span>
      <button class="delete" title="删除联系人" @click.stop="emit('delete', contact)">删除</button>
    </footer>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { typeMeta } from '../methodTypes'

const props = defineProps({
  contact: { type: Object, required: true }
})

const emit = defineEmits(['edit', 'delete', 'toggle-favorite'])

const visibleMethods = computed(() => props.contact.methods.slice(0, 3))

const initial = computed(() => (props.contact.name || '?').trim().slice(0, 1).toUpperCase())

const subtitle = computed(() => {
  const parts = [props.contact.company, props.contact.position].filter(Boolean)
  return parts.length ? parts.join(' · ') : '未填写公司与职位'
})

const avatarColor = computed(() => {
  const name = props.contact.name || ''
  let hash = 0
  for (let i = 0; i < name.length; i += 1) {
    hash = (hash * 31 + name.charCodeAt(i)) % 360
  }
  return `linear-gradient(135deg, hsl(${hash} 70% 62%), hsl(${(hash + 40) % 360} 72% 50%))`
})
</script>

<style scoped>
.card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px 18px 14px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.card:hover {
  transform: translateY(-2px);
  border-color: #d7dced;
  box-shadow: 0 10px 30px rgba(28, 33, 68, 0.1);
}

.star {
  position: absolute;
  top: 10px;
  right: 12px;
  width: 30px;
  height: 30px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #cbd5e1;
  font-size: 18px;
  line-height: 1;
  transition: transform 0.15s ease, color 0.15s ease, background 0.15s ease;
}

.star:hover {
  transform: scale(1.15);
  background: #fffbeb;
  color: #fbbf24;
}

.star.on {
  color: var(--star);
  text-shadow: 0 0 12px rgba(245, 158, 11, 0.35);
}

.head {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding-right: 22px;
}

.avatar {
  flex: 0 0 44px;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.25);
}

.who {
  min-width: 0;
}

.who h3 {
  margin: 0 0 2px;
  font-size: 16px;
  font-weight: 650;
}

.who p {
  margin: 0;
  font-size: 12.5px;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 230px;
}

.methods {
  display: flex;
  flex-direction: column;
  gap: 8px;
  cursor: pointer;
}

.method {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.tag {
  flex: 0 0 auto;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.value {
  font-size: 13.5px;
  color: #374151;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.none,
.more {
  margin: 0;
  font-size: 12.5px;
  color: var(--muted);
}

.foot {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-top: 12px;
  border-top: 1px dashed var(--line);
}

.addr {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.addr.muted {
  color: #cbd5e1;
}

.delete {
  flex: 0 0 auto;
  padding: 3px 8px;
  border: none;
  border-radius: 7px;
  background: transparent;
  color: #9ca3af;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.15s ease, color 0.15s ease, background 0.15s ease;
}

.card:hover .delete {
  opacity: 1;
}

.delete:hover {
  background: #fef2f2;
  color: var(--danger);
}
</style>
