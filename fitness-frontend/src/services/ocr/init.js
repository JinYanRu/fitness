/**
 * OCR 服务初始化配置
 */

import ocrManager from './index.js'
import UToolsOCRStrategy from './utools.js'
import BaiduOCRStrategy from './baidu.js'
import TencentOCRStrategy from './tencent.js'
import MockOCRStrategy from './mock.js'
import RapidOCRStrategy from './rapidocr.js'
import BackendOCRStrategy from './backend.js'

/**
 * 初始化 OCR 服务
 * @param {Object} config - 配置对象
 */
export function initOCR(config = {}) {
  // 注册后端 OCR 策略（通过 Spring Boot 后端服务）
  // 优先使用后端服务，它提供了智能营养成分解析
  const backendStrategy = new BackendOCRStrategy(config.backend || {})
  ocrManager.register('backend', backendStrategy, config.defaultStrategy === 'backend' || !config.defaultStrategy)

  // 注册 RapidOCR 策略（本地部署的 OCR 服务，直接调用）
  if (config.rapidocr?.baseUrl) {
    const strategy = new RapidOCRStrategy(config.rapidocr)
    ocrManager.register('rapidocr', strategy, config.defaultStrategy === 'rapidocr')
  }

  // 注册 Mock 策略（用于开发测试）
  ocrManager.register('mock', new MockOCRStrategy(), config.defaultStrategy === 'mock')

  // 注册 uTools MCP OCR 策略
  if (config.utools) {
    const strategy = new UToolsOCRStrategy(config.utools)
    ocrManager.register('utools', strategy, config.defaultStrategy === 'utools')
  }

  // 注册百度 OCR 策略
  if (config.baidu?.apiKey && config.baidu?.secretKey) {
    const strategy = new BaiduOCRStrategy(config.baidu)
    ocrManager.register('baidu', strategy, config.defaultStrategy === 'baidu')
  }

  // 注册腾讯云 OCR 策略
  if (config.tencent?.secretId && config.tencent?.secretKey) {
    const strategy = new TencentOCRStrategy(config.tencent)
    ocrManager.register('tencent', strategy, config.defaultStrategy === 'tencent')
  }

  console.log('[OCR] 初始化完成，可用策略:', ocrManager.getStrategies().map(s => s.name))

  return ocrManager
}

// 默认配置（使用后端服务）
const defaultConfig = {
  defaultStrategy: 'backend',
  backend: {
    baseUrl: '/api'  // 通过 Vite 代理访问后端
  },
  rapidocr: {
    baseUrl: 'http://111.228.49.250:10265'
  },
  baidu: {
    apiKey: '',
    secretKey: ''
  },
  tencent: {
    secretId: '',
    secretKey: '',
    proxyUrl: ''
  },
  utools: {
    apiUrl: ''
  }
}

// 自动初始化
export default initOCR(defaultConfig)