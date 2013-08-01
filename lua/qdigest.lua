module('qdigest', package.seeall)
__index = _M

function new(class, factor)
  local self = { size=0, capacity=1, factor=factor }
  setmetatable(self, class)
  return self
end

require('qdigest_offer')
require('qdigest_quantile')

function offer(self, value)
  return qd_offer(self, value)
end

function quantile(self, q)
  return qd_quantile(self, q)
end
