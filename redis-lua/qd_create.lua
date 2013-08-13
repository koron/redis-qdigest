--[[

USAGE:

    redis-cli --eval qd_create.lua {name} , {factor}

*   `name` - Name of a qdigest instance: used as a redis key.
*   `factor` - Compression factor.

--]]

local function qd_create(key, factor)
  if redis.call('EXISTS', key) ~= 0 then
    return 0
  else
    redis.call('HMSET', key, 'size', 0, 'capacity', 1, 'factor', factor)
    return 1
  end
end

return qd_create(KEYS[1], tonumber(ARGV[1]))
