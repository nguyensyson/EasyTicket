-- KEYS[1] = ticket:inventory:{eventId}:{ticketTypeId}
-- ARGV[1] = quantity requested
-- Returns: -1 if the key does not exist (inventory not loaded / unknown ticket type),
--          -2 if current stock < requested quantity (sold out),
--          otherwise the remaining stock after the atomic decrement.
local current = tonumber(redis.call('GET', KEYS[1]))
if current == nil then
    return -1
end

local requested = tonumber(ARGV[1])
if current < requested then
    return -2
end

redis.call('DECRBY', KEYS[1], requested)
return current - requested
