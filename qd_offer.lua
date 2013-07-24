--[[

USAGE:

    redis-cli --eval qd_offer.lua {name} , {value}

*   `name` - Name of a qdigest instance: used as a redis key.
*   `value` - Value to offer.

--]]

local function qd_quantile(key, value)
  -- TODO:
end

return qd_quantile(KEY[1], ARGV[1])
