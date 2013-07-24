--[[

USAGE:

    redis-cli --eval qd_offer.lua {name} , {value}

*   `name` - Name of a qdigest instance: used as a redis key.
*   `value` - Value to offer.

--]]

local function value2leaf(capacity, value)
  return value + capacity + 1
end

local function extendCapacity(key, value)
  -- TODO:
end

local function compress(key, id)
  -- TODO:
end

local function qd_offer(key, value)
  local capacity = redis.call('HGET', key, 'capacity')
  if value < 0 then
    return 0
  elseif value > capacity then
    extendCapacity(key, value)
  end
  local id = value2leaf(capacity, value)
  redis.call('HINCRBY', key, id, 1)
  redis.call('HINCRBY', key, 'size', 1)
  compress(key, id)
  return 1
end

return qd_offer(KEY[1], ARGV[1])
