--[[

Check factor of QDigest.  Return factor value of QDigest if succeeded,
otherwise return nil.

USAGE:

    redis-cli --eval qd_check.lua {name}

*   `name` - Name of a qdigest instance: used as a redis key.

--]]

local function qd_check(key)
  local v = redis.call('HMGET', key, 'size', 'capacity', 'factor')
  if v[1] == nil or v[2] == nil then
    return nil
  else
    return tonumber(v[3])
  end
end

return qd_check(KEYS[1])
