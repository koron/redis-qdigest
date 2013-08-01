--[[

USAGE:

    redis-cli --eval qd_quantile.lua {name} , {q}

*   `name` - Name of a qdigest instance: used as a redis key.
*   `q` - Parameter of quantile: 0.95 means 95%

--]]

local function get_data(key)
  return redis.call('HGETALL', key)
end

local function isLeaf(data, id)
  return id > data.capacity
end

local function leftChild(id)
  return id * 2
end

local function rightChild(id)
  return id * 2 + 1
end

local function leaf2value(data, id)
  return id - data.capacity - 1
end

local function rangeLeft(data, id)
  while not isLeaf(data, id) do
    id = leftChild(id)
  end
  return leaf2value(data, id)
end

local function rangeRight(data, id)
  while not isLeaf(data, id) do
    id = rightChild(id)
  end
  return leaf2value(data, id)
end

local function cmp_ranges(a, b)
  local ra, sa = a[2], a[2] - a[1]
  local rb, sb = b[2], b[2] - b[1]
  if ra < rb then
    return true
  elseif ra > rb then
    return false
  elseif sa < sb then
    return true
  elseif sa > sb then
    return false
  else
    return false
  end
end

local function get_sorted_ranges(data)
  local r = {}
  for k, v in pairs(data) do
    if type(k) == 'number' then
      table.insert(r, { rangeLeft(data, k), rangeRight(data, k), v })
    end
  end
  table.sort(r, cmp_ranges)
  return r
end

local function qd_quantile(key, q)
  local data = get_data(key)
  local ranges = get_sorted_ranges(data)
  local threshold = data.size * q
  local curr = 0
  for i, v in ipairs(ranges) do
    curr = curr + v[3]
    if curr > threshold then
      return v[2]
    end
  end
  return ranges[#ranges][2]
end

return qd_quantile(KEY[1], ARGV[1])
