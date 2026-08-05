/**
 * 全局轻量 Toast 提示
 * 在页面居中显示一条短暂消息，自动消失，无需用户点击确认。
 * 复用单个 DOM 元素，避免快速连续调用时堆叠。
 */

let toastEl = null
let toastTimer = null

const ensureStyle = () => {
  if (document.getElementById('custom-toast-style')) return
  const style = document.createElement('style')
  style.id = 'custom-toast-style'
  style.textContent = `
.custom-toast {
  position: fixed;
  top: 75%;
  left: 50%;
  transform: translateX(-50%) scale(0.8);
  background: rgba(0, 0, 0, 0.72);
  color: #fff;
  padding: 12px 24px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.5;
  z-index: 3000;
  max-width: 80vw;
  text-align: center;
  white-space: pre-line;
  opacity: 0;
  transition: all 0.3s;
  pointer-events: none;
}
.custom-toast.show { opacity: 1; transform: translateX(-50%) scale(1); }
`
  document.head.appendChild(style)
}

/**
 * 显示一条 toast 提示
 * @param {string} message 提示内容
 * @param {number} duration 显示时长（毫秒），默认 1500
 */
export const showToast = (message, duration = 1500) => {
  ensureStyle()
  if (!toastEl) {
    toastEl = document.createElement('div')
    toastEl.className = 'custom-toast'
    document.body.appendChild(toastEl)
  }
  toastEl.textContent = message
  toastEl.classList.remove('show')
  // 强制重排以重新触发过渡动画
  void toastEl.offsetWidth
  toastEl.classList.add('show')
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toastEl.classList.remove('show')
  }, duration)
}
