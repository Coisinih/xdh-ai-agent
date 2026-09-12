import http from './http'

/**
 * 解析 SSE 文本块，提取 data 行内容（不含 "data:" 前缀）。
 * 兼容 Spring WebFlux / SseEmitter 常见输出格式。
 */
function parseSseChunk(text) {
  const lines = text.split(/\r?\n/)
  const payloads = []
  for (const line of lines) {
    if (line.startsWith('data:')) {
      payloads.push(line.slice(5).trimStart())
    }
  }
  return payloads.join('\n')
}

/**
 * 使用 GET + fetch 读取 SSE 流；URL 由 axios 实例拼装（复用 baseURL 与 params 编码）。
 *
 * @param {string} path 如 '/ai/chat/sse'
 * @param {Record<string, string>} params 查询参数
 * @param {(chunk: string) => void} onChunk 每次解析出的增量文本
 * @param {AbortSignal} [signal]
 */
export async function streamSseGet(path, params, onChunk, signal) {
  const url = http.getUri({ url: path, params })
  const res = await fetch(url, {
    method: 'GET',
    headers: { Accept: 'text/event-stream' },
    signal,
  })

  if (!res.ok) {
    const errText = await res.text().catch(() => '')
    throw new Error(errText || `请求失败: ${res.status}`)
  }

  const reader = res.body?.getReader()
  if (!reader) {
    throw new Error('当前环境不支持流式响应')
  }

  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })

    const parts = buffer.split('\n\n')
    buffer = parts.pop() ?? ''

    for (const part of parts) {
      if (!part.trim()) continue
      const chunk = parseSseChunk(part)
      if (chunk !== '') onChunk(chunk)
    }
  }

  if (buffer.trim()) {
    const chunk = parseSseChunk(buffer)
    if (chunk !== '') onChunk(chunk)
  }
}
