require('qdigest')

function dump(label, obj)
  print(string.format('%s:', label))
  for k, v in pairs(obj) do
    print(string.format('  %s: %s', k, v))
  end
end

d = qdigest:new(8)
d:offer(0)
d:offer(1)
dump('1st dump', d)
d:offer(2)
dump('2nd dump', d)
--[[
d:offer(3)
d:offer(4)
--]]

print(string.format('quantile(0.0)=%d', d:quantile(0)))
print(string.format('quantile(0.5)=%d', d:quantile(0.5)))
print(string.format('quantile(1.0)=%d', d:quantile(1)))
