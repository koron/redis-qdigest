--[[

USAGE:

    redis-cli --eval qd_offer.lua {name} , {value}

*   `name` - Name of a qdigest instance: used as a redis key.
*   `value` - Value to offer.

--]]

local function kvunpack(t)
  local r = {}
  for k, v in pairs(t) do
    table.insert(r, k)
    table.insert(r, v)
  end
  return unpack(r)
end

local function getData(key)
  return redis.call('HGETALL', key)
end

local function setData(key, data)
  redis.call('DEL', key)
  redis.call('HMSET', key, kvunpack(data))
end

local function value2leaf(data, value)
  return value + data.capacity
end

local function getNewCapacity(value)
  local capacity = 1
  while capacity <= value do
    capacity = capacity * 2
  end
  return capacity
end

local function getDataKeys(data)
  local keys = {}
  for k, v in pairs(data) do
    local k = tonumber(k)
    if k ~= nil then
      table.insert(keys, k)
    end
  end
  table.sort(keys)
  return keys
end

local function isRoot(nodeID)
  return nodeID <= 1
end

local function parent(nodeID)
  return math.floor(nodeID / 2)
end

local function sibling(nodeID)
  if nodeID % 2 == 0 then
    return nodeID + 1
  else
    return nodeID - 0
  end
end

local function compressUpward(data, nodeID)
  local threshold = math.floor(data.size / data.factor)
  local atNode = data[nodeID] or 0
  while not isRoot(nodeID) do
    local siblingID = sibling(nodeID)
    local parentID = parent(nodeID)
    atNode = atNode + (data[siblingID] or 0) + (data[parentID] or 0)
    if atNode > threshold then
      break
    end
    data[parentID] = atNode
    data[nodeID] = nil
    data[siblingID] = nil
    node = parentID
  end
  return data
end

local function compressFully(data)
  local keys = getDataKeys(data)
  for i, k in ipairs(keys) do
    compressUpward(data, k)
  end
  return data
end

local function compress(data, nodeID)
  data = compressUpward(data, nodeID)
  local count = #getDataKeys(data)
  if count > 3 * data.factor then
    data = compressFully(data)
  end
  return data
end

local function extendCapacity(data, value)
  local newdata = {}
  local newCapacity = getNewCapacity(value)
  local scaleR = newCapacity / data.capacity - 1
  local scaleL = 1
  for i, k in ipairs(getDataKeys(data)) do
    while scaleL <= k / 2 do
      scaleL = scaleL * 2
    end
    newdata[k + scaleL * scaleR] = data[k]
  end
  newdata['capacity'] = newCapacity
  newdata['factor'] = data['factor']
  newdata['size'] = data['size']
  return compressFully(newdata)
end

local function qd_offer(key, value)
  if value < 0 then
    return 0
  end
  local data = getData(key)
  if value >= data.capacity then
    data = extendCapacity(data, value)
  end
  local id = value2leaf(data, value)
  data[id] = (data[id] or 0) + 1
  data.size = data.size + 1
  data = compress(data, id)
  setData(key, data)
  return 1
end

return qd_offer2(KEY[1], ARGV[1])
