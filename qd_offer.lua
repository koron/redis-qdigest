--[[

USAGE:

    redis-cli --eval qd_offer.lua {name} , {value}

*   `name` - Name of a qdigest instance: used as a redis key.
*   `value` - Value to offer.

--]]

local function value2leaf(capacity, value)
  return value + capacity + 1
end

local function getNewCapacity(value)
  capacity = 1
  while capacity < value do
    capacity = capacity * 2
  end
  return capacity
end

local function get(key)
  return redis.call('HGETALL', key)
end

local function set(key, data)
  redis.call('DEL', key)
  -- TODO: fix method to call HMSET
  redis.call('HMSET', key, data)
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

local function extendCapacity(key, value, capacity)
  local newCapacity = getNewCapacity(value)
  local scaleR = newCapacity / capacity - 1
  local scaleL = 1
  local data = get(key)
  local keys = getDataKeys(data)
  local newdata = {}
  for i, k in ipairs(keys) do
    while scaleL <= k / 2 do
      scaleL = scaleL * 2
    end
    newdata[k + scaleL * scaleR] = data[k]
  end
  newdata['capacity'] = data['capacity']
  newdata['factor'] = data['factor']
  newdata['size'] = data['size']
  compressDataFully(newdata)
  set(key, data)
end

local function compressUpward(key, id, factor)
  -- TODO:
end

local function compressFully(key)
  local data = get(key)
  compressDataFully(data)
  set(key, data)
end

local function compressDataFully(data)
  -- TODO:
end

local function compress(key, id, factor)
  compressUpward(key, id, factor)
  local size = redis.call('HLEN', key) - 3
  if size > 3 * factor then
    compressFully(key)
  end
end

local function qd_offer(key, value)
  local info = redis.call('HGET', key, 'capacity', 'factor')
  local capacity, factor = info[1], info[2]
  if value < 0 then
    return 0
  elseif value > capacity then
    extendCapacity(key, value, capacity)
  end
  local id = value2leaf(capacity, value)
  redis.call('HINCRBY', key, id, 1)
  redis.call('HINCRBY', key, 'size', 1)
  compress(key, id, factor)
  return 1
end

return qd_offer(KEY[1], ARGV[1])
