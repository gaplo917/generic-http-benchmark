type DummyResponse = {
  name: string | null
  isActive: boolean | null
  isVirtual: boolean | null
  threadGroupActiveCount: number | null
  threadGroupCount: number | null
}

type Thread = {
  name: string,
  isAlive: boolean,
  isVirtual: boolean
  threadGroup: {
    activeCount(): number
    activeGroupCount(): number
  }
}

function dummyResponse(thread?: Thread): DummyResponse {
  return {
    name: thread?.name ?? null,
    isActive: thread?.isAlive ?? null,
    isVirtual: thread?.isVirtual ?? null,
    threadGroupActiveCount: thread?.threadGroup?.activeCount() ?? null,
    threadGroupCount: thread?.threadGroup?.activeGroupCount() ?? null
  }
}

export { dummyResponse, DummyResponse }
