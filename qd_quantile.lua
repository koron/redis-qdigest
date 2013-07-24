--[[

USAGE:

    redis-cli --eval qd_quantile.lua {name} , {q}

*   `name` - Name of a qdigest instance: used as a redis key.
*   `q` - Parameter of quantile: 0.95 means 95%

--]]

local function get_data(key)
  return redis.call('HGETALL', key)
end

local function get_sorted_ranges(data)
  local r = {}
  -- TODO:
  return r
end

local function qd_quantile(key, q)
  local data = get_data(key)
  local ranges = get_sorted_ranges(data)
  local threshold = data.size * q
  local i, v, curr = 0
  for i, v in ipairs(ranges) do
    curr += v[2]
    if curr > threshold then
      return v[1]
    end
  end
  return ranges[#ranges][2]
end

return qd_quantile(KEY[1], ARGV[1])
