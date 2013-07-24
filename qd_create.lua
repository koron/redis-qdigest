--[[

USAGE:

    redis-cli --eval qd_create.lua , {name} {factor}

*   `name` - Name of a qdigest instance.
*   `factor` - Compression factor.

--]]

local function qd_create(name, factor)
  local key_info = 'qdigest_' + name + '_info'
  local key_data = 'qdigest_' + name + '_data'
  if redis.call('EXISTS', key_info) then
    return 0
  end
  redis.call('HSET', key_info, 'size', 0, 'capacity', 1, 'factor', factor)
  redis.call('DEL', key_data)
end

return qd_create(ARGV[1], tonumber(ARGV[2]))
