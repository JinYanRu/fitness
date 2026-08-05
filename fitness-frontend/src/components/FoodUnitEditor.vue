<template>
  <div class="food-unit-editor">
    <div class="section-header">
      <div class="section-title">常用单位 (可选)</div>
      <button class="btn-add-unit" @click="addUnit">+ 添加单位</button>
    </div>
    <div class="unit-tip">添加常用单位后，记录饮食时可以快速选择（如：1包=250g）</div>

    <div v-if="units && units.length > 0" class="units-list">
      <div v-for="(unit, index) in units" :key="index" class="unit-item">
        <div class="unit-inputs">
          <div class="unit-input-group">
            <label>单位名称</label>
            <input v-model="unit.unitName" type="text" placeholder="如：包、片、个" />
            <div class="unit-quick-tags">
              <span
                v-for="name in presetUnitNames"
                :key="name"
                :class="['unit-tag', unit.unitName === name ? 'active' : '']"
                @click="unit.unitName = name"
              >{{ name }}</span>
            </div>
          </div>
          <div class="unit-input-group">
            <label>等于</label>
            <div class="unit-value-input">
              <input v-model.number="unit.unitValue" type="number" min="0" placeholder="250" />
              <span class="unit-base">{{ servingUnit || 'g' }}</span>
            </div>
          </div>
        </div>
        <button class="btn-remove-unit" @click="removeUnit(index)">删除</button>
      </div>
    </div>
    <div v-else class="no-units">
      <span>暂无常用单位，点击上方按钮添加</span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  servingUnit: {
    type: String,
    default: 'g'
  }
})

const units = defineModel({
  type: Array,
  default: () => []
})

// 常用单位快捷预设，点击即可填入单位名称
const presetUnitNames = ['包', '个', '片', '杯', '碗', '块', '条', '袋', '瓶', '盒', '份', '勺']

const addUnit = () => {
  if (!units.value) {
    units.value = []
  }
  units.value.push({
    unitName: '',
    unitValue: null,
    isDefault: false
  })
}

const removeUnit = (index) => {
  units.value.splice(index, 1)
}
</script>

<style scoped>
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.section-title { font-size: 14px; color: var(--text-2); font-weight: 600; }
.unit-tip { font-size: 12px; color: var(--text-3); margin-bottom: 12px; }
.btn-add-unit {
  padding: 6px 14px;
  background: var(--primary-gradient);
  color: #fff;
  border: none;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.12s;
}
.btn-add-unit:active { transform: scale(0.95); }
.units-list { display: flex; flex-direction: column; gap: 12px; }
.unit-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  padding: 12px;
  background: var(--fill);
  border-radius: var(--radius);
}
.unit-inputs { flex: 1; display: flex; gap: 12px; }
.unit-input-group { flex: 1; }
.unit-input-group label { display: block; font-size: 12px; color: var(--text-3); margin-bottom: 4px; }
.unit-input-group input {
  width: 100%;
  padding: 9px 10px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-1);
  background: var(--card);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.unit-input-group input:focus { border-color: var(--primary); box-shadow: 0 0 0 4px var(--primary-100); }
.unit-quick-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 6px; }
.unit-tag {
  padding: 4px 12px;
  font-size: 12px;
  color: var(--text-2);
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 999px;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
}
.unit-tag.active { background: var(--primary-gradient); color: #fff; border-color: transparent; }
.unit-value-input { display: flex; align-items: center; gap: 6px; }
.unit-value-input input { flex: 1; }
.unit-base { font-size: 14px; color: var(--text-2); white-space: nowrap; }
.btn-remove-unit {
  padding: 8px 12px;
  background: transparent;
  color: var(--danger);
  border: 1.5px solid var(--danger);
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-remove-unit:active { background: #fef2f2; }
.no-units { text-align: center; padding: 16px; color: var(--text-3); font-size: 13px; }
</style>
