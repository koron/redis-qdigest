--[[

USAGE:

    redis-cli --eval qd_drop.lua {name}

*   `name` - Name of a qdigest instance: used as a redis key.

--]]

local function qd_drop(key)
  return redis.call('DEL', key)
end

return qd_drop(KEYS[1])
